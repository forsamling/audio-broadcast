package no.forsamling.audiobroadcast.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import no.forsamling.audiobroadcast.Global;
import no.forsamling.audiobroadcast.R;
import no.forsamling.audiobroadcast.model.ClingDevice;
import no.forsamling.audiobroadcast.utils.Logger;

import java.util.ArrayList;

public class ServerDevicesAdapter extends ArrayAdapter<ClingDevice> {
  public ServerDevicesAdapter(Context context, int textViewResourceId) {
    super(context, textViewResourceId);
  }

  public ArrayList<ClingDevice> getDevices() {
    int count = getCount();
    ArrayList<ClingDevice> clingDevices = new ArrayList<>();

    for(int i = 0; i<count; i++) {
      clingDevices.add(getItem(i));
    }
    return  clingDevices;

  }
  public boolean areAllItemsEnabled() {
    return false;
  }

  public boolean isEnabled(int position) {
    return ((ClingDevice) getItem(position)).device.isFullyHydrated();
  }

  public ServerDevicesAdapter(Context context, ArrayList<ClingDevice> deviceDisplays) {
    super(context, 0, deviceDisplays);
  }

  public OnDeviceClickListener getOnDeviceClickListener() {
    return onDeviceClickListener;
  }

  public void setOnDeviceClickListener(OnDeviceClickListener onDeviceClickListener) {
    this.onDeviceClickListener = onDeviceClickListener;
  }

  public interface OnDeviceClickListener {
      void onDeviceClicked(ClingDevice deviceDisplay);
  }

  private OnDeviceClickListener onDeviceClickListener;

  public View getView(int position, View convertView, ViewGroup parent) {

    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_device, parent, false);
    }

    TextView txtDeviceName = (TextView) convertView.findViewById(R.id.txt_device_name);

    final ClingDevice deviceDisplay =  getItem(position);

    Logger.print("DeviceName:" + deviceDisplay.toString());
    txtDeviceName.setText(deviceDisplay.toString());

    if (isEnabled(position)) {
      txtDeviceName.setEnabled(true);
    } else {
      txtDeviceName.setEnabled(false);
    }

    txtDeviceName.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          if(onDeviceClickListener!=null)
            onDeviceClickListener.onDeviceClicked(deviceDisplay);
      }
    });


    if (!Global.CONNECTED_TO_SERVERNAME.equals("") && deviceDisplay.toString().equals(Global.CONNECTED_TO_SERVERNAME)) {
      Animation anim = new AlphaAnimation(0.0f, 1.0f);
      anim.setDuration(500);
      anim.setStartOffset(0);
      anim.setRepeatMode(Animation.REVERSE);
      anim.setRepeatCount(Animation.INFINITE);
      txtDeviceName.startAnimation(anim);
    } else {
      txtDeviceName.clearAnimation();
    }

    return convertView;
  }
}
