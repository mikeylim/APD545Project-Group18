package com.hotel.repository;

import com.hotel.model.Guest;
import com.hotel.model.RoomType;
import com.hotel.model.Waitlist;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Waitlist entity operations.
 * Handles waitlist queries and management.
 */
public class WaitlistRepository extends GenericRepository<Waitlist, Long> {

    public WaitlistRepository() {
        super(Waitlist.class);
    }

    /**
     * Find all active (not converted) waitlist entries.
     */
    public List<Waitlist> findActiveEntries() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Waitlist> query = em.createQuery(
                "SELECT w FROM Waitlist w WHERE w.converted = false ORDER BY w.createdAt ASC",
                Waitlist.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find waitlist entries by guest.
     */
    public List<Waitlist> findByGuest(Guest guest) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Waitlist> query = em.createQuery(
                "SELECT w FROM Waitlist w WHERE w.guest = :guest ORDER BY w.createdAt DESC",
                Waitlist.class);
            query.setParameter("guest", guest);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find waitlist entries by room type.
     */
    public List<Waitlist> findByRoomType(RoomType roomType) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Waitlist> query = em.createQuery(
                "SELECT w FROM Waitlist w WHERE w.roomType = :roomType AND w.converted = false ORDER BY w.createdAt ASC",
                Waitlist.class);
            query.setParameter("roomType", roomType);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find waitlist entries that match available dates.
     * Used to notify guests when rooms become available.
     */
    public List<Waitlist> findMatchingEntries(RoomType roomType, LocalDate availableFrom, LocalDate availableTo) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Waitlist> query = em.createQuery(
                "SELECT w FROM Waitlist w WHERE w.roomType = :roomType " +
                "AND w.converted = false AND w.notified = false " +
                "AND w.desiredCheckIn >= :availableFrom " +
                "AND w.desiredCheckOut <= :availableTo " +
                "ORDER BY w.createdAt ASC",
                Waitlist.class);
            query.setParameter("roomType", roomType);
            query.setParameter("availableFrom", availableFrom);
            query.setParameter("availableTo", availableTo);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find entries that haven't been notified yet.
     */
    public List<Waitlist> findNotNotified() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Waitlist> query = em.createQuery(
                "SELECT w FROM Waitlist w WHERE w.notified = false AND w.converted = false ORDER BY w.createdAt ASC",
                Waitlist.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find entries by date range.
     */
    public List<Waitlist> findByDateRange(LocalDate startDate, LocalDate endDate) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Waitlist> query = em.createQuery(
                "SELECT w FROM Waitlist w WHERE w.desiredCheckIn >= :startDate AND w.desiredCheckIn <= :endDate " +
                "AND w.converted = false ORDER BY w.createdAt ASC",
                Waitlist.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Count active waitlist entries.
     */
    public long countActive() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(w) FROM Waitlist w WHERE w.converted = false", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Find waitlist entries for a specific room type and overlapping dates.
     */
    public List<Waitlist> findByRoomTypeAndOverlappingDates(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Waitlist> query = em.createQuery(
                "SELECT w FROM Waitlist w WHERE w.roomType = :roomType " +
                "AND w.converted = false " +
                "AND ((w.desiredCheckIn <= :checkOut AND w.desiredCheckOut >= :checkIn)) " +
                "ORDER BY w.createdAt ASC",
                Waitlist.class);
            query.setParameter("roomType", roomType);
            query.setParameter("checkIn", checkIn);
            query.setParameter("checkOut", checkOut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
