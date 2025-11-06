package github.eliotweber.webrtc_server;

public class EventEmitter {
    private final EventManager eventManager;
    public final EventEmitterLevelObject level;

    public EventEmitter(EventManager eventManager, String type, String id) {
        this.eventManager = eventManager;
        this.level = new EventEmitterLevelObject(type, id);
    }

    public void emit(String eventName, Object data) {
        this.eventManager.dispatchEvent(event);
    }
}
