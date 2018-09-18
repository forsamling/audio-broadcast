package com.royalone.audiobroadcast.utils;

import com.royalone.audiobroadcast.utils.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class BufferDecoder extends FrameDecoder {
  private int bufferSizeInBytes;

  public BufferDecoder(int bufferSizeInBytes) {
    this.bufferSizeInBytes = bufferSizeInBytes;
  }

  protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {
    if (buffer.readableBytes() < this.bufferSizeInBytes) {
      return null;
    }
    return buffer.readBytes(this.bufferSizeInBytes);
  }
}
