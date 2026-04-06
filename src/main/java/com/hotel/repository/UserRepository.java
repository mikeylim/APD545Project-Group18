package com.hotel.repository;

import com.hotel.model.Role;
import com.hotel.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity operations.
 * Handles admin/manager account queries.
 */
public class UserRepository extends GenericRepository<User, Long> {

    public UserRepository() {
        super(User.class);
    }

    /**
     * Find user by username for login.
     */
    public Optional<User> findByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Find all active users.
     */
    public List<User> findActiveUsers() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.active = true ORDER BY u.username", User.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find users by role.
     */
    public List<User> findByRole(Role role) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.role = :role AND u.active = true", User.class);
            query.setParameter("role", role);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Check if username exists.
     */
    public boolean existsByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }
}
