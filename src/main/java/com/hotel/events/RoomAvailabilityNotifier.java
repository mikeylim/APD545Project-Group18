package com.hotel.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple in-memory observer hub for room availability events.
 */
public final class RoomAvailabilityNotifier {
    private static final RoomAvailabilityNotifier INSTANCE = new RoomAvailabilityNotifier();

    private final List<RoomAvailabilityObserver> observers = new ArrayList<>();
    private final List<RoomAvailabilityEvent> recentEvents = new ArrayList<>();

    private RoomAvailabilityNotifier() {
    }

    public static RoomAvailabilityNotifier getInstance() {
        return INSTANCE;
    }

    public synchronized void subscribe(RoomAvailabilityObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public synchronized void unsubscribe(RoomAvailabilityObserver observer) {
        observers.remove(observer);
    }

    public synchronized void publish(RoomAvailabilityEvent event) {
        if (event == null) {
            return;
        }
        recentEvents.add(0, event);
        if (recentEvents.size() > 25) {
            recentEvents.remove(recentEvents.size() - 1);
        }
        for (RoomAvailabilityObserver observer : List.copyOf(observers)) {
            observer.onRoomAvailable(event);
        }
    }

    public synchronized List<RoomAvailabilityEvent> getRecentEvents() {
        return List.copyOf(recentEvents);
    }

    public synchronized void dismiss(RoomAvailabilityEvent event) {
        recentEvents.remove(event);
    }
}
