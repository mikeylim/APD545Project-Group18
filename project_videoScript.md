# NewnhamNexus Hotel Reservation System - Video Script

**Group:** #18 | **Course:** APD545 NAA - Winter 2026
**Members:** Dohyun Lim, Phuong Bac Nguyen, Eunki Kim
**Estimated Duration:** 8-10 minutes

---

## VIDEO OUTLINE

| Section | Speaker | Duration | Focus |
|---------|---------|----------|-------|
| 1. Introduction | Dohyun | 30 sec | Project overview |
| 2. Architecture Overview | Dohyun | 1 min | 3-tier architecture, patterns |
| 3. Kiosk Booking Demo | Dohyun | 2.5 min | Presentation + Business tier |
| 4. Admin Login & Dashboard | Phuong | 1 min | Authentication, Data tier |
| 5. Admin Features Demo | Phuong | 2.5 min | CRUD, Payments, Reports |
| 6. Feedback & Waitlist Demo | Eunki | 2 min | Entity relationships |
| 7. Technical Challenges | All | 1.5 min | 30 sec each |
| 8. Conclusion | All | 30 sec | Wrap-up |

---

## SECTION 1: INTRODUCTION (Dohyun - 30 seconds)

### Screen: Show Launcher.fxml (Main Menu)

**[DOHYUN SPEAKS]:**

> "Hello, we are Group 18 presenting the NewnhamNexus Hotel Reservation System. Our team members are Dohyun Lim, Phuong Bac Nguyen, and Eunki Kim.
>
> This is a desktop application built with JavaFX that replaces a manual hotel reservation system with a computerized solution. It features self-service kiosks for guests, an admin management portal, and a feedback system.
>
> Let me start by explaining our architecture."

---

## SECTION 2: ARCHITECTURE OVERVIEW (Dohyun - 1 minute)

### Screen: Show ProjectDocumentation.md or a diagram

**[DOHYUN SPEAKS]:**

> "Our application follows a 3-tier architecture.
>
> **The Presentation Tier** consists of JavaFX FXML views and controllers. We have over 20 screens including kiosk booking flow, admin dashboard, and feedback forms.
>
> **The Business Logic Tier** contains our service classes and design patterns. We implemented five design patterns:"

### Screen: Show code files briefly as you mention each

> "- **Singleton** for EntityManagerFactory and BookingSession
> - **Factory** for creating Room instances in DatabaseInitializer
> - **Strategy** for dynamic pricing - weekday versus weekend rates
> - **Observer** for notifying when rooms become available after checkout
> - **Decorator** for calculating add-on service pricing"

> "**The Data Tier** uses Hibernate ORM with an H2 embedded database. We have 9 entity tables and 2 join tables for many-to-many relationships.
>
> Now let me demonstrate the Kiosk booking flow."

---

## SECTION 3: KIOSK BOOKING DEMO (Dohyun - 2.5 minutes)

### Screen: Launcher.fxml

**[DOHYUN SPEAKS]:**

> "From the main menu, guests can access the self-service kiosk."

### Action: Click "Self-Service Kiosk"

### Screen: KioskWelcome.fxml

> "This is the welcome screen with hotel information. Let's start a new booking."

### Action: Click "Book Now"

### Screen: KioskGuestCount.fxml

> "Step 1: Guest selection. I'll select 2 adults and 1 child."

### Action: Select 2 adults, 1 child, click "Continue"

**Test Data:** 2 Adults, 1 Child

### Screen: KioskDateSelection.fxml

> "Step 2: Date selection. I'll book for April 10th to April 12th - that's a 2-night stay."

### Action: Select check-in April 10, check-out April 12, click "Find Available Rooms"

**Test Data:** Check-in: April 10, 2026 | Check-out: April 12, 2026

### Screen: KioskRoomSelection.fxml

> "Step 3: Room selection. The system suggests a Double Room based on our guest count. Notice the pricing - weekday rate is $150 per night. I'll select this room."

### Action: Click "Select" on Double Room

### Screen: KioskAddOns.fxml

> "Step 4: Add-on services. Here's where the **Decorator Pattern** is used. Each add-on wraps the base booking price. I'll add Breakfast Buffet at $25 per night and Parking at $15 per night."

### Action: Check "Breakfast Buffet" and "Parking", click "Continue"

**Test Data:** Breakfast Buffet ($25/night), Parking ($15/night)

### Screen: KioskGuestDetails.fxml

> "Step 5: Guest details. I'll enter the guest information."

### Action: Fill in the form

**Test Data:**
- First Name: John
- Last Name: Smith
- Email: john.smith@email.com
- Phone: 416-555-1234
- Check "Join Nexus Rewards" checkbox
- Check "Terms and Conditions" checkbox

> "Notice the loyalty program enrollment option. When checked, the guest earns points on this stay."

### Action: Click "Review Booking"

