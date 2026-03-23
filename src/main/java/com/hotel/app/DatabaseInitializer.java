package com.hotel.app;

import com.hotel.model.*;
import com.hotel.repository.AddonRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.service.RoomFactory;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Initializes the database with seed data.
 * Creates default rooms, addons, and admin users.
 */
public class DatabaseInitializer {

    private final RoomRepository roomRepository;
    private final AddonRepository addonRepository;

    public DatabaseInitializer() {
        this.roomRepository = new RoomRepository();
        this.addonRepository = new AddonRepository();
    }

    /**
     * Initialize database with seed data if empty.
     */
    public void initialize() {
        initializeRooms();
        initializeAddons();
        initializeAdminUsers();
    }

    private void initializeRooms() {
        // Check if rooms already exist
        if (roomRepository.count() > 0) {
            System.out.println("Rooms already initialized.");
            return;
        }

        System.out.println("Initializing rooms...");

        // Floor 1: Single rooms (101-105)
        for (int i = 1; i <= 5; i++) {
            Room room = RoomFactory.createSingleRoom("10" + i, 1);
            roomRepository.save(room);
        }

        // Floor 2: Double rooms (201-205)
        for (int i = 1; i <= 5; i++) {
            Room room = RoomFactory.createDoubleRoom("20" + i, 2);
            roomRepository.save(room);
        }

        // Floor 3: Deluxe rooms (301-303)
        for (int i = 1; i <= 3; i++) {
            Room room = RoomFactory.createDeluxeRoom("30" + i, 3);
            roomRepository.save(room);
        }

        // Floor 4: Penthouse suites (401-402)
        for (int i = 1; i <= 2; i++) {
            Room room = RoomFactory.createPenthouseRoom("40" + i, 4);
            roomRepository.save(room);
        }

        System.out.println("Rooms initialized: " + roomRepository.count());
    }

    private void initializeAddons() {
        // Check if addons already exist
        if (addonRepository.count() > 0) {
            System.out.println("Addons already initialized.");
            return;
        }

        System.out.println("Initializing addons...");

        // Per-night addons
        addonRepository.save(new Addon("Breakfast Buffet", 25.00, PricingModel.PER_NIGHT));
        addonRepository.save(new Addon("Parking", 15.00, PricingModel.PER_NIGHT));
        addonRepository.save(new Addon("Premium Wi-Fi", 10.00, PricingModel.PER_NIGHT));

        // Per-reservation addons
        addonRepository.save(new Addon("Spa Package", 150.00, PricingModel.PER_RESERVATION));
        addonRepository.save(new Addon("Airport Shuttle", 50.00, PricingModel.PER_RESERVATION));
        addonRepository.save(new Addon("Late Checkout (2pm)", 30.00, PricingModel.PER_RESERVATION));

        System.out.println("Addons initialized: " + addonRepository.count());
    }

    private void initializeAdminUsers() {
        EntityManager em = EntityManagerFactoryProvider.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            // Check if users already exist
            Long userCount = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
            if (userCount > 0) {
                System.out.println("Admin users already initialized.");
                return;
            }

            System.out.println("Initializing admin users...");

            tx.begin();

            // Create admin user
            User admin = new User("admin", BCrypt.hashpw("admin123", BCrypt.gensalt()), Role.ADMIN);
            em.persist(admin);

            // Create manager user
            User manager = new User("manager", BCrypt.hashpw("manager123", BCrypt.gensalt()), Role.MANAGER);
            em.persist(manager);

            tx.commit();
            System.out.println("Admin users initialized.");

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("Error initializing admin users: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}
