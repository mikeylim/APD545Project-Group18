package com.hotel.events;

/**
 * Observer for room availability updates.
 */
public interface RoomAvailabilityObserver {
    void onRoomAvailable(RoomAvailabilityEvent event);
}
