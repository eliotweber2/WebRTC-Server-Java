package github.eliotweber.webrtc_server;

import java.time.Instant;
import java.util.Map;

public class EventEmitter {
    private final EventManager eventManager;
    public final EventLevel level;

    public EventEmitter(EventManager eventManager, EventLevel level) {
        this.eventManager = eventManager;
        this.level = level;
    }

    public void emit(EventType eventType, Map<String, Object> data) {
        Event event = new Event(eventType, data, Instant.now());
        eventManager._dispatchEvent(event, this.level);
    }
}
