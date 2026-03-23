package com.hotel.repository;

import com.hotel.app.EntityManagerFactoryProvider;
import com.hotel.model.Room;
import com.hotel.model.RoomStatus;
import com.hotel.model.RoomType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Room entity operations.
 */
public class RoomRepository extends GenericRepository<Room, Long> {

    public RoomRepository() {
        super(Room.class);
    }

    public List<Room> findByStatus(RoomStatus status) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Room> query = em.createQuery(
                "SELECT r FROM Room r WHERE r.status = :status", Room.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Room> findAvailableRooms() {
        return findByStatus(RoomStatus.AVAILABLE);
    }

    public List<Room> findByType(RoomType type) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Room> query = em.createQuery(
                "SELECT r FROM Room r WHERE r.type = :type", Room.class);
            query.setParameter("type", type);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Room> findAvailableByType(RoomType type) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Room> query = em.createQuery(
                "SELECT r FROM Room r WHERE r.type = :type AND r.status = :status", Room.class);
            query.setParameter("type", type);
            query.setParameter("status", RoomStatus.AVAILABLE);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Room findByRoomNumber(String roomNumber) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Room> query = em.createQuery(
                "SELECT r FROM Room r WHERE r.roomNumber = :roomNumber", Room.class);
            query.setParameter("roomNumber", roomNumber);
            List<Room> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Find rooms available for a given date range.
     * A room is available if it's not reserved for any overlapping reservation.
     */
    public List<Room> findAvailableForDateRange(LocalDate checkIn, LocalDate checkOut) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Room> query = em.createQuery(
                "SELECT r FROM Room r WHERE r.status = :status " +
                "AND r NOT IN (" +
                "  SELECT rm FROM Reservation res JOIN res.rooms rm " +
                "  WHERE res.status NOT IN ('CANCELLED', 'CHECKED_OUT', 'NO_SHOW') " +
                "  AND res.checkInDate < :checkOut AND res.checkOutDate > :checkIn" +
                ")", Room.class);
            query.setParameter("status", RoomStatus.AVAILABLE);
            query.setParameter("checkIn", checkIn);
            query.setParameter("checkOut", checkOut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Room> findAvailableByTypeForDateRange(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Room> query = em.createQuery(
                "SELECT r FROM Room r WHERE r.type = :type AND r.status = :status " +
                "AND r NOT IN (" +
                "  SELECT rm FROM Reservation res JOIN res.rooms rm " +
                "  WHERE res.status NOT IN ('CANCELLED', 'CHECKED_OUT', 'NO_SHOW') " +
                "  AND res.checkInDate < :checkOut AND res.checkOutDate > :checkIn" +
                ")", Room.class);
            query.setParameter("type", type);
            query.setParameter("status", RoomStatus.AVAILABLE);
            query.setParameter("checkIn", checkIn);
            query.setParameter("checkOut", checkOut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
