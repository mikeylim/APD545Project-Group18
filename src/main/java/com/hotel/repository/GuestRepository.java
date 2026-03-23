package com.hotel.repository;

import com.hotel.app.EntityManagerFactoryProvider;
import com.hotel.model.Guest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Repository for Guest entity operations.
 */
public class GuestRepository extends GenericRepository<Guest, Long> {

    public GuestRepository() {
        super(Guest.class);
    }

    public Guest findByEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Guest> query = em.createQuery(
                "SELECT g FROM Guest g WHERE g.email = :email", Guest.class);
            query.setParameter("email", email);
            List<Guest> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public Guest findByPhone(String phone) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Guest> query = em.createQuery(
                "SELECT g FROM Guest g WHERE g.phone = :phone", Guest.class);
            query.setParameter("phone", phone);
            List<Guest> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public List<Guest> findByName(String name) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Guest> query = em.createQuery(
                "SELECT g FROM Guest g WHERE LOWER(g.firstName) LIKE :name OR LOWER(g.lastName) LIKE :name", Guest.class);
            query.setParameter("name", "%" + name.toLowerCase() + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Guest findByLoyaltyNumber(String loyaltyNumber) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Guest> query = em.createQuery(
                "SELECT g FROM Guest g WHERE g.loyaltyNumber = :loyaltyNumber", Guest.class);
            query.setParameter("loyaltyNumber", loyaltyNumber);
            List<Guest> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public List<Guest> findLoyaltyMembers() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Guest> query = em.createQuery(
                "SELECT g FROM Guest g WHERE g.loyaltyNumber IS NOT NULL", Guest.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
