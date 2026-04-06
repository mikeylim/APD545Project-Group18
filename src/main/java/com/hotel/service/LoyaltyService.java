package com.hotel.service;

import com.hotel.model.Guest;
import com.hotel.repository.GuestRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service for loyalty program management.
 * Handles point earning, redemption, and caps.
 * Singleton pattern to persist settings across the application.
 */
public class LoyaltyService {

    private static LoyaltyService instance;

    private final GuestRepository guestRepository;
    private final AuditService auditService;

    // Configurable rates (can be changed via setters)
    private double pointsPerDollar = 5.0;  // Earn 5 points per $1 spent (5% return rate)
    private int pointsPerRedemptionDollar = 100;  // 100 points = $1 redemption
    private int maxPointsPerRedemption = 5000;  // Max 5000 points per transaction ($50)
    private int minPointsToRedeem = 100;  // Minimum points to redeem

    private LoyaltyService() {
        this.guestRepository = new GuestRepository();
        this.auditService = AuditService.getInstance();
    }

    /**
     * Get the singleton instance.
     */
    public static synchronized LoyaltyService getInstance() {
        if (instance == null) {
            instance = new LoyaltyService();
        }
        return instance;
    }

    /**
     * Enroll a guest in the loyalty program.
     */
    public String enrollGuest(Guest guest) {
        if (guest.getLoyaltyNumber() != null) {
            return guest.getLoyaltyNumber();  // Already enrolled
        }

        String loyaltyNumber = generateLoyaltyNumber();
        guest.setLoyaltyNumber(loyaltyNumber);
        guest.setLoyaltyPoints(0);  // Start with 0 points
        guestRepository.update(guest);

        auditService.log("SYSTEM", "LOYALTY_ENROLL", "Guest", guest.getId().toString(),
            "Enrolled guest " + guest.getFullName() + " in loyalty program. Number: " + loyaltyNumber);

        return loyaltyNumber;
    }

    /**
     * Earn points for a payment amount.
     */
    public int earnPoints(Guest guest, double paymentAmount, String reason) {
        if (guest.getLoyaltyNumber() == null) {
            return 0;  // Not enrolled
        }

        int pointsEarned = (int) (paymentAmount * pointsPerDollar);
        guest.setLoyaltyPoints(guest.getLoyaltyPoints() + pointsEarned);
        guestRepository.update(guest);

        auditService.log("SYSTEM", "LOYALTY_EARN", "Guest", guest.getId().toString(),
            String.format("Earned %d points for $%.2f. Reason: %s. Balance: %d",
                pointsEarned, paymentAmount, reason, guest.getLoyaltyPoints()));

        return pointsEarned;
    }

    /**
     * Redeem points for a discount.
     */
    public double redeemPoints(Guest guest, int points, String reason) {
        if (guest.getLoyaltyNumber() == null) {
            throw new IllegalStateException("Guest is not enrolled in loyalty program");
        }

        if (points < minPointsToRedeem) {
            throw new IllegalArgumentException("Minimum " + minPointsToRedeem + " points required to redeem");
        }

        if (points > maxPointsPerRedemption) {
            throw new IllegalArgumentException("Maximum " + maxPointsPerRedemption + " points per redemption");
        }

        if (guest.getLoyaltyPoints() < points) {
            throw new IllegalArgumentException("Insufficient points. Available: " + guest.getLoyaltyPoints());
        }

        double dollarsPerPoint = 1.0 / pointsPerRedemptionDollar;
        double discount = points * dollarsPerPoint;
        guest.setLoyaltyPoints(guest.getLoyaltyPoints() - points);
        guestRepository.update(guest);

        auditService.log("SYSTEM", "LOYALTY_REDEEM", "Guest", guest.getId().toString(),
            String.format("Redeemed %d points for $%.2f discount. Reason: %s. Balance: %d",
                points, discount, reason, guest.getLoyaltyPoints()));

        return discount;
    }

    /**
     * Get guest's current point balance.
     */
    public int getPointBalance(Guest guest) {
        return guest.getLoyaltyPoints();
    }

    /**
     * Calculate how many points are needed for a given amount.
     */
    public int calculatePointsForAmount(double amount) {
        return (int) (amount * pointsPerRedemptionDollar);
    }

