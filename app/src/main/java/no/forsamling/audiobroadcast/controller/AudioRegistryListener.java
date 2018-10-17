package no.royalone.audiobroadcast.controller;

import no.royalone.audiobroadcast.Global;
import no.royalone.audiobroadcast.interfaces.AudioBroadCastEventsListener;
import no.royalone.audiobroadcast.model.ClingDevice;
import no.royalone.audiobroadcast.utils.Logger;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;


public class AudioRegistryListener extends DefaultRegistryListener {
  public String TAG = AudioRegistryListener.class.getSimpleName();

  public AudioBroadCastEventsListener audioBroadCastEventsListener;

  public void setEventsHandler(AudioBroadCastEventsListener eventsListener) {
    this.audioBroadCastEventsListener = eventsListener;
  }

  public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
    Logger.print("remoteDeviceDiscoveryStarted");
    ClingDevice d = new ClingDevice(device);
    Logger.print(d.toString());
    deviceAdded(device);

    if(audioBroadCastEventsListener!=null)
      audioBroadCastEventsListener.onSearchBegin();
  }

  public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
    Logger.print("remoteDeviceDiscoveryFailed");

    ClingDevice d = new ClingDevice(device);
    Logger.print(d.toString());
    deviceRemoved(device);
  }

  public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
    Logger.print("remoteDeviceAdded");

    ClingDevice d = new ClingDevice(device);
    Logger.print(d.toString());
    deviceAdded(device);
  }

  public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
    Logger.print("remoteDeviceRemoved");

    ClingDevice d = new ClingDevice(device);
    Logger.print(d.toString());
    deviceRemoved(device);
  }

  public void localDeviceAdded(Registry registry, LocalDevice device) {
    Logger.print("localDeviceAdded");
    ClingDevice d = new ClingDevice(device);
    Logger.print(d.toString());
//    deviceAdded(device);
  }

  public void localDeviceRemoved(Registry registry, LocalDevice device) {
    Logger.print("localDeviceRemoved");

    ClingDevice d = new ClingDevice(device);
    Logger.print(d.toString());
    deviceRemoved(device);
  }

  public void deviceAdded(Device<?, ?, ?> device) {

    Logger.print(TAG, device.getType().toString());
    Logger.print(TAG, new UDADeviceType(Global.DEVICE_TYPE).toString());
    if(device.getType().equals(new UDADeviceType(Global.DEVICE_TYPE))) {
      if(audioBroadCastEventsListener!=null)
        audioBroadCastEventsListener.onSearchAddDevice(device);
    }
  }

  public void deviceRemoved(Device<?, ?, ?> device) {
    this.audioBroadCastEventsListener.onSearchRemoveDevice(device);
  }
}

