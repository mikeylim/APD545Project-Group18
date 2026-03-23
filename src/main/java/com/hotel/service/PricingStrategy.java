package com.hotel.service;

import com.hotel.model.Room;
import java.time.LocalDate;

/**
 * Strategy Pattern interface for pricing calculations.
 * Different strategies can be applied for standard, weekend, and seasonal pricing.
 */
public interface PricingStrategy {

    /**
     * Calculate the price for a room on a specific date.
     *
     * @param room The room to price
     * @param date The date for pricing
     * @return The price for that date
     */
    double calculatePrice(Room room, LocalDate date);

    /**
     * Get the name of this pricing strategy.
     */
    String getStrategyName();

    /**
     * Get the multiplier applied by this strategy.
     */
    double getMultiplier();
}
