package com.hotel.service;

import com.hotel.model.Addon;
import com.hotel.model.PricingModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Decorator pattern for dynamically adding services to booking pricing.
 * Base component and decorators for add-on services.
 */
public abstract class BookingDecorator {

    // ========== Component Interface ==========

    /**
     * Component interface for booking pricing.
     */
    public interface BookingComponent {
        double getPrice();
        String getDescription();
        int getNights();
    }

    // ========== Concrete Component ==========

    /**
     * Base booking (room only).
     */
    public static class BaseBooking implements BookingComponent {
        private final double roomPrice;
        private final String roomDescription;
        private final int nights;

        public BaseBooking(double roomPrice, String roomDescription, int nights) {
            this.roomPrice = roomPrice;
            this.roomDescription = roomDescription;
            this.nights = nights;
        }

        @Override
        public double getPrice() {
            return roomPrice;
        }

        @Override
        public String getDescription() {
            return roomDescription;
        }

        @Override
        public int getNights() {
            return nights;
        }
    }

    // ========== Abstract Decorator ==========

    /**
     * Abstract decorator for add-on services.
     */
    public static abstract class ServiceDecorator implements BookingComponent {
        protected final BookingComponent wrappedBooking;

        public ServiceDecorator(BookingComponent booking) {
            this.wrappedBooking = booking;
        }

        @Override
        public int getNights() {
            return wrappedBooking.getNights();
        }
    }

    // ========== Concrete Decorators ==========

    /**
     * Wi-Fi add-on decorator.
     */
    public static class WifiDecorator extends ServiceDecorator {
        private static final double PRICE_PER_NIGHT = 10.0;

        public WifiDecorator(BookingComponent booking) {
            super(booking);
        }

        @Override
        public double getPrice() {
            return wrappedBooking.getPrice() + (PRICE_PER_NIGHT * getNights());
        }

        @Override
        public String getDescription() {
            return wrappedBooking.getDescription() + " + Premium Wi-Fi";
        }
    }

    /**
     * Breakfast add-on decorator.
     */
    public static class BreakfastDecorator extends ServiceDecorator {
        private static final double PRICE_PER_NIGHT = 25.0;

        public BreakfastDecorator(BookingComponent booking) {
            super(booking);
        }

        @Override
        public double getPrice() {
            return wrappedBooking.getPrice() + (PRICE_PER_NIGHT * getNights());
        }

        @Override
        public String getDescription() {
            return wrappedBooking.getDescription() + " + Breakfast Buffet";
        }
    }

    /**
     * Parking add-on decorator.
     */
    public static class ParkingDecorator extends ServiceDecorator {
        private static final double PRICE_PER_NIGHT = 15.0;

        public ParkingDecorator(BookingComponent booking) {
            super(booking);
        }

        @Override
        public double getPrice() {
            return wrappedBooking.getPrice() + (PRICE_PER_NIGHT * getNights());
        }

        @Override
        public String getDescription() {
            return wrappedBooking.getDescription() + " + Parking";
        }
    }

    /**
     * Spa package add-on decorator (per reservation).
     */
    public static class SpaDecorator extends ServiceDecorator {
        private static final double PRICE_PER_RESERVATION = 150.0;

        public SpaDecorator(BookingComponent booking) {
            super(booking);
        }

        @Override
        public double getPrice() {
            return wrappedBooking.getPrice() + PRICE_PER_RESERVATION;
        }

        @Override
        public String getDescription() {
            return wrappedBooking.getDescription() + " + Spa Package";
        }
    }

    /**
     * Airport shuttle add-on decorator (per reservation).
     */
    public static class AirportShuttleDecorator extends ServiceDecorator {
        private static final double PRICE_PER_RESERVATION = 50.0;

        public AirportShuttleDecorator(BookingComponent booking) {
            super(booking);
        }

        @Override
        public double getPrice() {
            return wrappedBooking.getPrice() + PRICE_PER_RESERVATION;
        }

        @Override
        public String getDescription() {
            return wrappedBooking.getDescription() + " + Airport Shuttle";
        }
    }

    /**
     * Late checkout add-on decorator (per reservation).
     */
    public static class LateCheckoutDecorator extends ServiceDecorator {
        private static final double PRICE_PER_RESERVATION = 30.0;

        public LateCheckoutDecorator(BookingComponent booking) {
            super(booking);
        }

        @Override
        public double getPrice() {
            return wrappedBooking.getPrice() + PRICE_PER_RESERVATION;
        }

        @Override
        public String getDescription() {
            return wrappedBooking.getDescription() + " + Late Checkout (2pm)";
        }
    }

    /**
     * Generic add-on decorator that works with any Addon entity.
     */
    public static class GenericAddonDecorator extends ServiceDecorator {
        private final Addon addon;

        public GenericAddonDecorator(BookingComponent booking, Addon addon) {
            super(booking);
            this.addon = addon;
        }

        @Override
        public double getPrice() {
            double addonCost;
            if (addon.getPricingModel() == PricingModel.PER_NIGHT) {
                addonCost = addon.getPrice() * getNights();
            } else {
                addonCost = addon.getPrice();
            }
            return wrappedBooking.getPrice() + addonCost;
        }

        @Override
        public String getDescription() {
            return wrappedBooking.getDescription() + " + " + addon.getName();
        }
    }

    // ========== Builder for easy decoration ==========

    /**
     * Builder class for creating decorated bookings.
     */
    public static class BookingBuilder {
        private BookingComponent booking;

        public BookingBuilder(double roomPrice, String roomDescription, int nights) {
            this.booking = new BaseBooking(roomPrice, roomDescription, nights);
        }

        public BookingBuilder addWifi() {
            booking = new WifiDecorator(booking);
            return this;
        }

        public BookingBuilder addBreakfast() {
            booking = new BreakfastDecorator(booking);
            return this;
        }

        public BookingBuilder addParking() {
            booking = new ParkingDecorator(booking);
            return this;
        }

        public BookingBuilder addSpa() {
            booking = new SpaDecorator(booking);
            return this;
        }

        public BookingBuilder addAirportShuttle() {
            booking = new AirportShuttleDecorator(booking);
            return this;
        }

        public BookingBuilder addLateCheckout() {
            booking = new LateCheckoutDecorator(booking);
            return this;
        }

        public BookingBuilder addAddon(Addon addon) {
            booking = new GenericAddonDecorator(booking, addon);
            return this;
        }

        public BookingBuilder addAddons(List<Addon> addons) {
            for (Addon addon : addons) {
                booking = new GenericAddonDecorator(booking, addon);
            }
            return this;
        }

        public BookingComponent build() {
            return booking;
        }

        public double getTotalPrice() {
            return booking.getPrice();
        }

        public String getFullDescription() {
            return booking.getDescription();
        }
    }

    // ========== Utility Methods ==========

    /**
     * Create a booking with selected add-ons using decorator pattern.
     */
    public static BookingComponent createBookingWithAddons(
            double roomPrice, String roomDescription, int nights, List<Addon> addons) {

        BookingComponent booking = new BaseBooking(roomPrice, roomDescription, nights);

        for (Addon addon : addons) {
            booking = new GenericAddonDecorator(booking, addon);
        }

        return booking;
    }

    /**
     * Calculate add-on total separately.
     */
    public static double calculateAddonTotal(List<Addon> addons, int nights) {
        double total = 0;
        for (Addon addon : addons) {
            if (addon.getPricingModel() == PricingModel.PER_NIGHT) {
                total += addon.getPrice() * nights;
            } else {
                total += addon.getPrice();
            }
        }
        return total;
    }
}
