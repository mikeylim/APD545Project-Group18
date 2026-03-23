package com.hotel.service;

import com.hotel.model.Room;
import java.time.LocalDate;

/**
 * Weekend pricing strategy - applies higher rate for weekends.
 * Typically Friday and Saturday nights have higher demand.
 */
public class WeekendPricingStrategy implements PricingStrategy {

    private static final double WEEKEND_MULTIPLIER = 1.25; // 25% increase

    @Override
    public double calculatePrice(Room room, LocalDate date) {
        return room.getBasePrice() * WEEKEND_MULTIPLIER;
    }

    @Override
    public String getStrategyName() {
        return "Weekend";
    }

    @Override
    public double getMultiplier() {
        return WEEKEND_MULTIPLIER;
    }
}