    /**
     * Calculate the dollar value of points.
     */
    public double calculatePointValue(int points) {
        return (double) points / pointsPerRedemptionDollar;
    }

    /**
     * Get maximum redeemable points for a reservation.
     */
    public int getMaxRedeemablePoints(Guest guest, double reservationTotal) {
        int guestPoints = guest.getLoyaltyPoints();
        int pointsForTotal = calculatePointsForAmount(reservationTotal);

        // Can't redeem more than maxPointsPerRedemption, guest's balance, or reservation total
        return Math.min(maxPointsPerRedemption, Math.min(guestPoints, pointsForTotal));
    }

    /**
     * Check if guest is enrolled in loyalty program.
     */
    public boolean isEnrolled(Guest guest) {
        return guest.getLoyaltyNumber() != null;
    }

    /**
     * Get all loyalty members.
     */
    public List<Guest> getLoyaltyMembers() {
        return guestRepository.findLoyaltyMembers();
    }

    /**
     * Find guest by loyalty number.
     */
    public Guest findByLoyaltyNumber(String loyaltyNumber) {
        return guestRepository.findByLoyaltyNumber(loyaltyNumber);
    }

    /**
     * Generate unique loyalty number.
     */
    private String generateLoyaltyNumber() {
        return "NN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ========== Getters for settings ==========

    /**
     * Get earning rate info for display.
     */
    public String getEarningRateDisplay() {
        return String.format("Earn: %.0f pts/$1", pointsPerDollar);
    }

    /**
     * Get redemption rate info for display.
     */
    public String getRedemptionRateDisplay() {
        return String.format("Redeem: %d pts = $1", pointsPerRedemptionDollar);
    }

    /**
     * Get points earned per dollar.
     */
    public double getPointsPerDollar() {
        return pointsPerDollar;
    }

    /**
     * Get points needed for $1 redemption.
     */
    public int getPointsPerRedemptionDollar() {
        return pointsPerRedemptionDollar;
    }

    /**
     * Get maximum points per redemption.
     */
    public int getMaxPointsPerRedemption() {
        return maxPointsPerRedemption;
    }

    /**
     * Get minimum points required to redeem.
     */
    public int getMinPointsToRedeem() {
        return minPointsToRedeem;
    }

    // ========== Setters for settings ==========

    /**
     * Set points earned per dollar spent.
     */
    public void setPointsPerDollar(double pointsPerDollar) {
        if (pointsPerDollar <= 0) {
            throw new IllegalArgumentException("Points per dollar must be positive");
        }
        this.pointsPerDollar = pointsPerDollar;
    }

    /**
     * Set points needed for $1 redemption.
     */
    public void setPointsPerRedemptionDollar(int pointsPerRedemptionDollar) {
        if (pointsPerRedemptionDollar <= 0) {
            throw new IllegalArgumentException("Points per redemption dollar must be positive");
        }
        this.pointsPerRedemptionDollar = pointsPerRedemptionDollar;
    }

    /**
     * Set maximum points per redemption.
     */
    public void setMaxPointsPerRedemption(int maxPointsPerRedemption) {
        if (maxPointsPerRedemption <= 0) {
            throw new IllegalArgumentException("Max points per redemption must be positive");
        }
        this.maxPointsPerRedemption = maxPointsPerRedemption;
    }

    /**
     * Set minimum points required to redeem.
     */
    public void setMinPointsToRedeem(int minPointsToRedeem) {
        if (minPointsToRedeem < 0) {
            throw new IllegalArgumentException("Min points to redeem cannot be negative");
        }
        this.minPointsToRedeem = minPointsToRedeem;
    }

    /**
     * Get loyalty summary for a guest.
     */
    public String getLoyaltySummary(Guest guest) {
        if (!isEnrolled(guest)) {
            return "Not enrolled in loyalty program";
        }

        return String.format("Loyalty #: %s | Points: %d | Value: $%.2f",
            guest.getLoyaltyNumber(),
            guest.getLoyaltyPoints(),
            calculatePointValue(guest.getLoyaltyPoints()));
    }
}
