package no.forsamling.audiobroadcast.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import no.forsamling.audiobroadcast.BaseApplication;
import no.forsamling.audiobroadcast.Global;
import no.forsamling.audiobroadcast.MainActivity;
import no.forsamling.audiobroadcast.R;
import no.forsamling.audiobroadcast.controller.AudioRegistryListener;
import no.forsamling.audiobroadcast.controller.Client;
import no.forsamling.audiobroadcast.controller.HeadSetReceiver;
import no.forsamling.audiobroadcast.controller.Playback;
import no.forsamling.audiobroadcast.controller.Recorder;
import no.forsamling.audiobroadcast.controller.Server;
import no.forsamling.audiobroadcast.interfaces.AudioBroadCastEventsListener;
import no.forsamling.audiobroadcast.model.ClingDevice;
import no.forsamling.audiobroadcast.utils.AppSettings;
import no.forsamling.audiobroadcast.utils.Logger;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.android.AndroidWifiSwitchableRouter;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.message.header.UDAServiceTypeHeader;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.registry.Registry;

import java.util.ArrayList;

public class UPnPAudioService extends AndroidUpnpServiceImpl {
  public String TAG = UPnPAudioService.class.getSimpleName();
  protected WifiManager.MulticastLock multicastLock;
  protected PowerManager.WakeLock wakeLock;
  protected WifiManager.WifiLock wifiLock;
  private AudioRegistryListener audioRegistryListener;
  private final LocalBinder _Binder = new LocalBinder();
  public Client client;
  public AudioBroadCastEventsListener eventsListener;

  public class LocalBinder extends Binder {
    public LocalBinder() {
      super();
    }

    public UpnpService get() {
      return super.get();
    }

    public UpnpServiceConfiguration getConfiguration() {
      return super.getConfiguration();
    }

    public ControlPoint getControlPoint() {
      return super.getControlPoint();
    }

    public Registry getRegistry() {
      return super.getRegistry();
    }

    public UPnPAudioService getService() {
      return UPnPAudioService.this;
    }
  }

  public IBinder onBind(Intent intent) {
    return this._Binder;
  }

  private HeadSetReceiver headSetReceiver;

  public void onCreate() {
    super.onCreate();

    Logger.print(TAG, "onCreate");

    this.audioRegistryListener = new AudioRegistryListener();
    this.headSetReceiver = new HeadSetReceiver();

    IntentFilter inf = new IntentFilter();
    inf.addAction(Intent.ACTION_HEADSET_PLUG);
    registerReceiver(this.headSetReceiver, inf);

    for (Device<?, ?, ?> device : this._Binder.getRegistry().getDevices()) {
      if (device instanceof LocalDevice) {
        this.audioRegistryListener.localDeviceAdded(this._Binder.getRegistry(), (LocalDevice) device);
      } else {
        this.audioRegistryListener.remoteDeviceAdded(this._Binder.getRegistry(), (RemoteDevice) device);
      }
    }
    this._Binder.getRegistry().addListener(this.audioRegistryListener);
  }

  public boolean seekServers() {
    Logger.print(TAG, "seekServers");
    this._Binder.getRegistry().removeAllRemoteDevices();
    this._Binder.getControlPoint().search(new UDAServiceTypeHeader(new UDAServiceType(Global.SERVICE_TYPE)));
    return true;
  }

  public boolean runServer() {
    DeviceDetails details = new DeviceDetails(AppSettings.with(BaseApplication.getContext()).getDeviceName(), new ManufacturerDetails("OALFF"), new ModelDetails("OALFF", "Microphone for broadcasting within a local network.", "v1.0"));
    LocalService service = new AnnotationLocalServiceBinder().read(ClingService.class);
    service.setManager(new DefaultServiceManager(service, ClingService.class));

    try {
        LocalDevice device = new LocalDevice(new DeviceIdentity(Global.AUDIO_BROADCAST_UDN), new UDADeviceType(Global.DEVICE_TYPE, 1), details, Global.getDeviceIcon(), service);
        this._Binder.getRegistry().addDevice(device);
        acquireWakeLock();
        Global._server = new Server();
        Global._server.run();
        new Thread(new Recorder(), "recorder").start();
        updateNotificationsWithListenersCount();
      Global._playback = new Playback();
    } catch (ValidationException e) {
      e.printStackTrace();
    }
    return true;
  }


