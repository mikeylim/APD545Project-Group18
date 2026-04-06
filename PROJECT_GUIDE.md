# NewnhamNexus Hotel Reservation System - Project Guide

## Architecture Overview

This project follows the **MVC (Model-View-Controller)** architecture pattern with a **3-tier structure**:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  (FXML Views + CSS Styling + Controllers)                   │
├─────────────────────────────────────────────────────────────┤
│                    BUSINESS LOGIC LAYER                      │
│  (Services + Design Patterns)                               │
├─────────────────────────────────────────────────────────────┤
│                    DATA ACCESS LAYER                         │
│  (Repositories + JPA Entities + H2 Database)                │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure

```
Project/
├── src/main/java/com/hotel/
│   ├── app/
│   │   ├── Main.java                    # Application entry point
│   │   ├── AppContext.java              # Dependency injection container
│   │   ├── DatabaseInitializer.java     # Seeds initial data
│   │   └── EntityManagerFactoryProvider.java  # Singleton EMF
│   ├── controller/
│   │   ├── LauncherController.java      # Main menu controller
│   │   ├── AdminController.java         # Admin dashboard controller
│   │   ├── KioskController.java         # Self-service kiosk controller
│   │   └── FeedbackController.java      # Feedback system controller
│   ├── model/
│   │   ├── Guest.java                   # Guest entity
│   │   ├── Room.java                    # Room entity
│   │   ├── Reservation.java             # Reservation entity
│   │   ├── Payment.java                 # Payment entity
│   │   ├── Addon.java                   # Add-on service entity
│   │   ├── User.java                    # Admin/Manager account entity
│   │   ├── Feedback.java                # Guest feedback entity
│   │   ├── Waitlist.java                # Waitlist entry entity
│   │   ├── AuditLog.java                # Activity log entity
│   │   └── enums/                       # RoomType, RoomStatus, etc.
│   ├── repository/
│   │   ├── GenericRepository.java       # Base CRUD operations
│   │   ├── RoomRepository.java
│   │   ├── GuestRepository.java
│   │   ├── ReservationRepository.java
│   │   ├── PaymentRepository.java
│   │   ├── AddonRepository.java
│   │   ├── UserRepository.java
│   │   ├── FeedbackRepository.java
│   │   ├── WaitlistRepository.java
│   │   └── AuditLogRepository.java
│   ├── service/
│   │   ├── BookingSession.java          # Kiosk state singleton
│   │   ├── ReservationService.java      # Booking logic
│   │   ├── PricingService.java          # Pricing calculations
│   │   ├── RoomFactory.java             # Factory Pattern
│   │   ├── PricingStrategy.java         # Strategy interface
│   │   ├── StandardPricingStrategy.java # Weekday pricing
│   │   ├── WeekendPricingStrategy.java  # Weekend pricing (1.25x)
│   │   ├── BookingDecorator.java        # Decorator Pattern
│   │   ├── LoyaltyService.java          # Loyalty points management
│   │   ├── WaitlistService.java         # Waitlist management
│   │   ├── PaymentService.java          # Payment processing
│   │   ├── FeedbackService.java         # Feedback management
│   │   ├── ReportingService.java        # Report generation
│   │   ├── AuditService.java            # Activity logging
│   │   └── AuthService.java             # Authentication
│   ├── events/                          # Observer Pattern
│   │   ├── RoomAvailabilityEvent.java
│   │   ├── RoomAvailabilityNotifier.java
│   │   └── RoomAvailabilityObserver.java
│   └── util/
│       └── PdfExportUtil.java           # PDF generation
│
├── src/main/resources/
│   ├── META-INF/
│   │   └── persistence.xml              # JPA configuration
│   ├── view/
│   │   ├── Launcher.fxml                # Main menu screen
│   │   ├── admin/                       # Admin screens (10 FXML files)
│   │   ├── kiosk/                       # Kiosk screens (8 FXML files)
│   │   └── feedback/                    # Feedback screens (3 FXML files)
│   ├── css/
│   │   └── styles.css                   # Global stylesheet
│   └── images/
│       └── hotel-main.jpg               # Hotel image for kiosk
│
├── data/                                # H2 database files (auto-created)
├── logs/                                # Rotating log files
├── pom.xml                              # Maven configuration
├── README.md                            # Project overview
├── SETUP.md                             # Setup instructions
├── PROJECT_GUIDE.md                     # This file
├── DATABASE_DESIGN.md                   # Database documentation
└── ProjectDocumentation.md              # Full project documentation
```

## Module Breakdown

### 1. Kiosk Module (Self-Service Booking)

**Controller:** `KioskController.java`

**Screens (Booking Flow):**
1. `KioskWelcome.fxml` - Welcome screen with hotel intro
2. `KioskGuestCount.fxml` - Select number of guests
3. `KioskDateSelection.fxml` - Choose check-in/check-out dates
4. `KioskRoomSelection.fxml` - Browse and select rooms
5. `KioskAddOns.fxml` - Add services (breakfast, parking, etc.)
6. `KioskGuestDetails.fxml` - Enter guest information
7. `KioskSummary.fxml` - Review booking details
8. `KioskConfirmation.fxml` - Booking confirmation

### 2. Admin Module (Staff Dashboard)

**Controller:** `AdminController.java`

