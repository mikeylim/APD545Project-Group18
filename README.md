# NewnhamNexus Hotel Reservation System

A desktop-only hotel reservation and billing system built with JavaFX, replacing manual processes with a computer-based solution featuring self-service kiosks, admin management, and guest feedback.

**Course:** APD545 - Advanced Application Development
**Final Due Date:** April 6th, 2026

---

## Project Overview

**NewnhamNexus Hotel** is a famous tourist hotel in North York, Toronto. The current manual reservation system has several issues:
- Records can be destroyed by fire/disaster or stolen
- Storing files requires extra cabinet space
- Searching for records is difficult and time-consuming
- Manual billing system is error-prone

This system replaces the manual process with a computerized solution, including **2 self-service kiosks** for contactless booking.

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Java 17+ | Core programming language |
| JavaFX | Desktop UI framework |
| Hibernate 6.4.4 | JPA/ORM implementation |
| H2 Database | Embedded file-based database |
| BCrypt (jbcrypt 0.4) | Password hashing |
| Maven | Build and dependency management |

---

## Architecture

This project follows a **3-tier architecture**:

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

**Cross-cutting concerns:** Logging, Security, Configuration

---

## Design Patterns Used

| Pattern | Implementation | Purpose |
|---------|----------------|---------|
| **Singleton** | `EntityManagerFactoryProvider` | Single database connection factory |
| **Factory** | `RoomFactory` | Create room instances (Single, Double, Deluxe, Penthouse) |
| **Strategy** | `PricingStrategy` | Dynamic pricing (Standard vs Weekend) |
| **Observer** | *(Final)* | Notify admins when rooms become available |
| **Decorator** | *(Final)* | Add services (Spa, Wi-Fi, Breakfast) to booking pricing |

---

## Project Milestones

### Milestone 1: Design & Blueprinting (March 9th, 2026) - 3%

| Requirement | Status |
|-------------|--------|
| UI Prototype (Kiosk + Admin screens) | Done |
| Class Diagram (Guest, Room, Reservation, Payment) | Done |
| Sequence Diagram (Booking Flow) | Done |
| ERD (Database Schema) | Done |

---

### Milestone 2: The Functional Core (March 23rd, 2026) - 7%

| Requirement | Status |
|-------------|--------|
| ORM/JPA layer with Singleton EntityManagerFactory | Done |
| Kiosk booking flow with database persistence | Done |
| Factory Pattern for room types | Done |
| Strategy Pattern for pricing (Standard vs Weekend) | Done |
| Admin navigation buttons open windows | Done |
| Database video (3+ min, all members speak) | Done |

**Database Entities (9 tables + 2 join tables):**
- Guest, Room, Reservation, Payment, Addon, User, Feedback, Waitlist, AuditLog
- Join tables: `reservation_rooms`, `reservation_addons`

---

### Final Submission (April 6th, 2026) - 8%

| Requirement | Status |
|-------------|--------|
| **Admin Functionality** | |
| Full CRUD for reservations | Pending |
| Role-based discount caps (Admin: 15%, Manager: 30%) | Pending |
| Loyalty system (earn/redeem points) | Pending |
| Search guests/reservations with filters | Pending |
| Paginated tables with sortable columns | Pending |
| Process payments (cash, card, loyalty points) | Pending |
| Deposits, partial payments, refunds | Pending |
| Checkout with balance settlement | Pending |
| **Patterns** | |
| Observer (room availability notifications) | Pending |
| Decorator (add services to bill dynamically) | Pending |
| **Cross-Cutting Concerns** | |
| BCrypt password hashing | Done |
| Rotating file logs (1MB limit, 10 files) | Pending |
| Activity logging (logins, changes, payments) | Pending |
| **Reporting** | |
| Revenue reports (day/week/month) with CSV/PDF export | Pending |
| Occupancy reports with CSV/PDF export | Pending |
| Feedback summary with CSV export | Pending |
| Activity logs with CSV/TXT export | Pending |
| **Waitlist** | |
| Add guests to waitlist | Pending |
| Observer notifications when rooms available | Pending |
| Convert waitlist entry to reservation | Pending |
| **Feedback** | |
| Guest feedback submission (1-5 stars + comments) | Pending |
| Filter by rating, date, sentiment, guest | Pending |

---

## How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Project
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

4. **Default Admin Accounts**

   | Username | Password | Role |
   |----------|----------|------|
   | admin | admin123 | ADMIN |
   | manager | manager123 | MANAGER |

### Database

- **Type:** H2 Embedded (file-based)
- **Location:** `./data/hoteldb.mv.db`
- **Auto-created:** Tables are created automatically on first run
- **Reset:** Delete the `/data` folder and restart the app

---

## Project Structure

