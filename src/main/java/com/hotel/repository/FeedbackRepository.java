package com.hotel.repository;

import com.hotel.model.Feedback;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Feedback entity operations.
 * Handles feedback queries and statistics.
 */
public class FeedbackRepository extends GenericRepository<Feedback, Long> {

    public FeedbackRepository() {
        super(Feedback.class);
    }

    /**
     * Find feedback by reservation.
     */
    public Optional<Feedback> findByReservation(Reservation reservation) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Feedback> query = em.createQuery(
                "SELECT f FROM Feedback f WHERE f.reservation = :reservation", Feedback.class);
            query.setParameter("reservation", reservation);
            List<Feedback> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Find feedback by reservation ID.
     */
    public Optional<Feedback> findByReservationId(Long reservationId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Feedback> query = em.createQuery(
                "SELECT f FROM Feedback f WHERE f.reservation.id = :reservationId", Feedback.class);
            query.setParameter("reservationId", reservationId);
            List<Feedback> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Find all feedback by guest.
     */
    public List<Feedback> findByGuest(Guest guest) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Feedback> query = em.createQuery(
                "SELECT f FROM Feedback f WHERE f.guest = :guest ORDER BY f.submittedAt DESC", Feedback.class);
            query.setParameter("guest", guest);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find feedback by rating.
     */
    public List<Feedback> findByRating(int rating) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Feedback> query = em.createQuery(
                "SELECT f FROM Feedback f WHERE f.rating = :rating ORDER BY f.submittedAt DESC", Feedback.class);
            query.setParameter("rating", rating);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find feedback by minimum rating.
     */
    public List<Feedback> findByMinimumRating(int minRating) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Feedback> query = em.createQuery(
                "SELECT f FROM Feedback f WHERE f.rating >= :minRating ORDER BY f.submittedAt DESC", Feedback.class);
            query.setParameter("minRating", minRating);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find feedback by sentiment tag.
     */
    public List<Feedback> findBySentimentTag(String sentimentTag) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Feedback> query = em.createQuery(
                "SELECT f FROM Feedback f WHERE f.sentimentTag LIKE :tag ORDER BY f.submittedAt DESC", Feedback.class);
            query.setParameter("tag", "%" + sentimentTag + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find feedback within a date range.
     */
    public List<Feedback> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Feedback> query = em.createQuery(
                "SELECT f FROM Feedback f WHERE f.submittedAt BETWEEN :startDate AND :endDate ORDER BY f.submittedAt DESC",
                Feedback.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get average rating.
     */
    public double getAverageRating() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Double> query = em.createQuery(
                "SELECT COALESCE(AVG(f.rating), 0) FROM Feedback f", Double.class);
            Double result = query.getSingleResult();
            return result != null ? result : 0.0;
        } finally {
            em.close();
        }
    }

    /**
     * Count feedback by rating.
     */
    public long countByRating(int rating) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(f) FROM Feedback f WHERE f.rating = :rating", Long.class);
            query.setParameter("rating", rating);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Get all feedback ordered by date.
     */
    public List<Feedback> findAllOrderByDate() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Feedback> query = em.createQuery(
                "SELECT f FROM Feedback f ORDER BY f.submittedAt DESC", Feedback.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
