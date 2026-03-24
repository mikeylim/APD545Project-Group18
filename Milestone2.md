# Milestone 2 - Implementation Summary

**Due Date:** March 23rd, 2026

## Requirements from PDF
1. ORM/JPA layer with Singleton EntityManagerFactory
2. Kiosk booking flow with data persisting to database
3. Factory Pattern for room types (Single, Double, Penthouse)
4. Strategy Pattern for pricing (Standard vs Weekend)
5. Admin navigation buttons open their windows

---

# What Was Implemented

## 1. Maven Setup (pom.xml)

**Location:** `/Project/pom.xml`

**What it does:**
- Manages project dependencies automatically
- Downloads required libraries (Hibernate, H2 Database, BCrypt, JavaFX)

**Libraries added:**

| Library | Version | Purpose |
|---------|---------|---------|
| hibernate-core | 6.4.4 | JPA/ORM implementation |
| h2 | 2.2.224 | Embedded database (no MySQL setup needed) |
| jbcrypt | 0.4 | Password hashing for admin accounts |
| javafx-controls/fxml | - | JavaFX UI components |

---

## 2. JPA Entity Classes (Model Updates)

**Location:** `/src/main/java/com/hotel/model/`

**What changed:**
- Added `@Entity`, `@Table`, `@Id`, `@Column` annotations to model classes
- Added `@ManyToOne`, `@ManyToMany` for relationships between entities
- Added default constructors (required by JPA)
- Added getters/setters for all fields

**Entity Files (9 tables):**

| File | Purpose |
|------|---------|
| `Guest.java` | Stores guest information (name, email, phone, loyalty) |
| `Room.java` | Stores room info (number, type, status, floor) |
| `Reservation.java` | Stores booking (guest, rooms, dates, pricing, status) |
| `Payment.java` | Stores payment records |
| `Addon.java` | Stores add-on services (breakfast, parking, etc.) |
| `User.java` | Stores admin/manager accounts |
| `Feedback.java` | Stores guest feedback after checkout |
| `Waitlist.java` | Stores waitlist entries |
| `AuditLog.java` | Stores activity logs |

**Enums:**

| Enum | Values |
|------|--------|
| `RoomType.java` | SINGLE, DOUBLE, DELUXE, PENTHOUSE (with price & capacity) |
| `RoomStatus.java` | AVAILABLE, OCCUPIED, RESERVED, OUT_OF_SERVICE |
| `ReservationStatus.java` | PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, NO_SHOW |
| `PaymentMethod.java` | CASH, CREDIT_CARD, LOYALTY_POINTS |
| `PricingModel.java` | PER_NIGHT, PER_RESERVATION |
| `Role.java` | ADMIN, MANAGER |

**Deleted:**
- `Billing.java` → Billing info is now part of `Reservation.java`

---

## 3. Persistence Configuration (persistence.xml)

**Location:** `/src/main/resources/META-INF/persistence.xml`

**What it does:**
- Configures database connection (H2 embedded database)
- Database file created at: `./data/hoteldb.mv.db`
- Auto-creates tables based on entity annotations
- Shows SQL queries in console for debugging

**Key settings:**
```xml
hibernate.hbm2ddl.auto = update   <!-- Creates/updates tables automatically -->
hibernate.show_sql = true         <!-- Shows SQL in console -->
```

---

## 4. Singleton EntityManagerFactory

**Location:** `/src/main/java/com/hotel/app/EntityManagerFactoryProvider.java`

**What it does:**
- Provides single point of access to database (Singleton pattern)
- EntityManagerFactory created once at startup
- EntityManager created per transaction (not shared across threads)

**Methods:**

| Method | Purpose |
|--------|---------|
| `getEntityManagerFactory()` | Returns the singleton EMF |
| `createEntityManager()` | Creates new EM for each operation |
| `close()` | Closes EMF on app shutdown |

---

## 5. Repository Classes (Data Access Layer)

**Location:** `/src/main/java/com/hotel/repository/`

**What they do:**
- Handle all database operations (save, update, delete, find)
- Services use repositories, not direct database access

**Files:**

