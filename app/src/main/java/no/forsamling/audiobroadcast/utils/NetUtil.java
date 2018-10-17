package no.forsamling.audiobroadcast.utils;

/**
 * Created by royalone on 2017-01-09.
 */

import android.content.Context;
/*import android.net.ConnectivityManager;
import android.net.NetworkInfo;*/
import android.net.wifi.WifiManager;

public class NetUtil {
    public static boolean wifiEnabled(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }
}
