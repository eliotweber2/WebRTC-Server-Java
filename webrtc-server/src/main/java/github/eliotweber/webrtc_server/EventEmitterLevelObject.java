package github.eliotweber.webrtc_server;

import java.util.*;

public class EventEmitterLevelObject {
    public String level;
    private List<String> labels = new ArrayList<>();

    public EventEmitterLevelObject(String type, String id) {
        switch (type) {
            case "server_manager":
                this.labels.add("global");
                this.labels.add("server_manager");
                break;
            case "server":
                this.labels.add("global");
                this.labels.add("server_manager");
                this.labels.add("server");
                this.labels.add(id);
                break;
            case "connection":
                this.labels.add("global");
                this.labels.add("connection");
                this.labels.add(id);
                break;
            case "api":
                this.labels.add("global");
                this.labels.add("api");
                break;
        }

        this.setLevel();
    }

    private void setLevel() {
        this.level = String.join(".", this.labels);
    }
}

/*
 * Event emitter level structure:
 * Global
 * - Server Manager
 * -- Server
 * --- Server ID
 * 
 * - Connection
 * -- Connection ID
 * - API
 */