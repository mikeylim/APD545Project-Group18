# NewnhamNexus Hotel Reservation System - Database Design

## Overview

This document explains the complete database design for Milestone 2, including all tables, relationships, and JPA annotations used.

---

## Database Tables Summary

| Table Name         | Entity Class   | Purpose                                      |
|--------------------|----------------|----------------------------------------------|
| guests             | Guest          | Store guest/customer information             |
| rooms              | Room           | Store hotel room information                 |
| reservations       | Reservation    | Store booking information                    |
| reservation_rooms  | (Join Table)   | Link reservations to multiple rooms          |
| reservation_addons | (Join Table)   | Link reservations to add-on services         |
| addons             | Addon          | Store add-on services (breakfast, parking)   |
| payments           | Payment        | Store payment transactions                   |
| users              | User           | Store admin/manager accounts                 |
| feedbacks          | Feedback       | Store guest reviews after checkout           |
| waitlist           | Waitlist       | Store waitlist entries when rooms unavailable|
| audit_logs         | AuditLog       | Store system activity logs                   |

---


## Database Diagram (Visual)

```
                           ┌─────────────────┐
                           │     GUESTS      │
                           ├─────────────────┤
                           │ PK: id          │
                           │ firstName       │
                           │ lastName        │
                           │ email           │
                           │ phone           │
                           │ loyaltyNumber   │
                           │ loyaltyPoints   │
                           └────────┬────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │ 1:N           │ 1:N           │ 1:N
                    ▼               ▼               ▼
           ┌────────────────┐ ┌──────────┐ ┌─────────────┐
           │  RESERVATIONS  │ │ FEEDBACK │ │  WAITLIST   │
           ├────────────────┤ ├──────────┤ ├─────────────┤
           │ PK: id         │ │ PK: id   │ │ PK: id      │
           │ confirmation#  │ │ FK:guest │ │ FK: guest   │
           │ FK: guest_id   │ │ FK:reserv│ │ roomType    │
           │ checkIn/Out    │ │ rating   │ │ dates       │
           │ adults/children│ │ comment  │ │ notified    │
           │ status         │ └──────────┘ └─────────────┘
           │ pricing fields │
           └───────┬────────┘
                   │
      ┌────────────┼────────────┐
      │            │            │
      │ M:N        │ M:N        │ 1:N
      ▼            ▼            ▼
┌──────────┐ ┌──────────┐ ┌──────────────┐
│  ROOMS   │ │  ADDONS  │ │   PAYMENTS   │
├──────────┤ ├──────────┤ ├──────────────┤
│ PK: id   │ │ PK: id   │ │ PK: id       │
│ roomNum  │ │ name     │ │ FK:reservation│
│ type     │ │ price    │ │ method       │
│ status   │ │ pricing  │ │ amount       │
│ floor    │ │ Model    │ │ date         │
└──────────┘ └──────────┘ └──────────────┘

      ┌──────────────┐     ┌─────────────────┐
      │    USERS     │     │   AUDIT_LOGS    │
      ├──────────────┤     ├─────────────────┤
      │ PK: id       │     │ PK: id          │
      │ username     │     │ timestamp       │
      │ passwordHash │     │ actor           │
      │ role         │     │ action          │
      │ active       │     │ entityType/Id   │
      └──────────────┘     └─────────────────┘
```

---


## Detailed Table Descriptions

### 1. GUESTS Table

**Purpose:** Stores information about hotel guests/customers.

| Column        | Type    | Constraints        | Description                    |
|---------------|---------|--------------------|--------------------------------|
| id            | BIGINT  | PK, AUTO_INCREMENT | Unique guest identifier        |
| firstName     | VARCHAR | NOT NULL           | Guest's first name             |
| lastName      | VARCHAR | NOT NULL           | Guest's last name              |
| email         | VARCHAR | NOT NULL           | Guest's email address          |
| phone         | VARCHAR | NOT NULL           | Guest's phone number           |
| loyaltyNumber | VARCHAR | UNIQUE             | Loyalty program number         |
| loyaltyPoints | INT     |                    | Accumulated loyalty points     |

