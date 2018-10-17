package no.forsamling.audiobroadcast.controller;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;

import no.forsamling.audiobroadcast.BaseApplication;
import no.forsamling.audiobroadcast.Global;
import no.forsamling.audiobroadcast.MainActivity;
import no.forsamling.audiobroadcast.utils.BufferDecoder;
import no.forsamling.audiobroadcast.utils.Logger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


public class Client {
    private ChannelFactory factory;
    private final String host;
    private final int port;
    private ClientBootstrap bootstrap;
    private ClientHandler clientHandler;

    public Client(String host, int port) {
        Logger.print("Audio Client Connecting", host + ":" + String.valueOf(port));
        this.host = host;
        this.port = port;
    }

    public void run() {
        final int bufferSizeInBytes = AudioTrack.getMinBufferSize(Global.RECORDER_SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO, Global.RECORDER_AUDIO_ENCODING);
        this.factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        ClientBootstrap bootstrap = new ClientBootstrap(this.factory);

        clientHandler = new ClientHandler(bufferSizeInBytes);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new BufferDecoder(bufferSizeInBytes), new ClientHandler(bufferSizeInBytes));
            }
        });
        bootstrap.setOption("tcpNoDelay", Boolean.valueOf(true));
        bootstrap.setOption("keepAlive", Boolean.valueOf(true));
        bootstrap.connect(new InetSocketAddress(this.host, this.port));
    }

    public void disconnect() {
        Logger.print("client audio play back stop");
        Client.this.factory.releaseExternalResources();
        Logger.print("client audio play back stopped");
    }


    public void disconnectManually(){
        try {
            clientHandler.channelClosed(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
