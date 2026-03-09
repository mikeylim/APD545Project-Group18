package com.hotel.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.hotel.app.Main;

/**
 * Controller for all Admin screens.
 * Manages administrator functions including reservations, payments, checkout, etc.
 */
public class AdminController {

    // Login elements
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label loginErrorLabel;

    // Dashboard elements
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label dateTimeLabel;
    @FXML private TableView<?> recentReservationsTable;

    // Reservation search elements
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker fromDateFilter;
    @FXML private DatePicker toDateFilter;

    @FXML
    public void initialize() {
        // Initialize user info labels if present
        if (userNameLabel != null) {
            userNameLabel.setText("John Admin");
        }
        if (userRoleLabel != null) {
            userRoleLabel.setText("Administrator");
        }
    }

    // Authentication methods
    @FXML
    private void handleLogin() {
        // For UI prototype, just navigate to dashboard
        loadScreen("/view/admin/AdminDashboard.fxml", "NewnhamNexus - Dashboard");
    }

    @FXML
    private void handleLogout() {
        loadScreen("/view/Launcher.fxml", "NewnhamNexus - Reservation System");
    }

    @FXML
    private void backToLauncher() {
        loadScreen("/view/Launcher.fxml", "NewnhamNexus - Reservation System");
    }

    // Navigation methods - Sidebar
    @FXML
    private void showDashboard() {
        loadScreen("/view/admin/AdminDashboard.fxml", "NewnhamNexus - Dashboard");
    }

    @FXML
    private void showReservations() {
        loadScreen("/view/admin/AdminReservations.fxml", "NewnhamNexus - Reservations");
    }

    @FXML
    private void showGuests() {
        loadScreen("/view/admin/AdminGuests.fxml", "NewnhamNexus - Guests");
    }

    @FXML
    private void showPayments() {
        loadScreen("/view/admin/AdminPayments.fxml", "NewnhamNexus - Payments");
    }

    @FXML
    private void showCheckout() {
        loadScreen("/view/admin/AdminCheckout.fxml", "NewnhamNexus - Check-out");
    }

    @FXML
    private void showWaitlist() {
        loadScreen("/view/admin/AdminWaitlist.fxml", "NewnhamNexus - Waitlist");
    }

    @FXML
    private void showFeedbackManagement() {
        loadScreen("/view/admin/AdminFeedback.fxml", "NewnhamNexus - Feedback");
    }

    @FXML
    private void showLoyalty() {
        loadScreen("/view/admin/AdminLoyalty.fxml", "NewnhamNexus - Loyalty Program");
    }

    @FXML
    private void showReports() {
        loadScreen("/view/admin/AdminReports.fxml", "NewnhamNexus - Reports");
    }

    // Quick action methods
    @FXML
    private void newReservation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New Reservation");
        alert.setHeaderText("Create New Reservation (Phone Booking)");
        alert.setContentText("This would open a form to create a new reservation via phone.");
        alert.showAndWait();
    }

    // Search methods
    @FXML
    private void searchReservations() {
        // Stub for search functionality
    }

    @FXML
    private void clearFilters() {
        if (searchField != null) searchField.clear();
        if (statusFilter != null) statusFilter.setValue(null);
        if (fromDateFilter != null) fromDateFilter.setValue(null);
        if (toDateFilter != null) toDateFilter.setValue(null);
    }

    @FXML
    private void searchGuests() {
        // Stub for guest search
    }

    @FXML
    private void clearGuestSearch() {
        // Stub for clearing guest search
    }

    // Payment methods
    @FXML
    private void searchForPayment() {
        // Stub for payment search
    }

    @FXML
    private void payFullBalance() {
        // Stub for paying full balance
    }

    @FXML
    private void processPayment() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Processed");
        alert.setHeaderText("Payment Successful");
        alert.setContentText("Payment has been processed successfully.");
        alert.showAndWait();
    }

    // Checkout methods
    @FXML
    private void applyDiscount() {
        // Stub for applying discount
    }

    @FXML
    private void generateBillPDF() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Generate Bill");
        alert.setHeaderText("PDF Generated");
        alert.setContentText("Final bill PDF has been generated.");
        alert.showAndWait();
    }

    @FXML
    private void completeCheckout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Check-out Complete");
        alert.setHeaderText("Guest Checked Out Successfully");
        alert.setContentText("Room has been marked as available. Please remind the guest to provide feedback at the kiosk.");
        alert.showAndWait();
    }

    // Waitlist methods
    @FXML
    private void addToWaitlist() {
        // Stub
    }

    @FXML
    private void filterWaitlist() {
        // Stub
    }

    @FXML
    private void confirmAddWaitlist() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Waitlist");
        alert.setHeaderText("Added to Waitlist");
        alert.setContentText("Guest has been added to the waitlist.");
        alert.showAndWait();
    }

    // Feedback methods
    @FXML
    private void exportFeedback() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText("Feedback Exported");
        alert.setContentText("Feedback summary has been exported to CSV.");
        alert.showAndWait();
    }

    // Loyalty methods
    @FXML
    private void enrollMember() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Loyalty Program");
        alert.setHeaderText("Enroll New Member");
        alert.setContentText("This would open a form to enroll a new loyalty member.");
        alert.showAndWait();
    }

    // Report methods
    @FXML
    private void generateRevenueReport() {
        // Stub
    }

    @FXML
    private void exportRevenueCSV() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText("Report Exported");
        alert.setContentText("Revenue report has been exported to CSV.");
        alert.showAndWait();
    }

    @FXML
    private void exportRevenuePDF() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText("Report Exported");
        alert.setContentText("Revenue report has been exported to PDF.");
        alert.showAndWait();
    }

    @FXML
    private void generateOccupancyReport() {
        // Stub
    }

    @FXML
    private void exportLogsCSV() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText("Logs Exported");
        alert.setContentText("Activity logs have been exported to CSV.");
        alert.showAndWait();
    }

    @FXML
    private void exportLogsTXT() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText("Logs Exported");
        alert.setContentText("Activity logs have been exported to TXT.");
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
