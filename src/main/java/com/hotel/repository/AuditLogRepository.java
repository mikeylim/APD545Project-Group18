package com.hotel.repository;

import com.hotel.model.AuditLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity operations.
 * Handles activity log queries and retrieval.
 */
public class AuditLogRepository extends GenericRepository<AuditLog, Long> {

    public AuditLogRepository() {
        super(AuditLog.class);
    }

    /**
     * Find logs by actor (username).
     */
    public List<AuditLog> findByActor(String actor) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<AuditLog> query = em.createQuery(
                "SELECT a FROM AuditLog a WHERE a.actor = :actor ORDER BY a.timestamp DESC",
                AuditLog.class);
            query.setParameter("actor", actor);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find logs by action type.
     */
    public List<AuditLog> findByAction(String action) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<AuditLog> query = em.createQuery(
                "SELECT a FROM AuditLog a WHERE a.action = :action ORDER BY a.timestamp DESC",
                AuditLog.class);
            query.setParameter("action", action);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find logs by entity type.
     */
    public List<AuditLog> findByEntityType(String entityType) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<AuditLog> query = em.createQuery(
                "SELECT a FROM AuditLog a WHERE a.entityType = :entityType ORDER BY a.timestamp DESC",
                AuditLog.class);
            query.setParameter("entityType", entityType);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find logs by entity ID.
     */
    public List<AuditLog> findByEntityId(String entityId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<AuditLog> query = em.createQuery(
                "SELECT a FROM AuditLog a WHERE a.entityId = :entityId ORDER BY a.timestamp DESC",
                AuditLog.class);
            query.setParameter("entityId", entityId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find logs within a date range.
     */
    public List<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<AuditLog> query = em.createQuery(
                "SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC",
                AuditLog.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find recent logs (last N entries).
     */
    public List<AuditLog> findRecent(int limit) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<AuditLog> query = em.createQuery(
                "SELECT a FROM AuditLog a ORDER BY a.timestamp DESC",
                AuditLog.class);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find all logs ordered by timestamp.
     */
    public List<AuditLog> findAllOrderByTimestamp() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<AuditLog> query = em.createQuery(
                "SELECT a FROM AuditLog a ORDER BY a.timestamp DESC",
                AuditLog.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Search logs by message content.
     */
    public List<AuditLog> searchByMessage(String searchTerm) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<AuditLog> query = em.createQuery(
                "SELECT a FROM AuditLog a WHERE LOWER(a.message) LIKE LOWER(:searchTerm) ORDER BY a.timestamp DESC",
                AuditLog.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find login attempts.
     */
    public List<AuditLog> findLoginAttempts() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<AuditLog> query = em.createQuery(
                "SELECT a FROM AuditLog a WHERE a.action IN ('LOGIN', 'LOGIN_FAILED', 'LOGOUT') ORDER BY a.timestamp DESC",
                AuditLog.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