  public void setEventsHandler(AudioBroadCastEventsListener eventsListener) {
    this.eventsListener = eventsListener;
    this.audioRegistryListener.setEventsHandler(this.eventsListener);
  }


  public void acquireWakeLock() {
    try {
      wakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(1, "audiobroadcast:server_cpu");
      wakeLock.acquire();
      WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
      wifiLock = wifiManager.createWifiLock("AUDIOBROADCAST_SERVER_WIFI");
      wifiLock.acquire();
      multicastLock = wifiManager.createMulticastLock("AUDIOBROADCAST_SERVER_MULTICAST");
      multicastLock.setReferenceCounted(false);
      multicastLock.acquire();
      Logger.print("WIFILOCK", String.valueOf(wifiLock.isHeld()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void releaseWakeLock() {

    try {
      if (!Global.isListening && !Global.isSpeaking) {
        if (this.multicastLock.isHeld()) {
          Logger.print("Release Multicast");
          this.multicastLock.release();
        }
        if (this.wifiLock.isHeld()) {
          Logger.print("Release WIFI");
          this.wifiLock.release();
        }
        if (this.wakeLock.isHeld()) {
          Logger.print("Release CPU");
          this.wakeLock.release();
        }
        stopForeground(true);
      }
    }catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean stopServer() {
    Global.isSpeaking = false;
    releaseWakeLock();


    if (Global._playback != null) {
      Global._playback.stop();
      Global._playback = null;
    }

    if (Global._server != null) {
      Global._server.stop();
      Global._playback = null;
    }

    LocalDevice djDevice = this._Binder.getRegistry().getLocalDevice(Global.AUDIO_BROADCAST_UDN, true);
    if (djDevice != null) {
      this._Binder.getRegistry().removeDevice(djDevice);
    }
    this.eventsListener.onServerStop();
    return true;
  }


  public void onDestroy() {
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        UPnPAudioService.this._Binder.getRegistry().removeListener(UPnPAudioService.this.audioRegistryListener);
        UPnPAudioService.this._Binder.getRegistry().removeAllLocalDevices();
        UPnPAudioService.this._Binder.getRegistry().removeAllRemoteDevices();
      }
    });


    unregisterReceiver(this.headSetReceiver);

    if (!ModelUtil.ANDROID_EMULATOR && isListeningForConnectivityChanges())
      unregisterReceiver(((AndroidWifiSwitchableRouter) upnpService.getRouter()).getBroadcastReceiver());

    AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        upnpService.shutdown();
        return null;
      }
    };

    asyncTask.execute();
  }


  public int onStartCommand(Intent intent, int flags, int startId) {
    Logger.print(TAG, "onStartCommand");
    return START_NOT_STICKY;
  }

