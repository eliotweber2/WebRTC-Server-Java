package github.eliotweber.webrtc_server;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.*;

public class ServerManager {
    private EventManager eventManager = new EventManager();
    private Map<String, Server> servers = new HashMap<>();
    private ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public ServerManager() {
    }

    public String[] getServers() {
        return servers.keySet().toArray(new String[0]);
    }

    public void registerServer(Server server) {
        servers.put(server.id, server);
        server.eventManager = eventManager;
        threadPool.execute(server);
    }
}