# NewnhamNexus Hotel Reservation System - Setup Guide

## Prerequisites

- **Java JDK 17** or later (Java 21 recommended)
- **Maven 3.6+** (for building and running)
- **IntelliJ IDEA** (optional, but recommended)

## Quick Start (Maven)

The easiest way to run the application is using Maven:

```bash
# 1. Clone the repository
git clone <repository-url>
cd Project

# 2. Build the project
mvn clean install

# 3. Run the application
mvn javafx:run
```

That's it! Maven will automatically download all dependencies including JavaFX.

## Default Login Accounts

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |

## Database

- **Type:** H2 Embedded (file-based)
- **Location:** `./data/hoteldb.mv.db`
- **Auto-created:** Tables are created automatically on first run
- **Reset:** Delete the `/data` folder and restart the app

## Project Dependencies (Managed by Maven)

All dependencies are defined in `pom.xml` and downloaded automatically:

| Dependency | Version | Purpose |
|------------|---------|---------|
| JavaFX | 21 | Desktop UI framework |
| Hibernate | 6.4.4 | JPA/ORM implementation |
| H2 Database | 2.2.224 | Embedded database |
| jBCrypt | 0.4 | Password hashing |
| Apache PDFBox | 2.0.31 | PDF report generation |

## Running in IntelliJ IDEA

### Option 1: Using Maven (Recommended)

1. Open IntelliJ IDEA
2. **File → Open** → Select the `Project` folder
3. Wait for Maven to import dependencies
4. Open the Maven tool window (View → Tool Windows → Maven)
5. Navigate to **Plugins → javafx → javafx:run**
6. Double-click to run

### Option 2: Run Configuration

1. **Run → Edit Configurations**
2. Click **+** → **Maven**
3. Set:
   - **Name:** `Run App`
   - **Command line:** `javafx:run`
4. Click **Apply** and **OK**
5. Click the green **Run** button

## Project Structure

```
Project/
├── src/main/java/
│   └── com/hotel/
│       ├── app/           # Main application + DI container
│       ├── controller/    # FXML controllers
│       ├── model/         # JPA entities
│       ├── repository/    # Database access layer
│       ├── service/       # Business logic + patterns
│       ├── events/        # Observer pattern
│       └── util/          # Utilities (PDF export)
├── src/main/resources/
│   ├── META-INF/          # persistence.xml (JPA config)
│   ├── view/              # FXML files
│   │   ├── admin/         # Admin screens
│   │   ├── kiosk/         # Kiosk screens
│   │   └── feedback/      # Feedback screens
│   ├── css/               # Stylesheets
│   └── images/            # Image assets
├── data/                  # H2 database (auto-created)
├── logs/                  # Log files (auto-created)
├── pom.xml                # Maven configuration
└── README.md
```

## Troubleshooting

### "Maven not found"

Install Maven:
- **Mac:** `brew install maven`
- **Windows:** Download from https://maven.apache.org/download.cgi
- **Linux:** `sudo apt install maven`

### "JavaFX runtime components are missing"

This shouldn't happen with Maven, but if it does:
1. Ensure you're using `mvn javafx:run` (not running Main.java directly)
2. Run `mvn clean install` first

### "Could not find or load main class"

1. Check that `pom.xml` has the correct main class: `com.hotel.app.Main`
2. Run `mvn clean install` to rebuild

### Database errors

1. Delete the `/data` folder
2. Restart the application
3. Tables will be recreated automatically

### Build fails

1. Ensure Java 17+ is installed: `java -version`
2. Ensure Maven is installed: `mvn -version`
3. Try: `mvn clean install -U` (force update dependencies)

### Port already in use (H2 database)

If you see "Database may be already in use" error:
1. Close any other instances of the application
2. Delete `/data` folder and restart

## Alternative: Manual JavaFX Setup (Without Maven)

If you need to run without Maven:

1. Download JavaFX SDK from https://gluonhq.com/products/javafx/
2. Extract to a known location
3. In IntelliJ, add VM options:
   ```
   --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
   ```

## Team Information

**Group:** #18
**Course:** APD545 NAA - Winter 2026
**Members:** Dohyun Lim, Phuong Bac Nguyen, Eunki Kim

## Need Help?

- Check `PROJECT_GUIDE.md` for architecture details
- Check `DATABASE_DESIGN.md` for database schema
- Check `ProjectDocumentation.md` for full documentation
