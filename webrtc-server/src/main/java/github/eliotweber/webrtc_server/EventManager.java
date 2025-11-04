package github.eliotweber.webrtc_server;

import java.util.*;

public class EventManager {
    private Map<String, List<EventObserver>> listeners;
    private List<String> eventTypes = new ArrayList<>(Arrays.asList(

    ));

    public String addEventListener(EventObserver observer) {
        for (String type : observer.setEventTypes()) {
            if (!eventTypes.contains(type)) {
                throw new IllegalArgumentException("Invalid event type: " + type);
            }

            listeners.get(type).add(observer);
        }

        return UUID.randomUUID().toString();
    }

    public void registerEventType(String type) {
        if (eventTypes.contains(type)) {
            throw new IllegalArgumentException("Type already registered: " + type);
        }

        eventTypes.add(type);
        listeners.put(type, new ArrayList<>());
    }

    public void removeEventListener(String observerId) {
        for (String type : eventTypes) {
            listeners.get(type).removeIf(observer -> observer._id.equals(observerId));
        }
    }

    public void dispatchEvent(Event event) {
        for (String type : event.types()) {
            for (EventObserver observer : listeners.get(type)) {
                if (observer.testEvent(event)) observer.onEvent(event);
            }
        }
    }
}
