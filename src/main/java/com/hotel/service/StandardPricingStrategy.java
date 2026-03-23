package com.hotel.service;

import com.hotel.model.Room;
import java.time.LocalDate;

/**
 * Standard pricing strategy - applies base price with no multiplier.
 * Used for weekday pricing.
 */
public class StandardPricingStrategy implements PricingStrategy {

    private static final double MULTIPLIER = 1.0;

    @Override
    public double calculatePrice(Room room, LocalDate date) {
        return room.getBasePrice() * MULTIPLIER;
    }

    @Override
    public String getStrategyName() {
        return "Standard (Weekday)";
    }

    @Override
    public double getMultiplier() {
        return MULTIPLIER;
    }
}