**JPA Annotations Used:**
- `@Entity` - Marks class as a JPA entity (database table)
- `@Table(name = "guests")` - Specifies the table name
- `@Id` - Marks the primary key
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` - Auto-increment
- `@Column(nullable = false)` - NOT NULL constraint
- `@Column(unique = true)` - UNIQUE constraint

---

### 2. ROOMS Table

**Purpose:** Stores information about hotel rooms.

| Column     | Type    | Constraints           | Description                    |
|------------|---------|-----------------------|--------------------------------|
| id         | BIGINT  | PK, AUTO_INCREMENT    | Unique room identifier         |
| roomNumber | VARCHAR | NOT NULL, UNIQUE      | Room number (e.g., "101")      |
| type       | VARCHAR | NOT NULL              | Room type (SINGLE, DOUBLE, etc)|
| status     | VARCHAR | NOT NULL              | Room status (AVAILABLE, etc)   |
| floor      | INT     |                       | Floor number                   |

**Room Types (Enum Values):**

| Type      | Display Name     | Base Price | Max Occupancy |
|-----------|------------------|------------|---------------|
| SINGLE    | Single Room      | $100/night | 2 guests      |
| DOUBLE    | Double Room      | $150/night | 4 guests      |
| DELUXE    | Deluxe Room      | $200/night | 2 guests      |
| PENTHOUSE | Penthouse Suite  | $350/night | 2 guests      |

**Room Statuses:**
- AVAILABLE - Room can be booked
- OCCUPIED - Guest currently staying
- RESERVED - Room booked for future date
- OUT_OF_SERVICE - Room unavailable (maintenance)

**JPA Annotations Used:**
- `@Enumerated(EnumType.STRING)` - Store enum as text, not number

---

### 3. RESERVATIONS Table

**Purpose:** Stores booking information linking guests to rooms.

| Column             | Type     | Constraints        | Description                    |
|--------------------|----------|--------------------|--------------------------------|
| id                 | BIGINT   | PK, AUTO_INCREMENT | Unique reservation ID          |
| confirmationNumber | VARCHAR  | NOT NULL, UNIQUE   | Customer-facing booking code   |
| guest_id           | BIGINT   | FK, NOT NULL       | References guests.id           |
| checkInDate        | DATE     | NOT NULL           | Check-in date                  |
| checkOutDate       | DATE     | NOT NULL           | Check-out date                 |
| adults             | INT      | NOT NULL           | Number of adult guests         |
| children           | INT      | NOT NULL           | Number of child guests         |
| status             | VARCHAR  | NOT NULL           | Reservation status             |
| subtotal           | DOUBLE   |                    | Room + addon cost before tax   |
| tax                | DOUBLE   |                    | Tax amount (13%)               |
| total              | DOUBLE   |                    | Final total with tax           |
| amountPaid         | DOUBLE   |                    | Amount paid so far             |
| createdAt          | DATETIME |                    | When reservation was created   |

**Reservation Statuses:**
- PENDING - Reservation created, not confirmed
- CONFIRMED - Reservation confirmed
- CHECKED_IN - Guest has arrived
- CHECKED_OUT - Guest has departed
- CANCELLED - Reservation cancelled
- NO_SHOW - Guest didn't arrive

**JPA Annotations Used:**
- `@ManyToOne(fetch = FetchType.EAGER)` - Many reservations can belong to one guest
- `@JoinColumn(name = "guest_id")` - Foreign key column name

---

### 4. RESERVATION_ROOMS Table (Join Table)

**Purpose:** Links reservations to rooms (Many-to-Many relationship).
A reservation can have multiple rooms, and a room can be in multiple reservations (on different dates).

| Column         | Type   | Constraints | Description               |
|----------------|--------|-------------|---------------------------|
| reservation_id | BIGINT | FK          | References reservations.id|
| room_id        | BIGINT | FK          | References rooms.id       |

**JPA Annotation:**
```java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "reservation_rooms",
    joinColumns = @JoinColumn(name = "reservation_id"),
    inverseJoinColumns = @JoinColumn(name = "room_id")
)
private List<Room> rooms;
```

---

### 5. ADDONS Table

**Purpose:** Stores add-on services available for reservations.

| Column       | Type    | Constraints        | Description                   |
|--------------|---------|--------------------|-------------------------------|
| id           | BIGINT  | PK, AUTO_INCREMENT | Unique addon identifier       |
| name         | VARCHAR | NOT NULL           | Addon name                    |
| price        | DOUBLE  | NOT NULL           | Addon price                   |
| pricingModel | VARCHAR | NOT NULL           | PER_NIGHT or PER_RESERVATION  |

**Seed Data:**

| Name              | Price  | Pricing Model    |
|-------------------|--------|------------------|
| Breakfast Buffet  | \$25    | PER_NIGHT        |
| Parking           | \$15    | PER_NIGHT        |
| Premium Wi-Fi     | \$10    | PER_NIGHT        |
| Spa Package       | \$150   | PER_RESERVATION  |
| Airport Shuttle   | \$50    | PER_RESERVATION  |
| Late Checkout     | \$30    | PER_RESERVATION  |

---

### 6. RESERVATION_ADDONS Table (Join Table)

**Purpose:** Links reservations to addons (Many-to-Many relationship).

| Column         | Type   | Constraints | Description               |
|----------------|--------|-------------|---------------------------|
| reservation_id | BIGINT | FK          | References reservations.id|
| addon_id       | BIGINT | FK          | References addons.id      |

---

### 7. PAYMENTS Table

**Purpose:** Stores payment transactions for reservations.

| Column               | Type     | Constraints        | Description                 |
|----------------------|----------|--------------------|-----------------------------|
| id                   | BIGINT   | PK, AUTO_INCREMENT | Unique payment ID           |
| reservation_id       | BIGINT   | FK, NOT NULL       | References reservations.id  |
| paymentMethod        | VARCHAR  | NOT NULL           | CASH, CREDIT_CARD, etc      |
| amount               | DOUBLE   | NOT NULL           | Payment amount              |
| paymentDate          | DATETIME |                    | When payment was made       |
| processedBy          | VARCHAR  |                    | Admin who processed         |
| transactionReference | VARCHAR  |                    | External transaction ID     |

**Payment Methods:**
- CASH
- CREDIT_CARD
- LOYALTY_POINTS

---

### 8. USERS Table

**Purpose:** Stores admin and manager accounts.

| Column       | Type    | Constraints           | Description                |
|--------------|---------|-----------------------|----------------------------|
| id           | BIGINT  | PK, AUTO_INCREMENT    | Unique user ID             |
| username     | VARCHAR | NOT NULL, UNIQUE      | Login username             |
| passwordHash | VARCHAR | NOT NULL              | BCrypt hashed password     |
| role         | VARCHAR | NOT NULL              | ADMIN or MANAGER           |
| active       | BOOLEAN |                       | Account active status      |

**Default Accounts:**

| Username | Password   | Role    |
|----------|------------|---------|
| admin    | admin123   | ADMIN   |
| manager  | manager123 | MANAGER |

---

### 9. FEEDBACKS Table

**Purpose:** Stores guest feedback/reviews after checkout.

| Column         | Type     | Constraints        | Description                |
|----------------|----------|--------------------|-----------------------------|
| id             | BIGINT   | PK, AUTO_INCREMENT | Unique feedback ID          |
| guest_id       | BIGINT   | FK, NOT NULL       | References guests.id        |
| reservation_id | BIGINT   | FK, NOT NULL       | References reservations.id  |
| rating         | INT      | NOT NULL           | 1-5 star rating             |
| comment        | VARCHAR  | (max 1000 chars)   | Guest comment               |
| sentimentTag   | VARCHAR  |                    | Category tag                |
| submittedAt    | DATETIME |                    | When feedback was submitted |

---

### 10. WAITLIST Table

**Purpose:** Stores waitlist entries when desired rooms are unavailable.

| Column          | Type     | Constraints        | Description                 |
|-----------------|----------|--------------------|-----------------------------|
| id              | BIGINT   | PK, AUTO_INCREMENT | Unique waitlist ID          |
| guest_id        | BIGINT   | FK, NOT NULL       | References guests.id        |
| roomType        | VARCHAR  | NOT NULL           | Desired room type           |
| desiredCheckIn  | DATE     | NOT NULL           | Desired check-in date       |
| desiredCheckOut | DATE     | NOT NULL           | Desired check-out date      |
| createdAt       | DATETIME |                    | When added to waitlist      |
| notified        | BOOLEAN  |                    | Has guest been notified     |
| converted       | BOOLEAN  |                    | Converted to reservation    |

---

### 11. AUDIT_LOGS Table

**Purpose:** Stores system activity logs for security and tracking.

| Column     | Type     | Constraints        | Description                 |
|------------|----------|--------------------|-----------------------------|
| id         | BIGINT   | PK, AUTO_INCREMENT | Unique log ID               |
| timestamp  | DATETIME | NOT NULL           | When action occurred        |
| actor      | VARCHAR  | NOT NULL           | Username who did action     |
| action     | VARCHAR  | NOT NULL           | Action type (CREATE, etc)   |
| entityType | VARCHAR  | NOT NULL           | What was affected           |
| entityId   | VARCHAR  |                    | ID of affected entity       |
| message    | VARCHAR  | (max 500 chars)    | Description of action       |

---

## Relationships Summary

### One-to-Many (1:N) Relationships

| Parent       | Child        | Description                              |
|--------------|--------------|------------------------------------------|
| Guest        | Reservation  | One guest can have many reservations     |
| Guest        | Feedback     | One guest can leave many feedbacks       |
| Guest        | Waitlist     | One guest can have many waitlist entries |
| Reservation  | Payment      | One reservation can have many payments   |
| Reservation  | Feedback     | One reservation can have one feedback    |

### Many-to-Many (M:N) Relationships

| Table 1      | Table 2      | Join Table          | Description                    |
|--------------|--------------|---------------------|--------------------------------|
| Reservation  | Room         | reservation_rooms   | Reservation can have many rooms|
| Reservation  | Addon        | reservation_addons  | Reservation can have many addons|

---

## JPA Annotations Reference

| Annotation                              | Purpose                                    |
|-----------------------------------------|--------------------------------------------|
| `@Entity`                               | Marks class as database table              |
| `@Table(name = "...")`                  | Specifies table name                       |
| `@Id`                                   | Marks primary key field                    |
| `@GeneratedValue(IDENTITY)`             | Auto-increment primary key                 |
| `@Column(nullable = false)`             | NOT NULL constraint                        |
| `@Column(unique = true)`                | UNIQUE constraint                          |
| `@Column(length = 1000)`                | VARCHAR length limit                       |
| `@Enumerated(EnumType.STRING)`          | Store enum as text (not ordinal number)    |
| `@ManyToOne`                            | Many-to-one relationship                   |
| `@OneToMany`                            | One-to-many relationship                   |
| `@ManyToMany`                           | Many-to-many relationship                  |
| `@JoinColumn(name = "...")`             | Foreign key column name                    |
| `@JoinTable(...)`                       | Defines join table for M:N relationship    |
| `fetch = FetchType.EAGER`               | Load related data immediately              |
| `fetch = FetchType.LAZY`                | Load related data only when accessed       |

---

### QUICK REFERENCE: Key Points

| Topic | Key Annotation | What to Say |
|-------|----------------|-------------|
| Primary Key | `@Id` + `@GeneratedValue` | "Auto-incrementing unique identifier" |
| Required Field | `@Column(nullable = false)` | "This field cannot be empty" |
| Unique Field | `@Column(unique = true)` | "No two records can have the same value" |
| Enum Storage | `@Enumerated(EnumType.STRING)` | "Stored as readable text, not numbers" |
| Foreign Key | `@ManyToOne` + `@JoinColumn` | "Links to another table" |
| Join Table | `@ManyToMany` + `@JoinTable` | "Creates intermediate table for M:N relationship" |

---