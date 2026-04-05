/***********************************************
 * Final Project: Hotel Reservation System - Project Documentation
 * Course: APD545 NAA - Winter 2026
 * This assignment represents our own work in accordance with Seneca Academic Policy.
 * Name: Dohyun Lim, Phuong Bac Nguyen, Eunki Kim
 * Group: #18
 * Date: Apr 6, 2026
 ***********************************************/

# NewnhamNexus Hotel Reservation System
## Project Documentation

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture Summary](#2-architecture-summary)
3. [Design Artifacts](#3-design-artifacts)
4. [Entity and Relationship Mapping](#4-entity-and-relationship-mapping)
5. [Pattern Usage](#5-pattern-usage)
6. [Business Rules](#6-business-rules)
7. [Security and Logging](#7-security-and-logging)
8. [Export and Reporting](#8-export-and-reporting)
9. [Challenges and Learnings](#9-challenges-and-learnings)

---

## 1. Project Overview

### 1.1 Summary

NewnhamNexus Hotel is a famous tourist hotel in North York, Toronto. This project replaces the existing manual reservation system with a computerized solution to address the following problems:

- Records vulnerable to destruction by fire/disaster or theft
- Storage requiring extra cabinet space
- Difficult and time-consuming record searches
- Error-prone manual billing system

The system includes **2 self-service kiosks** for contactless booking, an **admin management portal**, and a **guest feedback system**.

### 1.2 Key Features

| Module | Features |
|--------|----------|
| **Kiosk** | 6-step booking flow, room selection, add-on services, loyalty enrollment |
| **Admin Portal** | CRUD operations, payments, checkout, waitlist management, reporting |
| **Feedback** | 1-5 star ratings, comments, sentiment tagging |
| **Loyalty Program** | Nexus Rewards - earn/redeem points |
| **Reporting** | Revenue, occupancy, feedback reports with CSV/PDF export |

### 1.3 Technologies Used

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17+ | Core programming language |
| JavaFX | 21 | Desktop UI framework |
| Hibernate | 6.4.4 | JPA/ORM implementation |
| H2 Database | 2.2.224 | Embedded file-based database |
| BCrypt (jbcrypt) | 0.4 | Password hashing |
| Apache PDFBox | 2.0.31 | PDF report generation |
| Maven | 3.6+ | Build and dependency management |

---

## 2. Architecture Summary

### 2.1 Three-Tier Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION TIER                        │
│         JavaFX UI (Kiosk, Admin, Feedback)                 │
│              Controllers + FXML Views                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 APPLICATION/BUSINESS TIER                   │
│     Services, Business Rules, Design Patterns              │
│  (Pricing, Discounts, Loyalty, Waitlist Notifications)     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA TIER                             │
│         ORM-backed Repositories + H2 Database              │
│        (Persistence, Queries, Transactions)                │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Cross-Cutting Concerns

| Concern | Implementation |
|---------|----------------|
| **Logging** | Rotating file logs (1MB limit, 10 files max) in `/logs` directory |
| **Security** | BCrypt password hashing, role-based access control |
| **Configuration** | `persistence.xml` for JPA, `styles.css` for UI theming |
| **Exception Handling** | Try-catch blocks with user-friendly error alerts |

### 2.3 MVC Pattern

| Component | Implementation |
|-----------|----------------|
| **Model** | JPA Entity classes (`Guest`, `Room`, `Reservation`, etc.) |
| **View** | FXML files in `/resources/view/` |
| **Controller** | `KioskController`, `AdminController`, `FeedbackController` |

### 2.4 Dependency Injection

The `AppContext` class serves as a simple dependency injection container:

```java
public class AppContext {
    private static final RoomRepository roomRepository = new RoomRepository();
    private static final GuestRepository guestRepository = new GuestRepository();
    private static final ReservationRepository reservationRepository = new ReservationRepository();
    // ... other repositories

    private static final ReservationService reservationService = new ReservationService(
        reservationRepository, roomRepository, guestRepository, addonRepository);
    private static final PricingService pricingService = new PricingService();
    // ... other services

    // Getters for each dependency
    public static ReservationService getReservationService() { return reservationService; }
}
```

### 2.5 ORM Implementation

- **Framework:** Hibernate 6.4.4 with Jakarta Persistence API
- **Database:** H2 embedded (file: `./data/hoteldb.mv.db`)
- **Schema Generation:** `hibernate.hbm2ddl.auto = update`
- **Connection Pooling:** Hibernate default connection provider

---

## 3. Design Artifacts

### 3.1 Class Diagram

The main entity classes and their relationships:

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    Guest     │       │  Reservation │       │     Room     │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id           │ 1   N │ id           │ N   M │ id           │
│ firstName    │◄──────│ confirmation#│──────►│ roomNumber   │
│ lastName     │       │ guest_id(FK) │       │ type         │
│ email        │       │ checkIn      │       │ status       │
│ phone        │       │ checkOut     │       │ floor        │
│ loyaltyNum   │       │ status       │       └──────────────┘
│ loyaltyPts   │       │ total        │
└──────────────┘       └──────────────┘
                              │ N
                              │
                              │ M
                       ┌──────▼───────┐
                       │    Addon     │
                       ├──────────────┤
                       │ id           │
                       │ name         │
                       │ price        │
                       │ pricingModel │
                       └──────────────┘
```

Additional entities: `Payment`, `User`, `Feedback`, `Waitlist`, `AuditLog`

### 3.2 Sequence Diagram - Kiosk Booking Flow

```
┌─────────┐  ┌──────────────┐  ┌────────────────┐  ┌─────────────┐  ┌────────┐
│  User   │  │KioskController│  │BookingSession  │  │ReservService│  │Database│
└────┬────┘  └──────┬───────┘  └───────┬────────┘  └──────┬──────┘  └───┬────┘
     │              │                   │                  │             │
     │ Select Guests│                   │                  │             │
     │─────────────►│                   │                  │             │
     │              │ setGuestCount()   │                  │             │
     │              │──────────────────►│                  │             │
     │              │                   │                  │             │
     │ Select Dates │                   │                  │             │
     │─────────────►│                   │                  │             │
     │              │ setDates()        │                  │             │
     │              │──────────────────►│                  │             │
     │              │                   │                  │             │
     │ Select Room  │                   │                  │             │
     │─────────────►│                   │                  │             │
     │              │ findAvailable()   │                  │             │
     │              │──────────────────────────────────────────────────►│
     │              │                   │                  │             │
     │ Select Addons│                   │                  │             │
     │─────────────►│                   │                  │             │
     │              │ setAddons()       │                  │             │
     │              │──────────────────►│                  │             │
     │              │                   │                  │             │
     │ Enter Details│                   │                  │             │
     │─────────────►│                   │                  │             │
     │              │ setGuestDetails() │                  │             │
     │              │──────────────────►│                  │             │
     │              │                   │                  │             │
     │ Confirm      │                   │                  │             │
     │─────────────►│                   │                  │             │
     │              │ createReservation()                  │             │
     │              │─────────────────────────────────────►│             │
     │              │                   │                  │ persist()   │
     │              │                   │                  │────────────►│
     │              │                   │                  │             │
     │◄─────────────│ Confirmation #    │                  │             │
     │              │                   │                  │             │
```

### 3.3 Deployment Diagram

See: `UML-deployment-diagram.png`

```
┌────────────────────────────────────────────────────────────────┐
│                      DEPLOYMENT ENVIRONMENT                     │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌─────────────────┐    ┌─────────────────┐                   │
│   │   Kiosk 1       │    │   Kiosk 2       │                   │
│   │   (JavaFX)      │    │   (JavaFX)      │                   │
│   └────────┬────────┘    └────────┬────────┘                   │
│            │                      │                             │
│            └──────────┬───────────┘                             │
│                       │                                         │
│                       ▼                                         │
│            ┌─────────────────────┐                              │
│            │   Admin Workstation │                              │
│            │   (JavaFX Desktop)  │                              │
│            └─────────┬───────────┘                              │
│                      │                                          │
│                      ▼                                          │
│            ┌─────────────────────┐                              │
│            │   H2 Database       │                              │
│            │   (Embedded File)   │                              │
│            │   ./data/hoteldb    │                              │
│            └─────────────────────┘                              │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

### 3.4 Package Diagram

See: `package-diagram.png`

```
com.hotel
├── app/                    # Application bootstrap
│   ├── Main.java
│   ├── AppContext.java
│   ├── DatabaseInitializer.java
│   └── EntityManagerFactoryProvider.java
│
├── controller/             # JavaFX Controllers
│   ├── KioskController.java
│   ├── AdminController.java
│   └── FeedbackController.java
│
├── model/                  # JPA Entities
│   ├── Guest.java
│   ├── Room.java
│   ├── Reservation.java
│   ├── Payment.java
│   ├── Addon.java
│   ├── User.java
│   ├── Feedback.java
│   ├── Waitlist.java
│   ├── AuditLog.java
│   └── enums/
│
├── repository/             # Data Access Layer
│   ├── GenericRepository.java
│   └── *Repository.java
│
├── service/                # Business Logic
│   ├── ReservationService.java
│   ├── PricingService.java
│   ├── LoyaltyService.java
│   └── ...
│
├── events/                 # Observer Pattern
│   ├── RoomAvailabilityEvent.java
│   ├── RoomAvailabilityNotifier.java
│   └── RoomAvailabilityObserver.java
│
└── util/                   # Utilities
    └── PdfExportUtil.java
```

### 3.5 UI Screenshots

The application consists of three main interfaces:

1. **Launcher Screen** - Entry point with options for Kiosk, Admin, and Feedback
2. **Kiosk Screens** (6 steps) - Guest count, dates, room, add-ons, details, confirmation
3. **Admin Dashboard** - Navigation to all management screens
4. **Admin Screens** - Reservations, Guests, Payments, Checkout, Waitlist, Feedback, Loyalty, Reports

---

## 4. Entity and Relationship Mapping

### 4.1 Entities (9 Tables + 2 Join Tables)

| Entity | Table Name | Primary Key | Description |
|--------|------------|-------------|-------------|
| Guest | guests | id (BIGINT) | Hotel guests/customers |
| Room | rooms | id (BIGINT) | Hotel rooms |
| Reservation | reservations | id (BIGINT) | Booking records |
| Payment | payments | id (BIGINT) | Payment transactions |
| Addon | addons | id (BIGINT) | Add-on services |
| User | users | id (BIGINT) | Admin/Manager accounts |
| Feedback | feedbacks | id (BIGINT) | Guest reviews |
| Waitlist | waitlist | id (BIGINT) | Waitlist entries |
| AuditLog | audit_logs | id (BIGINT) | Activity logs |
| - | reservation_rooms | - | Join table (Reservation-Room) |
| - | reservation_addons | - | Join table (Reservation-Addon) |

### 4.2 Relationships

#### One-to-Many (1:N)

| Parent | Child | Description |
|--------|-------|-------------|
| Guest | Reservation | One guest can have many reservations |
| Guest | Feedback | One guest can leave many feedbacks |
| Guest | Waitlist | One guest can have many waitlist entries |
| Reservation | Payment | One reservation can have many payments |

#### Many-to-Many (M:N)

| Entity 1 | Entity 2 | Join Table | Description |
|----------|----------|------------|-------------|
| Reservation | Room | reservation_rooms | Reservation can have multiple rooms |
| Reservation | Addon | reservation_addons | Reservation can have multiple add-ons |

### 4.3 Key JPA Annotations Used

```java
// Entity and Table
@Entity
@Table(name = "guests")
public class Guest {

    // Primary Key with Auto-Increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Required Field
    @Column(nullable = false)
    private String firstName;

    // Unique Constraint
    @Column(unique = true)
    private String loyaltyNumber;

    // Enum Stored as String
    @Enumerated(EnumType.STRING)
    private RoomType type;
}

// Many-to-One Relationship
@Entity
public class Reservation {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guest_id")
    private Guest guest;
}

// Many-to-Many with Join Table
@Entity
public class Reservation {
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "reservation_rooms",
        joinColumns = @JoinColumn(name = "reservation_id"),
        inverseJoinColumns = @JoinColumn(name = "room_id")
    )
    private List<Room> rooms;
}
```

### 4.4 Cascade and Fetch Strategy Notes

| Relationship | Fetch Type | Cascade | Reason |
|--------------|------------|---------|--------|
| Reservation → Guest | EAGER | None | Always need guest info with reservation |
| Reservation → Rooms | EAGER | None | Always need room info with reservation |
| Reservation → Addons | EAGER | None | Always need addon info with reservation |
| Reservation → Payments | LAZY | PERSIST | Load payments only when needed |
| Guest → Waitlist | EAGER | None | Quick access for notifications |

---

## 5. Pattern Usage

### 5.1 Singleton Pattern

**Purpose:** Ensure single instance management for shared resources.

**Implementations:**

1. **EntityManagerFactoryProvider** - Single database connection factory
```java
public class EntityManagerFactoryProvider {
    private static EntityManagerFactory emf;

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("HotelPU");
        }
        return emf;
    }
}
```

2. **LoyaltyService** - Single instance for loyalty point management
3. **AuditService** - Single instance for activity logging
4. **BookingSession** - Single instance for kiosk booking state

### 5.2 Factory Pattern

**Purpose:** Create room instances without exposing creation logic.

**Implementation:** `RoomFactory` and `RoomPlanFactory`

```java
public class RoomFactory {
    public static Room createRoom(String roomNumber, RoomType type, int floor) {
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setType(type);
        room.setFloor(floor);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setBasePrice(getBasePriceForType(type));
        room.setMaxOccupancy(getMaxOccupancyForType(type));
        return room;
    }

    private static double getBasePriceForType(RoomType type) {
        return switch (type) {
            case SINGLE -> 100.0;
            case DOUBLE -> 150.0;
            case DELUXE -> 200.0;
            case PENTHOUSE -> 350.0;
        };
    }
}
```

**Usage:** In `DatabaseInitializer` to create all initial room instances.

### 5.3 Strategy Pattern

**Purpose:** Dynamic pricing calculation based on day type.

**Implementation:**

```java
// Strategy Interface
public interface PricingStrategy {
    double calculatePrice(double basePrice);
    String getStrategyName();
}

// Standard Pricing (Weekdays)
public class StandardPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * 1.0;  // No markup
    }
}

// Weekend Pricing (Friday/Saturday)
public class WeekendPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * 1.25;  // 25% markup
    }
}

// Context: PricingService
public class PricingService {
    public PricingStrategy getStrategyForDate(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
            return new WeekendPricingStrategy();
        }
        return new StandardPricingStrategy();
    }
}
```

### 5.4 Observer Pattern

**Purpose:** Notify admins when rooms become available after checkout.

**Implementation:**

```java
// Event Data
public class RoomAvailabilityEvent {
    private final Room room;
    private final LocalDate availableFrom;
    private final LocalDate availableTo;
    // getters...
}

// Observer Interface
public interface RoomAvailabilityObserver {
    void onRoomAvailable(RoomAvailabilityEvent event);
}

// Notifier (Subject)
public class RoomAvailabilityNotifier {
    private final List<RoomAvailabilityObserver> observers = new ArrayList<>();

    public void subscribe(RoomAvailabilityObserver observer) {
        observers.add(observer);
    }

    public void notifyRoomAvailable(RoomAvailabilityEvent event) {
        for (RoomAvailabilityObserver observer : observers) {
            observer.onRoomAvailable(event);
        }
    }
}
```

**Usage:** When checkout completes in `AdminController`, it publishes a `RoomAvailabilityEvent`. The waitlist observer checks for matching entries and notifies guests.

### 5.5 Decorator Pattern

**Purpose:** Dynamically add services to booking pricing.

**Implementation:**

```java
// Base Interface
public interface BookingComponent {
    double getPrice();
    String getDescription();
}

// Concrete Component
public class BaseBooking implements BookingComponent {
    private final double basePrice;
    public double getPrice() { return basePrice; }
}

// Abstract Decorator
public abstract class BookingDecorator implements BookingComponent {
    protected BookingComponent wrapped;
    public BookingDecorator(BookingComponent wrapped) {
        this.wrapped = wrapped;
    }
}

// Concrete Decorator
public class ServiceDecorator extends BookingDecorator {
    private final Addon addon;
    private final int nights;

    @Override
    public double getPrice() {
        double addonPrice = addon.getPricingModel() == PricingModel.PER_NIGHT
            ? addon.getPrice() * nights
            : addon.getPrice();
        return wrapped.getPrice() + addonPrice;
    }
}
```

**Usage:** In `PricingService.calculateTotalWithAddons()` to build up the final price with all selected add-ons.

---

## 6. Business Rules

### 6.1 Room Occupancy Rules

| Room Type | Max Occupancy | Notes |
|-----------|---------------|-------|
| Single | 2 guests | Adults + Children combined |
| Double | 4 guests | Adults + Children combined |
| Deluxe | 4 guests | Adults + Children combined |
| Penthouse | 6 guests | Adults + Children combined |

### 6.2 Pricing Rules

| Rule | Description |
|------|-------------|
| Base Pricing | Single: $100, Double: $150, Deluxe: $200, Penthouse: $350 |
| Weekend Multiplier | Friday/Saturday nights: 1.25x base price |
| Tax Rate | 13% HST applied to subtotal |
| Add-on Pricing | PER_NIGHT or PER_RESERVATION based on addon type |

### 6.3 Discount Rules

| Role | Maximum Discount | Notes |
|------|------------------|-------|
| ADMIN | 15% | Applied at checkout |
| MANAGER | 30% | Applied at checkout |

### 6.4 Loyalty Program Rules (Nexus Rewards)

| Rule | Value |
|------|-------|
| Points Earned | 10 points per $1 spent |
| Points Redemption | 100 points = $1 discount |
| Welcome Bonus | 100 points on enrollment |
| Minimum Redemption | 100 points |

### 6.5 Feedback Rules

| Rule | Description |
|------|-------------|
| Rating Range | 1-5 stars |
| One Per Reservation | Only one feedback per reservation allowed |
| Requires Confirmation # | Guest must provide valid confirmation number |
| Sentiment Tags | Excellent, Good, Average, Poor, Terrible (auto-assigned) |

### 6.6 Reservation Status Flow

```
PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT
    ↓         ↓
CANCELLED   NO_SHOW
```

---

## 7. Security and Logging

### 7.1 Authentication

**Password Hashing:** BCrypt with default work factor

```java
// Hashing (during user creation)
String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

// Verification (during login)
boolean valid = BCrypt.checkpw(plainPassword, storedHash);
```

### 7.2 Role-Based Access Control

| Feature | ADMIN | MANAGER |
|---------|-------|---------|
| View Reservations | ✓ | ✓ |
| Create Reservations | ✓ | ✓ |
| Process Payments | ✓ | ✓ |
| Apply Discount (15%) | ✓ | ✓ |
| Apply Discount (30%) | ✗ | ✓ |
| View Reports | ✓ | ✓ |
| Export Reports | ✓ | ✓ |

### 7.3 Logging Configuration

**File Logging:**
- Location: `./logs/hotel.log`
- Rotation: 1MB per file, 10 files maximum
- Format: `[TIMESTAMP] [LEVEL] [CLASS] - MESSAGE`

**Database Logging (AuditLog):**
- Actor: Username who performed action
- Action: CREATE, UPDATE, DELETE, LOGIN, LOGOUT, PAYMENT
- Entity Type: What was affected
- Message: Human-readable description

### 7.4 Exception Handling

All exceptions are caught and displayed to users via JavaFX Alert dialogs:

```java
try {
    // Operation
} catch (Exception e) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText("Operation Failed");
    alert.setContentText(e.getMessage());
    alert.showAndWait();

    // Log the error
    logger.error("Operation failed: " + e.getMessage(), e);
}
```

---

## 8. Export and Reporting

### 8.1 Report Types

| Report | Description | Filters |
|--------|-------------|---------|
| Revenue Report | Total revenue by period | Date range (day/week/month) |
| Occupancy Report | Room utilization statistics | Date range, room type |
| Feedback Summary | Rating averages and comments | Date range, rating, sentiment |
| Activity Logs | System activity audit trail | Date range, actor, action |

### 8.2 Export Formats

| Format | Implementation | Use Case |
|--------|----------------|----------|
| CSV | Built-in Java file writing | Data analysis in Excel |
| PDF | Apache PDFBox library | Printable reports |
| TXT | Built-in Java file writing | Activity logs |

### 8.3 Sample Report Structure

**Revenue Report (CSV):**
```csv
Period,Room Revenue,Addon Revenue,Total Revenue,Reservations
2026-03-01,1500.00,250.00,1750.00,5
2026-03-02,2100.00,180.00,2280.00,7
```

**Occupancy Report (PDF):**
```
NEWNHAM NEXUS - Occupancy Report
Period: March 1-31, 2026

Room Type    | Available | Occupied | Occupancy Rate
-------------|-----------|----------|---------------
Single       | 155       | 120      | 77.4%
Double       | 155       | 145      | 93.5%
Deluxe       | 93        | 85       | 91.4%
Penthouse    | 62        | 58       | 93.5%
```

### 8.4 PDF Export Implementation

```java
public class PdfExportUtil {
    public static void exportDocument(String title, List<String> content, String filePath)
            throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                stream.beginText();
                stream.newLineAtOffset(50, 750);
                stream.showText(title);
                stream.endText();

                // Write content lines...
            }

            document.save(filePath);
        }
    }
}
```

---

## 9. Challenges and Learnings

### 9.1 Team Challenges

#### Challenge 1: Code Merging and Version Control

**Problem:** Multiple team members working on different features simultaneously resulted in numerous merge conflicts (20+ conflicting files at one point).

**Solution:** Established a "primary version" approach where one member's UI/UX was the base, and architectural components were carefully merged. Improved communication about file ownership and used feature branches consistently.

#### Challenge 2: Integrating JavaFX Frontend with Hibernate/JPA Backend

**Problem:** Entity objects weren't persisting correctly, lazy loading caused exceptions, and EntityManager lifecycle management was challenging.

**Solution:** Created service classes as intermediaries between controllers and repositories. Switched to EAGER fetch for critical relationships and ensured `repository.update()` was called when modifying entities.

#### Challenge 3: Implementing Design Patterns Correctly

**Problem:** Initially had pattern classes implemented but they weren't actually being used in the code.

**Solution:** Went through each pattern systematically:
- Factory: Used in DatabaseInitializer
- Strategy: Wired into PricingService
- Observer: Connected to checkout flow
- Decorator: Integrated into addon pricing

#### Challenge 4: UI/UX Consistency

**Problem:** Different screens had inconsistent styling, sidebar layouts, and color schemes.

**Solution:** Created shared `styles.css` and established conventions for common components. Did a final pass to ensure all admin screens had consistent navigation and styling.

#### Challenge 5: Database Schema Evolution

**Problem:** Adding new fields to entities caused issues with existing H2 database data.

**Solution:** Used Hibernate's "update" schema generation and handled null values carefully with wrapper types and null-safe getters.

### 9.2 Individual Reflections

**See:** `reflect.txt` for detailed individual reflections from each team member.

**Summary of Key Learnings:**

1. **Design Patterns** - Implementing patterns isn't just about creating classes; they must be meaningfully integrated into the codebase.

2. **JPA/Hibernate** - Understanding entity lifecycle (transient, managed, detached) is crucial for proper persistence.

3. **Git Workflow** - Merge conflicts can be minimized with good communication and branch management.

4. **Separation of Concerns** - Service classes make code more testable and maintainable.

5. **Security** - BCrypt for password hashing, proper salting, and work factors are essential.

6. **Database Design** - Early decisions have long-lasting effects; changing relationships later requires updating multiple files.

7. **Team Collaboration** - Writing clean, readable code with comments and naming conventions helps when others need to work with the code.

### 9.3 Suggestions for Future Improvements

1. **Multi-tenant Support** - Allow multiple hotel properties
2. **Email Notifications** - Send actual confirmation emails
3. **Mobile App** - Develop companion mobile application
4. **Online Payments** - Integrate Stripe/PayPal
5. **Room Images** - Add photo gallery for rooms
6. **Multi-language** - Support for multiple languages
7. **API Layer** - RESTful API for third-party integrations

---

## Appendices

### A. File Structure

```
/Project
├── pom.xml
├── README.md
├── DATABASE_DESIGN.md
├── ProjectDocumentation.md
├── reflect.txt
├── UML-deployment-diagram.png
├── package-diagram.png
├── data/
│   └── hoteldb.mv.db
├── logs/
│   └── hotel.log
└── src/main/
    ├── java/com/hotel/
    │   ├── app/
    │   ├── controller/
    │   ├── events/
    │   ├── model/
    │   ├── repository/
    │   ├── service/
    │   └── util/
    └── resources/
        ├── META-INF/persistence.xml
        ├── css/styles.css
        └── view/
            ├── Launcher.fxml
            ├── kiosk/
            ├── admin/
            └── feedback/
```

### B. How to Run

1. **Prerequisites:** Java 17+, Maven 3.6+
2. **Build:** `mvn clean install`
3. **Run:** `mvn javafx:run`
4. **Default Accounts:**
   - Admin: `admin` / `admin123`
   - Manager: `manager` / `manager123`

### C. References

- JavaFX Documentation: https://openjfx.io/
- Hibernate ORM: https://hibernate.org/orm/
- H2 Database: https://h2database.com/
- Apache PDFBox: https://pdfbox.apache.org/
- BCrypt: https://github.com/jeremyh/jBCrypt

---

**End of Documentation**

*Group 18 - APD545 Winter 2026*
*Dohyun Lim, Phuong Bac Nguyen, Eunki Kim*
