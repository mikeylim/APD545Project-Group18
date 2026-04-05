package com.hotel.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.hotel.app.Main;
import com.hotel.model.Feedback;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.hotel.model.Room;
import com.hotel.service.FeedbackService;
import com.hotel.service.ReservationService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for guest feedback screens.
 * Handles feedback submission after checkout.
 */
public class FeedbackController {

    // Services
    private final ReservationService reservationService = new ReservationService();
    private final FeedbackService feedbackService = new FeedbackService();

    // Lookup elements
    @FXML private RadioButton lookupByConfirmation;
    @FXML private RadioButton lookupByPhone;
    @FXML private ToggleGroup lookupToggleGroup;
    @FXML private TextField lookupField;
    @FXML private Label lookupErrorLabel;

    // Reservation selection (for multiple reservations)
    @FXML private VBox reservationSelectionSection;
    @FXML private ComboBox<String> reservationComboBox;
    private List<Reservation> foundReservations = new ArrayList<>();

    // Guest info section
    @FXML private VBox guestInfoSection;
    @FXML private Label guestNameLabel;
    @FXML private Label roomLabel;
    @FXML private Label stayDatesLabel;

    // Rating section
    @FXML private VBox ratingSection;
    @FXML private VBox notEligibleSection;
    @FXML private TextArea commentsArea;
    @FXML private Label charCountLabel;
    @FXML private Label ratingLabel;

    // Star rating buttons
    @FXML private Button star1;
    @FXML private Button star2;
    @FXML private Button star3;
    @FXML private Button star4;
    @FXML private Button star5;

    // Tag toggles
    @FXML private ToggleButton tagCleanliness;
    @FXML private ToggleButton tagComfort;
    @FXML private ToggleButton tagService;
    @FXML private ToggleButton tagLocation;
    @FXML private ToggleButton tagValue;

