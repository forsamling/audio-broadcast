package com.royalone.audiobroadcast.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.royalone.audiobroadcast.Global;
import com.royalone.audiobroadcast.R;
import com.royalone.audiobroadcast.model.ClingDevice;

import java.util.ArrayList;

public class ClientDevicesAdapter extends ArrayAdapter<String> {
  public ClientDevicesAdapter(Context context, int textViewResourceId) {
    super(context, textViewResourceId);
  }

  public boolean areAllItemsEnabled() {
    return false;
  }

  public ClientDevicesAdapter(Context context, ArrayList<String> deviceDisplays) {
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

    final String deviceName =  getItem(position);
    txtDeviceName.setText(deviceName);

    if (isEnabled(position)) {
      txtDeviceName.setEnabled(true);
    } else {
      txtDeviceName.setEnabled(false);
    }

    return convertView;
  }
}