| Repository | Purpose |
|------------|---------|
| `GenericRepository.java` | Base CRUD operations (save, update, delete, findById, findAll) |
| `RoomRepository.java` | Find rooms by status, type, availability for dates |
| `GuestRepository.java` | Find guests by email, phone, name, loyalty number |
| `ReservationRepository.java` | Find reservations by confirmation#, guest, status, date |
| `AddonRepository.java` | Find addons by name |

---

## 6. Factory Pattern (RoomFactory.java)

**Location:** `/src/main/java/com/hotel/service/RoomFactory.java`

**What it does:**
- Creates Room objects with correct type settings
- Encapsulates room creation logic

**Methods:**

| Method | Creates |
|--------|---------|
| `createSingleRoom(roomNumber)` | Single room ($100, max 2 guests) |
| `createDoubleRoom(roomNumber)` | Double room ($150, max 4 guests) |
| `createDeluxeRoom(roomNumber)` | Deluxe room ($200, max 4 guests) |
| `createPenthouseRoom(roomNumber)` | Penthouse ($350, max 6 guests) |

**Usage example:**
```java
Room room = RoomFactory.createSingleRoom("101");
Room room = RoomFactory.createDoubleRoom("201", 2);  // with floor number
```

---

## 7. Strategy Pattern (Pricing)

**Location:** `/src/main/java/com/hotel/service/`

**What it does:**
- Different pricing calculations based on day of week
- Weekdays use standard pricing (1.0x multiplier)
- Weekends (Fri/Sat) use weekend pricing (1.25x multiplier)

**Files:**

| File | Purpose |
|------|---------|
| `PricingStrategy.java` | Interface defining `calculatePrice()` method |
| `StandardPricingStrategy.java` | Weekday pricing (multiplier = 1.0) |
| `WeekendPricingStrategy.java` | Weekend pricing (multiplier = 1.25) |
| `PricingService.java` | Chooses correct strategy based on date |

**How it works:**
```java
PricingService pricing = new PricingService();

// Wednesday (weekday) -> uses StandardPricingStrategy
double price1 = pricing.calculateRoomPrice(room, LocalDate.of(2026, 3, 25));
// Result: $100 * 1.0 = $100

// Saturday (weekend) -> uses WeekendPricingStrategy
double price2 = pricing.calculateRoomPrice(room, LocalDate.of(2026, 3, 28));
// Result: $100 * 1.25 = $125
```

---

## 8. Database Initializer

**Location:** `/src/main/java/com/hotel/app/DatabaseInitializer.java`

**What it does:**
- Seeds database with initial data on first run
- Only runs if database is empty

**Creates:**

**15 Rooms:**

| Floor | Rooms | Type | Price | Max Guests |
|-------|-------|------|-------|------------|
| 1 | 101-105 | Single | $100/night | 2 |
| 2 | 201-205 | Double | $150/night | 4 |
| 3 | 301-303 | Deluxe | $200/night | 4 |
| 4 | 401-402 | Penthouse | $350/night | 6 |

**6 Addons:**

| Addon | Price | Type |
|-------|-------|------|
| Breakfast Buffet | $25 | per night |
| Parking | $15 | per night |
| Premium Wi-Fi | $10 | per night |
| Spa Package | $150 | per reservation |
| Airport Shuttle | $50 | per reservation |
| Late Checkout (2pm) | $30 | per reservation |

**2 Admin Users:**

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |

---

## 9. Reservation Service

**Location:** `/src/main/java/com/hotel/service/ReservationService.java`

**What it does:**
- Handles complete kiosk booking flow
- Creates reservations with guests, rooms, addons
- Calculates pricing using Strategy pattern

**Methods:**

| Method | Purpose |
|--------|---------|
| `createReservation()` | Full booking creation |
| `getAvailableRooms()` | Find rooms available for dates |
| `getAllAddons()` | Get available addons |
| `calculateEstimate()` | Calculate price before booking |
| `validateOccupancy()` | Check if guests fit in selected rooms |
| `suggestRooms()` | Suggest room combination for guest count |
| `checkIn()` / `checkOut()` | Update reservation status |

---

## 10. Kiosk Booking Flow (Database Persistence)

**Location:** `/src/main/java/com/hotel/service/BookingSession.java`

### BookingSession Singleton

**What it does:**
- Holds all booking data as guest progresses through 6-step kiosk flow
- Maintains state across screen navigation
- Provides data to `ReservationService` for final persistence

