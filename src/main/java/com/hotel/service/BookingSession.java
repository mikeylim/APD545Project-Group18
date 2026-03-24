package com.hotel.service;

import com.hotel.model.Addon;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.RoomType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton session to hold booking data across kiosk screens.
 * Stores the current booking state as the guest progresses through the flow.
 */
public class BookingSession {

    private static BookingSession instance;

    // Guest count
    private int adults = 1;
    private int children = 0;

    // Dates
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    // Room selection
    private List<Room> selectedRooms = new ArrayList<>();
    private RoomType selectedRoomType = RoomType.DOUBLE; // Default suggestion

    // Custom room counts (for "Choose My Own Rooms")
    private int singleRoomCount = 0;
    private int doubleRoomCount = 0;
    private int deluxeRoomCount = 0;
    private int penthouseRoomCount = 0;

    // Addons
    private List<Addon> selectedAddons = new ArrayList<>();

    // Guest details
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean joinLoyalty = false;

    // Completed reservation (after confirmation)
    private Reservation completedReservation;

    // Private constructor for singleton
    private BookingSession() {}

    /**
     * Get the singleton instance.
     */
    public static synchronized BookingSession getInstance() {
        if (instance == null) {
            instance = new BookingSession();
        }
        return instance;
    }

    /**
     * Reset session for a new booking.
     */
    public void reset() {
        adults = 1;
        children = 0;
        checkInDate = null;
        checkOutDate = null;
        selectedRooms.clear();
        selectedRoomType = RoomType.DOUBLE;
        singleRoomCount = 0;
        doubleRoomCount = 0;
        deluxeRoomCount = 0;
        penthouseRoomCount = 0;
        selectedAddons.clear();
        firstName = null;
        lastName = null;
        email = null;
        phone = null;
        joinLoyalty = false;
        completedReservation = null;
    }

    /**
     * Calculate number of nights.
     */
    public int getNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }
        return (int) (checkOutDate.toEpochDay() - checkInDate.toEpochDay());
    }

    /**
     * Get total guest count.
     */
    public int getTotalGuests() {
        return adults + children;
    }

    /**
     * Create a Guest object from session data.
     */
    public Guest createGuest() {
        return new Guest(firstName, lastName, email, phone);
    }

    // Getters and Setters
    public int getAdults() {
        return adults;
    }

    public void setAdults(int adults) {
        this.adults = adults;
    }

    public int getChildren() {
        return children;
    }

    public void setChildren(int children) {
        this.children = children;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public List<Room> getSelectedRooms() {
        return selectedRooms;
    }

    public void setSelectedRooms(List<Room> selectedRooms) {
        this.selectedRooms = selectedRooms;
    }

    public void addRoom(Room room) {
        this.selectedRooms.add(room);
    }

    public void clearRooms() {
        this.selectedRooms.clear();
    }

    public RoomType getSelectedRoomType() {
        return selectedRoomType;
    }

    public void setSelectedRoomType(RoomType selectedRoomType) {
        this.selectedRoomType = selectedRoomType;
    }

    public List<Addon> getSelectedAddons() {
        return selectedAddons;
    }

    public void setSelectedAddons(List<Addon> selectedAddons) {
        this.selectedAddons = selectedAddons;
    }

    public void addAddon(Addon addon) {
        if (!selectedAddons.contains(addon)) {
            this.selectedAddons.add(addon);
        }
    }

    public void removeAddon(Addon addon) {
        this.selectedAddons.remove(addon);
    }

    public void clearAddons() {
        this.selectedAddons.clear();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isJoinLoyalty() {
        return joinLoyalty;
    }

    public void setJoinLoyalty(boolean joinLoyalty) {
        this.joinLoyalty = joinLoyalty;
    }

    public Reservation getCompletedReservation() {
        return completedReservation;
    }

    public void setCompletedReservation(Reservation completedReservation) {
        this.completedReservation = completedReservation;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Custom room count methods
    public void setCustomRoomCounts(int single, int doubleRoom, int deluxe, int penthouse) {
        this.singleRoomCount = single;
        this.doubleRoomCount = doubleRoom;
        this.deluxeRoomCount = deluxe;
        this.penthouseRoomCount = penthouse;
    }

    public int getSingleRoomCount() {
        return singleRoomCount;
    }

    public int getDoubleRoomCount() {
        return doubleRoomCount;
    }

    public int getDeluxeRoomCount() {
        return deluxeRoomCount;
    }

    public int getPenthouseRoomCount() {
        return penthouseRoomCount;
    }

    public int getTotalRoomCount() {
        return singleRoomCount + doubleRoomCount + deluxeRoomCount + penthouseRoomCount;
    }

    public boolean hasCustomRooms() {
        return getTotalRoomCount() > 0;
    }

    /**
     * Calculate total room cost based on room selection.
     */
    public double calculateRoomCost() {
        int nights = getNights();
        if (nights == 0) nights = 1;

        if (hasCustomRooms()) {
            return (singleRoomCount * 100.0 * nights) +
                   (doubleRoomCount * 150.0 * nights) +
                   (deluxeRoomCount * 200.0 * nights) +
                   (penthouseRoomCount * 350.0 * nights);
        } else {
            return selectedRoomType.getBasePrice() * nights;
        }
    }
}
