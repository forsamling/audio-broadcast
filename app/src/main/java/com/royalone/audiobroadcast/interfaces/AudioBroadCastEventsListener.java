package com.royalone.audiobroadcast.interfaces;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.RemoteDevice;

public interface AudioBroadCastEventsListener {
    void onClientConnect();

    void onClientConnected();

    void onClientDisconnected();

    void onClientSearchError(String str);

    void onSearchAddDevice(Device<?, ?, ?> device);

    void onSearchBegin();

    void onSearchDeviceFailed(RemoteDevice remoteDevice, Exception exception);

    void onSearchEnd();

    void onSearchRemoveDevice(Device<?, ?, ?> device);

    void onServerReady();

    void onServerStart();

    void onServerStop();

    void onServerConnected(String userName);

    void onServerDisConnected(String userName);
}
