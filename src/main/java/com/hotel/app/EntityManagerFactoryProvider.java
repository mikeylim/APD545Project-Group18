package com.hotel.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Singleton provider for EntityManagerFactory.
 * As per project requirements, the EntityManagerFactory must be created once
 * and treated as a singleton.
 */
public class EntityManagerFactoryProvider {

    private static final String PERSISTENCE_UNIT_NAME = "HotelPU";
    private static EntityManagerFactory emf;

    // Private constructor to prevent instantiation
    private EntityManagerFactoryProvider() {}

    /**
     * Get the singleton EntityManagerFactory instance.
     * Creates it if it doesn't exist yet.
     */
    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return emf;
    }

    /**
     * Create a new EntityManager.
     * As per project requirements, EntityManager should be created per transaction
     * or unit of work and must not be shared across threads.
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Close the EntityManagerFactory.
     * Should be called when the application shuts down.
     */
    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }
}
