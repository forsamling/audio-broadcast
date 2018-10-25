package no.forsamling.audiobroadcast.controller;

import android.media.AudioFormat;
import android.media.AudioTrack;

import no.forsamling.audiobroadcast.Global;

import no.forsamling.audiobroadcast.MainActivity;
import no.forsamling.audiobroadcast.utils.BufferDecoder;
import no.forsamling.audiobroadcast.utils.Logger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.util.concurrent.Executors;


public class Client {
    private ChannelFactory factory;
    private final String host;
    private final int port;
    private ClientBootstrap bootstrap;
    private ClientHandler clientHandler;
    private BufferDecoder bufferDecoder;
    private   Channel channel;

    ChannelFuture future;
    public Client(String host, int port) {
        Logger.print("Audio Client Connecting", host + ":" + String.valueOf(port));
        this.host = host;
        this.port = port;
    }

    public void run() {
        Logger.print("Client RUN!!!~~~~~");
        final int bufferSizeInBytes = AudioTrack.getMinBufferSize(Global.RECORDER_SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO, Global.RECORDER_AUDIO_ENCODING);
        this.factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        bootstrap = new ClientBootstrap(this.factory);

        clientHandler = new ClientHandler(bufferSizeInBytes);
        bufferDecoder = new BufferDecoder(bufferSizeInBytes);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(bufferDecoder, clientHandler);
            }
        });
        bootstrap.setOption("tcpNoDelay", Boolean.valueOf(true));
        bootstrap.setOption("keepAlive", Boolean.valueOf(true));

        Logger.print("BOOTSTRAP CONNECT");
        future =  bootstrap.connect(new InetSocketAddress(this.host, this.port));

        channel = future.awaitUninterruptibly().getChannel();
    }

    public void disconnect() {
        Logger.print("client audio play back stop");
        Client.this.factory.releaseExternalResources();
        Logger.print("client audio play back stopped");
    }


    public void disconnectManually(){
        if (channel != null) {
            channel.close().awaitUninterruptibly();
        }
//        http://lists.jboss.org/pipermail/netty-users/2011-July/004708.html
        Logger.print("disconnectManually");
        try {

            clientHandler.channelClosed(null, null);
            disconnect();
            bootstrap.releaseExternalResources();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }
}