**Screens:**
- `AdminLogin.fxml` - Staff login (BCrypt authentication)
- `AdminDashboard.fxml` - Overview with stats
- `AdminReservations.fxml` - Manage bookings (CRUD)
- `AdminGuests.fxml` - Guest management
- `AdminPayments.fxml` - Payment processing
- `AdminCheckout.fxml` - Guest checkout with balance settlement
- `AdminWaitlist.fxml` - Waitlist management
- `AdminFeedback.fxml` - View guest feedback
- `AdminLoyalty.fxml` - Loyalty program management
- `AdminReports.fxml` - Generate reports (CSV/PDF export)

### 3. Feedback Module

**Controller:** `FeedbackController.java`

**Screens:**
- `FeedbackEntry.fxml` - Enter confirmation number
- `FeedbackForm.fxml` - Submit rating and comments
- `FeedbackConfirmation.fxml` - Thank you screen

## Design Patterns Implemented

| Pattern | Implementation | Purpose |
|---------|----------------|---------|
| **Singleton** | `EntityManagerFactoryProvider`, `LoyaltyService`, `AuditService`, `BookingSession` | Single instance management |
| **Factory** | `RoomFactory`, `RoomPlanFactory` | Create room instances |
| **Strategy** | `PricingStrategy`, `StandardPricingStrategy`, `WeekendPricingStrategy` | Dynamic pricing |
| **Observer** | `RoomAvailabilityNotifier`, `RoomAvailabilityObserver` | Room availability notifications |
| **Decorator** | `BookingDecorator`, `ServiceDecorator` | Add services to booking pricing |

## Key Technical Details

### Database Configuration

- **Type:** H2 Embedded (file-based)
- **Location:** `./data/hoteldb.mv.db`
- **ORM:** Hibernate 6.4.4 with JPA
- **Schema:** Auto-generated from entities

### FXML Special Character Escaping

In FXML files, special characters must be escaped:
- Dollar sign: `\$` (e.g., `text="\$150.00"`)
- Percent sign: `\%` (e.g., `text="15\% off"`)
- Ampersand: `&amp;` (e.g., `text="Rules &amp; Regulations"`)

### CSS Style Classes

Common style classes defined in `styles.css`:
- `.main-container` - Root container styling
- `.header` - Page header
- `.sidebar` - Admin sidebar navigation
- `.sidebar-item` - Sidebar menu items
- `.sidebar-item-active` - Active menu item
- `.card` - Content cards
- `.btn-primary` - Primary action buttons
- `.btn-secondary` - Secondary buttons
- `.btn-success` - Success/confirm buttons
- `.form-field` - Input fields
- `.form-label` - Form labels
- `.kiosk-step-indicator` - Kiosk progress steps
- `.kiosk-step-indicator-active` - Current step
- `.kiosk-step-indicator-completed` - Completed step

### Navigation Pattern

Controllers use a common `loadScreen()` method:
```java
private void loadScreen(String fxmlPath, String title) {
    try {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) someNode.getScene().getWindow();
        stage.setScene(new Scene(root, 1400, 900));
        stage.setTitle(title);
    } catch (IOException e) {
        // Show error alert
    }
}
```

## Implementation Status

### Milestone 1: UI Prototype - COMPLETE
- [x] Launcher screen
- [x] All Kiosk screens (8 screens)
- [x] All Admin screens (10 screens)
- [x] Feedback screens (3 screens)
- [x] CSS styling
- [x] Navigation between screens

### Milestone 2: Backend - COMPLETE
- [x] JPA Entity classes (Guest, Reservation, Room, Payment, etc.)
- [x] H2 Database setup with Hibernate ORM
- [x] Repository classes for data access
- [x] Service classes for business logic
- [x] Factory Pattern for room creation
- [x] Strategy Pattern for pricing

### Final Submission: Full Integration - COMPLETE
- [x] Connect UI to backend
- [x] Form validation
- [x] Data persistence
- [x] Session management
- [x] Observer Pattern for room availability
- [x] Decorator Pattern for add-on pricing
- [x] BCrypt password hashing
- [x] Rotating file logs
- [x] Report generation (CSV/PDF)
- [x] Loyalty program
- [x] Waitlist management

## Running the Application

### Using Maven (Recommended)

```bash
# Build the project
mvn clean install

# Run the application
mvn javafx:run
```

### Default Login Accounts

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |

## Common Issues & Solutions

### "JavaFX runtime components are missing"
- Use `mvn javafx:run` to run with Maven
- Or ensure VM options are set with `--module-path` and `--add-modules`

### "Could not find or load main class"
- Check Main class is `com.hotel.app.Main`
- Run `mvn clean install` first

### Database issues
- Delete the `/data` folder and restart to reset database
- Tables are auto-created on first run

### Styling not applied
- Run `mvn clean install` to ensure resources are copied
- Delete `target` folder and rebuild if issues persist

## Team Information

**Group:** #18
**Course:** APD545 NAA - Winter 2026
**Members:** Dohyun Lim, Phuong Bac Nguyen, Eunki Kim

## Documentation

| Document | Description |
|----------|-------------|
| `README.md` | Project overview and quick start |
| `SETUP.md` | Detailed setup instructions |
| `PROJECT_GUIDE.md` | This file - architecture and structure |
| `DATABASE_DESIGN.md` | Database schema and JPA documentation |
| `ProjectDocumentation.md` | Full project documentation |
