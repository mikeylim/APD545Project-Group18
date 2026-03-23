package com.hotel.repository;

import com.hotel.model.Addon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Repository for Addon entity operations.
 */
public class AddonRepository extends GenericRepository<Addon, Long> {

    public AddonRepository() {
        super(Addon.class);
    }

    public Addon findByName(String name) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Addon> query = em.createQuery(
                "SELECT a FROM Addon a WHERE a.name = :name", Addon.class);
            query.setParameter("name", name);
            List<Addon> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public List<Addon> findAllActive() {
        return findAll();
    }
}
