package com.hotel.service;

import com.hotel.model.*;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.GuestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for payment processing.
 * Handles payments, refunds, deposits, and balance tracking.
 */
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final LoyaltyService loyaltyService;
    private final AuditService auditService;

    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
        this.reservationRepository = new ReservationRepository();
        this.guestRepository = new GuestRepository();
        this.loyaltyService = LoyaltyService.getInstance();
        this.auditService = AuditService.getInstance();
    }

    /**
     * Process a payment for a reservation.
     */
    public Payment processPayment(Reservation reservation, PaymentMethod method, double amount, String processedBy) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        double balance = reservation.getBalance();
        if (amount > balance) {
            throw new IllegalArgumentException("Payment amount ($" + amount + ") exceeds balance ($" + balance + ")");
        }

        // Handle loyalty points payment
        if (method == PaymentMethod.LOYALTY_POINTS) {
            Guest guest = reservation.getGuest();
            int pointsRequired = loyaltyService.calculatePointsForAmount(amount);

            if (guest.getLoyaltyPoints() < pointsRequired) {
                throw new IllegalArgumentException("Insufficient loyalty points. Required: " + pointsRequired +
                    ", Available: " + guest.getLoyaltyPoints());
            }

            // Deduct loyalty points
            loyaltyService.redeemPoints(guest, pointsRequired, "Payment for reservation " + reservation.getConfirmationNumber());
        }

        // Create payment record
        Payment payment = new Payment(reservation, method, amount);
        payment.setProcessedBy(processedBy);
        payment.setTransactionReference(generateTransactionReference());

        Payment savedPayment = paymentRepository.save(payment);

        // Update reservation amount paid
        reservation.setAmountPaid(reservation.getAmountPaid() + amount);
        reservationRepository.update(reservation);

        // Earn loyalty points for cash/card payments
        if (method != PaymentMethod.LOYALTY_POINTS && reservation.getGuest().getLoyaltyNumber() != null) {
            loyaltyService.earnPoints(reservation.getGuest(), amount,
                "Payment for reservation " + reservation.getConfirmationNumber());
        }

        auditService.log(processedBy, "PAYMENT", "Payment", savedPayment.getId().toString(),
            String.format("Processed %s payment of $%.2f for reservation %s",
                method, amount, reservation.getConfirmationNumber()));

        return savedPayment;
    }

    /**
     * Process a deposit at booking time.
     */
    public Payment processDeposit(Reservation reservation, PaymentMethod method, double amount, String processedBy) {
        auditService.log(processedBy, "DEPOSIT", "Payment", null,
            String.format("Processing deposit of $%.2f for reservation %s",
                amount, reservation.getConfirmationNumber()));

        return processPayment(reservation, method, amount, processedBy);
    }

    /**
     * Process a refund.
     */
    public Payment processRefund(Reservation reservation, PaymentMethod method, double amount, String reason, String processedBy) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }

        if (amount > reservation.getAmountPaid()) {
            throw new IllegalArgumentException("Refund amount ($" + amount + ") exceeds amount paid ($" +
                reservation.getAmountPaid() + ")");
        }

        // Create refund as negative payment
        Payment refund = new Payment(reservation, method, -amount);
        refund.setProcessedBy(processedBy);
        refund.setTransactionReference("REF-" + generateTransactionReference());

        Payment savedRefund = paymentRepository.save(refund);

        // Update reservation amount paid
        reservation.setAmountPaid(reservation.getAmountPaid() - amount);
        reservationRepository.update(reservation);

        auditService.log(processedBy, "REFUND", "Payment", savedRefund.getId().toString(),
            String.format("Processed %s refund of $%.2f for reservation %s. Reason: %s",
                method, amount, reservation.getConfirmationNumber(), reason));

        return savedRefund;
    }

    /**
     * Process a refund (legacy method for backward compatibility).
     */
    public Payment processRefund(Reservation reservation, double amount, String reason, String processedBy) {
        return processRefund(reservation, PaymentMethod.CASH, amount, reason, processedBy);
    }

    /**
     * Pay full balance for a reservation.
     */
    public Payment payFullBalance(Reservation reservation, PaymentMethod method, String processedBy) {
        double balance = reservation.getBalance();
        if (balance <= 0) {
            throw new IllegalArgumentException("Reservation is already fully paid");
        }
        return processPayment(reservation, method, balance, processedBy);
    }

    /**
     * Get payment history for a reservation.
     */
    public List<Payment> getPaymentHistory(Reservation reservation) {
        return paymentRepository.findByReservation(reservation);
    }

    /**
     * Get payment history by reservation ID.
     */
    public List<Payment> getPaymentHistory(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId);
    }

    /**
     * Check if reservation is fully paid.
     */
    public boolean isFullyPaid(Reservation reservation) {
        return reservation.isFullyPaid();
    }

    /**
     * Get outstanding balance.
     */
    public double getBalance(Reservation reservation) {
        return reservation.getBalance();
    }

    /**
     * Get total revenue by date range.
     */
    public double getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.getTotalRevenueByDateRange(startDate, endDate);
    }

    /**
     * Get all payments by date range.
     */
    public List<Payment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Get all refunds.
     */
    public List<Payment> getRefunds() {
        return paymentRepository.findRefunds();
    }

    /**
     * Apply a discount to a reservation.
     * Returns the discount amount applied.
     * Discount can only be applied once per reservation.
     */
    public double applyDiscount(Reservation reservation, double discountPercent, String processedBy, double maxAllowed) {
        // Check if discount was already applied
        if (reservation.hasDiscount()) {
            throw new IllegalArgumentException("A discount of " +
                String.format("%.1f%%", reservation.getDiscountPercent()) +
                " has already been applied to this reservation.");
        }

        if (discountPercent <= 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100 percent");
        }

        if (discountPercent > maxAllowed) {
            throw new IllegalArgumentException("Discount of " + discountPercent + "% exceeds your maximum allowed (" +
                maxAllowed + "%)");
        }

        // Calculate discount based on original subtotal (before any discount)
        double discountAmount = reservation.getSubtotal() * (discountPercent / 100.0);

        // Store discount info
        reservation.setDiscountPercent(discountPercent);
        reservation.setDiscountAmount(discountAmount);

        // Update reservation pricing
        double newSubtotal = reservation.getSubtotal() - discountAmount;
        reservation.setSubtotal(newSubtotal);

        // Recalculate tax and total
        double tax = newSubtotal * 0.13; // 13% HST
        reservation.setTax(tax);
        reservation.setTotal(newSubtotal + tax);

        reservationRepository.update(reservation);

        auditService.log(processedBy, "DISCOUNT", "Reservation", reservation.getId().toString(),
            String.format("Applied %.1f%% discount ($%.2f) to reservation %s",
                discountPercent, discountAmount, reservation.getConfirmationNumber()));

        return discountAmount;
    }

    /**
     * Generate unique transaction reference.
     */
    private String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