### Screen: KioskSummary.fxml

> "Step 6: Booking summary. The pricing breakdown shows:
> - Room: $300 for 2 nights
> - Add-ons: $80 for breakfast and parking over 2 nights
> - Subtotal: $380
> - Tax at 13%: $49.40
> - Total: $429.40
>
> The **BookingSession singleton** maintains all this data across the 6 screens."

### Action: Click "Confirm Reservation"

### Screen: KioskConfirmation.fxml

> "The reservation is confirmed with a unique confirmation number. This data is now persisted in our H2 database using JPA.
>
> Now I'll hand it over to Phuong to demonstrate the Admin features."

---

## SECTION 4: ADMIN LOGIN & DASHBOARD (Phuong - 1 minute)

### Screen: Launcher.fxml

**[PHUONG SPEAKS]:**

> "Thank you Dohyun. I developed the Admin functionality including authentication, CRUD operations, and reporting. Let me show you the admin portal."

### Action: Click "Admin Portal"

### Screen: AdminLogin.fxml

> "This is the admin login screen. Passwords are hashed using **BCrypt** - we never store plain text passwords."

### Action: Enter credentials

**Test Data:** Username: `manager` | Password: `manager123`

> "I'll log in as a manager, which has higher discount privileges than a regular admin."

### Action: Click "Login"

### Screen: AdminDashboard.fxml

> "The dashboard shows real-time statistics pulled from our database - total reservations, today's check-ins, check-outs, and occupancy rate. This data comes from our **Repository classes** which use JPA queries."

---

## SECTION 5: ADMIN FEATURES DEMO (Phuong - 2.5 minutes)

### Screen: AdminDashboard.fxml

**[PHUONG SPEAKS]:**

> "Let me show the key admin features."

### Action: Click "Reservations" in sidebar

### Screen: AdminReservations.fxml

> "Here's the reservations management screen with full CRUD functionality. You can see the booking we just created for John Smith."

### Action: Point out the new reservation in the table

> "The table is paginated and sortable. I can search by guest name or confirmation number."

### Action: Type "Smith" in search box

> "Let me demonstrate the payment processing."

### Action: Click "Payments" in sidebar

### Screen: AdminPayments.fxml

> "In payments, I can process payments for any reservation. Let me find John Smith's reservation and process a partial payment."

### Action: Select the reservation, enter payment details

**Test Data:**
- Amount: $200.00
- Payment Method: Credit Card
- Click "Process Payment"

> "The system tracks partial payments. The remaining balance is automatically calculated."

### Action: Click "Reports" in sidebar

### Screen: AdminReports.fxml

> "For reporting, I built a **ReportingService** that aggregates data from multiple tables. We can generate Revenue Reports, Occupancy Reports, and Feedback Summaries."

### Action: Select "Revenue Report", set date range, click "Generate"

**Test Data:** Date range: April 1-30, 2026

> "Reports can be exported to CSV for spreadsheet analysis or PDF for printing."

### Action: Click "Export PDF" (show the file being created)

> "The PDF export uses **Apache PDFBox** library. The file is saved to the project directory.
>
> Now I'll hand it over to Eunki to demonstrate the Feedback and Waitlist features."

---

## SECTION 6: FEEDBACK & WAITLIST DEMO (Eunki - 2 minutes)

### Screen: AdminDashboard.fxml

**[EUNKI SPEAKS]:**

> "Thank you Phuong. I was responsible for the Feedback system, Waitlist management, and JPA entity design. Let me demonstrate these features."

### Action: Click "Back to Main Menu" or navigate to Launcher

### Screen: Launcher.fxml

> "First, let me show the guest feedback system."

### Action: Click "Guest Feedback"

### Screen: FeedbackEntry.fxml

> "Guests enter their confirmation number to submit feedback."

### Action: Enter the confirmation number from earlier demo

**Test Data:** Enter the confirmation number shown in Dohyun's demo (e.g., NNX-20260410xxxxx)

### Screen: FeedbackForm.fxml

> "The system validates the confirmation number against our database using a **JPQL query**. Then guests can rate their stay from 1 to 5 stars and leave comments."

### Action: Click 4 stars, enter comment

**Test Data:**
- Rating: 4 stars
- Comment: "Great room and friendly staff. Breakfast was excellent!"

### Action: Click "Submit Feedback"

### Screen: FeedbackConfirmation.fxml

> "The feedback is stored with a sentiment tag automatically assigned based on the rating."

### Action: Go back to Admin Portal, login as admin, navigate to Waitlist

### Screen: AdminWaitlist.fxml

**[EUNKI SPEAKS]:**

> "Now let me show the Waitlist feature. When rooms are unavailable, guests can join a waitlist."

### Action: Click "Add to Waitlist"

**Test Data:**
- Guest: Create or select a guest
- Room Type: Penthouse
- Desired Check-in: April 15, 2026
- Desired Check-out: April 17, 2026

