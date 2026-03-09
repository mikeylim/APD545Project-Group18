package com.hotel.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.hotel.app.Main;

/**
 * Controller for the main launcher screen.
 * Allows users to choose between Kiosk, Admin, and Feedback modes.
 */
public class LauncherController {

    @FXML
    private void openKiosk() {
        loadScreen("/view/kiosk/KioskWelcome.fxml", "NewnhamNexus - Kiosk");
    }

    @FXML
    private void openAdminLogin() {
        loadScreen("/view/admin/AdminLogin.fxml", "NewnhamNexus - Admin Login");
    }

    @FXML
    private void openFeedback() {
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
