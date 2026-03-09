package com.hotel.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.hotel.app.Main;

/**
 * Controller for all Kiosk screens.
 * Manages the guest self-service booking flow.
 */
public class KioskController {

    // Guest count screen elements
    @FXML private Label adultsLabel;
    @FXML private Label childrenLabel;
    @FXML private Label totalGuestsLabel;
    @FXML private Label errorLabel;

    // Date selection elements
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private Label nightsLabel;
    @FXML private Label guestsSummaryLabel;
    @FXML private Label dateValidationLabel;

    // Booking state (simplified for UI prototype)
    private int adults = 1;
    private int children = 0;

    @FXML
    public void initialize() {
        updateGuestCounts();
    }

    // Navigation methods
    @FXML
    private void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rules & Regulations");
        alert.setHeaderText("NewnhamNexus - Rules & Regulations");
        alert.setContentText(
            "1. Check-in time: 3:00 PM\n" +
            "2. Check-out time: 11:00 AM\n" +
            "3. No smoking in rooms\n" +
            "4. Pets allowed with prior approval\n" +
            "5. Valid ID required at check-in\n" +
            "6. Payment due at check-out\n" +
            "7. Cancellation policy: 24 hours notice"
        );
        alert.showAndWait();
    }

    @FXML
    private void startBooking() {
        loadScreen("/view/kiosk/KioskGuestCount.fxml", "NewnhamNexus - Guest Count");
    }

    @FXML
    private void backToMainMenu() {
        loadScreen("/view/Launcher.fxml", "NewnhamNexus - Reservation System");
    }

    @FXML
    private void backToWelcome() {
        loadScreen("/view/kiosk/KioskWelcome.fxml", "NewnhamNexus - Kiosk");
    }

    @FXML
    private void backToGuestCount() {
        loadScreen("/view/kiosk/KioskGuestCount.fxml", "NewnhamNexus - Guest Count");
    }

    @FXML
    private void backToDateSelection() {
        loadScreen("/view/kiosk/KioskDateSelection.fxml", "NewnhamNexus - Select Dates");
    }

    @FXML
    private void backToRoomSelection() {
        loadScreen("/view/kiosk/KioskRoomSelection.fxml", "NewnhamNexus - Select Room");
    }

    @FXML
    private void backToAddOns() {
        loadScreen("/view/kiosk/KioskAddOns.fxml", "NewnhamNexus - Add-Ons");
    }

    @FXML
    private void backToGuestDetails() {
        loadScreen("/view/kiosk/KioskGuestDetails.fxml", "NewnhamNexus - Guest Details");
    }

    @FXML
    private void goToDateSelection() {
        loadScreen("/view/kiosk/KioskDateSelection.fxml", "NewnhamNexus - Select Dates");
    }

    @FXML
    private void goToRoomSelection() {
        loadScreen("/view/kiosk/KioskRoomSelection.fxml", "NewnhamNexus - Select Room");
    }

    @FXML
    private void goToAddOns() {
        loadScreen("/view/kiosk/KioskAddOns.fxml", "NewnhamNexus - Add-Ons");
    }

    @FXML
    private void skipAddOns() {
        goToGuestDetails();
    }

    @FXML
    private void goToGuestDetails() {
        loadScreen("/view/kiosk/KioskGuestDetails.fxml", "NewnhamNexus - Guest Details");
    }

    @FXML
    private void goToSummary() {
        loadScreen("/view/kiosk/KioskSummary.fxml", "NewnhamNexus - Review Booking");
    }

    @FXML
    private void confirmBooking() {
        loadScreen("/view/kiosk/KioskConfirmation.fxml", "NewnhamNexus - Booking Confirmed");
    }

    @FXML
    private void startNewBooking() {
        loadScreen("/view/kiosk/KioskWelcome.fxml", "NewnhamNexus - Kiosk");
    }

    // Guest count methods
    @FXML
    private void increaseAdults() {
        if (adults < 10) {
            adults++;
            updateGuestCounts();
        }
    }

    @FXML
    private void decreaseAdults() {
        if (adults > 1) {
            adults--;
            updateGuestCounts();
        }
    }

    @FXML
    private void increaseChildren() {
        if (children < 10) {
            children++;
            updateGuestCounts();
        }
    }

    @FXML
    private void decreaseChildren() {
        if (children > 0) {
            children--;
            updateGuestCounts();
        }
    }

    private void updateGuestCounts() {
        if (adultsLabel != null) {
            adultsLabel.setText(String.valueOf(adults));
        }
        if (childrenLabel != null) {
            childrenLabel.setText(String.valueOf(children));
        }
        if (totalGuestsLabel != null) {
            totalGuestsLabel.setText(String.valueOf(adults + children));
        }
    }

    @FXML
    private void showRoomPolicies() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Room Booking Policies");
        alert.setHeaderText("Occupancy Limits");
        alert.setContentText(
            "Single Room: Up to 2 guests\n" +
            "Double Room: Up to 4 guests\n" +
            "Deluxe Room: Up to 2 guests\n" +
            "Penthouse Suite: Up to 2 guests\n\n" +
            "Please ensure your selection meets the occupancy requirements for your group size."
        );
        alert.showAndWait();
    }

    private void loadScreen(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = Main.getPrimaryStage();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load screen");
            alert.setContentText("Error loading " + fxmlPath + ": " + e.getMessage());
            alert.showAndWait();
        }
    }
}
