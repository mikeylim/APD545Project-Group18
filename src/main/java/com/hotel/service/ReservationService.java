package com.hotel.service;

import com.hotel.model.*;
import com.hotel.repository.AddonRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing reservations.
 * Handles the complete kiosk booking flow.
 */
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final AddonRepository addonRepository;
    private final PricingService pricingService;

    public ReservationService() {
        this.reservationRepository = new ReservationRepository();
        this.guestRepository = new GuestRepository();
        this.roomRepository = new RoomRepository();
        this.addonRepository = new AddonRepository();
        this.pricingService = new PricingService();
    }

    /**
     * Create a new reservation (kiosk flow).
     */
    public Reservation createReservation(Guest guest, int adults, int children,
                                          LocalDate checkIn, LocalDate checkOut,
                                          List<Room> rooms, List<Addon> addons) {

        // Save or update guest
        Guest savedGuest;
        Guest existingGuest = guestRepository.findByEmail(guest.getEmail());
        if (existingGuest != null) {
            existingGuest.setFirstName(guest.getFirstName());
            existingGuest.setLastName(guest.getLastName());
            existingGuest.setPhone(guest.getPhone());
            savedGuest = guestRepository.update(existingGuest);
        } else {
            savedGuest = guestRepository.save(guest);
        }

        // Create reservation
        Reservation reservation = new Reservation(savedGuest, adults, children, checkIn, checkOut);

        // Add rooms
        for (Room room : rooms) {
            reservation.addRoom(room);
        }

        // Add addons
        for (Addon addon : addons) {
            reservation.addAddon(addon);
        }

        // Calculate pricing
        pricingService.calculateReservationPricing(reservation);

        // Set status to confirmed
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // Save reservation
        return reservationRepository.save(reservation);
    }

    /**
     * Get available rooms for date range.
     */
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableForDateRange(checkIn, checkOut);
    }

    /**
     * Get available rooms by type for date range.
     */
    public List<Room> getAvailableRoomsByType(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableByTypeForDateRange(type, checkIn, checkOut);
    }

    /**
     * Get all available addons.
     */
    public List<Addon> getAllAddons() {
        return addonRepository.findAll();
    }

    /**
     * Find reservation by confirmation number.
     */
    public Reservation findByConfirmationNumber(String confirmationNumber) {
        return reservationRepository.findByConfirmationNumber(confirmationNumber);
    }

    /**
     * Validate occupancy for selected rooms.
     */
    public boolean validateOccupancy(List<Room> rooms, int totalGuests) {
        int totalCapacity = rooms.stream()
            .mapToInt(Room::getMaxOccupancy)
            .sum();
        return totalGuests <= totalCapacity;
    }

    /**
     * Suggest rooms based on guest count.
     */
    public String suggestRooms(int adults, int children) {
        int totalGuests = adults + children;

        if (totalGuests <= 2) {
            return "1 Single or 1 Deluxe room";
        } else if (totalGuests <= 4) {
            return "1 Double room or 2 Single rooms";
        } else if (totalGuests <= 6) {
            return "1 Double + 1 Single room or 3 Single rooms";
        } else {
            int doubleRooms = totalGuests / 4;
            int remaining = totalGuests % 4;
            int singleRooms = (remaining > 0) ? (remaining + 1) / 2 : 0;
            return String.format("%d Double room(s) + %d Single room(s)", doubleRooms, singleRooms);
        }
    }

    /**
     * Calculate estimated price.
     */
    public double calculateEstimate(List<Room> rooms, List<Addon> addons,
                                    LocalDate checkIn, LocalDate checkOut) {
        int nights = (int) (checkOut.toEpochDay() - checkIn.toEpochDay());

        double roomCost = pricingService.calculateTotalRoomCost(rooms, checkIn, checkOut);
        double addonCost = pricingService.calculateAddonCost(addons, nights);
        double subtotal = roomCost + addonCost;
        double tax = pricingService.calculateTax(subtotal);

        return subtotal + tax;
    }

    /**
     * Get all reservations.
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Get reservations by status.
     */
    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    /**
     * Update reservation status.
     */
    public Reservation updateStatus(Reservation reservation, ReservationStatus newStatus) {
        reservation.setStatus(newStatus);
        return reservationRepository.update(reservation);
    }

    /**
     * Cancel reservation.
     */
    public Reservation cancelReservation(Reservation reservation) {
        return updateStatus(reservation, ReservationStatus.CANCELLED);
    }

    /**
     * Check in guest.
     */
    public Reservation checkIn(Reservation reservation) {
        return updateStatus(reservation, ReservationStatus.CHECKED_IN);
    }

    /**
     * Check out guest.
     */
    public Reservation checkOut(Reservation reservation) {
        return updateStatus(reservation, ReservationStatus.CHECKED_OUT);
    }
}
