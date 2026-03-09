package com.hotel.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.hotel.app.Main;

/**
 * Controller for guest feedback screens.
 * Handles feedback submission after checkout.
 */
public class FeedbackController {

    // Feedback form elements
    @FXML private TextField confirmationField;
    @FXML private Label lookupErrorLabel;
    @FXML private VBox guestInfoSection;
    @FXML private VBox ratingSection;
    @FXML private VBox notEligibleSection;
    @FXML private Label guestNameLabel;
    @FXML private Label roomLabel;
    @FXML private Label stayDatesLabel;
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
    }

    @FXML
    private void lookupReservation() {
        String confNum = confirmationField != null ? confirmationField.getText() : "";

        if (confNum.isEmpty()) {
            if (lookupErrorLabel != null) {
                lookupErrorLabel.setText("Please enter a confirmation number");
            }
            return;
        }

        // For UI prototype, simulate successful lookup
        if (lookupErrorLabel != null) lookupErrorLabel.setText("");

        // Show guest info
        if (guestInfoSection != null) {
            guestInfoSection.setVisible(true);
            guestInfoSection.setManaged(true);
        }
        if (guestNameLabel != null) guestNameLabel.setText("John Smith");
        if (roomLabel != null) roomLabel.setText("Double #205");
        if (stayDatesLabel != null) stayDatesLabel.setText("Mar 5-7, 2026");

        // Show rating section
        if (ratingSection != null) {
            ratingSection.setVisible(true);
            ratingSection.setManaged(true);
        }

        // Hide not eligible section
        if (notEligibleSection != null) {
            notEligibleSection.setVisible(false);
            notEligibleSection.setManaged(false);
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

        loadScreen("/view/feedback/FeedbackConfirmation.fxml", "NewnhamNexus - Thank You");
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
