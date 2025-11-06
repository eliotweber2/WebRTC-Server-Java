package github.eliotweber.webrtc_server;

import java.util.Map;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.Timer;

public abstract class Server extends Thread {
    String id;
    EventManager eventManager;
    Map<String, WebrtcConnectionHandler> connections = new HashMap<>();
    Timer heartbeatTimer;


    public abstract void setup();

    public abstract void onNewClient(String clientId, WebrtcConnectionHandler handler);

    public void run() {
        this.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.println("Uncaught exception in server " + id + ": " + e.getMessage());
                e.printStackTrace();
                cleanup();
            }
        });
        heartbeatTimer = new Timer(true);
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                return;
            }
        }, 0, 55000);
        this.setup();
    };

    private void cleanup() {
        heartbeatTimer.cancel();
        for (WebrtcConnectionHandler handler : connections.values()) {
            handler.onClose();
        }
    }
}