**Data stored:**
```java
// Guest count
private int adults = 1;
private int children = 0;

// Dates
private LocalDate checkInDate;
private LocalDate checkOutDate;

// Room selection
private RoomType selectedRoomType;
private int singleRoomCount, doubleRoomCount, deluxeRoomCount, penthouseRoomCount;

// Addons
private List<Addon> selectedAddons;

// Guest details
private String firstName, lastName, email, phone;
private boolean joinLoyalty;

// Completed reservation
private Reservation completedReservation;
```

### KioskController Updates

**Location:** `/src/main/java/com/hotel/controller/KioskController.java`

**6-Step Booking Process:**

| Step | Screen | Data Captured |
|------|--------|---------------|
| 1 | Guest Count | Adults, children count |
| 2 | Date Selection | Check-in, check-out dates |
| 3 | Room Selection | Suggested or custom room selection |
| 4 | Add-ons | Optional services (Wi-Fi, Breakfast, etc.) |
| 5 | Guest Details | Name, email, phone, loyalty opt-in |
| 6 | Summary & Confirm | Review and confirm reservation |

**On Confirmation:**
```java
// When guest clicks "Confirm Reservation"
Reservation reservation = reservationService.createReservation(
    guest,                    // Guest entity from form data
    session.getAdults(),      // Adult count
    session.getChildren(),    // Children count
    session.getCheckInDate(), // Check-in date
    session.getCheckOutDate(),// Check-out date
    selectedRooms,            // List of Room entities
    session.getSelectedAddons() // List of Addon entities
);
// Reservation persisted to H2 database via JPA EntityManager
```

### Print Confirmation Feature

**Location:** `KioskConfirmation.fxml` + `KioskController.java`

- Print button shows formatted reservation summary
- Displays confirmation number, guest info, dates, room, pricing

---

## 11. Main.java Updates

**Location:** `/src/main/java/com/hotel/app/Main.java`

**Changes:**
- Added `init()` method → Initializes database on startup
- Added `stop()` method → Closes EntityManagerFactory on shutdown

---

# How to Test Milestone 2

## TEST 1: Verify Database Initialization
1. Run the application (Main.java)
2. Check the console output

**Expected output:**
```
Initializing database...
Initializing rooms...
Rooms initialized: 15
Initializing addons...
Addons initialized: 6
Initializing admin users...
Admin users initialized.
Database initialization complete.
```

## TEST 2: Verify Database File Created
1. After running the app, check the project folder
2. Look for: `/Project/data/hoteldb.mv.db`

This file contains all your data. If it exists, the database is working.

## TEST 3: Verify SQL Queries in Console
1. Run the app
2. Look for SQL statements in console like:
```sql
Hibernate: select r1_0.id,r1_0.floor,r1_0.roomNumber,r1_0.status,r1_0.type
           from rooms r1_0
```

This shows Hibernate is communicating with the database.

## TEST 4: Test Kiosk Booking Flow (Full Database Persistence)
1. Run the application
2. Click "Self-Service Kiosk" on launcher
3. Complete all 6 steps:
   - Select guests (e.g., 2 adults)
   - Select dates
   - Choose room
   - Select/skip add-ons
   - Enter guest details
   - Confirm reservation
4. Check console for:
```
===========================================
RESERVATION SAVED TO DATABASE!
Confirmation Number: NN-XXXXXXXX
Guest: [Name]
Room: [Room Number]
Total: $XXX.XX
===========================================
```

## TEST 5: Verify Reservation in Admin Portal
1. After completing kiosk booking
2. Go back to Main Menu
3. Click "Admin Portal"
4. Login: admin / admin123
5. Click "Reservations"
6. Your booking should appear in the list

## TEST 6: Test Factory Pattern
```java
Room single = RoomFactory.createSingleRoom("101");
System.out.println("Type: " + single.getType());           // SINGLE
System.out.println("Base Price: " + single.getBasePrice()); // 100.0
System.out.println("Max Occupancy: " + single.getMaxOccupancy()); // 2
```

