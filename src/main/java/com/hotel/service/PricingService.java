package com.hotel.service;

import com.hotel.model.Addon;
import com.hotel.model.Reservation;
import com.hotel.model.Room;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for calculating prices using the Strategy Pattern.
 * Automatically selects the appropriate pricing strategy based on the date.
 */
public class PricingService {

    private static final double TAX_RATE = 0.13; // 13% HST (Ontario)

    private final PricingStrategy standardStrategy;
    private final PricingStrategy weekendStrategy;

    public PricingService() {
        this.standardStrategy = new StandardPricingStrategy();
        this.weekendStrategy = new WeekendPricingStrategy();
    }

    /**
     * Get the appropriate pricing strategy for a given date.
     */
    public PricingStrategy getStrategyForDate(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        // Weekend = Friday and Saturday nights (higher rates)
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
            return weekendStrategy;
        }
        return standardStrategy;
    }

    /**
     * Calculate the price for a single room on a specific date.
     */
    public double calculateRoomPrice(Room room, LocalDate date) {
        PricingStrategy strategy = getStrategyForDate(date);
        return strategy.calculatePrice(room, date);
    }

    /**
     * Calculate the total room cost for a stay.
     */
    public double calculateRoomCost(Room room, LocalDate checkIn, LocalDate checkOut) {
        double total = 0;
        LocalDate current = checkIn;
        while (current.isBefore(checkOut)) {
            total += calculateRoomPrice(room, current);
            current = current.plusDays(1);
        }
        return total;
    }

    /**
     * Calculate the total room cost for multiple rooms.
     */
    public double calculateTotalRoomCost(List<Room> rooms, LocalDate checkIn, LocalDate checkOut) {
        double total = 0;
        for (Room room : rooms) {
            total += calculateRoomCost(room, checkIn, checkOut);
        }
        return total;
    }

    /**
     * Calculate the total addon cost using the Decorator Pattern.
     * Wraps add-ons as decorators to compute the combined price.
     */
    public double calculateAddonCost(List<Addon> addons, int nights) {
        if (addons == null || addons.isEmpty()) {
            return 0;
        }
        // Use Decorator pattern - create a booking component with add-ons
        BookingDecorator.BookingComponent decorated =
            BookingDecorator.createBookingWithAddons(0, "Add-ons", nights, addons);
        return decorated.getPrice();
    }

    /**
     * Calculate tax amount.
     */
    public double calculateTax(double subtotal) {
        return subtotal * TAX_RATE;
    }

    /**
     * Calculate complete reservation pricing.
     * Sets subtotal, tax, and total on the reservation.
     */
    public void calculateReservationPricing(Reservation reservation) {
        LocalDate checkIn = reservation.getCheckInDate();
        LocalDate checkOut = reservation.getCheckOutDate();
        int nights = reservation.getNights();

        // Calculate room costs
        double roomCost = calculateTotalRoomCost(reservation.getRooms(), checkIn, checkOut);

        // Calculate addon costs
        double addonCost = calculateAddonCost(reservation.getAddons(), nights);

        // Calculate subtotal and tax
        double subtotal = roomCost + addonCost;
        double tax = calculateTax(subtotal);
        double total = subtotal + tax;

        // Set values on reservation
        reservation.setSubtotal(subtotal);
        reservation.setTax(tax);
        reservation.setTotal(total);
    }

    /**
     * Get pricing breakdown as formatted string.
     */
    public String getPricingBreakdown(Reservation reservation) {
        StringBuilder sb = new StringBuilder();
        LocalDate checkIn = reservation.getCheckInDate();
        LocalDate checkOut = reservation.getCheckOutDate();
        int nights = reservation.getNights();

        sb.append("=== Pricing Breakdown ===\n\n");

        // Room breakdown
        sb.append("Rooms:\n");
        for (Room room : reservation.getRooms()) {
            double roomTotal = calculateRoomCost(room, checkIn, checkOut);
            sb.append(String.format("  %s (%s): $%.2f for %d nights\n",
                room.getRoomNumber(), room.getType().getDisplayName(), roomTotal, nights));
        }

        // Addon breakdown
        if (!reservation.getAddons().isEmpty()) {
            sb.append("\nAdd-ons:\n");
            for (Addon addon : reservation.getAddons()) {
                double addonTotal = addon.calculateCost(nights);
                sb.append(String.format("  %s: $%.2f\n", addon.getName(), addonTotal));
            }
        }

        sb.append(String.format("\nSubtotal: $%.2f\n", reservation.getSubtotal()));
        sb.append(String.format("Tax (13%%): $%.2f\n", reservation.getTax()));
        sb.append(String.format("Total: $%.2f\n", reservation.getTotal()));

        return sb.toString();
    }
}
