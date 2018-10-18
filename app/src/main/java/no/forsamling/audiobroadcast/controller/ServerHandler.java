package no.forsamling.audiobroadcast.controller;

import no.forsamling.audiobroadcast.Global;
import no.forsamling.audiobroadcast.utils.Logger;
import no.forsamling.audiobroadcast.utils.Notice;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.Iterator;

public class ServerHandler extends SimpleChannelHandler {
  public String TAG = ServerHandler.class.getSimpleName();

  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    Logger.print(TAG, "exceptionCaught");

    if(e.getCause().getLocalizedMessage()!=null)
      Logger.print(TAG, e.getCause().getLocalizedMessage());
    e.getCause().printStackTrace();
    e.getChannel().close();
  }

  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    Channel c = e.getChannel();
    Logger.print(TAG, "Channel connected: " + String.valueOf(c.getId()));
    Global.connectedClients.add(new ClientDetails(c));
    Global.audioService.updateNotificationsWithListenersCount();
  }

  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    Channel c = e.getChannel();
    Logger.print(TAG, "Channel Closed: " + String.valueOf(c.getId()));
    Iterator<ClientDetails> it = Global.connectedClients.iterator();
    while (it.hasNext()) {
      if (((ClientDetails) it.next()).channel.getId() == c.getId()) {
        it.remove();
        break;
      }
    }
    c.close();
    Notice.show("A device has disconnected.");
    Global.audioService.updateNotificationsWithListenersCount();
  }
}
