package com.hotel.events;

import com.hotel.model.RoomType;

import java.time.LocalDateTime;

/**
 * Event raised when a room becomes available for potential waitlist conversion.
 */
public record RoomAvailabilityEvent(
    Long roomId,
    String roomNumber,
    RoomType roomType,
    LocalDateTime availableAt,
    int matchingWaitlistCount
) {
}