## TEST 7: Test Strategy Pattern (Pricing)
```java
PricingService pricing = new PricingService();
Room room = RoomFactory.createSingleRoom("101"); // $100 base

// Weekday (Wednesday)
LocalDate wednesday = LocalDate.of(2026, 3, 25);
double weekdayPrice = pricing.calculateRoomPrice(room, wednesday);
System.out.println("Weekday: $" + weekdayPrice);  // Should print $100.0

// Weekend (Saturday)
LocalDate saturday = LocalDate.of(2026, 3, 28);
double weekendPrice = pricing.calculateRoomPrice(room, saturday);
System.out.println("Weekend: $" + weekendPrice);  // Should print $125.0
```

---

# File Structure Summary

```
/Project
├── pom.xml                              <- Maven configuration
├── data/
│   └── hoteldb.mv.db                    <- H2 database file (auto-created)
├── src/main/java/com/hotel/
│   ├── app/
│   │   ├── Main.java                    <- Entry point (updated)
│   │   ├── DatabaseInitializer.java     <- Seeds database
│   │   └── EntityManagerFactoryProvider.java <- Singleton EMF
│   ├── controller/
│   │   └── KioskController.java         <- Kiosk flow with DB persistence (updated)
│   ├── model/
│   │   ├── Guest.java                   <- JPA Entity
│   │   ├── Room.java                    <- JPA Entity
│   │   ├── Reservation.java             <- JPA Entity
│   │   ├── Payment.java                 <- JPA Entity
│   │   ├── Addon.java                   <- JPA Entity
│   │   ├── User.java                    <- JPA Entity
│   │   ├── Feedback.java                <- JPA Entity
│   │   ├── Waitlist.java                <- JPA Entity
│   │   ├── AuditLog.java                <- JPA Entity
│   │   ├── RoomType.java                <- Enum with prices
│   │   ├── RoomStatus.java              <- Enum
│   │   ├── ReservationStatus.java       <- Enum
│   │   ├── PaymentMethod.java           <- Enum
│   │   ├── PricingModel.java            <- Enum
│   │   └── Role.java                    <- Enum
│   ├── repository/
│   │   ├── GenericRepository.java       <- Base CRUD
│   │   ├── RoomRepository.java          <- Room queries
│   │   ├── GuestRepository.java         <- Guest queries
│   │   ├── ReservationRepository.java   <- Reservation queries
│   │   └── AddonRepository.java         <- Addon queries
│   └── service/
│       ├── BookingSession.java          <- Singleton for kiosk state (NEW)
│       ├── RoomFactory.java             <- Factory Pattern
│       ├── PricingStrategy.java         <- Strategy interface
│       ├── StandardPricingStrategy.java <- Weekday pricing
│       ├── WeekendPricingStrategy.java  <- Weekend pricing
│       ├── PricingService.java          <- Pricing logic
│       └── ReservationService.java      <- Booking logic
├── src/main/resources/
│   ├── META-INF/
│   │   └── persistence.xml              <- JPA configuration
│   ├── css/
│   │   └── styles.css                   <- Updated kiosk step indicator styles
│   └── view/kiosk/
│       ├── KioskWelcome.fxml
│       ├── KioskGuestCount.fxml         <- Step 1
│       ├── KioskDateSelection.fxml      <- Step 2
│       ├── KioskRoomSelection.fxml      <- Step 3
│       ├── KioskAddOns.fxml             <- Step 4
│       ├── KioskGuestDetails.fxml       <- Step 5
│       ├── KioskSummary.fxml            <- Step 6
│       └── KioskConfirmation.fxml       <- Confirmation with print
└── DATABASE_DESIGN.md                   <- Database documentation
```

---

# Notes

1. The database file (`hoteldb.mv.db`) is in `.gitignore` - each developer will have their own local database.

2. On first run, the app creates all tables and seed data automatically.

3. The admin passwords are hashed with BCrypt - you cannot see them in the database, but you can log in with `admin/admin123` or `manager/manager123`.

4. **The Kiosk UI is now fully connected to the backend** - completing a booking in the kiosk persists the Guest and Reservation to the H2 database.

5. To reset the database, delete the `/data` folder and restart the app.

6. The `BookingSession` singleton maintains booking state across all 6 kiosk screens, ensuring data is not lost during navigation.

7. Progress indicators use CSS classes (`kiosk-step-indicator`, `kiosk-step-indicator-active`, `kiosk-step-indicator-completed`) for consistent styling across all screens.