> "The **Observer Pattern** comes into play here. When an admin checks out a guest, a **RoomAvailabilityEvent** is published. Any matching waitlist entries are notified."

### Action: Show the waitlist entry in the table

> "The entity relationships I designed include:
> - Guest to Reservation: One-to-Many
> - Guest to Waitlist: One-to-Many
> - Reservation to Room: Many-to-Many using a join table
> - Reservation to Addon: Many-to-Many using a join table"

---

## SECTION 7: TECHNICAL CHALLENGES (All - 1.5 minutes, 30 seconds each)

### Screen: Can show relevant code or stay on any screen

**[DOHYUN SPEAKS - 30 seconds]:**

> "My biggest challenge was implementing the **Observer Pattern** correctly. Initially, I had the event classes created, but no observers were actually subscribed. The events were being published but nothing was listening. I fixed this by adding observer subscriptions in the AdminController's static initializer, so when a checkout happens, the waitlist is automatically checked for matching entries."

**[PHUONG SPEAKS - 30 seconds]:**

> "My challenge was implementing **role-based discount caps**. Admins can apply up to 15% discount, while managers can apply up to 30%. I had to validate the discount percentage against the current user's role in the checkout screen. If an admin tries to enter 20%, the system shows an error and caps it at 15%. This required checking the authenticated user's role before processing."

**[EUNKI SPEAKS - 30 seconds]:**

> "My challenge was writing the **JPQL query for waitlist matching**. When a room becomes available, I needed to find waitlist entries where the desired dates overlap with the available dates. The query checks if the desired check-in is before the available end date AND the desired check-out is after the available start date. Getting this date logic correct took several attempts."

---

## SECTION 8: CONCLUSION (All - 30 seconds)

### Screen: Launcher.fxml or ProjectDocumentation

**[DOHYUN SPEAKS]:**

> "To summarize, we built a complete hotel reservation system using JavaFX with a 3-tier architecture."

**[PHUONG SPEAKS]:**

> "We implemented five design patterns: Singleton, Factory, Strategy, Observer, and Decorator."

**[EUNKI SPEAKS]:**

> "And we used Hibernate ORM with JPA for database persistence across 9 entity tables."

**[ALL SPEAK TOGETHER or DOHYUN]:**

> "Thank you for watching. This was Group 18 - NewnhamNexus Hotel Reservation System."

---

## TEST DATA SUMMARY

Use this data consistently throughout the demo:

### Kiosk Booking
| Field | Value |
|-------|-------|
| Adults | 2 |
| Children | 1 |
| Check-in | April 10, 2026 |
| Check-out | April 12, 2026 |
| Room | Double Room |
| Add-ons | Breakfast Buffet, Parking |
| First Name | John |
| Last Name | Smith |
| Email | john.smith@email.com |
| Phone | 416-555-1234 |
| Loyalty | Yes (checked) |

### Admin Login
| Account | Username | Password | Use For |
|---------|----------|----------|---------|
| Manager | manager | manager123 | Main demo (higher discount cap) |
| Admin | admin | admin123 | Alternative |

### Payment Processing
| Field | Value |
|-------|-------|
| Amount | $200.00 |
| Method | Credit Card |

### Feedback
| Field | Value |
|-------|-------|
| Confirmation # | (from kiosk booking) |
| Rating | 4 stars |
| Comment | "Great room and friendly staff. Breakfast was excellent!" |

### Waitlist
| Field | Value |
|-------|-------|
| Room Type | Penthouse |
| Check-in | April 15, 2026 |
| Check-out | April 17, 2026 |

### Report Generation
| Field | Value |
|-------|-------|
| Report Type | Revenue Report |
| Date Range | April 1-30, 2026 |

---

## RECORDING TIPS

1. **Before Recording:**
   - Delete the `/data` folder to start with fresh database
   - Run `mvn javafx:run` to start the application
   - Maximize the window to 1400x900 for best visibility

2. **During Recording:**
   - Speak clearly and at a moderate pace
   - Pause briefly when switching screens
   - Point out or highlight important UI elements
   - Keep mouse movements smooth and deliberate

3. **Screen Recording:**
   - Use OBS Studio, QuickTime, or Zoom recording
   - Record at 1080p resolution minimum
   - Include system audio if playing any sounds

4. **Editing:**
   - Trim any long pauses or mistakes
   - Add transitions between sections if desired
   - Ensure audio levels are consistent

---

## BACKUP SCRIPTS (If something goes wrong)

### If database has existing data:
> "As you can see, there's already some test data in the system from our development. Let me create a new booking to demonstrate the flow."

### If an error occurs:
> "Let me restart that section. Sometimes the database connection needs a moment to establish."

### If time is running short:
> "Due to time constraints, I'll briefly show the remaining features..."

---

**END OF SCRIPT**
