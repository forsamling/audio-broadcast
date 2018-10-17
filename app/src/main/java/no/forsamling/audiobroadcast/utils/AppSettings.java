package no.royalone.audiobroadcast.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import no.royalone.audiobroadcast.Global;

public class AppSettings {

  private static AppSettings instance = null;
  Context mContext = null;


  private AppSettings(@NonNull Context context) {
    if (mContext == null) {
      mContext = context;
    }
  }

  /**
   * @param context
   * @return Returns a 'Prefs' instance
   */

  public static AppSettings with(@NonNull Context context) {
    if (instance == null) {
      instance = new AppSettings(context);
    }
    return instance;
  }

  public void setDeviceName(String deviceName) {
    Prefs.with(mContext).write("pref_device_name", deviceName);
  }

  public String getDeviceName() {
    return Prefs.with(mContext).read("pref_device_name", Global.getDeviceBrand());
  }

  public Boolean isBroadCastMode(){
    return Prefs.with(mContext).readBoolean("pref_broadcast");
  }

  public void setBroadCastMode(boolean bMode) {
    Prefs.with(mContext).writeBoolean("pref_broadcast", bMode);
  }

  public String getVersion(){
    String version = "";
    try {
      version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return  version;
  }
}
