package github.eliotweber.webrtc_server;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.*;

public class ServerManager {
    private EventManager eventManager = new EventManager();
    private Map<String, Server> servers = new HashMap<>();

    public ServerManager() {
    }

    public void registerServer(Server server) {
        servers.put(server.id, server);
        server.eventManager = eventManager;
        server.onSetup();
    }

    public void start() {
        WebrtcServerApplication.runServer();
    }
}