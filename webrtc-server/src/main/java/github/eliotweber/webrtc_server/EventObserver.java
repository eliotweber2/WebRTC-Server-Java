package github.eliotweber.webrtc_server;

import java.util.List;

public abstract class EventObserver {

    public String _id;

    public abstract List<String> setEventTypes();

    public boolean testEvent(Event event) {
        return true;
    }

    public abstract void onEvent(Event event);
}