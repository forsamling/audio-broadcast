package no.forsamling.audiobroadcast;

import android.app.Application;
import android.content.Context;

import no.forsamling.audiobroadcast.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by royal on 8/19/2017.
 */
//https://stackoverflow.com/questions/6794576/jboss-netty-fails-to-send-data-continuously
//  https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView


@ReportsCrashes(
  mailTo = "apps@forsamling.no", // my email here
  mode = ReportingInteractionMode.TOAST,
  resToastText = R.string.crash_toast_text)
public class BaseApplication extends Application {
    public static BaseApplication _application;

    public final String TAG = BaseApplication.class.getSimpleName();

    public static BaseApplication getInstance() {
        return _application;
    }

    @Override
    public void onCreate() {
        super.onCreate();

      ACRA.init(this);
        _application = this;
    }

    public static Context getContext() {
    return _application.getApplicationContext();
  }

}
