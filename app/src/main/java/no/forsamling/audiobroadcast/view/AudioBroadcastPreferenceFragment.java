package no.forsamling.audiobroadcast.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import no.forsamling.audiobroadcast.BaseApplication;
import no.forsamling.audiobroadcast.Global;
import no.forsamling.audiobroadcast.MainActivity;
import no.forsamling.audiobroadcast.R;
import no.forsamling.audiobroadcast.utils.AppSettings;
import no.forsamling.audiobroadcast.utils.Logger;
import no.forsamling.audiobroadcast.utils.Notice;

/**
 * Created by royalone on 2017-02-25.
 */

public class AudioBroadcastPreferenceFragment extends PreferenceFragment {
  private static final String TAG = AudioBroadcastPreferenceFragment.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);
    ListPreference appModeRef = (ListPreference) findPreference(getString(R.string.pref_app_mode));

    if (AppSettings.with(BaseApplication.getContext()).isBroadCastMode())
      appModeRef.setValueIndex(1);
    else
      appModeRef.setValueIndex(0);
    appModeRef.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        String val = (String) newValue;

        Logger.print("Selected " + val);

        try {
          selectAppMode(val);
        } catch (Exception e) {
          e.printStackTrace();
        }

        return true;
      }
    });


    Preference nameDevicePref = findPreference(getString(R.string.pref_name_device));
    nameDevicePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        // view the app's website in a web browser
        final LinearLayout layout = new LinearLayout(AudioBroadcastPreferenceFragment.this.getActivity());
        LayoutInflater.from(BaseApplication.getContext()).inflate(R.layout.dialog_device_name, layout);
        ((EditText) layout.findViewById(R.id.txtDeviceName)).setText(AppSettings.with(BaseApplication.getContext()).getDeviceName());

        final AlertDialog dialog = new AlertDialog.Builder(AudioBroadcastPreferenceFragment.this.getActivity()).setTitle(getString(R.string.pref_app_mode)).setPositiveButton(getString(R.string.btn_save), null).setNegativeButton(getString(R.string.btn_cancel), null).setInverseBackgroundForced(true).setView(layout).create();
        dialog.getWindow().setSoftInputMode(16);
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            String newName = ((EditText) layout.findViewById(R.id.txtDeviceName)).getEditableText().toString().trim();
            if (newName.length() == 0) {
              Notice.show("Please input device name");
              return;
            }

            AppSettings.with(BaseApplication.getContext()).setDeviceName(newName);
            dialog.dismiss();
          }
        });

        return true;
      }
    });
  }


  public void selectAppMode(String val) throws Exception{

      if (val.equals(getString(R.string.mode_broadcast))) {

        if (!AppSettings.with(BaseApplication.getContext()).isBroadCastMode()) {
          AppSettings.with(BaseApplication.getContext()).setBroadCastMode(true);

          if (Global.isListening) {
            Global.audioService.disconnectClient();
            Global.isListening = false;
          }

          MainActivity.getInstance().setAppMode();
        }

      } else {

        if (AppSettings.with(BaseApplication.getContext()).isBroadCastMode()) {

          Global.isSpeaking = false;
          Global.audioService.stopServer();

          Notice.show("Stopped BroadCasting");
          AppSettings.with(BaseApplication.getContext()).setBroadCastMode(false);



          MainActivity.getInstance().setAppMode();

        }
      }
  }
  @Override
  public void onResume() {
    super.onResume();

  }

  @Override
  public void onPause() {
    super.onPause();
  }

}
