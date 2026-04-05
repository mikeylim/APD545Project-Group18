package com.hotel.repository;

import com.hotel.model.Payment;
import com.hotel.model.PaymentMethod;
import com.hotel.model.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Payment entity operations.
 * Handles payment queries and transactions.
 */
public class PaymentRepository extends GenericRepository<Payment, Long> {

    public PaymentRepository() {
        super(Payment.class);
    }

    /**
     * Find all payments for a reservation.
     */
    public List<Payment> findByReservation(Reservation reservation) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.reservation = :reservation ORDER BY p.paymentDate DESC",
                Payment.class);
            query.setParameter("reservation", reservation);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find all payments for a reservation by ID.
     */
    public List<Payment> findByReservationId(Long reservationId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.reservation.id = :reservationId ORDER BY p.paymentDate DESC",
                Payment.class);
            query.setParameter("reservationId", reservationId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find payments by payment method.
     */
    public List<Payment> findByPaymentMethod(PaymentMethod method) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.paymentMethod = :method ORDER BY p.paymentDate DESC",
                Payment.class);
            query.setParameter("method", method);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find payments within a date range.
     */
    public List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC",
                Payment.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find all refunds (negative amounts).
     */
    public List<Payment> findRefunds() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.amount < 0 ORDER BY p.paymentDate DESC",
                Payment.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Calculate total payments for a reservation.
     */
    public double getTotalPaymentsForReservation(Long reservationId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Double> query = em.createQuery(
                "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.reservation.id = :reservationId",
                Double.class);
            query.setParameter("reservationId", reservationId);
            Double result = query.getSingleResult();
            return result != null ? result : 0.0;
        } finally {
            em.close();
        }
    }

    /**
     * Find payments processed by a specific admin.
     */
    public List<Payment> findByProcessedBy(String username) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.processedBy = :username ORDER BY p.paymentDate DESC",
                Payment.class);
            query.setParameter("username", username);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get total revenue within a date range.
     */
    public double getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Double> query = em.createQuery(
                "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate",
                Double.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            Double result = query.getSingleResult();
            return result != null ? result : 0.0;
        } finally {
            em.close();
        }
    }
}