  public void getServerAddress(final Device device) {
    final Service service = device.findService(new UDAServiceType(Global.SERVICE_TYPE));

    this._Binder.getControlPoint().execute(new ActionCallback(new ActionInvocation(service.getAction("GetServerAddress"))) {
      public void success(ActionInvocation invocation) {
        Global.CONNECTED_TO_SERVERNAME = new ClingDevice(device).toString();

        Logger.print("Server address:" + (String) invocation.getOutput("ServerAddress").getValue());
        UPnPAudioService.this.connectToServer(service, (String) invocation.getOutput("ServerAddress").getValue());
      }

      public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        if (defaultMsg != null)
          Logger.print(defaultMsg);
      }
    });
  }

  protected void connectToServer(Service service, final String serverAddress) {
    ActionInvocation setTargetInvocation = new ActionInvocation(service.getAction("UserConnect"));
    setTargetInvocation.setInput("Username", AppSettings.with(this).getDeviceName());
    setTargetInvocation.setInput("Version", "1.0");
    this._Binder.getControlPoint().execute(new ActionCallback(setTargetInvocation) {
      public void success(ActionInvocation invocation) {
        boolean success;

        String response = (String) invocation.getOutput("Response").getValue();
        if (response.charAt(0) == '0') {
          success = true;
        } else {
          success = false;
        }

        Logger.print(response);

        if (success) {
          String[] split = serverAddress.split(":");
          Global.CONNECTED_TO_ADDRESS = split[0];
          Global.CONNECTED_TO_PORT = Integer.valueOf(split[1]).intValue();
          client = new Client(Global.CONNECTED_TO_ADDRESS, Global.CONNECTED_TO_PORT);
          client.run();
          return;
        }
        UPnPAudioService.this.eventsListener.onClientSearchError(response.substring(2));
      }

      public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        if (defaultMsg != null)
          Logger.print(defaultMsg);
        UPnPAudioService.this.eventsListener.onClientSearchError(defaultMsg);
      }
    });
  }

  public void disconnectClient() {
    this.eventsListener.onClientDisconnected();

    ArrayList<ClingDevice> clingDevices = MainActivity.getInstance().serverDevicesAdapter.getDevices();

    Device device = null;

    for (int i = 0; i < clingDevices.size(); i++) {
      ClingDevice clingDevice = clingDevices.get(i);

      if (clingDevice.toString().equals(Global.CONNECTED_TO_SERVERNAME)) {
        device = clingDevice.getDevice();
      }
    }

    MainActivity.getInstance().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        client.disconnectManually();
      }
    });

    if (device != null) {
      final Service service = device.findService(new UDAServiceType(Global.SERVICE_TYPE));
      ActionInvocation setTargetInvocation = new ActionInvocation(service.getAction("UserDisconnect"));
      setTargetInvocation.setInput("Username", AppSettings.with(BaseApplication.getContext()).getDeviceName());
      setTargetInvocation.setInput("Version", "1.0");
      this._Binder.getControlPoint().execute(new ActionCallback(setTargetInvocation) {
        @Override
        public void success(ActionInvocation invocation) {
          Logger.print("Stop Listening");
          Global.CONNECTED_TO_ADDRESS = "";
          Global.CONNECTED_TO_PORT = 0;

          Global.audioService.eventsListener.onServerDisConnected(AppSettings.with(BaseApplication.getContext()).getDeviceName());
        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

        }
      });
    }
  }

  public void updateNotificationsWithListenersCount() {
    String strNotification = "";
    if (Global.isListening) {
      updateNotification(Global.CONNECTED_TO_SERVERNAME, "Connected");
    } else if (Global.isSpeaking) {
      int listeners = Global.connectedUsers.size();
      if (listeners > 0) {
        strNotification = String.valueOf(listeners) + " client" + (listeners == 1 ? "" : "s");
      } else {
        strNotification = Global.NOTIFICATION_TEXT_WAITING_FOR_LISTENERS;
      }
      updateNotification(AppSettings.with(BaseApplication.getContext()).getDeviceName(), strNotification);
    } else {
      stopForeground(true);
    }
  }



//  https://stackoverflow.com/questions/31264157/how-to-avoid-black-screen-when-intent-flag-activity-new-task-intent-flag-activ/31264611
  protected void updateNotification(String broadcastName, String subtitle) {
    Intent intent = new Intent(this, MainActivity.class);

//    intent.setAction(Intent.ACTION_MAIN);
//    intent.addCategory(Intent.CATEGORY_LAUNCHER);
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    startForeground(2, new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.ic_stat_headset_mic).
            setWhen(System.currentTimeMillis()).setContentTitle("AudioBroadCast: " + broadcastName)
            .setContentText(subtitle).setContentIntent(PendingIntent.getActivity(BaseApplication.getContext(), 0, intent, 0)).setOngoing(true).build());
  }
}
