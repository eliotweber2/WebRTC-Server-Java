package github.eliotweber.webrtc_server;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.*;

public class ServerManager {
    public final EventManager eventManager = new EventManager();
    private Map<String, Server> servers = new HashMap<>();
    private ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public final String id = UUID.randomUUID().toString();
    public final EventLevel level = new EventLevel("server_manager", id, null);

    public void start() {
        eventManager.addEventListener(new EventObserver() {
            {
                eventTypes = new EventType[] {EventType.NEW_CONNECTION};
                level = new EventLevel("api", "*", null);
            }

            @Override
            public void onEvent(Event event) {
                System.out.println("New connection event received: " + event);
            }
        });
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