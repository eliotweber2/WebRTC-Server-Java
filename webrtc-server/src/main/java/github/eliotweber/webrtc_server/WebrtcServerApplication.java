package github.eliotweber.webrtc_server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.RTCConfiguration;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCIceServer;
import dev.onvoid.webrtc.RTCOfferOptions;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.media.audio.AudioDeviceModule;
import dev.onvoid.webrtc.media.audio.AudioLayer;


@SpringBootApplication(exclude = {  SecurityAutoConfiguration.class })
@RestController
@CrossOrigin(origins="*")
public class WebrtcServerApplication {

  private static final String prefix = "webrtc";

  public final String id = UUID.randomUUID().toString();
  public final EventLevel level = new EventLevel("api", id, null);

  public final EventManager eventManager;

  private static RTCConfiguration config = new RTCConfiguration();

  public static final RTCOfferOptions options = new RTCOfferOptions();

  private static final AudioDeviceModule audioDeviceModule = new AudioDeviceModule(AudioLayer.kDummyAudio);
  private static final PeerConnectionFactory factory = new PeerConnectionFactory(audioDeviceModule);

  public static final String[] IceServers = new String[] {
    "stun:stun.l.google.com:19302",
  };

  static {
    for (String server : IceServers) {
      RTCIceServer iceServer = new RTCIceServer();
      iceServer.urls.add(server);
      config.iceServers.add(iceServer);
    }
  }

  private Map <String, ConnectionManager> connections = new HashMap<>();  
  private ServerManager serverManager;


	public static void runServer() {
		SpringApplication.run(WebrtcServerApplication.class);
	}

  public WebrtcServerApplication() {
    this.serverManager = new ServerManager();

    this.eventManager = this.serverManager.eventManager;
    this.eventManager.addLevel("api", this.level);
    
    this.serverManager.start();
  }

  private WebrtcConnectionHandler getHandlerByName(String name) {
    switch (name) {
      case "default":
        return new DefaultHandler();
      default:
        return new DefaultHandler();
    }
  }

  @GetMapping("/")
  public String defaultResponse() {
    return "Hello World!";
  }


  @PostMapping("/" + prefix + "/new-connection")
  @SuppressWarnings("unchecked")
  public JSONObject getOffer() {
    System.out.println("New connection requested");
    String id = UUID.randomUUID().toString();
    ConnectionManager manager = setupConnection(id);
    JSONObject response = new JSONObject();
    try {
      RTCSessionDescription offer = manager.getOffer().get();
      response.put("offerType",offer.sdpType.toString());
      response.put("offerSdp",offer.sdp);
      response.put("id", id);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return response;
  }

  @PostMapping("/" + prefix + "/connections/{id}/reconnect")
  @SuppressWarnings("unchecked")
  public JSONObject reconnectConnection(@PathVariable String id) {
      System.out.println("Reconnection requested for connection " + id);
      ConnectionManager manager = this.connections.get(id);
      manager.setupConnection(WebrtcServerApplication.factory, WebrtcServerApplication.config);
      JSONObject response = new JSONObject();
      try {
          RTCSessionDescription offer = manager.getOffer().get();
          response.put("offerType",offer.sdpType.toString());
          response.put("offerSdp",offer.sdp);
          response.put("id", id);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return response;
  }

  @GetMapping("/" + prefix + "/connections")
  public List<String> getConnections() {
      return new ArrayList<>(connections.keySet());
  }

  private ConnectionManager setupConnection(String id) {
    WebrtcConnectionHandler handler = getHandlerByName("default");
    ConnectionManager manager = new ConnectionManager(id,WebrtcServerApplication.factory, WebrtcServerApplication.config, handler);
    this.connections.put(id, manager);
    return manager;
  }

  @PostMapping("/" + prefix + "/connections/{id}/offer")
  //@SuppressWarnings("unchecked")
  public void receiveOffer(@PathVariable String id, @RequestBody JSONObject body) {
      ConnectionManager handler = this.connections.get(id);
      RTCSdpType type;
      if (body.get("offerType").equals("ANSWER")) {
          type = RTCSdpType.ANSWER;
      } else {
          Exception e = new Exception("Invalid SDP type: " + body.get("offerType"));
          e.printStackTrace();
          throw new RuntimeException(e);
      }
      RTCSessionDescription remoteOffer = new RTCSessionDescription(type, (String) body.get("offerSdp"));
      handler.receiveOffer(remoteOffer);
  }

  @GetMapping("/" + prefix + "/connections/{id}/ice-candidates")
  @SuppressWarnings("unchecked")
  public JSONObject getIceCandidates(@PathVariable String id) {
      ConnectionManager handler = this.connections.get(id);
      List<RTCIceCandidate> iceCandidates = handler.iceCandidates;
      JSONArray candidatesArray = new JSONArray();
      JSONObject response = new JSONObject();

      for (RTCIceCandidate candidate : iceCandidates) {
          JSONObject candidateJson = new JSONObject();
          candidateJson.put("candidate", candidate.sdp);
          candidateJson.put("sdpMid", candidate.sdpMid);
          candidateJson.put("sdpMLineIndex", candidate.sdpMLineIndex);
          candidateJson.put("serverUrl", candidate.serverUrl);
          candidatesArray.add(candidateJson);
      }

      response.put("candidates", candidatesArray);      
      return response;
  }

  @PostMapping("/" + prefix + "/connections/{id}/ice-candidates")
  public void addIceCandidate(@PathVariable String id, @RequestBody JSONObject body) {
      //System.out.println("Adding ICE candidate to connection " + id);
      ConnectionManager handler = this.connections.get(id);
      RTCIceCandidate candidate = (RTCIceCandidate) body.get("candidate");
      handler.iceCandidates.add(candidate);
  }

  @GetMapping("/" + prefix + "/servers")
  @SuppressWarnings("unchecked")
  public JSONObject getServers() {
      JSONObject response = new JSONObject();
      response.put("servers", this.serverManager.getServers());
      return response;
  }
}