    private int currentRating = 0;
    private Reservation currentReservation = null;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    public void initialize() {
        // Add character count listener if commentsArea exists
        if (commentsArea != null && charCountLabel != null) {
            commentsArea.textProperty().addListener((obs, oldVal, newVal) -> {
                int length = newVal != null ? newVal.length() : 0;
                charCountLabel.setText(length + "/500 characters");
                if (length > 500) {
                    commentsArea.setText(newVal.substring(0, 500));
                }
            });
        }

        // Update placeholder based on lookup method selection
        if (lookupByConfirmation != null && lookupByPhone != null && lookupField != null) {
            lookupByConfirmation.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    lookupField.setPromptText("e.g., NNX-2026030901");
                }
            });
            lookupByPhone.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    lookupField.setPromptText("e.g., 416-555-1234");
                }
            });
        }
    }

    @FXML
    private void lookupReservation() {
        String searchValue = lookupField != null ? lookupField.getText().trim() : "";

        if (searchValue.isEmpty()) {
            showError("Please enter a confirmation number or phone number");
            return;
        }

        clearError();
        currentReservation = null;
        foundReservations.clear();

        // Determine lookup method
        boolean byPhone = lookupByPhone != null && lookupByPhone.isSelected();

        if (byPhone) {
            // Lookup by phone - find all checked-out reservations without feedback
            List<Reservation> allReservations = reservationService.findCheckedOutByPhone(searchValue);

            // Filter to only those without feedback
            for (Reservation r : allReservations) {
                if (!feedbackService.getFeedbackByReservation(r).isPresent()) {
                    foundReservations.add(r);
                }
            }

            if (foundReservations.isEmpty()) {
                if (allReservations.isEmpty()) {
                    showError("No checked-out reservations found for this phone number");
                } else {
                    showError("Feedback has already been submitted for all your stays");
                }
                hideAllSections();
                return;
            }

            // If multiple reservations found, show selection
            if (foundReservations.size() > 1) {
                showReservationSelection();
                return;
            }

            // Single reservation found
            proceedWithReservation(foundReservations.get(0));

        } else {
            // Lookup by confirmation number
            Reservation reservation = reservationService.findByConfirmationNumber(searchValue);

            if (reservation == null) {
                showError("Reservation not found. Please check your confirmation number.");
                hideAllSections();
                return;
            }

            proceedWithReservation(reservation);
        }
    }

    private void showReservationSelection() {
        hideAllSections();

        if (reservationSelectionSection != null) {
            reservationSelectionSection.setVisible(true);
            reservationSelectionSection.setManaged(true);
        }

        if (reservationComboBox != null) {
            reservationComboBox.getItems().clear();
            for (Reservation r : foundReservations) {
                // Format: "Mar 5-7, 2026 | DOUBLE #205 | Conf: NNX-123"
                StringBuilder display = new StringBuilder();
                display.append(r.getCheckInDate().format(DATE_FORMATTER))
                       .append(" - ")
                       .append(r.getCheckOutDate().format(DATE_FORMATTER))
                       .append(" | ");

                // Room info
                for (Room room : r.getRooms()) {
                    display.append(room.getType().name()).append(" #").append(room.getRoomNumber()).append(" ");
                }
                display.append("| Conf: ").append(r.getConfirmationNumber());

                reservationComboBox.getItems().add(display.toString());
            }
        }
    }

    @FXML
    private void selectReservation() {
        if (reservationComboBox == null || reservationComboBox.getSelectionModel().isEmpty()) {
            showError("Please select a stay from the list");
            return;
        }

        int selectedIndex = reservationComboBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < foundReservations.size()) {
            Reservation selected = foundReservations.get(selectedIndex);
            proceedWithReservation(selected);
        }
    }

    private void proceedWithReservation(Reservation reservation) {
        // Hide selection section if visible
        if (reservationSelectionSection != null) {
            reservationSelectionSection.setVisible(false);
            reservationSelectionSection.setManaged(false);
        }

        // Check if reservation is checked out
        if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            showNotEligible("Feedback can only be submitted after check-out.");
            return;
        }

        // Check if balance is fully paid
        if (!reservation.isFullyPaid()) {
            showNotEligible("Please settle your outstanding balance before submitting feedback.");
            return;
        }

        // Check if feedback already submitted
        if (feedbackService.getFeedbackByReservation(reservation).isPresent()) {
            showError("Feedback has already been submitted for this reservation.");
            hideAllSections();
            return;
        }

        // All validations passed - show the feedback form
        currentReservation = reservation;
        showGuestInfo(reservation);
        showRatingSection();
    }

    private void showGuestInfo(Reservation reservation) {
        if (guestInfoSection != null) {
            guestInfoSection.setVisible(true);
            guestInfoSection.setManaged(true);
        }

        if (guestNameLabel != null) {
            guestNameLabel.setText(reservation.getGuest().getFullName());
        }

        if (roomLabel != null) {
            // Format room info
            StringBuilder roomInfo = new StringBuilder();
            for (Room room : reservation.getRooms()) {
                if (roomInfo.length() > 0) roomInfo.append(", ");
                roomInfo.append(room.getType().name()).append(" #").append(room.getRoomNumber());
            }
            roomLabel.setText(roomInfo.toString());
        }

        if (stayDatesLabel != null) {
            String dates = reservation.getCheckInDate().format(DATE_FORMATTER) + " - " +
                          reservation.getCheckOutDate().format(DATE_FORMATTER);
            stayDatesLabel.setText(dates);
        }
    }

    private void showRatingSection() {
        if (ratingSection != null) {
            ratingSection.setVisible(true);
            ratingSection.setManaged(true);
        }
        if (notEligibleSection != null) {
            notEligibleSection.setVisible(false);
            notEligibleSection.setManaged(false);
        }
    }

    private void showNotEligible(String message) {
        if (notEligibleSection != null) {
            notEligibleSection.setVisible(true);
            notEligibleSection.setManaged(true);
            // Update message if there's a label for it
        }
        if (ratingSection != null) {
            ratingSection.setVisible(false);
            ratingSection.setManaged(false);
        }
        if (guestInfoSection != null) {
            guestInfoSection.setVisible(false);
            guestInfoSection.setManaged(false);
        }
    }

    private void hideAllSections() {
        if (reservationSelectionSection != null) {
            reservationSelectionSection.setVisible(false);
            reservationSelectionSection.setManaged(false);
        }
        if (guestInfoSection != null) {
            guestInfoSection.setVisible(false);
            guestInfoSection.setManaged(false);
        }
        if (ratingSection != null) {
            ratingSection.setVisible(false);
            ratingSection.setManaged(false);
        }
        if (notEligibleSection != null) {
            notEligibleSection.setVisible(false);
            notEligibleSection.setManaged(false);
        }
    }

    private void showError(String message) {
        if (lookupErrorLabel != null) {
            lookupErrorLabel.setText(message);
            lookupErrorLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private void clearError() {
        if (lookupErrorLabel != null) {
            lookupErrorLabel.setText("");
        }
    }

    // Star rating methods
    @FXML private void setRating1() { setRating(1); }
    @FXML private void setRating2() { setRating(2); }
    @FXML private void setRating3() { setRating(3); }
    @FXML private void setRating4() { setRating(4); }
    @FXML private void setRating5() { setRating(5); }

    private void setRating(int rating) {
        currentRating = rating;
        updateStarDisplay();
        updateRatingLabel();
    }

    private void updateStarDisplay() {
        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            if (stars[i] != null) {
                if (i < currentRating) {
                    stars[i].setStyle("-fx-background-color: transparent; -fx-font-size: 40px; -fx-text-fill: #f39c12;");
                } else {
                    stars[i].setStyle("-fx-background-color: transparent; -fx-font-size: 40px; -fx-text-fill: #bdc3c7;");
                }
            }
        }
    }

    private void updateRatingLabel() {
        if (ratingLabel == null) return;
        switch (currentRating) {
            case 1: ratingLabel.setText("Poor"); break;
            case 2: ratingLabel.setText("Fair"); break;
            case 3: ratingLabel.setText("Good"); break;
            case 4: ratingLabel.setText("Very Good"); break;
            case 5: ratingLabel.setText("Excellent"); break;
            default: ratingLabel.setText("Click to rate"); break;
        }
    }

    @FXML
    private void submitFeedback() {
        if (currentRating == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Rating Required");
            alert.setHeaderText("Please select a rating");
            alert.setContentText("You must select a star rating before submitting feedback.");
            alert.showAndWait();
            return;
        }

        if (currentReservation == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No reservation selected");
            alert.setContentText("Please look up your reservation first.");
            alert.showAndWait();
            return;
        }

        // Collect sentiment tags
        List<String> tags = new ArrayList<>();
        if (tagCleanliness != null && tagCleanliness.isSelected()) tags.add("Cleanliness");
        if (tagComfort != null && tagComfort.isSelected()) tags.add("Comfort");
        if (tagService != null && tagService.isSelected()) tags.add("Service");
        if (tagLocation != null && tagLocation.isSelected()) tags.add("Location");
        if (tagValue != null && tagValue.isSelected()) tags.add("Value");
        String sentimentTags = String.join(",", tags);

        // Get comments
        String comments = commentsArea != null ? commentsArea.getText() : "";

        try {
            // Submit feedback to database
            feedbackService.submitFeedback(currentReservation, currentRating, comments, sentimentTags);

            // Navigate to confirmation screen
            loadScreen("/view/feedback/FeedbackConfirmation.fxml", "NewnhamNexus - Thank You");

        } catch (IllegalStateException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Submission Failed");
            alert.setHeaderText("Could not submit feedback");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred");
            alert.setContentText("Failed to submit feedback: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void backToLauncher() {
        loadScreen("/view/Launcher.fxml", "NewnhamNexus - Reservation System");
    }

    @FXML
    private void newFeedback() {
        loadScreen("/view/feedback/GuestFeedback.fxml", "NewnhamNexus - Feedback");
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