```
/Project
├── pom.xml                              # Maven configuration
├── README.md                            # This file
├── Milestone2.md                        # Milestone 2 documentation
├── DATABASE_DESIGN.md                   # Database design documentation
├── data/
│   └── hoteldb.mv.db                    # H2 database (auto-created)
├── src/main/java/com/hotel/
│   ├── app/
│   │   ├── Main.java                    # Application entry point
│   │   ├── DatabaseInitializer.java     # Seeds initial data
│   │   └── EntityManagerFactoryProvider.java  # Singleton EMF
│   ├── controller/
│   │   ├── KioskController.java         # Kiosk booking flow
│   │   ├── AdminController.java         # Admin dashboard
│   │   └── ...
│   ├── model/
│   │   ├── Guest.java                   # JPA Entity
│   │   ├── Room.java                    # JPA Entity
│   │   ├── Reservation.java             # JPA Entity
│   │   ├── Payment.java                 # JPA Entity
│   │   ├── Addon.java                   # JPA Entity
│   │   ├── User.java                    # JPA Entity
│   │   ├── Feedback.java                # JPA Entity
│   │   ├── Waitlist.java                # JPA Entity
│   │   ├── AuditLog.java                # JPA Entity
│   │   └── enums/                       # RoomType, RoomStatus, etc.
│   ├── repository/
│   │   ├── GenericRepository.java       # Base CRUD operations
│   │   ├── RoomRepository.java
│   │   ├── GuestRepository.java
│   │   ├── ReservationRepository.java
│   │   └── AddonRepository.java
│   └── service/
│       ├── BookingSession.java          # Kiosk state singleton
│       ├── ReservationService.java      # Booking logic
│       ├── RoomFactory.java             # Factory Pattern
│       ├── PricingStrategy.java         # Strategy interface
│       ├── StandardPricingStrategy.java # Weekday pricing
│       ├── WeekendPricingStrategy.java  # Weekend pricing (1.25x)
│       └── PricingService.java          # Pricing logic
├── src/main/resources/
│   ├── META-INF/
│   │   └── persistence.xml              # JPA configuration
│   ├── css/
│   │   └── styles.css                   # Application styles
│   └── view/
│       ├── kiosk/                       # Kiosk FXML screens
│       ├── admin/                       # Admin FXML screens
│       └── ...
```

---

## Room Types & Pricing

| Type | Base Price | Max Occupancy | Weekend Price (1.25x) |
|------|------------|---------------|----------------------|
| Single | $100/night | 2 guests | $125/night |
| Double | $150/night | 4 guests | $187.50/night |
| Deluxe | $200/night | 4 guests | $250/night |
| Penthouse | $350/night | 6 guests | $437.50/night |

---

## Add-on Services

| Service | Price | Pricing Model |
|---------|-------|---------------|
| Breakfast Buffet | $25 | Per Night |
| Parking | $15 | Per Night |
| Premium Wi-Fi | $10 | Per Night |
| Spa Package | $150 | Per Reservation |
| Airport Shuttle | $50 | Per Reservation |
| Late Checkout (2pm) | $30 | Per Reservation |

---

## Kiosk Booking Flow

```
┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐
│ Step 1  │──▶│ Step 2  │──▶│ Step 3  │──▶│ Step 4  │──▶│ Step 5  │──▶│ Step 6  │
│ Guests  │   │  Dates  │   │  Room   │   │ Add-ons │   │ Details │   │ Confirm │
└─────────┘   └─────────┘   └─────────┘   └─────────┘   └─────────┘   └─────────┘
```

1. **Guest Count** - Select adults and children
2. **Date Selection** - Pick check-in and check-out dates
3. **Room Selection** - Suggested room or custom selection
4. **Add-ons** - Optional services (Wi-Fi, Breakfast, Spa, etc.)
5. **Guest Details** - Name, email, phone, loyalty opt-in
6. **Summary & Confirm** - Review and confirm reservation

---

## Business Rules

### Occupancy
- Single room: up to 2 people
- Double room: up to 4 people
- Deluxe room: up to 4 people
- Penthouse: up to 6 people

### Pricing
- Weekday pricing: 1.0x multiplier
- Weekend pricing (Fri/Sat): 1.25x multiplier
- Seasonal multipliers: configurable date ranges

### Discounts
- Admin: up to 15%
- Manager: up to 30%

### Loyalty
- Configurable earning rate per payment
- Redemption caps per reservation

---

## Documentation

| Document | Description |
|----------|-------------|
| `README.md` | Project overview and setup |
| `Milestone2.md` | Milestone 2 implementation details |
| `DATABASE_DESIGN.md` | Database schema and JPA documentation |
| `videoScript.md` | Video presentation script |

---

## Grading Rubric

| Category | Weight |
|----------|--------|
| Design & Architecture | 20% |
| Patterns & Principles | 15% |
| ORM & Persistence | 15% |
| Functionality - Kiosk | 10% |
| Functionality - Admin | 15% |
| Functionality - Waitlist & Loyalty | 10% |
| Reporting & Feedback | 10% |
| Logging & Security | 5% |

**Passing threshold:** Minimum 50% overall, with working kiosk booking, admin login, and ORM persistence.

---

## Optional: Multithreaded Server

For bonus points, implement a multithreaded server allowing multiple admins to work simultaneously:
- Server listens for admin connections
- Each admin session runs in a separate thread
- Client-server communication via sockets
- Test with 2+ concurrent admin sessions

---

## License

This project is for educational purposes only - APD545 Course Project at Seneca College.
