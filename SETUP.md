# NewnhamNexus Hotel Reservation System - Setup Guide

## Prerequisites

- **Java JDK 21** or later
- **IntelliJ IDEA** (Community or Ultimate)
- **JavaFX SDK 25.0.2** (or compatible version)

## Step 1: Download JavaFX SDK

1. Go to [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)
2. Download **JavaFX SDK 25.0.2** (or latest LTS) for your operating system
3. Extract to a location you'll remember (e.g., `~/development/javafx-sdk-25.0.2/`)
4. Note the path to the `lib` folder (e.g., `~/development/javafx-sdk-25.0.2/lib`)

## Step 2: Clone the Repository

```bash
git clone <repository-url>
cd Project
```

## Step 3: Open in IntelliJ IDEA

1. Open IntelliJ IDEA
2. **File → Open** → Select the `Project` folder
3. Wait for IntelliJ to index the project

## Step 4: Configure JavaFX Library

1. **File → Project Structure** (or `Cmd+;` on Mac, `Ctrl+Alt+Shift+S` on Windows)
2. Go to **Libraries** (under Project Settings)
3. Click **+** → **Java**
4. Navigate to your JavaFX SDK `lib` folder and select it
5. Click **OK** to add all the JAR files
6. Click **Apply** and **OK**

## Step 5: Configure Run Configuration

1. **Run → Edit Configurations**
2. Click **+** → **Application**
3. Set the following:
   - **Name**: `Main`
   - **Main class**: `com.hotel.app.Main`
   - **Module**: Select your project module
4. Click **Modify options** → **Add VM options**
5. In the **VM options** field, add:

```
--module-path /path/to/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml
```

**Replace `/path/to/javafx-sdk-25.0.2/lib` with your actual JavaFX lib path!**

Example paths:
- **Mac**: `/Users/yourname/development/javafx-sdk-25.0.2/lib`
- **Windows**: `C:\Users\yourname\development\javafx-sdk-25.0.2\lib`

6. Click **Apply** and **OK**

## Step 6: Run the Application

1. Click the green **Run** button or press `Shift+F10`
2. The NewnhamNexus Reservation System should launch

## Troubleshooting

### "Error: JavaFX runtime components are missing"
- Make sure VM options are set correctly in Run Configuration
- Verify the path to JavaFX lib folder is correct

### "Could not find or load main class"
- Check that Main class is set to `com.hotel.app.Main`
- Verify Project Structure → Modules → Sources has `src/main/java` marked as Sources

### "Module not found" errors
- Ensure JavaFX library is added in Project Structure → Libraries
- Verify all JavaFX JARs are included

### Styling not applied
- Do **Build → Rebuild Project** to ensure resources are copied
- Delete the `out` folder and rebuild if issues persist

## Project Structure

```
Project/
├── src/main/java/
│   └── com/hotel/
│       ├── app/           # Main application entry point
│       ├── controller/    # FXML controllers
│       ├── model/         # Data models (TO BE IMPLEMENTED)
│       ├── repository/    # Database access (TO BE IMPLEMENTED)
│       ├── service/       # Business logic (TO BE IMPLEMENTED)
│       └── ...
├── src/main/resources/
│   ├── view/              # FXML files
│   │   ├── admin/         # Admin screens
│   │   ├── kiosk/         # Kiosk screens
│   │   └── feedback/      # Feedback screens
│   ├── css/               # Stylesheets
│   └── images/            # Image assets
└── README.md
```

## Need Help?

Contact your team members or refer to the PROJECT_GUIDE.md for architecture details.
