package com.hotel.model;

/**
 * Room types with their base prices and capacities.
 * Based on business rules:
 * - Single: up to 2 people
 * - Double: up to 4 people
 * - Deluxe: up to 2 people (higher price)
 * - Penthouse: up to 2 people (highest price)
 */
public enum RoomType {
    SINGLE("Single Room", 100.00, 2),
    DOUBLE("Double Room", 150.00, 4),
    DELUXE("Deluxe Room", 200.00, 2),
    PENTHOUSE("Penthouse Suite", 350.00, 2);

    private final String displayName;
    private final double basePrice;
    private final int maxOccupancy;

    RoomType(String displayName, double basePrice, int maxOccupancy) {
        this.displayName = displayName;
        this.basePrice = basePrice;
        this.maxOccupancy = maxOccupancy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }
}
