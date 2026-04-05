package com.hotel.repository;

import com.hotel.app.EntityManagerFactoryProvider;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Reservation entity operations.
 */
public class ReservationRepository extends GenericRepository<Reservation, Long> {

    public ReservationRepository() {
        super(Reservation.class);
    }

    public Reservation findByConfirmationNumber(String confirmationNumber) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.confirmationNumber = :confNum", Reservation.class);
            query.setParameter("confNum", confirmationNumber);
            List<Reservation> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public List<Reservation> findByGuest(Guest guest) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.guest = :guest ORDER BY r.checkInDate DESC", Reservation.class);
            query.setParameter("guest", guest);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findByStatus(ReservationStatus status) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.status = :status ORDER BY r.checkInDate", Reservation.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.checkInDate >= :startDate AND r.checkInDate <= :endDate " +
                "ORDER BY r.checkInDate", Reservation.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findTodayCheckIns() {
        LocalDate today = LocalDate.now();
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.checkInDate = :today AND r.status = :status", Reservation.class);
            query.setParameter("today", today);
            query.setParameter("status", ReservationStatus.CONFIRMED);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findTodayCheckOuts() {
        LocalDate today = LocalDate.now();
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.checkOutDate = :today AND r.status = :status", Reservation.class);
            query.setParameter("today", today);
            query.setParameter("status", ReservationStatus.CHECKED_IN);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> findActiveReservations() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.status IN (:statuses) ORDER BY r.checkInDate", Reservation.class);
            query.setParameter("statuses", List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN));
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> searchReservations(String searchTerm) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE " +
                "LOWER(r.confirmationNumber) LIKE :term OR " +
                "LOWER(r.guest.firstName) LIKE :term OR " +
                "LOWER(r.guest.lastName) LIKE :term OR " +
                "LOWER(r.guest.phone) LIKE :term " +
                "ORDER BY r.checkInDate DESC", Reservation.class);
            query.setParameter("term", "%" + searchTerm.toLowerCase() + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find checked-out reservations by guest phone number.
     * Returns most recent first.
     */
    public List<Reservation> findCheckedOutByPhone(String phone) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                "SELECT r FROM Reservation r WHERE r.guest.phone = :phone " +
                "AND r.status = :status ORDER BY r.checkOutDate DESC", Reservation.class);
            query.setParameter("phone", phone);
            query.setParameter("status", ReservationStatus.CHECKED_OUT);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
