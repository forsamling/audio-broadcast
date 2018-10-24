package no.forsamling.audiobroadcast.controller;

import android.util.Log;

import no.forsamling.audiobroadcast.BaseApplication;
import no.forsamling.audiobroadcast.Global;
import no.forsamling.audiobroadcast.utils.AppSettings;
import no.forsamling.audiobroadcast.utils.PortSeeker;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.concurrent.Executors;

public class Server {
    private ChannelFactory factory;

    public void run() {
        Log.i("ServerThread", "begin");
        Global.isSpeaking = true;
        try {
            runServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runServer() throws IOException {
        if (Global.connectedClients != null) {
            Log.i("ServerThread", "Cleaning up from previous run");
            if (Global.connectedClients.size() > 0) {
                throw new RuntimeException("This isn't supposed to happen.");
            }
        }
        Global.SERVER_ADDRESS = Global.getServerIP();
        if (Global.SERVER_ADDRESS == null) {
            throw new IOException("Unable to determine your IP.");
        }
        Global.SERVER_PORT = PortSeeker.getNextAvailable(Global.DEFAULT_PORT);

        InetSocketAddress address = new InetSocketAddress(Global.SERVER_PORT);
        this.factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        ServerBootstrap bootstrap = new ServerBootstrap(this.factory);


        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new ServerHandler());
            }
        });


        bootstrap.setOption("child.tcpNoDelay", Boolean.valueOf(true));
        bootstrap.setOption("child.keepAlive", Boolean.valueOf(true));

      // Bind and start to accept incoming connections.
        bootstrap.bind(address);

        if(Global.audioService.eventsListener != null)
          Global.audioService.eventsListener.onServerReady();
    }

    public void stream(byte[] data) {
        if(!AppSettings.with(BaseApplication.getContext()).isMute())
            for (ClientDetails cd : Global.connectedClients) {
                cd.writeData(data);
            }
    }

    public void stop() {
        ChannelGroup allChannels = new DefaultChannelGroup("audiobroadcast-server");
        Iterator<ClientDetails> it = Global.connectedClients.iterator();
        while (it.hasNext()) {
            allChannels.add(((ClientDetails) it.next()).channel);
            it.remove();
        }
        allChannels.close().awaitUninterruptibly();
        this.factory.releaseExternalResources();
    }
}
