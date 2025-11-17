package github.eliotweber.webrtc_server;

import java.util.*;

public class EventLevel {
    public final String level;
    private final List<String> labels;

    public EventLevel(String type, String id, EventType eventType) {
        labels = new ArrayList<>();

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

        if (eventType != null) {
            this.labels.add(eventType.toString().toLowerCase());
        }

        level = String.join(".", this.labels);
    }

    public EventLevel(EventLevel level, EventType eventType) {
        labels = Arrays.asList(level.getArray());
        this.labels.add(eventType.toString().toLowerCase());
        this.level = String.join(".", this.labels);
        
    }

    public String[] getArray() {
        return this.level.split(".");
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