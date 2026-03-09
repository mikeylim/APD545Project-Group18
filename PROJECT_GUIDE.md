# NewnhamNexus Hotel Reservation System - Project Guide

## Architecture Overview

This project follows the **MVC (Model-View-Controller)** architecture pattern with a **3-tier structure**:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  (FXML Views + CSS Styling)                                 │
├─────────────────────────────────────────────────────────────┤
│                    BUSINESS LOGIC LAYER                      │
│  (Controllers + Services)                                    │
├─────────────────────────────────────────────────────────────┤
│                    DATA ACCESS LAYER                         │
│  (Repositories + Models)                                     │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure

```
Project/
├── src/main/java/com/hotel/
│   ├── app/
│   │   └── Main.java              # Application entry point
│   ├── controller/
│   │   ├── LauncherController.java    # Main menu controller
│   │   ├── AdminController.java       # Admin dashboard controller
│   │   ├── KioskController.java       # Self-service kiosk controller
│   │   └── FeedbackController.java    # Feedback system controller
│   ├── model/                     # Data models (TO BE IMPLEMENTED)
│   ├── repository/                # Database access (TO BE IMPLEMENTED)
│   └── service/                   # Business logic (TO BE IMPLEMENTED)
│
├── src/main/resources/
│   ├── view/
│   │   ├── Launcher.fxml          # Main menu screen
│   │   ├── admin/                 # Admin screens (11 FXML files)
│   │   ├── kiosk/                 # Kiosk screens (8 FXML files)
│   │   └── feedback/              # Feedback screens (2 FXML files)
│   ├── css/
│   │   └── styles.css             # Global stylesheet
│   └── images/
│       └── hotel-main.jpg         # Hotel image for kiosk
│
├── SETUP.md                       # Setup instructions
├── PROJECT_GUIDE.md               # This file
└── .gitignore                     # Git ignore rules
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
- `AdminLogin.fxml` - Staff login
- `AdminDashboard.fxml` - Overview with stats
- `AdminReservations.fxml` - Manage bookings
- `AdminGuests.fxml` - Guest management
- `AdminPayments.fxml` - Payment processing
- `AdminCheckout.fxml` - Guest checkout
- `AdminWaitlist.fxml` - Waitlist management
- `AdminFeedback.fxml` - View guest feedback
- `AdminLoyalty.fxml` - Loyalty program management
- `AdminReports.fxml` - Generate reports

### 3. Feedback Module

**Controller:** `FeedbackController.java`

**Screens:**
- `GuestFeedback.fxml` - Submit feedback form
- `FeedbackConfirmation.fxml` - Thank you screen

## Key Technical Details

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

### Milestone 1: UI Prototype (CURRENT)
- [x] Launcher screen
- [x] All Kiosk screens (8 screens)
- [x] All Admin screens (11 screens)
- [x] Feedback screens (2 screens)
- [x] CSS styling
- [x] Navigation between screens

### Milestone 2: Backend (TO BE IMPLEMENTED)
- [ ] Model classes (Guest, Reservation, Room, Payment, etc.)
- [ ] Database setup (SQLite or MySQL)
- [ ] Repository classes for data access
- [ ] Service classes for business logic

### Milestone 3: Integration (TO BE IMPLEMENTED)
- [ ] Connect UI to backend
- [ ] Form validation
- [ ] Data persistence
- [ ] Session management

## Adding New Screens

1. Create FXML file in appropriate folder (`view/admin/`, `view/kiosk/`, etc.)
2. Set the controller in FXML: `fx:controller="com.hotel.controller.YourController"`
3. Add navigation method in controller
4. Add button/link to navigate to new screen

## Common Issues & Solutions

### "JavaFX runtime components are missing"
- Ensure VM options are set with `--module-path` and `--add-modules`

### "Could not find or load main class"
- Check Main class is `com.hotel.app.Main`
- Verify `src/main/java` is marked as Sources in Project Structure

### Styling not applied
- Run Build → Rebuild Project
- Delete `out` folder and rebuild

### "Invalid path" or "Missing expression" errors
- Check for unescaped `$` or `%` characters in FXML
- Use `\$` for dollar signs, `\%` for percent signs

## Team Responsibilities (Suggested)

| Module | Primary Developer | Backup |
|--------|------------------|--------|
| Kiosk UI | | |
| Admin UI | | |
| Feedback UI | | |
| Models | | |
| Repositories | | |
| Services | | |
| Testing | | |

## Git Workflow

1. Always pull before starting work: `git pull origin main`
2. Create feature branches: `git checkout -b feature/your-feature`
3. Make small, focused commits
4. Push and create pull request for review
5. Merge after approval

## Contact

For questions or issues, reach out to team members or refer to SETUP.md.
