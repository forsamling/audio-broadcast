package com.royalone.audiobroadcast.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.royalone.audiobroadcast.utils.Notice;

public class HeadSetReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
      int state = intent.getIntExtra("state", -1);

      StringBuilder sb = new StringBuilder("Headset ");

      switch (state) {
        case 0:
          // Headset unplugged
          sb.append("disconnected");
          break;
        case 1:
          // Headset plugged in
          sb.append("connected");
          break;
      }

      sb.append(".");

      int microphone = intent.getIntExtra("microphone", 0);

      switch (microphone) {
        case 0:
          // Microphone unplugged
          sb.append("\nNo microphone.");
          break;
        case 1:
          // Microphone plugged in
          sb.append("\nMicrophone detected.");
          break;
      }

      Notice.show(sb.toString());
    }
  }
}
