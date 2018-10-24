package no.forsamling.audiobroadcast;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import no.forsamling.audiobroadcast.interfaces.AudioBroadCastEventsListener;
import no.forsamling.audiobroadcast.model.ClingDevice;
import no.forsamling.audiobroadcast.services.UPnPAudioService;
import no.forsamling.audiobroadcast.utils.AppSettings;
import no.forsamling.audiobroadcast.utils.Logger;
import no.forsamling.audiobroadcast.utils.Notice;
import no.forsamling.audiobroadcast.view.AudioBroadcastPreferenceFragment;
import no.forsamling.audiobroadcast.view.ClientDevicesAdapter;
import no.forsamling.audiobroadcast.view.CustomTextView;
import no.forsamling.audiobroadcast.view.ServerDevicesAdapter;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.RemoteDevice;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ServerDevicesAdapter.OnDeviceClickListener, SwipeRefreshLayout.OnRefreshListener {
  public String TAG = MainActivity.class.getSimpleName();

  public static MainActivity _instance;
  public ClientDevicesAdapter clientDevicesAdapter;
  public ServerDevicesAdapter serverDevicesAdapter;

  @Bind(R.id.list_remote_devices) ListView listRemoteDevices;
  @Bind(R.id.content) View layoutContent;
  @Bind(R.id.txt_devices_not_found) CustomTextView txtDevicesNotFound;
  @Bind(R.id.layout_devices) View layoutDevices;

  @Bind(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
  @Bind(R.id.layout_server_settings) LinearLayout layoutServerSettings;
  @Bind(R.id.btn_start_stop) ToggleButton btnStartStop;
  @Bind(R.id.btn_mute_unmute) ToggleButton btnMuteUnMute;
  protected EventsListener eventListener;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
    = new BottomNavigationView.OnNavigationItemSelectedListener() {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      switch (item.getItemId()) {
        case R.id.navigation_home:
          layoutDevices.setVisibility(View.VISIBLE);
          layoutContent.setVisibility(View.GONE);

          if (AppSettings.with(BaseApplication.getContext()).isBroadCastMode())
           layoutServerSettings.setVisibility(View.VISIBLE);
          return true;
        case R.id.navigation_settings:
          changeFragment(audioBroadcastPreferenceFragment);
          layoutServerSettings.setVisibility(View.GONE);
          layoutDevices.setVisibility(View.GONE);
          layoutContent.setVisibility(View.VISIBLE);
          return true;
      }
      return false;
    }

  };

  public UpnpServiceConnection _serviceConnection;

  @Override
  public void onDeviceClicked(ClingDevice deviceDisplay) {
    if (!Global.CONNECTED_TO_SERVERNAME.equals(deviceDisplay.toString())) {
      if (!Global.CONNECTED_TO_SERVERNAME.equals("")) {
        Global.audioService.disconnectClient();
        Global.CONNECTED_TO_SERVERNAME = "";
      }
      if (deviceDisplay.getDevice().isFullyHydrated()) {
        Global.audioService.getServerAddress(deviceDisplay.getDevice());

      } else {
        Notice.show("Cannot broadcast");
      }
    } else {
      Global.audioService.disconnectClient();
      Global.CONNECTED_TO_SERVERNAME = "";
    }
  }

  static public MainActivity getInstance() {
    return _instance;
  }

  public void clearDeviceList() {
    try {
      serverDevicesAdapter.clear();
      serverDevicesAdapter.notifyDataSetChanged();
      clientDevicesAdapter.clear();
      clientDevicesAdapter.notifyDataSetChanged();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onRefresh() {
    Global.audioService.seekServers();
    if(swipeRefreshLayout.isRefreshing())
      swipeRefreshLayout.setRefreshing(false);
  }

  protected class UpnpServiceConnection implements ServiceConnection {
    protected UpnpServiceConnection() {
    }

    public void onServiceConnected(ComponentName className, IBinder service) {
      Logger.print("UpnpService Connected");
      Global.audioService = ((UPnPAudioService.LocalBinder) service).getService();
      Global.audioService.setEventsHandler(eventListener);
      setAppMode();
    }

    public void onServiceDisconnected(ComponentName className) {
      Logger.print("UpnpService Disconnected");
      Global.audioService = null;
    }
  }

  public void changeFragment(Fragment fragment) {
    getFragmentManager().beginTransaction()
      .replace(R.id.content, fragment)
      .commit();
  }

  public void getValidSampleRates() {
    for (int rate : new int[] {4800, 8000, 11025, 16000, 22050, 44100, 48000, 50000, 50400, 88200,
      96000, 176400, 192000, 352800, 2822400, 5644800}) {  // add the rates you wish to check against
      int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
      if (bufferSize > 0) {
        // buffer size is valid, Sample rate supported
          Logger.print(TAG, "VALID SAMPLE RATE:" + rate);
      } else {
        Logger.print(TAG, "INVALID SAMPLE RATE:" + rate);
      }
    }

    AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    String rate = null;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
      rate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
      String size = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
      Logger.print("Buffer Size and sample rate", "Size :" + size + " & Rate: " + rate);
    }
  }

  AudioBroadcastPreferenceFragment audioBroadcastPreferenceFragment = new AudioBroadcastPreferenceFragment();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    _instance = this;

    ButterKnife.bind(this);

    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    serverDevicesAdapter = new ServerDevicesAdapter(this, new ArrayList<ClingDevice>());
    eventListener = new EventsListener();

    ArrayList<String> devices = new ArrayList<>();
    clientDevicesAdapter = new ClientDevicesAdapter(this, devices);

    listRemoteDevices.setAdapter(clientDevicesAdapter);

    serverDevicesAdapter.setOnDeviceClickListener(this);

    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    initAudioManager();

    if (!Global.isHeadSet()) {
      Notice.show("Please plug in HeadSet Receiver");
    }

    Logger.print("MainActivity onCreate");
    turnOnAudioService();

    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
      android.R.color.holo_green_light,
      android.R.color.holo_orange_light,
      android.R.color.holo_red_light);

    NotificationManager notificationManager =
      (NotificationManager) BaseApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
      && !notificationManager.isNotificationPolicyAccessGranted()) {

      Intent intent = new Intent(
        android.provider.Settings
          .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

      startActivity(intent);
    }

    checkPermissins();

    getValidSampleRates();

//    disableAlarms();

    btnStartStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
          Global.audioService.runServer();
          btnMuteUnMute.setVisibility(View.VISIBLE);

          Global.audioService.updateNotificationsWithListenersCount();
        } else {
          Global.audioService.stopServer();
          btnMuteUnMute.setVisibility(View.GONE);
          AppSettings.with(BaseApplication.getContext()).setMute(false);

          Global.audioService.updateNotificationsWithListenersCount();
        }
      }
    });

    btnMuteUnMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked)
          AppSettings.with(BaseApplication.getContext()).setMute(true);
        else
          AppSettings.with(BaseApplication.getContext()).setMute(false);
      }
    });
  }

  private void disableAlarms() {
    AlarmManager alarmManager = (AlarmManager) BaseApplication.getContext().getSystemService(Context.ALARM_SERVICE);
    Intent updateServiceIntent = new Intent(BaseApplication.getContext(), PendingIntent.class);
    PendingIntent pendingUpdateIntent = PendingIntent.getService(BaseApplication.getContext(), 0, updateServiceIntent, 0);

    // Cancel alarms
    try {
      alarmManager.cancel(pendingUpdateIntent);
    } catch (Exception e) {
      Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
    }
  }

  private void checkPermissins() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
        String[] permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE,  Manifest.permission.RECORD_AUDIO};
        requestPermissions(permissions, Global.PERMISSION_REQUEST_READ_PHONE_STATE);
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case 100: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Toast.makeText(this, "Permission granted: ", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(this, "Permission NOT granted: ", Toast.LENGTH_SHORT).show();
        }

        return;
      }
    }
  }

  private void mute() {
    //mute audio
    AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
  }

  public void unmute() {
    //unmute audio
    AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
  }

  @Override
  public void onResume() {
    super.onResume();
    mute();
  }

  @Override
  public void onPause() {
    super.onPause();
    unmute();
  }

  protected class EventsListener implements AudioBroadCastEventsListener {

    @Override
    public void onClientConnect() {

    }

    @Override
    public void onClientConnected() {
      Logger.print("MainActivity onClientConnected");

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Logger.print(Global.CONNECTED_TO_SERVERNAME);
          MainActivity.this.serverDevicesAdapter.notifyDataSetChanged();
        }
      });

      Global.audioService.updateNotificationsWithListenersCount();
    }

    @Override
    public void onClientDisconnected() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          serverDevicesAdapter.notifyDataSetChanged();

          if (serverDevicesAdapter.getCount() == 0)
            txtDevicesNotFound.setVisibility(View.GONE);
        }
      });

      Global.audioService.updateNotificationsWithListenersCount();
    }

    @Override
    public void onClientSearchError(String str) {

    }

    @Override
    public void onSearchAddDevice(final Device<?, ?, ?> device) {
      Logger.print("onSearchAddDevice");
      runOnUiThread(new Runnable() {
        public void run() {
          ClingDevice d = new ClingDevice(device);
          int position = serverDevicesAdapter.getPosition(d);
          if (position >= 0) {
            serverDevicesAdapter.remove(d);
            serverDevicesAdapter.insert(d, position);
            return;
          }
          serverDevicesAdapter.add(d);

          txtDevicesNotFound.setVisibility(View.GONE);

          serverDevicesAdapter.notifyDataSetChanged();
        }
      });
    }

    @Override
    public void onSearchBegin() {
      Logger.print("onSearchBegin");
    }

    @Override
    public void onSearchDeviceFailed(RemoteDevice remoteDevice, Exception exception) {

    }

    @Override
    public void onSearchEnd() {

    }

    @Override
    public void onSearchRemoveDevice(final Device<?, ?, ?> device) {
      runOnUiThread(new Runnable() {
        public void run() {
          serverDevicesAdapter.remove(new ClingDevice(device));

          if (new ClingDevice(device).toString().equals(Global.CONNECTED_TO_SERVERNAME)) {


            if (Global.isListening) {
              Global.audioService.disconnectClient();
              Global.isListening = false;
            }

            Global.CONNECTED_TO_SERVERNAME = "";
          }

          if (serverDevicesAdapter.getCount() == 0)
            txtDevicesNotFound.setVisibility(View.VISIBLE);

        }
      });
    }

    @Override
    public void onServerReady() {

    }

    @Override
    public void onServerStart() {

    }

    @Override
    public void onServerStop() {

    }

    @Override
    public void onServerConnected(final String userName) {
      MainActivity.this.clientDevicesAdapter.add(userName);

      runOnUiThread(new Runnable() {

        @Override
        public void run() {
          MainActivity.this.clientDevicesAdapter.notifyDataSetChanged();
          txtDevicesNotFound.setVisibility(View.GONE);
        }
      });
    }

    @Override
    public void onServerDisConnected(final String userName) {
        Logger.print("onServerDisConnected = " + userName);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Logger.print("server is removing username= " + userName);
            for(int i=0; i< MainActivity.this.clientDevicesAdapter.getCount(); i++) {
              String deviceName = MainActivity.this.clientDevicesAdapter.getItem(i);
              Logger.print("connected devices name:" + deviceName);
            }

            MainActivity.this.clientDevicesAdapter.remove(userName);
            MainActivity.this.clientDevicesAdapter.notifyDataSetChanged();
            if ( MainActivity.this.clientDevicesAdapter.getCount() == 0)
              txtDevicesNotFound.setVisibility(View.VISIBLE);
          }
        });

    }
  }

  public void setAppMode() {

    Global.CONNECTED_TO_SERVERNAME = "";

    clearDeviceList();


    txtDevicesNotFound.setVisibility(View.VISIBLE);
    if (AppSettings.with(BaseApplication.getContext()).isBroadCastMode()) {

      Global.audioService.stopServer();
//      Global.audioService.runServer();
      AppSettings.with(BaseApplication.getContext()).setMute(false);

      listRemoteDevices.setAdapter(clientDevicesAdapter);

      setTitle(getString(R.string.app_name) + " - " + getString(R.string.mode_broadcast));

      swipeRefreshLayout.setEnabled(false);

      layoutServerSettings.setVisibility(View.VISIBLE);

      btnStartStop.setChecked(false);
      btnMuteUnMute.setVisibility(View.GONE);
    } else {

      if (Global.isListening) {
        if(Global.audioService.client != null)
          Global.audioService.client.disconnect();
      }

      Global.audioService.seekServers();

      listRemoteDevices.setAdapter(serverDevicesAdapter);

      setTitle(getString(R.string.app_name) + " - " + getString(R.string.mode_listen));

      swipeRefreshLayout.setEnabled(true);

      layoutServerSettings.setVisibility(View.GONE);
    }
  }

  private void initAudioManager() {
    Logger.print(TAG, "initAudioManager");
    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    audioManager.setMode(AudioManager.MODE_IN_CALL);
    audioManager.setSpeakerphoneOn(false);
  }

  private void turnOnAudioService() {
    Logger.print(TAG, "turnOnAudioService");
    Log.i(TAG, "turnOnAudioService");
    startService(new Intent(this, UPnPAudioService.class));
    _serviceConnection = new UpnpServiceConnection();
    bindService(new Intent(this, UPnPAudioService.class), _serviceConnection, BIND_AUTO_CREATE);
  }

  protected void onDestroy() {
    Logger.print(TAG, "onDestroy");
    unbindService(_serviceConnection);
    stopService(new Intent(this, UPnPAudioService.class));
    ButterKnife.unbind(this);
    super.onDestroy();
  }
}
