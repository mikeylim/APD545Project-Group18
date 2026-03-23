package com.hotel.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the NewnhamNexus Reservation System.
 * This application provides both Kiosk (self-service) and Admin interfaces.
 */
public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void init() {
        // Initialize database with seed data
        System.out.println("Initializing database...");
        try {
            DatabaseInitializer initializer = new DatabaseInitializer();
            initializer.initialize();
            System.out.println("Database initialization complete.");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Load the launcher screen that lets user choose Kiosk or Admin mode
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Launcher.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        stage.setTitle("NewnhamNexus - Reservation System");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    @Override
    public void stop() {
        // Close EntityManagerFactory on application shutdown
        System.out.println("Shutting down...");
        EntityManagerFactoryProvider.close();
        System.out.println("Application closed.");
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
