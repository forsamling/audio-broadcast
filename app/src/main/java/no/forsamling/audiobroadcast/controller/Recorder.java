package no.royalone.audiobroadcast.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build.VERSION;

import no.royalone.audiobroadcast.BaseApplication;
import no.royalone.audiobroadcast.Global;
import no.royalone.audiobroadcast.utils.Logger;
import no.royalone.audiobroadcast.utils.Notice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Recorder implements Runnable {
  public void run() {
    ((AudioManager) BaseApplication.getContext().getSystemService(Context.AUDIO_SERVICE)).setParameters("noise_suppression=on");

    try {
      recordAudio();
    } catch (IOException e) {
      e.printStackTrace();
      if (e.getLocalizedMessage() != null)
        Notice.show(e.getLocalizedMessage());
    }
  }

  private void recordAudio() throws IOException {
    Logger.print("recordAudio");
    Throwable th;
    final int SAMPLE_RATE = 8000;
    AudioRecord audioRecorder;
    try {
      int bufferSizeInBytes = AudioRecord.getMinBufferSize(Global.RECORDER_SAMPLERATE, Global.RECORDER_CHANNELS, Global.RECORDER_AUDIO_ENCODING) ;

      int bufferSizeInShorts = bufferSizeInBytes / 2;

      if (bufferSizeInBytes == AudioRecord.ERROR || bufferSizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
        bufferSizeInBytes = SAMPLE_RATE * 2;
      }


      audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, Global.RECORDER_SAMPLERATE,Global.RECORDER_CHANNELS, Global.RECORDER_AUDIO_ENCODING, bufferSizeInBytes);
      try {
        noiseCancellation(audioRecorder);
        short[] shortBuffer = new short[bufferSizeInShorts];
        audioRecorder.startRecording();
        while (Global.isSpeaking) {
          int shortsRead = audioRecorder.read(shortBuffer, 0, bufferSizeInShorts);
          if (shortsRead == -2 || shortsRead == -3) {
            Logger.print("recordAudio", "Error reading from microphone.");
            Global.isSpeaking = false;
            break;
          }
          byte[] byteBuffer = new byte[bufferSizeInBytes];
          ByteBuffer.wrap(byteBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortBuffer);

          if (Global._playback != null) {
            Global._playback.play(shortBuffer);
          }

          if (Global._server != null)
            Global._server.stream(byteBuffer);
        }

        audioRecorder.stop();
        audioRecorder.release();
      } catch (Exception e) {

      }
    } catch (Exception e1) {
      audioRecorder = null;
      if (audioRecorder != null) {
        audioRecorder.stop();
        audioRecorder.release();
      }
    }
  }

  @TargetApi(16)
  protected void noiseCancellation(AudioRecord audioRecorder) {
    if (VERSION.SDK_INT >= 16 && NoiseSuppressor.isAvailable()) {
      NoiseSuppressor.create(audioRecorder.getAudioSessionId());
    }
  }
}
