package github.eliotweber.webrtc_server;

import java.util.*;

public class EventManager {
    private EventTreeNode rootNode;

    private Map<String, EventLevel> levels;

    public String addEventListener(EventObserver observer) {
        for (EventType type : observer.eventTypes) {
            EventLevel level = new EventLevel(observer.level, type);
            List<String> path = Arrays.asList(level.getArray());
            rootNode.addListener(observer, path);
        }

        observer._id = UUID.randomUUID().toString();

        return observer._id;
    }

    public void removeEventListener(String observerId) {
        rootNode.removeEventListener(observerId);
    }

    public void _dispatchEvent(Event event, EventLevel level) {
        
        List<EventObserver> observers = rootNode.gotoChild(Arrays.asList(level.getArray())).observers;

        for (EventObserver observer : observers) {
            if (observer.testEvent(event)) observer.onEvent(event);
        }
        
    }

    public void addLevel(String name, EventLevel level) {
        levels.put(name, level);
    }

    public EventLevel getLevel(String name) {
        if (!levels.containsKey(name)) {
            throw new RuntimeException("Level not found: " + name);
        }
        return levels.get(name);
    }
}

class EventTreeNode {
    public String name;
    public final List<EventObserver> observers = new ArrayList<>();
    private final Map<String, EventTreeNode> children = new HashMap<>();

    public EventTreeNode(String name) {
        this.name = name;
    }

    public void addListener(EventObserver observer, List<String> path) {
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

        child.addListener(observer, path.subList(1, path.size()));
    }

    public void removeEventListener(String observerId) {
        this.observers.removeIf(observer -> observer._id.equals(observerId));

        for (EventTreeNode child : children.values()) {
            child.removeEventListener(observerId);
        }
    }

    public List<EventObserver> getAllListeners() {
        List<EventObserver> allObservers = new ArrayList<EventObserver>(this.observers);

        for (EventTreeNode child : children.values()) {
            allObservers.addAll(child.getAllListeners());
        }

        return allObservers;
    }

    public EventTreeNode gotoChild(List<String> path) {
        if (path.size() == 0) {
            return this;
        }

        String childName = path.get(0);
        EventTreeNode child = children.get(childName);

        if (child == null) {
            return null;
        }

        return child.gotoChild(path.subList(1, path.size()));
    }
}