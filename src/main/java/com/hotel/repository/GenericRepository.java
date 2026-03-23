package com.hotel.repository;

import com.hotel.app.EntityManagerFactoryProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository providing basic CRUD operations.
 * Uses JPA EntityManager for persistence operations.
 */
public abstract class GenericRepository<T, ID> {

    private final Class<T> entityClass;

    protected GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected EntityManager getEntityManager() {
        return EntityManagerFactoryProvider.createEntityManager();
    }

    public T save(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public T update(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<T> findById(ID id) {
        EntityManager em = getEntityManager();
        try {
            T entity = em.find(entityClass, id);
            return Optional.ofNullable(entity);
        } finally {
            em.close();
        }
    }

    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            String query = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            TypedQuery<T> typedQuery = em.createQuery(query, entityClass);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T managed = em.merge(entity);
            em.remove(managed);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }

    public long count() {
        EntityManager em = getEntityManager();
        try {
            String query = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(query, Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
}
