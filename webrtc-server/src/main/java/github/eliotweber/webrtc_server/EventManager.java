package github.eliotweber.webrtc_server;

import java.util.*;

public class EventManager {
    private EventTreeNode rootNode;
    private List<String> eventTypes = new ArrayList<>(Arrays.asList(

    ));

    public String addEventListener(EventObserver observer) {
        for (String type : observer.setEventTypes()) {
            
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

class EventTreeNode {
    public String name;
    private List<EventObserver> observers = new ArrayList<>();
    private Map<String, EventTreeNode> children = new HashMap<>();

    public EventTreeNode(String name) {
        this.name = name;
    }

    public void addEmitter(EventObserver observer, List<String> path) {
        if (path.size() == 0) {
            this.observers.add(observer);
            return;
        }

        String childName = path.get(0);
        EventTreeNode child = children.get(childName);

        if (child == null) {
            child = new EventTreeNode(childName);
            children.put(childName, child);
        }

        child.addEmitter(observer, path.subList(1, path.size()));
    }

    public List<EventObserver> getAllListeners() {
        List<EventObserver> allObservers = new ArrayList<EventObserver>(this.observers);

        for (EventTreeNode child : children.values()) {
            allObservers.addAll(child.getAllListeners());
        }

        return allObservers;
    }
}