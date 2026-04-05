package com.hotel.service;

import com.hotel.model.*;
import com.hotel.repository.WaitlistRepository;
import com.hotel.repository.RoomRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for waitlist management.
 * Uses Observer pattern to notify subscribers when rooms become available.
 */
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final RoomRepository roomRepository;
    private final AuditService auditService;
    private final List<RoomAvailabilityObserver> observers;

    public WaitlistService() {
        this.waitlistRepository = new WaitlistRepository();
        this.roomRepository = new RoomRepository();
        this.auditService = AuditService.getInstance();
        this.observers = new ArrayList<>();
    }

    /**
     * Add a guest to the waitlist.
     */
    public Waitlist addToWaitlist(Guest guest, RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        Waitlist entry = new Waitlist(guest, roomType, checkIn, checkOut);
        Waitlist saved = waitlistRepository.save(entry);

        auditService.log("SYSTEM", "WAITLIST_ADD", "Waitlist", saved.getId().toString(),
            String.format("Added %s to waitlist for %s room (%s to %s)",
                guest.getFullName(), roomType, checkIn, checkOut));

        return saved;
    }

    /**
     * Get all active waitlist entries.
     */
    public List<Waitlist> getActiveEntries() {
        return waitlistRepository.findActiveEntries();
    }

    /**
     * Get waitlist entries by room type.
     */
    public List<Waitlist> getEntriesByRoomType(RoomType roomType) {
        return waitlistRepository.findByRoomType(roomType);
    }

    /**
     * Get waitlist entries by guest.
     */
    public List<Waitlist> getEntriesByGuest(Guest guest) {
        return waitlistRepository.findByGuest(guest);
    }

    /**
     * Check for available rooms and notify waitlist entries.
     * Called when a room becomes available (after checkout/cancellation).
     */
    public List<Waitlist> checkAndNotify(RoomType roomType, LocalDate availableFrom, LocalDate availableTo) {
        // Find matching waitlist entries
        List<Waitlist> matchingEntries = waitlistRepository.findMatchingEntries(roomType, availableFrom, availableTo);
        List<Waitlist> notifiedEntries = new ArrayList<>();

        for (Waitlist entry : matchingEntries) {
            // Check if rooms are actually available for their dates
            List<Room> availableRooms = roomRepository.findAvailableByTypeForDateRange(
                roomType, entry.getDesiredCheckIn(), entry.getDesiredCheckOut());

            if (!availableRooms.isEmpty()) {
                entry.setNotified(true);
                waitlistRepository.update(entry);
                notifiedEntries.add(entry);

                // Notify all observers
                notifyObservers(entry, availableRooms.get(0));

                auditService.log("SYSTEM", "WAITLIST_NOTIFY", "Waitlist", entry.getId().toString(),
                    String.format("Notified %s that %s room is available",
                        entry.getGuest().getFullName(), roomType));
            }
        }

        return notifiedEntries;
    }

    /**
     * Convert a waitlist entry to a reservation.
     */
    public void convertToReservation(Waitlist entry) {
        entry.setConverted(true);
        waitlistRepository.update(entry);

        auditService.log("SYSTEM", "WAITLIST_CONVERT", "Waitlist", entry.getId().toString(),
            String.format("Converted waitlist entry to reservation for %s", entry.getGuest().getFullName()));
    }

    /**
     * Remove a waitlist entry.
     */
    public void removeEntry(Waitlist entry, String reason) {
        waitlistRepository.delete(entry);

        auditService.log("SYSTEM", "WAITLIST_REMOVE", "Waitlist", entry.getId().toString(),
            String.format("Removed waitlist entry for %s. Reason: %s", entry.getGuest().getFullName(), reason));
    }

    /**
     * Get count of active waitlist entries.
     */
    public long getActiveCount() {
        return waitlistRepository.countActive();
    }

    // ========== Observer Pattern Implementation ==========

    /**
     * Register an observer to be notified when rooms become available.
     */
    public void addObserver(RoomAvailabilityObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Remove an observer.
     */
    public void removeObserver(RoomAvailabilityObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notify all observers of room availability.
     */
    private void notifyObservers(Waitlist entry, Room availableRoom) {
        for (RoomAvailabilityObserver observer : observers) {
            observer.onRoomAvailable(entry, availableRoom);
        }
    }

    /**
     * Observer interface for room availability notifications.
     */
    public interface RoomAvailabilityObserver {
        void onRoomAvailable(Waitlist waitlistEntry, Room availableRoom);
    }

    /**
     * Console notification observer (for admin alerts).
     */
    public static class ConsoleNotificationObserver implements RoomAvailabilityObserver {
        @Override
        public void onRoomAvailable(Waitlist entry, Room room) {
            System.out.println("=== WAITLIST NOTIFICATION ===");
            System.out.println("Room Available: " + room.getRoomNumber() + " (" + room.getType() + ")");
            System.out.println("Guest: " + entry.getGuest().getFullName());
            System.out.println("Email: " + entry.getGuest().getEmail());
            System.out.println("Phone: " + entry.getGuest().getPhone());
            System.out.println("Desired Dates: " + entry.getDesiredCheckIn() + " to " + entry.getDesiredCheckOut());
            System.out.println("=============================");
        }
    }

    /**
     * Admin dashboard notification observer.
     * Stores notifications for display in admin UI.
     */
    public static class AdminDashboardObserver implements RoomAvailabilityObserver {
        private final List<WaitlistNotification> notifications = new ArrayList<>();

        @Override
        public void onRoomAvailable(Waitlist entry, Room room) {
            notifications.add(new WaitlistNotification(entry, room));
        }

        public List<WaitlistNotification> getNotifications() {
            return new ArrayList<>(notifications);
        }

        public void clearNotifications() {
            notifications.clear();
        }

        public int getNotificationCount() {
            return notifications.size();
        }
    }

    /**
     * Notification data class.
     */
    public static class WaitlistNotification {
        private final Waitlist waitlistEntry;
        private final Room availableRoom;
        private final java.time.LocalDateTime notifiedAt;

        public WaitlistNotification(Waitlist entry, Room room) {
            this.waitlistEntry = entry;
            this.availableRoom = room;
            this.notifiedAt = java.time.LocalDateTime.now();
        }

        public Waitlist getWaitlistEntry() { return waitlistEntry; }
        public Room getAvailableRoom() { return availableRoom; }
        public java.time.LocalDateTime getNotifiedAt() { return notifiedAt; }
    }
}
