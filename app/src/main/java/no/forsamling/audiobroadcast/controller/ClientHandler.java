package no.royalone.audiobroadcast.controller;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import no.royalone.audiobroadcast.Global;
import no.royalone.audiobroadcast.utils.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ClientHandler extends SimpleChannelHandler {
  public String TAG = ClientHandler.class.getSimpleName();

  private AudioTrack audioTrack;
  int bufferSizeInBytes;

  public ClientHandler(int bufferSizeInBytes) {
    this.bufferSizeInBytes = bufferSizeInBytes;
    this.audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Global.RECORDER_SAMPLERATE,  AudioFormat.CHANNEL_OUT_MONO, Global.RECORDER_AUDIO_ENCODING, bufferSizeInBytes, 1);
    float maxVolume = AudioTrack.getMaxVolume();
    this.audioTrack.setStereoVolume(maxVolume, maxVolume);
    Global.isListening = true;

    Logger.print("onClientConnected");
    Global.audioService.eventsListener.onClientConnected();
    this.audioTrack.play();
  }

  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Channel c = e.getChannel();
    if (Global.isListening && c.isConnected() && c.isReadable()) {
      short[] shortBuffer = new short[(this.bufferSizeInBytes / 2)];
      ByteBuffer.wrap(((ChannelBuffer) e.getMessage()).array()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortBuffer);
      this.audioTrack.write(shortBuffer, 0, shortBuffer.length);
    }
  }


  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    Logger.print(TAG, "exceptionCaught");

    e.getCause().printStackTrace();
    e.getChannel().close();
  }

  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    Logger.print(TAG, "channelClosed");

    Global.isListening = false;
    this.audioTrack.flush();
    this.audioTrack.stop();
    e.getChannel().close();
    Global.audioService.disconnectClient();
  }
}
