package github.eliotweber.webrtc_server;

public abstract class EventObserver {

    public String _id;

    public EventLevel level;

    public EventType[] eventTypes;

    public boolean testEvent(Event event) {
        return true;
    }

    public abstract void onEvent(Event event);
}