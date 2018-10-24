package no.forsamling.audiobroadcast.services;

import no.forsamling.audiobroadcast.Global;

import no.forsamling.audiobroadcast.utils.Logger;
import no.forsamling.audiobroadcast.utils.Notice;

import org.teleal.cling.binding.annotations.UpnpAction;
import org.teleal.cling.binding.annotations.UpnpInputArgument;
import org.teleal.cling.binding.annotations.UpnpOutputArgument;
import org.teleal.cling.binding.annotations.UpnpService;
import org.teleal.cling.binding.annotations.UpnpServiceId;
import org.teleal.cling.binding.annotations.UpnpServiceType;
import org.teleal.cling.binding.annotations.UpnpStateVariable;


@UpnpService(
  serviceId = @UpnpServiceId("AudioBroadCast"),
  serviceType = @UpnpServiceType(value = Global.SERVICE_TYPE, version = 1)
)
public class ClingService {
  @UpnpStateVariable
  private String Response;
  @UpnpStateVariable
  private String ServerAddress;
  @UpnpStateVariable
  private String Username;
  @UpnpStateVariable
  private String Version;

  @UpnpAction(out = {@UpnpOutputArgument(name = "ServerAddress")})
  public String getServerAddress() {
    StringBuilder sb = new StringBuilder();
    sb.append(Global.SERVER_ADDRESS);
    sb.append(":");
    sb.append(Global.SERVER_PORT);
    return sb.toString();
  }

  @UpnpAction(out = {@UpnpOutputArgument(name = "Response")})
  public String userConnect(@UpnpInputArgument(name = "Username", stateVariable = "Username") String username, @UpnpInputArgument(name = "Version", stateVariable = "Version") String version) {
    StringBuilder sb = new StringBuilder();

    sb.append("'");
    sb.append(username);
    sb.append("' connected with v");
    sb.append(version);
    sb.append("\n(");
    sb.append(" user");



    sb.append(" listening to broadcast)");

    try {
      Global.connectedUsers.add(username);
      Global.audioService.updateNotificationsWithListenersCount();

      Global.audioService.eventsListener.onServerConnected(username);
    }catch (Exception e) {
      e.printStackTrace();
    }

    Notice.show(sb.toString());
    Logger.print("userConnect");
    return "0";
  }

  @UpnpAction(out = {@UpnpOutputArgument(name = "Response")})
  public String userDisconnect(@UpnpInputArgument(name = "Username", stateVariable = "Username") String username, @UpnpInputArgument(name = "Version", stateVariable = "Version") String version) {
    StringBuilder sb = new StringBuilder();

    sb.append("'");
    sb.append(username);
    sb.append("' disconnected with v");
    sb.append(version);
    sb.append("\n(");
    sb.append(" user");

    try {

      Global.connectedUsers.remove(username);
      Global.audioService.updateNotificationsWithListenersCount();
      Global.audioService.eventsListener.onServerDisConnected(username);
    }catch (Exception e) {
      e.printStackTrace();
    }

    Notice.show(sb.toString());
    Logger.print("userDisconnect");
    return "0";
  }

}
