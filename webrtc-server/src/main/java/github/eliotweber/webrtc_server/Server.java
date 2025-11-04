package github.eliotweber.webrtc_server;

import java.util.Map;
import java.util.HashMap;

public abstract class Server {
    String id;
    EventManager eventManager;
    Map<String, WebrtcConnectionHandler> connections = new HashMap<>();


    public abstract void onSetup();
}
