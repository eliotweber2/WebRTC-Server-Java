package github.eliotweber.webrtc_server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
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
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.media.audio.AudioDeviceModule;
import dev.onvoid.webrtc.media.audio.AudioLayer;


@SpringBootApplication
@RestController
@CrossOrigin(origins="*")
public class WebrtcServerApplication {

  private static final String prefix = "webrtc";

  private static RTCConfiguration config = new RTCConfiguration();

  public static final RTCOfferOptions options = new RTCOfferOptions();

  private static final AudioDeviceModule audioDeviceModule = new AudioDeviceModule(AudioLayer.kDummyAudio);
  private static final PeerConnectionFactory factory = new PeerConnectionFactory(audioDeviceModule);

  public static final String[] IceServers = new String[] {
    "stun:stun.l.google.com:19302",
    "stun:stun1.l.google.com:19302",
    "stun:stun2.l.google.com:19302",
    "stun:stun3.l.google.com:19302",
    "stun:stun4.l.google.com:19302"
  };

  static {
    for (String server : IceServers) {
      RTCIceServer iceServer = new RTCIceServer();
      iceServer.urls.add(server);
      config.iceServers.add(iceServer);
    }
  }

  private Map <String, ConnectionHandler> connections = new HashMap<>();  


	public static void main(String[] args) {
		SpringApplication.run(WebrtcServerApplication.class, args);
	}

  //private static final String[] handlers = new String[] {"default"};

  @GetMapping("/")
  public String defaultResponse() {
    return "Hello World!";
  }


  @PostMapping("/" + prefix + "/new-connection")
  //@SuppressWarnings("unchecked")
  public OfferReturnObject getOffer() {
    System.out.println("New connection requested");
    String id = UUID.randomUUID().toString();
    ConnectionHandler handler = setupConnection(id);
    OfferReturnObject response = new OfferReturnObject();
    try {
      RTCSessionDescription offer = handler.getOffer().get();
      response.sdp = offer.sdp;
      response.type = offer.sdpType.toString();
      System.out.println(offer.sdp);
      System.out.println(offer.sdpType.toString());
      response.id = id;
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println(id);
    return response;
  }

  

  @GetMapping("/" + prefix + "/connections")
  public List<String> getConnections() {
      return new ArrayList<>(connections.keySet());
  }

  @PostMapping("/" + prefix + "/connections/{id}")
  private ConnectionHandler setupConnection(@PathVariable String id) {
    ConnectionHandler handler = new ConnectionHandler(id,WebrtcServerApplication.factory, WebrtcServerApplication.config);
    this.connections.put(id, handler);
    return handler;
  }

  @PostMapping("/" + prefix + "/connections/{id}/offer")
  public void receiveOffer(@PathVariable String id, @RequestBody JSONObject body) {
      ConnectionHandler handler = this.connections.get(id);
      RTCSessionDescription remoteOffer = (RTCSessionDescription) body.get("offer");
      handler.receiveOffer(remoteOffer);
  }

  @GetMapping("/" + prefix + "/connections/{id}/ice-candidates")
  public List<RTCIceCandidate> getIceCandidates(@PathVariable String id) {
      ConnectionHandler handler = this.connections.get(id);
      return handler.iceCandidates;
  }

  @PostMapping("/" + prefix + "/connections/{id}/ice-candidates")
  public void addIceCandidate(@PathVariable String id, @RequestBody JSONObject body) {
      ConnectionHandler handler = this.connections.get(id);
      RTCIceCandidate candidate = (RTCIceCandidate) body.get("candidate");
      handler.iceCandidates.add(candidate);
  }
}

class OfferReturnObject {
    public String sdp;
    public String type;
    public String id;
}