package com.royalone.audiobroadcast.controller;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

public class ClientDetails {
    public Channel channel;

    public ClientDetails(Channel session) {
        this.channel = session;
    }

    public void writeData(byte[] input) {
        if (this.channel.isConnected() && this.channel.isWritable()) {
            this.channel.write(ChannelBuffers.wrappedBuffer(input));
        }
    }
}
