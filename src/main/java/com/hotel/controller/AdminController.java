package com.hotel.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.hotel.app.Main;
import com.hotel.model.*;
import com.hotel.service.*;
import com.hotel.repository.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.hotel.util.PdfExportUtil;
import com.hotel.events.RoomAvailabilityEvent;
import com.hotel.events.RoomAvailabilityNotifier;

/**
 * Controller for all Admin screens.
 * Manages administrator functions including reservations, payments, checkout, etc.
 * Fully integrated with backend services.
 */
public class AdminController {

    // ========== Services ==========
    private static AuthService authService;
    private static ReservationService reservationService;
    private static PaymentService paymentService;
    private static LoyaltyService loyaltyService;
    private static FeedbackService feedbackService;
    private static WaitlistService waitlistService;
    private static ReportingService reportingService;
    private static AuditService auditService;

    // Static initializer for services
    static {
        authService = new AuthService();
        reservationService = new ReservationService();
        paymentService = new PaymentService();
        loyaltyService = LoyaltyService.getInstance();
        feedbackService = new FeedbackService();
        waitlistService = new WaitlistService();
        reportingService = new ReportingService();
        auditService = AuditService.getInstance();

        // Add console observer for waitlist notifications
        waitlistService.addObserver(new WaitlistService.ConsoleNotificationObserver());

        // Subscribe to RoomAvailabilityNotifier (Observer pattern)
        RoomAvailabilityNotifier.getInstance().subscribe(event -> {
            System.out.println("=== ROOM AVAILABILITY EVENT (Observer Pattern) ===");
            System.out.println("Room: " + event.roomNumber() + " (" + event.roomType() + ")");
            System.out.println("Available at: " + event.availableAt());
            System.out.println("Matching waitlist entries: " + event.matchingWaitlistCount());
            System.out.println("===============================================");
        });
    }

    // ========== FXML Elements - Login ==========
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label loginErrorLabel;

    // ========== FXML Elements - Dashboard ==========
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label dateTimeLabel;
    @FXML private TableView<Reservation> recentReservationsTable;
    @FXML private Label todayCheckInsLabel;
    @FXML private Label todayCheckOutsLabel;
    @FXML private Label occupancyLabel;
    @FXML private Label pendingPaymentsLabel;

    // ========== FXML Elements - Reservations ==========
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker fromDateFilter;
    @FXML private DatePicker toDateFilter;
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> confirmationCol;
    @FXML private TableColumn<Reservation, String> guestNameCol;
    @FXML private TableColumn<Reservation, LocalDate> checkInCol;
    @FXML private TableColumn<Reservation, LocalDate> checkOutCol;
    @FXML private TableColumn<Reservation, String> statusCol;
    @FXML private TableColumn<Reservation, Double> totalCol;

    // Detail panel labels
    @FXML private VBox detailPanel;
    @FXML private Label detailConfNumber;
    @FXML private Label detailGuestName;
    @FXML private Label detailPhone;
    @FXML private Label detailEmail;
    @FXML private Label detailRoom;
    @FXML private Label detailDates;
    @FXML private Label detailGuestCount;
    @FXML private Label detailStatus;
    @FXML private Label detailSubtotal;
    @FXML private Label detailTax;
    @FXML private Label detailTotal;
    @FXML private Label detailPaid;
    @FXML private Label detailBalance;
    @FXML private Label resultCountLabel;

    // ========== FXML Elements - Guests ==========
    @FXML private TextField guestSearchField;
    @FXML private ComboBox<String> loyaltyFilter;
    @FXML private TableView<Guest> guestsTable;
    @FXML private Label guestCountLabel;
    @FXML private VBox guestDetailPanel;
    @FXML private Label guestInitialsLabel;
    @FXML private Label guestFullNameLabel;
    @FXML private Label guestLoyaltyStatusLabel;
    @FXML private Label guestEmailLabel;
    @FXML private Label guestPhoneLabel;
    @FXML private Label guestLoyaltyNumberLabel;
    @FXML private Label guestPointsLabel;
    @FXML private VBox guestReservationHistoryBox;
    @FXML private Button enrollLoyaltyBtn;

    // ========== FXML Elements - Payments ==========
    @FXML private TextField paymentSearchField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private ComboBox<String> transactionTypeCombo;
    @FXML private TextField paymentAmountField;
    @FXML private Label paymentConfNum;
    @FXML private Label paymentGuestName;
    @FXML private Label paymentTotalLabel;
    @FXML private Label paymentPaidLabel;
    @FXML private Label paymentBalanceLabel;
    @FXML private TableView<Payment> paymentHistoryTable;
    @FXML private TableView<Reservation> pendingPaymentsTable;
    @FXML private VBox loyaltyPointsSection;
    @FXML private Label availablePointsLabel;

    // ========== FXML Elements - Checkout ==========
    @FXML private TextField checkoutSearchField;
    @FXML private TextField discountField;
    @FXML private Label checkoutRoomLabel;
    @FXML private Label checkoutRoomTypeLabel;
    @FXML private Label checkoutGuestLabel;
    @FXML private Label checkoutConfLabel;
    @FXML private Label checkoutStayLabel;
    @FXML private Label checkoutOriginalSubtotalDesc;
    @FXML private Label checkoutOriginalSubtotalLabel;
    @FXML private Label checkoutDiscountDesc;
    @FXML private Label checkoutDiscountLabel;
    @FXML private Label checkoutSubtotalDesc;
    @FXML private Label checkoutSubtotalLabel;
    @FXML private Label checkoutTaxDescLabel;
    @FXML private Label checkoutTaxLabel;
    @FXML private Label checkoutTotalLabel;
    @FXML private Label checkoutPaidLabel;
    @FXML private Label checkoutBalanceDescLabel;
    @FXML private Label checkoutBalanceLabel;
    @FXML private Label maxDiscountLabel;
    @FXML private VBox balanceWarning;
    @FXML private TableView<Reservation> checkoutTable;

    // ========== FXML Elements - Waitlist ==========
    @FXML private ComboBox<String> waitlistRoomTypeCombo;
    @FXML private DatePicker waitlistCheckIn;
    @FXML private DatePicker waitlistCheckOut;
    @FXML private TableView<Waitlist> waitlistTable;
    @FXML private Label waitlistTotalLabel;
    @FXML private Label waitlistSingleLabel;
    @FXML private Label waitlistDoubleLabel;
    @FXML private Label waitlistDeluxeLabel;
    @FXML private Label waitlistPenthouseLabel;
    @FXML private TextField waitlistGuestName;
    @FXML private TextField waitlistPhone;
    @FXML private TextField waitlistEmail;
    @FXML private ComboBox<String> waitlistRoomType;
    @FXML private Spinner<Integer> waitlistAdults;
    @FXML private Spinner<Integer> waitlistChildren;
    @FXML private TextArea waitlistNotes;
    @FXML private VBox waitlistNotificationPanel;
    @FXML private Label waitlistNotificationTime;
    @FXML private Label waitlistNotificationText;

    // ========== FXML Elements - Feedback ==========
    @FXML private ComboBox<String> ratingFilter;
    @FXML private DatePicker feedbackFromDate;
    @FXML private DatePicker feedbackToDate;
    @FXML private ComboBox<String> sentimentFilter;
    @FXML private TextField feedbackGuestSearch;
    @FXML private TableView<Feedback> feedbackTable;
    @FXML private Label avgRatingLabel;
    @FXML private Label totalFeedbackLabel;
    @FXML private Label fiveStarLabel;
    @FXML private Label fourStarLabel;
    @FXML private Label threeStarLabel;
    @FXML private Label lowStarLabel;
    @FXML private Label fiveStarPercentLabel;
    @FXML private Label fourStarPercentLabel;
    @FXML private Label threeStarPercentLabel;
    @FXML private Label lowStarPercentLabel;
    @FXML private HBox commonTagsContainer;

    // ========== FXML Elements - Loyalty ==========
    @FXML private TextField loyaltySearchField;
    @FXML private TableView<Guest> loyaltyMembersTable;
    @FXML private Label loyaltyTotalMembersLabel;
    @FXML private Label loyaltyTotalPointsLabel;
    @FXML private Label loyaltyTotalValueLabel;
    @FXML private Label loyaltyAvgPointsLabel;
    @FXML private Label loyaltyEarningRateLabel;
    @FXML private Label loyaltyRedemptionRateLabel;
    // Loyalty Program Settings
    @FXML private TextField settingsPointsPerDollar;
    @FXML private TextField settingsPointsPerRedemption;
    @FXML private TextField settingsMaxRedemption;
    @FXML private TextField settingsMinRedemption;

    // ========== FXML Elements - Reports ==========
    // Revenue Report
    @FXML private ComboBox<String> revenuePeriod;
    @FXML private Label revenueDateLabel;
    @FXML private DatePicker revenueDate;
    @FXML private Label revenueFromLabel;
    @FXML private DatePicker revenueFromDate;
    @FXML private Label revenueToLabel;
    @FXML private DatePicker revenueToDate;
    @FXML private ComboBox<String> revenueRoomType;
    @FXML private TableView<ReportingService.RevenueReport.DailyRevenue> revenueTable;
    @FXML private Label revenueTotalLabel;
    @FXML private Label revenueReservationsLabel;
    @FXML private Label revenueTaxLabel;
    @FXML private Label revenueDiscountsLabel;
    // Occupancy Report
    @FXML private ComboBox<String> occupancyView;
    @FXML private Label occupancyDateLabel;
    @FXML private DatePicker occupancyDate;
    @FXML private Label occupancyFromLabel;
    @FXML private DatePicker occupancyFromDate;
    @FXML private Label occupancyToLabel;
    @FXML private DatePicker occupancyToDate;
    @FXML private ComboBox<String> occupancyRoomType;
    @FXML private TableView<ReportingService.OccupancyReport.DailyOccupancy> occupancyTable;
    @FXML private Label occupancyAvgLabel;
    @FXML private Label occupancyTotalRoomsLabel;
    @FXML private Label occupancyPeakLabel;
    @FXML private Label occupancyLowestLabel;
    // Activity Logs
    @FXML private TextField logSearchField;
    @FXML private ComboBox<String> logActionFilter;
    @FXML private ComboBox<String> logActorFilter;
    @FXML private DatePicker logFromDate;
    @FXML private DatePicker logToDate;
    @FXML private TableView<AuditLog> activityLogsTable;

    // ========== Current State ==========
    private Reservation currentReservation;
    private Guest currentGuest;

    @FXML
    public void initialize() {
        // Initialize user info labels if on dashboard
        if (userNameLabel != null && authService.isLoggedIn()) {
            User user = authService.getCurrentUser();
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText(user.getRole().toString());
        }

        // Initialize date/time
        if (dateTimeLabel != null) {
            dateTimeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")));
        }

        // Initialize status filter with formatted names
        if (statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList(
                "All", "Confirmed", "Checked In", "Checked Out", "Cancelled", "No Show"
            ));
            statusFilter.setValue("All");
        }

        // Initialize payment method combo
        if (paymentMethodCombo != null) {
            paymentMethodCombo.setItems(FXCollections.observableArrayList(
                "Cash", "Credit Card", "Loyalty Points"
            ));
            paymentMethodCombo.setValue("Cash");
        }

        // Initialize transaction type combo
        if (transactionTypeCombo != null) {
            transactionTypeCombo.setValue("Payment");
        }

        // Initialize waitlist room type combo
        if (waitlistRoomTypeCombo != null) {
            waitlistRoomTypeCombo.setItems(FXCollections.observableArrayList(
                "All", "SINGLE", "DOUBLE", "DELUXE", "PENTHOUSE"
            ));
            waitlistRoomTypeCombo.setValue("All");
        }

        // Initialize max discount label
        if (maxDiscountLabel != null && authService.isLoggedIn()) {
            maxDiscountLabel.setText("Max: " + authService.getMaxDiscountPercent() + "%");
        }

        // Initialize loyalty rate labels
        if (loyaltyEarningRateLabel != null) {
            loyaltyEarningRateLabel.setText(loyaltyService.getEarningRateDisplay());
        }
        if (loyaltyRedemptionRateLabel != null) {
            loyaltyRedemptionRateLabel.setText(loyaltyService.getRedemptionRateDisplay());
        }

        // Setup table columns
        setupTableColumns();

        // Load data based on current screen
        loadScreenData();
    }

    private void setupTableColumns() {
        // Apply constrained resize policy to remove blank columns
        applyConstrainedResizePolicy();

        // Reservations table columns and selection listener
        if (reservationsTable != null) {
            setupReservationsTableColumns();
            setupReservationSelectionListener();
        }

        // Recent reservations table (dashboard)
        if (recentReservationsTable != null) {
            setupRecentReservationsTableColumns();
        }

        // Guests table columns and selection listener
        if (guestsTable != null) {
            setupGuestsTableColumns();
            setupGuestSelectionListener();
        }

        // Payment history table columns
        if (paymentHistoryTable != null) {
            setupPaymentHistoryTableColumns();
        }

        // Pending payments table columns and selection
        if (pendingPaymentsTable != null) {
            setupPendingPaymentsTableColumns();
            setupPendingPaymentsSelectionListener();
        }

        // Waitlist table columns
        if (waitlistTable != null) {
            setupWaitlistTableColumns();
        }

        // Feedback table columns
        if (feedbackTable != null) {
            setupFeedbackTableColumns();
        }

        // Loyalty members table columns
        if (loyaltyMembersTable != null) {
            setupLoyaltyMembersTableColumns();
        }

        // Checkout table columns and selection listener
        if (checkoutTable != null) {
            setupCheckoutTableColumns();
            setupCheckoutTableSelectionListener();
        }
    }

    /**
     * Apply constrained resize policy to all tables to prevent blank column at the end.
     */
    private void applyConstrainedResizePolicy() {
        if (recentReservationsTable != null) {
            recentReservationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        }
        if (reservationsTable != null) {
            reservationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        }
        if (guestsTable != null) {
            guestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        }
        if (paymentHistoryTable != null) {
            paymentHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        }
        if (pendingPaymentsTable != null) {
            pendingPaymentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        }
        if (checkoutTable != null) {
            checkoutTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        }
        if (waitlistTable != null) {
            waitlistTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        }
        if (feedbackTable != null) {
            feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        }
        if (loyaltyMembersTable != null) {
            loyaltyMembersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        }
    }

    private void setupCheckoutTableSelectionListener() {
        checkoutTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentReservation = newSelection;
                updateCheckoutDisplay();
                // Clear discount field when selecting different reservation
                if (discountField != null) {
                    discountField.clear();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void setupCheckoutTableColumns() {
        ObservableList<TableColumn<Reservation, ?>> columns = checkoutTable.getColumns();
        if (columns.size() >= 6) {
            ((TableColumn<Reservation, String>) columns.get(0)).setCellValueFactory(cellData -> {
                List<Room> rooms = cellData.getValue().getRooms();
                return new javafx.beans.property.SimpleStringProperty(
                    rooms.isEmpty() ? "N/A" : rooms.get(0).getRoomNumber());
            });
            ((TableColumn<Reservation, String>) columns.get(1)).setCellValueFactory(new PropertyValueFactory<>("confirmationNumber"));
            ((TableColumn<Reservation, String>) columns.get(2)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuest().getFullName()));
            ((TableColumn<Reservation, LocalDate>) columns.get(3)).setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
            ((TableColumn<Reservation, String>) columns.get(4)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", cellData.getValue().getBalance())));
            ((TableColumn<Reservation, String>) columns.get(5)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatStatus(cellData.getValue().getStatus())));
        }
    }

    @SuppressWarnings("unchecked")
    private void setupReservationsTableColumns() {
        ObservableList<TableColumn<Reservation, ?>> columns = reservationsTable.getColumns();
        if (columns.size() >= 8) {
            ((TableColumn<Reservation, String>) columns.get(0)).setCellValueFactory(new PropertyValueFactory<>("confirmationNumber"));
            ((TableColumn<Reservation, String>) columns.get(1)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuest().getFullName()));
            ((TableColumn<Reservation, String>) columns.get(2)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuest().getPhone()));
            ((TableColumn<Reservation, String>) columns.get(3)).setCellValueFactory(cellData -> {
                List<Room> rooms = cellData.getValue().getRooms();
                return new javafx.beans.property.SimpleStringProperty(
                    rooms.isEmpty() ? "-" : rooms.size() + " room(s)");
            });
            ((TableColumn<Reservation, LocalDate>) columns.get(4)).setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
            ((TableColumn<Reservation, LocalDate>) columns.get(5)).setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
            ((TableColumn<Reservation, String>) columns.get(6)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", cellData.getValue().getTotal())));
            ((TableColumn<Reservation, String>) columns.get(7)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatStatus(cellData.getValue().getStatus())));
        }
    }

    private void setupReservationSelectionListener() {
        reservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentReservation = newSelection;
                updateReservationDetailPanel();
            }
        });
    }

    private void updateReservationDetailPanel() {
        if (currentReservation == null || detailConfNumber == null) return;

        Guest guest = currentReservation.getGuest();
        List<Room> rooms = currentReservation.getRooms();

        detailConfNumber.setText(currentReservation.getConfirmationNumber());
        detailGuestName.setText(guest.getFullName());
        detailPhone.setText(guest.getPhone() != null ? guest.getPhone() : "-");
        detailEmail.setText(guest.getEmail() != null ? guest.getEmail() : "-");

        if (rooms.isEmpty()) {
            detailRoom.setText("-");
        } else {
            String roomInfo = rooms.stream()
                .map(r -> r.getType() + " #" + r.getRoomNumber())
                .reduce((a, b) -> a + ", " + b)
                .orElse("-");
            detailRoom.setText(roomInfo);
        }

        detailDates.setText(currentReservation.getCheckInDate() + " to " + currentReservation.getCheckOutDate());
        detailGuestCount.setText(currentReservation.getAdults() + " Adults, " + currentReservation.getChildren() + " Children");
        detailStatus.setText(formatStatus(currentReservation.getStatus()));

        // Color code status
        switch (currentReservation.getStatus()) {
            case CONFIRMED -> detailStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
            case CHECKED_IN -> detailStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
            case CHECKED_OUT -> detailStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
            case CANCELLED -> detailStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            default -> detailStatus.setStyle("-fx-font-weight: bold;");
        }

        // Update billing
        detailSubtotal.setText(String.format("$%.2f", currentReservation.getSubtotal()));
        detailTax.setText(String.format("$%.2f", currentReservation.getTax()));
        detailTotal.setText(String.format("$%.2f", currentReservation.getTotal()));
        detailPaid.setText(String.format("$%.2f", currentReservation.getAmountPaid()));
        detailBalance.setText(String.format("$%.2f", currentReservation.getBalance()));

        // Color code balance
        if (currentReservation.getBalance() <= 0) {
            detailBalance.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
        } else {
            detailBalance.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void clearReservationSelection() {
        currentReservation = null;
        if (reservationsTable != null) {
            reservationsTable.getSelectionModel().clearSelection();
        }
        if (detailConfNumber != null) {
            detailConfNumber.setText("Select a reservation");
            detailGuestName.setText("-");
            detailPhone.setText("-");
            detailEmail.setText("-");
            detailRoom.setText("-");
            detailDates.setText("-");
            detailGuestCount.setText("-");
            detailStatus.setText("-");
            detailSubtotal.setText("$0.00");
            detailTax.setText("$0.00");
            detailTotal.setText("$0.00");
            detailPaid.setText("$0.00");
            detailBalance.setText("$0.00");
        }
    }

    @FXML
    private void checkInSelectedReservation() {
        if (currentReservation == null) {
            showError("Please select a reservation first");
            return;
        }

        if (currentReservation.getStatus() != ReservationStatus.CONFIRMED) {
            showError("Only CONFIRMED reservations can be checked in. Current status: " + currentReservation.getStatus());
            return;
        }

        try {
            reservationService.checkIn(currentReservation);
            showInfo("Check-In", "Success", "Guest " + currentReservation.getGuest().getFullName() + " has been checked in.");
            auditService.log(authService.getCurrentUsername(), "CHECK_IN", "Reservation",
                currentReservation.getId().toString(), "Checked in guest: " + currentReservation.getGuest().getFullName());
            loadReservations();
            updateReservationDetailPanel();
        } catch (Exception e) {
            showError("Check-in failed: " + e.getMessage());
        }
    }

    @FXML
    private void goToPaymentForSelected() {
        if (currentReservation == null) {
            showError("Please select a reservation first");
            return;
        }
        // Navigate to payments page - the search will use the current reservation
        showPayments();
    }

    @FXML
    private void cancelSelectedReservation() {
        if (currentReservation == null) {
            showError("Please select a reservation first");
            return;
        }

        if (currentReservation.getStatus() == ReservationStatus.CHECKED_IN) {
            showError("Cannot cancel a reservation that is already checked in. Please checkout first.");
            return;
        }

        if (currentReservation.getStatus() == ReservationStatus.CANCELLED) {
            showError("Reservation is already cancelled.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Reservation");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will cancel reservation " + currentReservation.getConfirmationNumber() +
            " for " + currentReservation.getGuest().getFullName());

        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                reservationService.cancelReservation(currentReservation);
                showInfo("Cancelled", "Reservation Cancelled", "Reservation has been cancelled successfully.");
                auditService.log(authService.getCurrentUsername(), "CANCEL", "Reservation",
                    currentReservation.getId().toString(), "Cancelled reservation: " + currentReservation.getConfirmationNumber());
                loadReservations();
                updateReservationDetailPanel();
            } catch (Exception e) {
                showError("Cancellation failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void markAsNoShow() {
        if (currentReservation == null) {
            showError("Please select a reservation first");
            return;
        }

        // Only CONFIRMED reservations can be marked as no-show
        if (currentReservation.getStatus() != ReservationStatus.CONFIRMED) {
            showError("Only confirmed reservations can be marked as no-show.\n\nCurrent status: " +
                formatStatus(currentReservation.getStatus()));
            return;
        }

        // Check if check-in date has passed
        LocalDate today = LocalDate.now();
        if (!currentReservation.getCheckInDate().isBefore(today)) {
            showError("Cannot mark as no-show before the check-in date.\n\n" +
                "Check-in date: " + currentReservation.getCheckInDate() + "\n" +
                "Today: " + today);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Mark as No Show");
        confirm.setHeaderText("Mark reservation as No Show?");
        confirm.setContentText("Reservation " + currentReservation.getConfirmationNumber() +
            " for " + currentReservation.getGuest().getFullName() +
            "\n\nCheck-in date was: " + currentReservation.getCheckInDate() +
            "\n\nThis will mark the guest as a no-show.");

        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                currentReservation.setStatus(ReservationStatus.NO_SHOW);
                ReservationRepository reservationRepo = new ReservationRepository();
                reservationRepo.update(currentReservation);

                showInfo("No Show", "Reservation Marked as No Show",
                    "Reservation " + currentReservation.getConfirmationNumber() + " has been marked as no-show.");
                auditService.log(authService.getCurrentUsername(), "NO_SHOW", "Reservation",
                    currentReservation.getId().toString(),
                    "Marked as no-show: " + currentReservation.getConfirmationNumber() +
                    " for " + currentReservation.getGuest().getFullName());

                loadReservations();
                updateReservationDetailPanel();
            } catch (Exception e) {
                showError("Failed to mark as no-show: " + e.getMessage());
            }
        }
    }

    @FXML
    private void deleteSelectedReservation() {
        if (currentReservation == null) {
            showError("Please select a reservation first");
            return;
        }

        // Check if reservation has any payments
        PaymentRepository paymentRepo = new PaymentRepository();
        List<Payment> payments = paymentRepo.findByReservation(currentReservation);

        if (!payments.isEmpty()) {
            double totalPaid = payments.stream().mapToDouble(Payment::getAmount).sum();
            showError("Cannot delete reservation with payments. Total paid: $" + String.format("%.2f", totalPaid) +
                "\n\nPlease cancel the reservation instead, or contact system administrator.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Reservation");
        confirm.setHeaderText("Permanently Delete Reservation?");
        confirm.setContentText("This will permanently delete reservation " + currentReservation.getConfirmationNumber() +
            " for " + currentReservation.getGuest().getFullName() + ".\n\nThis action cannot be undone!");

        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                ReservationRepository reservationRepo = new ReservationRepository();
                String confNum = currentReservation.getConfirmationNumber();
                Long resId = currentReservation.getId();
                String guestName = currentReservation.getGuest().getFullName();

                reservationRepo.delete(currentReservation);

                showInfo("Deleted", "Reservation Deleted", "Reservation " + confNum + " has been permanently deleted.");
                auditService.log(authService.getCurrentUsername(), "DELETE", "Reservation",
                    resId.toString(), "Deleted reservation: " + confNum + " for " + guestName);

                currentReservation = null;
                loadReservations();
                updateReservationDetailPanel();
            } catch (Exception e) {
                showError("Delete failed: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setupRecentReservationsTableColumns() {
        ObservableList<TableColumn<Reservation, ?>> columns = recentReservationsTable.getColumns();
        if (columns.size() >= 5) {
            ((TableColumn<Reservation, String>) columns.get(0)).setCellValueFactory(new PropertyValueFactory<>("confirmationNumber"));
            ((TableColumn<Reservation, String>) columns.get(1)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuest().getFullName()));
            ((TableColumn<Reservation, LocalDate>) columns.get(2)).setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
            ((TableColumn<Reservation, LocalDate>) columns.get(3)).setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
            ((TableColumn<Reservation, String>) columns.get(4)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatStatus(cellData.getValue().getStatus())));
        }
    }

    @SuppressWarnings("unchecked")
    private void setupGuestsTableColumns() {
        ObservableList<TableColumn<Guest, ?>> columns = guestsTable.getColumns();
        if (columns.size() >= 6) {
            ((TableColumn<Guest, Long>) columns.get(0)).setCellValueFactory(new PropertyValueFactory<>("id"));
            ((TableColumn<Guest, String>) columns.get(1)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
            ((TableColumn<Guest, String>) columns.get(2)).setCellValueFactory(new PropertyValueFactory<>("email"));
            ((TableColumn<Guest, String>) columns.get(3)).setCellValueFactory(new PropertyValueFactory<>("phone"));
            ((TableColumn<Guest, String>) columns.get(4)).setCellValueFactory(cellData -> {
                String loyaltyNum = cellData.getValue().getLoyaltyNumber();
                return new javafx.beans.property.SimpleStringProperty(loyaltyNum != null ? loyaltyNum : "-");
            });
            ((TableColumn<Guest, String>) columns.get(5)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getLoyaltyPoints())));
        }
    }

    private void setupGuestSelectionListener() {
        guestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentGuest = newSelection;
                updateGuestDetailPanel();
            }
        });
    }

    private void updateGuestDetailPanel() {
        if (currentGuest == null || guestFullNameLabel == null) return;

        // Update initials
        String initials = "";
        if (currentGuest.getFirstName() != null && !currentGuest.getFirstName().isEmpty()) {
            initials += currentGuest.getFirstName().charAt(0);
        }
        if (currentGuest.getLastName() != null && !currentGuest.getLastName().isEmpty()) {
            initials += currentGuest.getLastName().charAt(0);
        }
        guestInitialsLabel.setText(initials.isEmpty() ? "--" : initials.toUpperCase());

        guestFullNameLabel.setText(currentGuest.getFullName());
        guestEmailLabel.setText(currentGuest.getEmail() != null ? currentGuest.getEmail() : "-");
        guestPhoneLabel.setText(currentGuest.getPhone() != null ? currentGuest.getPhone() : "-");

        // Loyalty info
        if (currentGuest.getLoyaltyNumber() != null && !currentGuest.getLoyaltyNumber().isEmpty()) {
            guestLoyaltyStatusLabel.setText("Loyalty Member");
            guestLoyaltyStatusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 12px;");
            guestLoyaltyNumberLabel.setText(currentGuest.getLoyaltyNumber());
            if (enrollLoyaltyBtn != null) {
                enrollLoyaltyBtn.setDisable(true);
                enrollLoyaltyBtn.setText("Already Enrolled");
            }
        } else {
            guestLoyaltyStatusLabel.setText("Not a Member");
            guestLoyaltyStatusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
            guestLoyaltyNumberLabel.setText("-");
            if (enrollLoyaltyBtn != null) {
                enrollLoyaltyBtn.setDisable(false);
                enrollLoyaltyBtn.setText("Enroll in Loyalty");
            }
        }

        guestPointsLabel.setText(String.format("%,d", currentGuest.getLoyaltyPoints()));

        // Load reservation history
        loadGuestReservationHistory();
    }

    private void loadGuestReservationHistory() {
        if (guestReservationHistoryBox == null || currentGuest == null) return;

        guestReservationHistoryBox.getChildren().clear();

        List<Reservation> guestReservations = reservationService.getAllReservations().stream()
            .filter(r -> r.getGuest().getId().equals(currentGuest.getId()))
            .sorted((r1, r2) -> r2.getCheckInDate().compareTo(r1.getCheckInDate()))
            .limit(3)
            .toList();

        if (guestReservations.isEmpty()) {
            Label noHistory = new Label("No reservation history");
            noHistory.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
            guestReservationHistoryBox.getChildren().add(noHistory);
        } else {
            for (Reservation res : guestReservations) {
                HBox row = new HBox();
                row.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 8; -fx-background-radius: 4;");

                VBox info = new VBox(2);
                HBox.setHgrow(info, Priority.ALWAYS);

                Label confLabel = new Label(res.getConfirmationNumber());
                confLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

                Label dateLabel = new Label(res.getCheckInDate() + " to " + res.getCheckOutDate());
                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

                info.getChildren().addAll(confLabel, dateLabel);

                Label statusLabel = new Label(formatStatus(res.getStatus()));
                statusLabel.setStyle("-fx-font-size: 11px;");
                switch (res.getStatus()) {
                    case CONFIRMED -> statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 11px;");
                    case CHECKED_IN -> statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                    case CHECKED_OUT -> statusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
                    case CANCELLED -> statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
                    default -> statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11px;");
                }

                row.getChildren().addAll(info, statusLabel);
                guestReservationHistoryBox.getChildren().add(row);
            }
        }
    }

    @FXML
    private void clearGuestSelection() {
        currentGuest = null;
        if (guestsTable != null) {
            guestsTable.getSelectionModel().clearSelection();
        }
        if (guestFullNameLabel != null) {
            guestInitialsLabel.setText("--");
            guestFullNameLabel.setText("Select a guest");
            guestLoyaltyStatusLabel.setText("-");
            guestEmailLabel.setText("-");
            guestPhoneLabel.setText("-");
            guestLoyaltyNumberLabel.setText("-");
            guestPointsLabel.setText("0");
        }
        if (guestReservationHistoryBox != null) {
            guestReservationHistoryBox.getChildren().clear();
            Label placeholder = new Label("Select a guest to view reservations");
            placeholder.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
            guestReservationHistoryBox.getChildren().add(placeholder);
        }
        if (enrollLoyaltyBtn != null) {
            enrollLoyaltyBtn.setDisable(true);
            enrollLoyaltyBtn.setText("Enroll in Loyalty");
        }
    }

    @FXML
    private void viewGuestReservations() {
        if (currentGuest == null) {
            showError("Please select a guest first");
            return;
        }
        // Navigate to reservations and could filter by guest
        showReservations();
    }

    @FXML
    private void enrollGuestInLoyalty() {
        if (currentGuest == null) {
            showError("Please select a guest first");
            return;
        }

        if (currentGuest.getLoyaltyNumber() != null && !currentGuest.getLoyaltyNumber().isEmpty()) {
            showInfo("Already Enrolled", "Loyalty Member", "This guest is already enrolled in the loyalty program.");
            return;
        }

        // Confirm enrollment
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Enroll in Loyalty Program");
        confirm.setHeaderText("Enroll " + currentGuest.getFullName() + "?");
        confirm.setContentText("Guest will be enrolled with email: " + currentGuest.getEmail());

        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                loyaltyService.enrollGuest(currentGuest);
                showInfo("Enrollment Successful", "Loyalty Member Created",
                    "Loyalty Number: " + currentGuest.getLoyaltyNumber() + "\n" +
                    "Starting Points: " + currentGuest.getLoyaltyPoints());
                auditService.log(authService.getCurrentUsername(), "ENROLL_LOYALTY", "Guest",
                    currentGuest.getId().toString(), "Enrolled guest in loyalty: " + currentGuest.getFullName());
                updateGuestDetailPanel();
                loadAllGuests(); // Refresh table
            } catch (Exception e) {
                showError("Enrollment failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void deleteSelectedGuest() {
        if (currentGuest == null) {
            showError("Please select a guest first");
            return;
        }

        // Check if guest has any reservations
        ReservationRepository reservationRepo = new ReservationRepository();
        List<Reservation> reservations = reservationRepo.findByGuest(currentGuest);

        if (!reservations.isEmpty()) {
            showError("Cannot delete guest with existing reservations.\n\n" +
                currentGuest.getFullName() + " has " + reservations.size() + " reservation(s).\n\n" +
                "Please delete the reservations first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Guest");
        confirm.setHeaderText("Permanently Delete Guest?");
        confirm.setContentText("This will permanently delete " + currentGuest.getFullName() +
            " (" + currentGuest.getEmail() + ").\n\nThis action cannot be undone!");

        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                GuestRepository guestRepo = new GuestRepository();
                String guestName = currentGuest.getFullName();
                Long guestId = currentGuest.getId();

                guestRepo.delete(currentGuest);

                showInfo("Deleted", "Guest Deleted", guestName + " has been permanently deleted.");
                auditService.log(authService.getCurrentUsername(), "DELETE", "Guest",
                    guestId.toString(), "Deleted guest: " + guestName);

                currentGuest = null;
                loadAllGuests();
                clearGuestSelection();
            } catch (Exception e) {
                showError("Delete failed: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setupPaymentHistoryTableColumns() {
        ObservableList<TableColumn<Payment, ?>> columns = paymentHistoryTable.getColumns();
        if (columns.size() >= 4) {
            // Column 0: Date
            ((TableColumn<Payment, String>) columns.get(0)).setCellValueFactory(cellData -> {
                LocalDateTime date = cellData.getValue().getPaymentDate();
                String formatted = date != null ? date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "-";
                return new javafx.beans.property.SimpleStringProperty(formatted);
            });
            // Column 1: Method
            ((TableColumn<Payment, String>) columns.get(1)).setCellValueFactory(cellData -> {
                PaymentMethod method = cellData.getValue().getPaymentMethod();
                String formatted = formatPaymentMethod(method);
                return new javafx.beans.property.SimpleStringProperty(formatted);
            });
            // Column 2: Amount
            ((TableColumn<Payment, String>) columns.get(2)).setCellValueFactory(cellData -> {
                double amount = cellData.getValue().getAmount();
                String formatted = String.format("$%.2f", amount);
                return new javafx.beans.property.SimpleStringProperty(formatted);
            });
            // Column 3: Reference
            ((TableColumn<Payment, String>) columns.get(3)).setCellValueFactory(new PropertyValueFactory<>("transactionReference"));
        }
    }

    /**
     * Format PaymentMethod enum to human-readable string.
     * E.g., "CREDIT_CARD" -> "Credit Card"
     */
    private String formatPaymentMethod(PaymentMethod method) {
        if (method == null) return "-";
        String name = method.name();
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) result.append(" ");
            result.append(word.charAt(0)).append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * Parse payment method string to enum.
     * E.g., "Credit Card" -> CREDIT_CARD
     */
    private PaymentMethod parsePaymentMethod(String formatted) {
        if (formatted == null) return PaymentMethod.CASH;
        String enumName = formatted.toUpperCase().replace(" ", "_");
        try {
            return PaymentMethod.valueOf(enumName);
        } catch (IllegalArgumentException e) {
            return PaymentMethod.CASH;
        }
    }

    @FXML
    private void clearPaymentForm() {
        currentReservation = null;
        if (pendingPaymentsTable != null) {
            pendingPaymentsTable.getSelectionModel().clearSelection();
        }
        if (paymentSearchField != null) {
            paymentSearchField.clear();
        }
        if (paymentAmountField != null) {
            paymentAmountField.clear();
        }
        if (transactionTypeCombo != null) {
            transactionTypeCombo.setValue("Payment");
        }
        if (paymentMethodCombo != null) {
            paymentMethodCombo.setValue("Cash");
        }
        if (paymentConfNum != null) {
            paymentConfNum.setText("Search for a reservation");
        }
        if (paymentGuestName != null) {
            paymentGuestName.setText("-");
        }
        if (paymentTotalLabel != null) {
            paymentTotalLabel.setText("$0.00");
        }
        if (paymentPaidLabel != null) {
            paymentPaidLabel.setText("$0.00");
        }
        if (paymentBalanceLabel != null) {
            paymentBalanceLabel.setText("$0.00");
            paymentBalanceLabel.setStyle("-fx-font-weight: bold;");
        }
        if (loyaltyPointsSection != null) {
            loyaltyPointsSection.setVisible(false);
            loyaltyPointsSection.setManaged(false);
        }
        if (paymentHistoryTable != null) {
            paymentHistoryTable.getItems().clear();
        }
    }

    @SuppressWarnings("unchecked")
    private void setupPendingPaymentsTableColumns() {
        ObservableList<TableColumn<Reservation, ?>> columns = pendingPaymentsTable.getColumns();
        if (columns.size() >= 6) {
            ((TableColumn<Reservation, String>) columns.get(0)).setCellValueFactory(new PropertyValueFactory<>("confirmationNumber"));
            ((TableColumn<Reservation, String>) columns.get(1)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuest().getFullName()));
            ((TableColumn<Reservation, String>) columns.get(2)).setCellValueFactory(cellData -> {
                List<Room> rooms = cellData.getValue().getRooms();
                return new javafx.beans.property.SimpleStringProperty(
                    rooms.isEmpty() ? "-" : rooms.get(0).getRoomNumber());
            });
            ((TableColumn<Reservation, String>) columns.get(3)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", cellData.getValue().getTotal())));
            ((TableColumn<Reservation, String>) columns.get(4)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", cellData.getValue().getAmountPaid())));
            ((TableColumn<Reservation, String>) columns.get(5)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", cellData.getValue().getBalance())));
        }
    }

    private void setupPendingPaymentsSelectionListener() {
        pendingPaymentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentReservation = newSelection;
                updatePaymentDisplay();
                // Clear input fields when selecting a different reservation
                clearPaymentInputFields();
            }
        });
    }

    /**
     * Clear only the input fields (amount, transaction type, payment method) but keep the selected reservation info.
     */
    private void clearPaymentInputFields() {
        if (paymentAmountField != null) {
            paymentAmountField.clear();
        }
        if (transactionTypeCombo != null) {
            transactionTypeCombo.setValue("Payment");
        }
        if (paymentMethodCombo != null) {
            paymentMethodCombo.setValue("Cash");
        }
    }

    private void loadPendingPayments() {
        List<Reservation> pendingPayments = reservationService.getAllReservations().stream()
            .filter(r -> r.getStatus() != ReservationStatus.CANCELLED && r.getStatus() != ReservationStatus.CHECKED_OUT)
            .filter(r -> r.getBalance() > 0)
            .toList();
        pendingPaymentsTable.setItems(FXCollections.observableArrayList(pendingPayments));
    }

    @SuppressWarnings("unchecked")
    private void setupWaitlistTableColumns() {
        ObservableList<TableColumn<Waitlist, ?>> columns = waitlistTable.getColumns();
        if (columns.size() >= 10) {
            // #
            ((TableColumn<Waitlist, Number>) columns.get(0)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(waitlistTable.getItems().indexOf(cellData.getValue()) + 1));
            // Guest Name
            ((TableColumn<Waitlist, String>) columns.get(1)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuest().getFullName()));
            // Phone
            ((TableColumn<Waitlist, String>) columns.get(2)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuest().getPhone()));
            // Email
            ((TableColumn<Waitlist, String>) columns.get(3)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuest().getEmail()));
            // Room Type
            ((TableColumn<Waitlist, String>) columns.get(4)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatRoomType(cellData.getValue().getRoomType())));
            // Guests (adults, children)
            ((TableColumn<Waitlist, String>) columns.get(5)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuestCountDisplay()));
            // Check-in
            ((TableColumn<Waitlist, LocalDate>) columns.get(6)).setCellValueFactory(new PropertyValueFactory<>("desiredCheckIn"));
            // Check-out
            ((TableColumn<Waitlist, LocalDate>) columns.get(7)).setCellValueFactory(new PropertyValueFactory<>("desiredCheckOut"));
            // Added On
            ((TableColumn<Waitlist, String>) columns.get(8)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatedAt().toLocalDate().toString()));
            // Actions column with Remove button
            TableColumn<Waitlist, Void> actionsCol = (TableColumn<Waitlist, Void>) columns.get(9);
            actionsCol.setCellFactory(col -> new TableCell<>() {
                private final Button removeBtn = new Button("X");
                {
                    removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 2 8; -fx-background-insets: 0; -fx-cursor: hand;");
                    removeBtn.setOnAction(e -> {
                        Waitlist entry = getTableView().getItems().get(getIndex());
                        removeFromWaitlist(entry);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : removeBtn);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void setupFeedbackTableColumns() {
        ObservableList<TableColumn<Feedback, ?>> columns = feedbackTable.getColumns();
        if (columns.size() >= 7) {
            // Column 0: Date
            TableColumn<Feedback, String> dateCol = (TableColumn<Feedback, String>) columns.get(0);
            dateCol.setCellValueFactory(cellData -> {
                LocalDateTime submittedAt = cellData.getValue().getSubmittedAt();
                String formatted = submittedAt != null ? submittedAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")) : "";
                return new javafx.beans.property.SimpleStringProperty(formatted);
            });

            // Column 1: Conf. #
            TableColumn<Feedback, String> confCol = (TableColumn<Feedback, String>) columns.get(1);
            confCol.setCellValueFactory(cellData -> {
                Reservation res = cellData.getValue().getReservation();
                return new javafx.beans.property.SimpleStringProperty(res != null ? res.getConfirmationNumber() : "");
            });

            // Column 2: Guest
            TableColumn<Feedback, String> guestCol = (TableColumn<Feedback, String>) columns.get(2);
            guestCol.setCellValueFactory(cellData -> {
                Guest guest = cellData.getValue().getGuest();
                return new javafx.beans.property.SimpleStringProperty(guest != null ? guest.getFullName() : "");
            });

            // Column 3: Rating (with stars)
            TableColumn<Feedback, Integer> ratingCol = (TableColumn<Feedback, Integer>) columns.get(3);
            ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
            ratingCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Integer rating, boolean empty) {
                    super.updateItem(rating, empty);
                    if (empty || rating == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText("★".repeat(rating) + "☆".repeat(5 - rating));
                        setStyle("-fx-text-fill: #f39c12;");
                    }
                }
            });

            // Column 4: Comment
            TableColumn<Feedback, String> commentCol = (TableColumn<Feedback, String>) columns.get(4);
            commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
            commentCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String comment, boolean empty) {
                    super.updateItem(comment, empty);
                    if (empty || comment == null) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        String truncated = comment.length() > 50 ? comment.substring(0, 47) + "..." : comment;
                        setText(truncated);
                        if (comment.length() > 50) {
                            setTooltip(new Tooltip(comment));
                        }
                    }
                }
            });

            // Column 5: Sentiment
            TableColumn<Feedback, String> sentimentCol = (TableColumn<Feedback, String>) columns.get(5);
            sentimentCol.setCellValueFactory(cellData -> {
                int rating = cellData.getValue().getRating();
                String sentiment = rating >= 4 ? "Positive" : (rating == 3 ? "Neutral" : "Negative");
                return new javafx.beans.property.SimpleStringProperty(sentiment);
            });
            sentimentCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String sentiment, boolean empty) {
                    super.updateItem(sentiment, empty);
                    if (empty || sentiment == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(sentiment);
                        switch (sentiment) {
                            case "Positive" -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            case "Neutral" -> setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            case "Negative" -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            default -> setStyle("");
                        }
                    }
                }
            });

            // Column 6: Actions
            TableColumn<Feedback, Void> actionsCol = (TableColumn<Feedback, Void>) columns.get(6);
            actionsCol.setCellFactory(col -> new TableCell<>() {
                private final Button viewBtn = new Button("View");
                {
                    viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-insets: 0;");
                    viewBtn.setOnAction(e -> {
                        Feedback feedback = getTableView().getItems().get(getIndex());
                        showFeedbackDetails(feedback);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : viewBtn);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void setupLoyaltyMembersTableColumns() {
        ObservableList<TableColumn<Guest, ?>> columns = loyaltyMembersTable.getColumns();
        if (columns.size() >= 7) {
            // Column 0: Loyalty #
            ((TableColumn<Guest, String>) columns.get(0)).setCellValueFactory(new PropertyValueFactory<>("loyaltyNumber"));

            // Column 1: Name
            ((TableColumn<Guest, String>) columns.get(1)).setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));

            // Column 2: Email
            ((TableColumn<Guest, String>) columns.get(2)).setCellValueFactory(new PropertyValueFactory<>("email"));

            // Column 3: Phone
            ((TableColumn<Guest, String>) columns.get(3)).setCellValueFactory(new PropertyValueFactory<>("phone"));

            // Column 4: Points
            TableColumn<Guest, Integer> pointsCol = (TableColumn<Guest, Integer>) columns.get(4);
            pointsCol.setCellValueFactory(new PropertyValueFactory<>("loyaltyPoints"));
            pointsCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Integer points, boolean empty) {
                    super.updateItem(points, empty);
                    if (empty || points == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(String.format("%,d", points));
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #f39c12;");
                    }
                }
            });

            // Column 5: Value
            TableColumn<Guest, String> valueCol = (TableColumn<Guest, String>) columns.get(5);
            valueCol.setCellValueFactory(cellData -> {
                int points = cellData.getValue().getLoyaltyPoints();
                double value = points * 0.01; // $0.01 per point
                return new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", value));
            });
            valueCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(value);
                        setStyle("-fx-text-fill: #27ae60;");
                    }
                }
            });

            // Column 6: Actions
            TableColumn<Guest, Void> actionsCol = (TableColumn<Guest, Void>) columns.get(6);
            actionsCol.setCellFactory(col -> new TableCell<>() {
                private final Button viewBtn = new Button("View");
                {
                    viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-insets: 0;");
                    viewBtn.setOnAction(e -> {
                        Guest member = getTableView().getItems().get(getIndex());
                        showLoyaltyMemberDetails(member);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : viewBtn);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            });
        }
    }

    private void loadScreenData() {
        // Load dashboard data
        if (todayCheckInsLabel != null) {
            loadDashboardStats();
        }

        // Load recent reservations on dashboard
        if (recentReservationsTable != null) {
            loadRecentReservations();
        }

        // Load reservations if on reservations screen
        if (reservationsTable != null) {
            loadReservations();
        }

        // Load guests if on guests screen
        if (guestsTable != null && guestSearchField != null) {
            loadAllGuests();
        }

        // Load feedback summary if on feedback screen
        if (avgRatingLabel != null) {
            loadFeedbackSummary();
        }

        // Load waitlist if on waitlist screen
        if (waitlistTable != null) {
            loadWaitlist();
        }

        // Load loyalty members if on loyalty screen
        if (loyaltyMembersTable != null) {
            loadLoyaltyMembers();
            initializeLoyaltySettings();
        }

        // Load checkout table if on checkout screen
        if (checkoutTable != null) {
            loadCheckedInReservations();
        }

        // Load pending payments if on payments screen
        if (pendingPaymentsTable != null) {
            loadPendingPayments();
        }

        // Load reports data if on reports screen
        if (revenueTable != null || occupancyTable != null || activityLogsTable != null) {
            loadReportsData();
        }
    }

    private void loadCheckedInReservations() {
        List<Reservation> checkedIn = reservationService.getReservationsByStatus(ReservationStatus.CHECKED_IN);
        checkoutTable.setItems(FXCollections.observableArrayList(checkedIn));
    }

    private void loadDashboardStats() {
        LocalDate today = LocalDate.now();
        List<Reservation> allReservations = reservationService.getAllReservations();

        // Count today's check-ins
        long checkIns = allReservations.stream()
            .filter(r -> r.getCheckInDate().equals(today))
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.CHECKED_IN)
            .count();

        // Count today's check-outs
        long checkOuts = allReservations.stream()
            .filter(r -> r.getCheckOutDate().equals(today))
            .filter(r -> r.getStatus() == ReservationStatus.CHECKED_IN)
            .count();

        // Calculate occupancy
        RoomRepository roomRepo = new RoomRepository();
        long totalRooms = roomRepo.count();
        long occupiedRooms = allReservations.stream()
            .filter(r -> r.getStatus() == ReservationStatus.CHECKED_IN)
            .mapToLong(r -> r.getRooms().size())
            .sum();

        // Count pending payments (reservations with balance > 0)
        long pendingPayments = allReservations.stream()
            .filter(r -> r.getStatus() != ReservationStatus.CANCELLED && r.getStatus() != ReservationStatus.CHECKED_OUT)
            .filter(r -> r.getBalance() > 0)
            .count();

        // Update labels
        if (todayCheckInsLabel != null) {
            todayCheckInsLabel.setText(String.valueOf(checkIns));
        }
        if (todayCheckOutsLabel != null) {
            todayCheckOutsLabel.setText(String.valueOf(checkOuts));
        }
        if (occupancyLabel != null) {
            occupancyLabel.setText(occupiedRooms + "/" + totalRooms);
        }
        if (pendingPaymentsLabel != null) {
            pendingPaymentsLabel.setText(String.valueOf(pendingPayments));
        }
    }

    private void loadRecentReservations() {
        List<Reservation> allReservations = reservationService.getAllReservations();
        // Get most recent 10 reservations
        List<Reservation> recent = allReservations.stream()
            .sorted((r1, r2) -> {
                if (r1.getId() == null) return 1;
                if (r2.getId() == null) return -1;
                return r2.getId().compareTo(r1.getId());
            })
            .limit(10)
            .toList();

        recentReservationsTable.setItems(FXCollections.observableArrayList(recent));
    }

    private void loadAllGuests() {
        GuestRepository guestRepo = new GuestRepository();
        List<Guest> guests = guestRepo.findAll();
        guestsTable.setItems(FXCollections.observableArrayList(guests));
        if (guestCountLabel != null) {
            guestCountLabel.setText("Showing " + guests.size() + " guests");
        }
    }

    // ========== Authentication ==========

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        if (authService.login(username, password)) {
            loadScreen("/view/admin/AdminDashboard.fxml", "NewnhamNexus - Dashboard");
        } else {
            if (loginErrorLabel != null) {
                loginErrorLabel.setText("Invalid username or password");
                loginErrorLabel.setVisible(true);
            } else {
                showError("Invalid username or password");
            }
        }
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        loadScreen("/view/Launcher.fxml", "NewnhamNexus - Reservation System");
    }

    @FXML
    private void backToLauncher() {
        authService.logout();
        loadScreen("/view/Launcher.fxml", "NewnhamNexus - Reservation System");
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Recovery");
        alert.setHeaderText("Forgot Your Password?");
        alert.setContentText(
            "Please contact the system administrator to reset your password:\n\n" +
            "Email: admin@newnhamnexus.com\n" +
            "Phone: (416) 555-0100\n" +
            "Office: Front Desk, Room 101\n\n" +
            "For security reasons, password resets require identity verification."
        );
        alert.showAndWait();
    }

    // ========== Navigation ==========

    @FXML private void showDashboard() { loadScreen("/view/admin/AdminDashboard.fxml", "NewnhamNexus - Dashboard"); }
    @FXML private void showReservations() { loadScreen("/view/admin/AdminReservations.fxml", "NewnhamNexus - Reservations"); }
    @FXML private void showGuests() { loadScreen("/view/admin/AdminGuests.fxml", "NewnhamNexus - Guests"); }
    @FXML private void showPayments() { loadScreen("/view/admin/AdminPayments.fxml", "NewnhamNexus - Payments"); }
    @FXML private void showCheckout() { loadScreen("/view/admin/AdminCheckout.fxml", "NewnhamNexus - Check-out"); }
    @FXML private void showWaitlist() { loadScreen("/view/admin/AdminWaitlist.fxml", "NewnhamNexus - Waitlist"); }
    @FXML private void showFeedbackManagement() { loadScreen("/view/admin/AdminFeedback.fxml", "NewnhamNexus - Feedback"); }
    @FXML private void showLoyalty() { loadScreen("/view/admin/AdminLoyalty.fxml", "NewnhamNexus - Loyalty Program"); }
    @FXML private void showReports() { loadScreen("/view/admin/AdminReports.fxml", "NewnhamNexus - Reports"); }

    // ========== Reservations ==========

    private void loadReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        if (reservationsTable != null) {
            reservationsTable.setItems(FXCollections.observableArrayList(reservations));
        }
        if (resultCountLabel != null) {
            resultCountLabel.setText("Showing " + reservations.size() + " results");
        }
    }

    @FXML
    private void searchReservations() {
        String searchTerm = searchField != null ? searchField.getText().trim() : "";
        String status = statusFilter != null ? statusFilter.getValue() : "All";
        LocalDate fromDate = fromDateFilter != null ? fromDateFilter.getValue() : null;
        LocalDate toDate = toDateFilter != null ? toDateFilter.getValue() : null;

        List<Reservation> results;

        if (!searchTerm.isEmpty()) {
            // Search by confirmation number or guest name
            Reservation byConf = reservationService.findByConfirmationNumber(searchTerm);
            if (byConf != null) {
                results = List.of(byConf);
            } else {
                results = reservationService.getAllReservations().stream()
                    .filter(r -> r.getGuest().getFullName().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
            }
        } else if (fromDate != null && toDate != null) {
            results = reservationService.getReservationsByDateRange(fromDate, toDate);
        } else if (!"All".equals(status)) {
            ReservationStatus statusEnum = parseStatus(status);
            if (statusEnum != null) {
                results = reservationService.getReservationsByStatus(statusEnum);
            } else {
                results = reservationService.getAllReservations();
            }
        } else {
            results = reservationService.getAllReservations();
        }

        if (reservationsTable != null) {
            reservationsTable.setItems(FXCollections.observableArrayList(results));
        }
        if (resultCountLabel != null) {
            resultCountLabel.setText("Showing " + results.size() + " results");
        }

        auditService.log(authService.getCurrentUsername(), "SEARCH", "Reservation", null,
            "Searched reservations: " + searchTerm);
    }

    @FXML
    private void clearFilters() {
        if (searchField != null) searchField.clear();
        if (statusFilter != null) statusFilter.setValue("All");
        if (fromDateFilter != null) fromDateFilter.setValue(null);
        if (toDateFilter != null) toDateFilter.setValue(null);
        loadReservations();
    }

    @FXML
    private void newReservation() {
        // Create a dialog for phone booking
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("New Phone Reservation");
        dialog.setHeaderText("Create reservation from phone call");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create Reservation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        Spinner<Integer> adultsSpinner = new Spinner<>(1, 10, 1);
        Spinner<Integer> childrenSpinner = new Spinner<>(0, 10, 0);
        DatePicker checkInPicker = new DatePicker(LocalDate.now());
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        ComboBox<String> roomTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "SINGLE", "DOUBLE", "DELUXE", "PENTHOUSE"
        ));
        roomTypeCombo.setValue("SINGLE");

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Adults:"), 0, 4);
        grid.add(adultsSpinner, 1, 4);
        grid.add(new Label("Children:"), 0, 5);
        grid.add(childrenSpinner, 1, 5);
        grid.add(new Label("Check-in:"), 0, 6);
        grid.add(checkInPicker, 1, 6);
        grid.add(new Label("Check-out:"), 0, 7);
        grid.add(checkOutPicker, 1, 7);
        grid.add(new Label("Room Type:"), 0, 8);
        grid.add(roomTypeCombo, 1, 8);

        dialog.getDialogPane().setContent(grid);

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    // Validate
                    if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
                        showError("First and last name are required");
                        return null;
                    }
                    if (checkOutPicker.getValue().isBefore(checkInPicker.getValue()) ||
                        checkOutPicker.getValue().equals(checkInPicker.getValue())) {
                        showError("Check-out date must be after check-in date");
                        return null;
                    }

                    // Create guest
                    Guest guest = new Guest(
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim()
                    );

                    // Find available room
                    RoomType roomType = RoomType.valueOf(roomTypeCombo.getValue());
                    List<Room> availableRooms = reservationService.getAvailableRoomsByType(
                        roomType, checkInPicker.getValue(), checkOutPicker.getValue());

                    if (availableRooms.isEmpty()) {
                        showError("No " + roomType + " rooms available for the selected dates");
                        return null;
                    }

                    // Create reservation with first available room
                    Reservation reservation = reservationService.createReservation(
                        guest,
                        adultsSpinner.getValue(),
                        childrenSpinner.getValue(),
                        checkInPicker.getValue(),
                        checkOutPicker.getValue(),
                        List.of(availableRooms.get(0)),
                        List.of()
                    );

                    return reservation;
                } catch (Exception e) {
                    showError("Failed to create reservation: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reservation -> {
            showInfo("Reservation Created", "Success",
                "Reservation " + reservation.getConfirmationNumber() + " created successfully.\n" +
                "Total: $" + String.format("%.2f", reservation.getTotal()) + "\n" +
                "Please inform the guest that payment can be made at check-in or via the front desk.");
            auditService.log(authService.getCurrentUsername(), "CREATE", "Reservation",
                reservation.getId().toString(), "Created phone reservation: " + reservation.getConfirmationNumber());

            // Refresh data based on current screen
            if (reservationsTable != null) {
                loadReservations();
            }
            if (recentReservationsTable != null) {
                loadRecentReservations();
                loadDashboardStats();
            }
        });
    }

    // ========== Payments ==========

    @FXML
    private void searchForPayment() {
        String searchTerm = paymentSearchField != null ? paymentSearchField.getText().trim() : "";
        if (searchTerm.isEmpty()) {
            showError("Please enter a confirmation number or guest name");
            return;
        }

        // First try to find by exact confirmation number
        Reservation reservation = reservationService.findByConfirmationNumber(searchTerm);

        if (reservation != null) {
            currentReservation = reservation;
            updatePaymentDisplay();
            clearPaymentInputFields();
            // Also select it in the pending payments table if present
            if (pendingPaymentsTable != null) {
                pendingPaymentsTable.getSelectionModel().select(reservation);
            }
        } else {
            // Search by partial confirmation number OR guest name
            List<Reservation> results = reservationService.getAllReservations().stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .filter(r -> r.getConfirmationNumber().toLowerCase().contains(searchTerm.toLowerCase()) ||
                             r.getGuest().getFullName().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();

            if (results.isEmpty()) {
                showError("No reservation found for: " + searchTerm);
            } else if (results.size() == 1) {
                currentReservation = results.get(0);
                updatePaymentDisplay();
                clearPaymentInputFields();
                if (pendingPaymentsTable != null) {
                    pendingPaymentsTable.getSelectionModel().select(currentReservation);
                }
            } else {
                // Multiple results - filter to show in pending payments table
                if (pendingPaymentsTable != null) {
                    pendingPaymentsTable.setItems(FXCollections.observableArrayList(results));
                }
                showInfo("Multiple Results", "Found " + results.size() + " reservations",
                    "Please select a reservation from the table.");
            }
        }
    }

    private void updatePaymentDisplay() {
        if (currentReservation == null) return;

        // Update selected reservation info
        if (paymentConfNum != null) {
            paymentConfNum.setText(currentReservation.getConfirmationNumber());
        }
        if (paymentGuestName != null) {
            paymentGuestName.setText(currentReservation.getGuest().getFullName());
        }
        if (paymentTotalLabel != null) {
            paymentTotalLabel.setText(String.format("$%.2f", currentReservation.getTotal()));
        }
        if (paymentPaidLabel != null) {
            paymentPaidLabel.setText(String.format("$%.2f", currentReservation.getAmountPaid()));
        }
        if (paymentBalanceLabel != null) {
            paymentBalanceLabel.setText(String.format("$%.2f", currentReservation.getBalance()));
            // Color code balance
            if (currentReservation.getBalance() <= 0) {
                paymentBalanceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
            } else {
                paymentBalanceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            }
        }

        // Update loyalty points section if guest has points
        // Refresh guest from database to get current loyalty points
        if (loyaltyPointsSection != null && availablePointsLabel != null) {
            GuestRepository guestRepo = new GuestRepository();
            Guest guest = guestRepo.findById(currentReservation.getGuest().getId()).orElse(null);
            if (guest != null && guest.getLoyaltyNumber() != null && guest.getLoyaltyPoints() > 0) {
                loyaltyPointsSection.setVisible(true);
                loyaltyPointsSection.setManaged(true);
                double pointsValue = guest.getLoyaltyPoints() * 0.01; // $0.01 per point
                availablePointsLabel.setText(String.format("%d pts (= $%.2f)", guest.getLoyaltyPoints(), pointsValue));
            } else {
                loyaltyPointsSection.setVisible(false);
                loyaltyPointsSection.setManaged(false);
            }
        }

        // Load payment history
        if (paymentHistoryTable != null) {
            List<Payment> payments = paymentService.getPaymentHistory(currentReservation);
            paymentHistoryTable.setItems(FXCollections.observableArrayList(payments));
        }
    }

    @FXML
    private void payFullBalance() {
        if (currentReservation == null) {
            showError("Please select a reservation first");
            return;
        }

        if (currentReservation.isFullyPaid()) {
            showInfo("Payment", "Already Paid", "This reservation has already been fully paid.");
            return;
        }

        // Just fill in the amount field with the balance - don't process yet
        if (paymentAmountField != null) {
            paymentAmountField.setText(String.format("%.2f", currentReservation.getBalance()));
        }
        // Set transaction type to Payment
        if (transactionTypeCombo != null) {
            transactionTypeCombo.setValue("Payment");
        }
    }

    @FXML
    private void processPayment() {
        if (currentReservation == null) {
            showError("Please select a reservation first");
            return;
        }

        String amountStr = paymentAmountField != null ? paymentAmountField.getText().trim() : "";
        if (amountStr.isEmpty()) {
            showError("Please enter an amount");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showError("Please enter a positive amount");
                return;
            }

            String methodStr = paymentMethodCombo != null ? paymentMethodCombo.getValue() : "Cash";
            String transactionType = transactionTypeCombo != null ? transactionTypeCombo.getValue() : "Payment";
            PaymentMethod method = parsePaymentMethod(methodStr);

            Payment payment;
            String actionType;
            String successTitle;
            String successHeader;

            if ("Refund".equals(transactionType)) {
                // Use processRefund for refunds - it expects positive amount
                payment = paymentService.processRefund(currentReservation, method, amount,
                    "Admin refund", authService.getCurrentUsername());
                actionType = "Refund";
                successTitle = "Refund Processed";
                successHeader = "Refund Successful";
            } else {
                // Use processPayment for payments and deposits
                payment = paymentService.processPayment(currentReservation,
                    method, amount, authService.getCurrentUsername());
                actionType = transactionType;
                successTitle = transactionType + " Successful";
                successHeader = transactionType + " Processed";
            }

            showInfo(successTitle, successHeader,
                String.format("%s of $%.2f processed successfully.\nTransaction: %s",
                    actionType, Math.abs(payment.getAmount()), payment.getTransactionReference()));

            updatePaymentDisplay();
            loadPendingPayments(); // Refresh pending payments table
            if (paymentAmountField != null) paymentAmountField.clear();
        } catch (NumberFormatException e) {
            showError("Invalid amount format. Please enter a valid number.");
        } catch (Exception e) {
            showError("Transaction failed: " + e.getMessage());
        }
    }

    // ========== Checkout ==========

    @FXML
    private void searchForCheckout() {
        String searchTerm = checkoutSearchField != null ? checkoutSearchField.getText().trim() : "";
        if (searchTerm.isEmpty()) {
            showError("Please enter a confirmation number or guest name");
            return;
        }

        // First try exact confirmation number match
        Reservation reservation = reservationService.findByConfirmationNumber(searchTerm);

        if (reservation != null) {
            if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
                showError("Guest must be checked-in before checkout. Current status: " + formatStatus(reservation.getStatus()));
                return;
            }
            currentReservation = reservation;
            updateCheckoutDisplay();
            if (discountField != null) discountField.clear();
            if (checkoutTable != null) {
                checkoutTable.getSelectionModel().select(reservation);
            }
        } else {
            // Search by partial confirmation number OR guest name among checked-in reservations
            List<Reservation> results = reservationService.getReservationsByStatus(ReservationStatus.CHECKED_IN).stream()
                .filter(r -> r.getConfirmationNumber().toLowerCase().contains(searchTerm.toLowerCase()) ||
                             r.getGuest().getFullName().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();

            if (results.isEmpty()) {
                showError("No checked-in reservation found for: " + searchTerm);
            } else if (results.size() == 1) {
                currentReservation = results.get(0);
                updateCheckoutDisplay();
                if (discountField != null) discountField.clear();
                if (checkoutTable != null) {
                    checkoutTable.getSelectionModel().select(currentReservation);
                }
            } else {
                // Multiple results - show in table
                if (checkoutTable != null) {
                    checkoutTable.setItems(FXCollections.observableArrayList(results));
                }
                showInfo("Multiple Results", "Found " + results.size() + " reservations",
                    "Please select a reservation from the table.");
            }
        }
    }

    @FXML
    private void applyDiscount() {
        if (currentReservation == null) {
            showError("Please select a reservation first");
            return;
        }

        String discountStr = discountField != null ? discountField.getText().trim() : "";
        if (discountStr.isEmpty()) {
            showError("Please enter a discount percentage");
            return;
        }

        try {
            double discountPercent = Double.parseDouble(discountStr);
            double maxAllowed = authService.getMaxDiscountPercent();

            double discountAmount = paymentService.applyDiscount(currentReservation,
                discountPercent, authService.getCurrentUsername(), maxAllowed);

            showInfo("Discount Applied", "Discount Successful",
                String.format("%.1f%% discount ($%.2f) applied successfully.", discountPercent, discountAmount));
            updateCheckoutDisplay();
            loadCheckedInReservations(); // Refresh the table to show updated balance
            if (discountField != null) discountField.clear();
        } catch (NumberFormatException e) {
            showError("Invalid discount format. Please enter a number.");
        } catch (Exception e) {
            showError("Discount failed: " + e.getMessage());
        }
    }

    private void updateCheckoutDisplay() {
        if (currentReservation == null) return;

        List<Room> rooms = currentReservation.getRooms();

        // Room info
        if (checkoutRoomLabel != null) {
            checkoutRoomLabel.setText(rooms.isEmpty() ? "N/A" : "#" + rooms.get(0).getRoomNumber());
        }
        if (checkoutRoomTypeLabel != null) {
            checkoutRoomTypeLabel.setText(rooms.isEmpty() ? "-" : formatRoomType(rooms.get(0).getType()));
        }

        // Guest info
        if (checkoutGuestLabel != null) {
            checkoutGuestLabel.setText(currentReservation.getGuest().getFullName());
        }
        if (checkoutConfLabel != null) {
            checkoutConfLabel.setText(currentReservation.getConfirmationNumber());
        }
        if (checkoutStayLabel != null) {
            long nights = java.time.temporal.ChronoUnit.DAYS.between(
                currentReservation.getCheckInDate(), currentReservation.getCheckOutDate());
            checkoutStayLabel.setText(String.format("%s to %s (%d night%s)",
                currentReservation.getCheckInDate(),
                currentReservation.getCheckOutDate(),
                nights, nights == 1 ? "" : "s"));
        }

        // Billing info - handle discount display
        boolean hasDiscount = currentReservation.hasDiscount();

        // Original subtotal (before discount)
        if (checkoutOriginalSubtotalLabel != null) {
            double originalSubtotal = currentReservation.getSubtotal() + currentReservation.getDiscountAmount();
            checkoutOriginalSubtotalLabel.setText(String.format("$%.2f", hasDiscount ? originalSubtotal : currentReservation.getSubtotal()));
        }

        // Discount row (only show if discount applied)
        if (checkoutDiscountDesc != null && checkoutDiscountLabel != null) {
            checkoutDiscountDesc.setVisible(hasDiscount);
            checkoutDiscountDesc.setManaged(hasDiscount);
            checkoutDiscountLabel.setVisible(hasDiscount);
            checkoutDiscountLabel.setManaged(hasDiscount);
            if (hasDiscount) {
                checkoutDiscountDesc.setText(String.format("Discount (%.0f%%)", currentReservation.getDiscountPercent()));
                checkoutDiscountLabel.setText(String.format("-$%.2f", currentReservation.getDiscountAmount()));
            }
        }

        // Subtotal after discount (only show if discount applied)
        if (checkoutSubtotalDesc != null && checkoutSubtotalLabel != null) {
            checkoutSubtotalDesc.setVisible(hasDiscount);
            checkoutSubtotalDesc.setManaged(hasDiscount);
            checkoutSubtotalLabel.setVisible(hasDiscount);
            checkoutSubtotalLabel.setManaged(hasDiscount);
            if (hasDiscount) {
                checkoutSubtotalDesc.setText("After Discount");
                checkoutSubtotalLabel.setText(String.format("$%.2f", currentReservation.getSubtotal()));
            }
        }

        if (checkoutTaxDescLabel != null) {
            checkoutTaxDescLabel.setText("Tax (13% HST)");
        }
        if (checkoutTaxLabel != null) {
            checkoutTaxLabel.setText(String.format("$%.2f", currentReservation.getTax()));
        }
        if (checkoutTotalLabel != null) {
            checkoutTotalLabel.setText(String.format("$%.2f", currentReservation.getTotal()));
        }
        if (checkoutPaidLabel != null) {
            checkoutPaidLabel.setText(String.format("$%.2f", currentReservation.getAmountPaid()));
        }
        if (checkoutBalanceLabel != null) {
            double balance = currentReservation.getBalance();
            // Handle negative balance (refund due) vs positive balance (payment due)
            if (balance < 0) {
                // Overpayment - show as refund due
                if (checkoutBalanceDescLabel != null) {
                    checkoutBalanceDescLabel.setText("Refund Due");
                    checkoutBalanceDescLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #3498db;");
                }
                checkoutBalanceLabel.setText(String.format("$%.2f", Math.abs(balance)));
                checkoutBalanceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #3498db;");
            } else if (balance == 0) {
                // Fully paid - no balance
                if (checkoutBalanceDescLabel != null) {
                    checkoutBalanceDescLabel.setText("Balance Due");
                    checkoutBalanceDescLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #27ae60;");
                }
                checkoutBalanceLabel.setText("$0.00");
                checkoutBalanceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #27ae60;");
            } else {
                // Outstanding balance
                if (checkoutBalanceDescLabel != null) {
                    checkoutBalanceDescLabel.setText("Balance Due");
                    checkoutBalanceDescLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                }
                checkoutBalanceLabel.setText(String.format("$%.2f", balance));
                checkoutBalanceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e74c3c;");
            }
        }

        // Show/hide balance warning
        if (balanceWarning != null) {
            boolean hasBalance = currentReservation.getBalance() > 0;
            balanceWarning.setVisible(hasBalance);
            balanceWarning.setManaged(hasBalance);
        }
    }

    /**
     * Format RoomType enum to human-readable string.
     */
    private String formatRoomType(RoomType type) {
        if (type == null) return "";
        return switch (type) {
            case SINGLE -> "Single Room";
            case DOUBLE -> "Double Room";
            case DELUXE -> "Deluxe Room";
            case PENTHOUSE -> "Penthouse Suite";
        };
    }

    @FXML
    private void generateBillPDF() {
        if (currentReservation == null) {
            showError("Please search for a reservation first");
            return;
        }

        // Generate bill summary
        StringBuilder bill = new StringBuilder();
        bill.append("═".repeat(45)).append("\n");
        bill.append("        NEWNHAM NEXUS HOTEL\n");
        bill.append("            FINAL BILL\n");
        bill.append("═".repeat(45)).append("\n\n");
        bill.append("Confirmation: ").append(currentReservation.getConfirmationNumber()).append("\n");
        bill.append("Guest:        ").append(currentReservation.getGuest().getFullName()).append("\n");
        bill.append("Check-in:     ").append(currentReservation.getCheckInDate()).append("\n");
        bill.append("Check-out:    ").append(currentReservation.getCheckOutDate()).append("\n");
        if (!currentReservation.getRooms().isEmpty()) {
            bill.append("Room:         #").append(currentReservation.getRooms().get(0).getRoomNumber());
            bill.append(" (").append(formatRoomType(currentReservation.getRooms().get(0).getType())).append(")\n");
        }
        bill.append("\n");
        bill.append("─".repeat(45)).append("\n");

        // Show discount if applied
        if (currentReservation.hasDiscount()) {
            double originalSubtotal = currentReservation.getSubtotal() + currentReservation.getDiscountAmount();
            bill.append(String.format("%-30s %13s\n", "Subtotal:", String.format("$%.2f", originalSubtotal)));
            bill.append(String.format("%-30s %13s\n",
                String.format("Discount (%.0f%%):", currentReservation.getDiscountPercent()),
                String.format("-$%.2f", currentReservation.getDiscountAmount())));
            bill.append(String.format("%-30s %13s\n", "After Discount:", String.format("$%.2f", currentReservation.getSubtotal())));
        } else {
            bill.append(String.format("%-30s %13s\n", "Subtotal:", String.format("$%.2f", currentReservation.getSubtotal())));
        }

        bill.append(String.format("%-30s %13s\n", "Tax (13% HST):", String.format("$%.2f", currentReservation.getTax())));
        bill.append("─".repeat(45)).append("\n");
        bill.append(String.format("%-30s %13s\n", "TOTAL:", String.format("$%.2f", currentReservation.getTotal())));
        bill.append(String.format("%-30s %13s\n", "Amount Paid:", String.format("$%.2f", currentReservation.getAmountPaid())));
        double balance = currentReservation.getBalance();
        if (balance < 0) {
            bill.append(String.format("%-30s %13s\n", "REFUND DUE:", String.format("$%.2f", Math.abs(balance))));
        } else {
            bill.append(String.format("%-30s %13s\n", "Balance Due:", String.format("$%.2f", balance)));
        }
        bill.append("═".repeat(45)).append("\n\n");
        bill.append("    Thank you for staying with us!\n");
        bill.append("   We hope to see you again soon.\n");

        // Show bill and offer to save
        TextArea preview = new TextArea(bill.toString());
        preview.setEditable(false);
        preview.setWrapText(false);
        preview.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        preview.setPrefRowCount(20);
        preview.setPrefColumnCount(45);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Final Bill");
        alert.setHeaderText("Bill Preview");
        alert.getDialogPane().setContent(preview);
        alert.setResizable(true);
        alert.showAndWait();

        auditService.log(authService.getCurrentUsername(), "GENERATE_BILL", "Reservation",
            currentReservation.getId().toString(), "Generated bill for " + currentReservation.getConfirmationNumber());
    }

    @FXML
    private void printReceipt() {
        if (currentReservation == null) {
            showError("Please select a reservation first");
            return;
        }

        // Generate receipt
        StringBuilder receipt = new StringBuilder();
        receipt.append("╔══════════════════════════════════════╗\n");
        receipt.append("║      NEWNHAM NEXUS HOTEL             ║\n");
        receipt.append("║           RECEIPT                    ║\n");
        receipt.append("╚══════════════════════════════════════╝\n\n");
        receipt.append("Date: ").append(LocalDate.now()).append("\n");
        receipt.append("Confirmation: ").append(currentReservation.getConfirmationNumber()).append("\n");
        receipt.append("Guest: ").append(currentReservation.getGuest().getFullName()).append("\n\n");
        receipt.append("─".repeat(40)).append("\n");

        // Show discount if applied
        if (currentReservation.hasDiscount()) {
            double originalSubtotal = currentReservation.getSubtotal() + currentReservation.getDiscountAmount();
            receipt.append(String.format("%-25s %13s\n", "Subtotal:", String.format("$%.2f", originalSubtotal)));
            receipt.append(String.format("%-25s %13s\n",
                String.format("Discount (%.0f%%):", currentReservation.getDiscountPercent()),
                String.format("-$%.2f", currentReservation.getDiscountAmount())));
        } else {
            receipt.append(String.format("%-25s %13s\n", "Subtotal:", String.format("$%.2f", currentReservation.getSubtotal())));
        }

        receipt.append(String.format("%-25s %13s\n", "Tax (13% HST):", String.format("$%.2f", currentReservation.getTax())));
        receipt.append("─".repeat(40)).append("\n");
        receipt.append(String.format("%-25s %13s\n", "TOTAL:", String.format("$%.2f", currentReservation.getTotal())));
        receipt.append(String.format("%-25s %13s\n", "Amount Paid:", String.format("$%.2f", currentReservation.getAmountPaid())));
        double receiptBalance = currentReservation.getBalance();
        if (receiptBalance < 0) {
            receipt.append(String.format("%-25s %13s\n", "REFUND DUE:", String.format("$%.2f", Math.abs(receiptBalance))));
        } else if (receiptBalance > 0) {
            receipt.append(String.format("%-25s %13s\n", "Balance Due:", String.format("$%.2f", receiptBalance)));
        }
        receipt.append("─".repeat(40)).append("\n\n");
        receipt.append("  Thank you for staying with us!\n");
        receipt.append("   Please visit us again soon.\n");

        // Show receipt preview
        TextArea preview = new TextArea(receipt.toString());
        preview.setEditable(false);
        preview.setWrapText(false);
        preview.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        preview.setPrefRowCount(18);
        preview.setPrefColumnCount(42);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Receipt");
        alert.setHeaderText("Receipt Preview - Ready to Print");
        alert.getDialogPane().setContent(preview);
        alert.setResizable(true);

        ButtonType printButton = new ButtonType("Print", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(printButton, closeButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == printButton) {
                showInfo("Print", "Print Job Sent", "Receipt has been sent to the printer.");
                auditService.log(authService.getCurrentUsername(), "PRINT_RECEIPT", "Reservation",
                    currentReservation.getId().toString(), "Printed receipt for " + currentReservation.getConfirmationNumber());
            }
        });
    }

    @FXML
    private void completeCheckout() {
        if (currentReservation == null) {
            showError("Please select a reservation first");
            return;
        }

        if (!currentReservation.isFullyPaid()) {
            showError("Cannot checkout: Outstanding balance of $" +
                String.format("%.2f", currentReservation.getBalance()));
            return;
        }

        try {
            String guestName = currentReservation.getGuest().getFullName();
            double checkoutBalance = currentReservation.getBalance();

            reservationService.checkOut(currentReservation);

            // Check waitlist for room availability and publish events via Observer pattern
            RoomAvailabilityNotifier notifier = RoomAvailabilityNotifier.getInstance();
            for (Room room : currentReservation.getRooms()) {
                // Notify waitlist service (internal observer)
                var notifiedEntries = waitlistService.checkAndNotify(room.getType(),
                    LocalDate.now(), LocalDate.now().plusMonths(1));

                // Publish event via RoomAvailabilityNotifier (Observer pattern)
                RoomAvailabilityEvent event = new RoomAvailabilityEvent(
                    room.getId(),
                    room.getRoomNumber(),
                    room.getType(),
                    LocalDateTime.now(),
                    notifiedEntries.size()
                );
                notifier.publish(event);
            }

            auditService.log(authService.getCurrentUsername(), "CHECKOUT", "Reservation",
                currentReservation.getId().toString(),
                "Checked out guest: " + guestName);

            // Build checkout message based on balance
            String checkoutMessage = guestName + " has been checked out.";
            if (checkoutBalance < 0) {
                checkoutMessage += String.format("\n\n⚠️ REFUND DUE: $%.2f\nPlease process refund to guest.", Math.abs(checkoutBalance));
            }
            checkoutMessage += "\n\nPlease remind the guest to provide feedback at the kiosk.";

            showInfo("Check-out Complete", "Guest Checked Out Successfully", checkoutMessage);

            currentReservation = null;
            clearCheckoutDisplay();
            loadCheckedInReservations(); // Refresh the table
        } catch (Exception e) {
            showError("Checkout failed: " + e.getMessage());
        }
    }

    private void clearCheckoutDisplay() {
        if (checkoutRoomLabel != null) checkoutRoomLabel.setText("Select a reservation");
        if (checkoutRoomTypeLabel != null) checkoutRoomTypeLabel.setText("-");
        if (checkoutGuestLabel != null) checkoutGuestLabel.setText("-");
        if (checkoutConfLabel != null) checkoutConfLabel.setText("-");
        if (checkoutStayLabel != null) checkoutStayLabel.setText("-");
        if (checkoutOriginalSubtotalLabel != null) checkoutOriginalSubtotalLabel.setText("$0.00");
        if (checkoutDiscountDesc != null) {
            checkoutDiscountDesc.setVisible(false);
            checkoutDiscountDesc.setManaged(false);
        }
        if (checkoutDiscountLabel != null) {
            checkoutDiscountLabel.setVisible(false);
            checkoutDiscountLabel.setManaged(false);
        }
        if (checkoutSubtotalDesc != null) {
            checkoutSubtotalDesc.setVisible(false);
            checkoutSubtotalDesc.setManaged(false);
        }
        if (checkoutSubtotalLabel != null) {
            checkoutSubtotalLabel.setVisible(false);
            checkoutSubtotalLabel.setManaged(false);
        }
        if (checkoutTaxDescLabel != null) checkoutTaxDescLabel.setText("Tax (13% HST)");
        if (checkoutTaxLabel != null) checkoutTaxLabel.setText("$0.00");
        if (checkoutTotalLabel != null) checkoutTotalLabel.setText("$0.00");
        if (checkoutPaidLabel != null) checkoutPaidLabel.setText("$0.00");
        if (checkoutBalanceDescLabel != null) {
            checkoutBalanceDescLabel.setText("Balance Due");
            checkoutBalanceDescLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e74c3c;");
        }
        if (checkoutBalanceLabel != null) {
            checkoutBalanceLabel.setText("$0.00");
            checkoutBalanceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        }
        if (balanceWarning != null) {
            balanceWarning.setVisible(false);
            balanceWarning.setManaged(false);
        }
        if (discountField != null) discountField.clear();
        if (checkoutSearchField != null) checkoutSearchField.clear();
        if (checkoutTable != null) checkoutTable.getSelectionModel().clearSelection();
    }

    // ========== Waitlist ==========

    private void loadWaitlist() {
        List<Waitlist> entries = waitlistService.getActiveEntries();
        if (waitlistTable != null) {
            waitlistTable.setItems(FXCollections.observableArrayList(entries));
        }
        updateWaitlistStats();
    }

    private void updateWaitlistStats() {
        List<Waitlist> entries = waitlistService.getActiveEntries();

        if (waitlistTotalLabel != null) {
            waitlistTotalLabel.setText(String.valueOf(entries.size()));
        }

        // Count by room type
        long singleCount = entries.stream().filter(e -> e.getRoomType() == RoomType.SINGLE).count();
        long doubleCount = entries.stream().filter(e -> e.getRoomType() == RoomType.DOUBLE).count();
        long deluxeCount = entries.stream().filter(e -> e.getRoomType() == RoomType.DELUXE).count();
        long penthouseCount = entries.stream().filter(e -> e.getRoomType() == RoomType.PENTHOUSE).count();

        if (waitlistSingleLabel != null) waitlistSingleLabel.setText(String.valueOf(singleCount));
        if (waitlistDoubleLabel != null) waitlistDoubleLabel.setText(String.valueOf(doubleCount));
        if (waitlistDeluxeLabel != null) waitlistDeluxeLabel.setText(String.valueOf(deluxeCount));
        if (waitlistPenthouseLabel != null) waitlistPenthouseLabel.setText(String.valueOf(penthouseCount));
    }

    @FXML
    private void addToWaitlist() {
        // The right panel form is already visible in the FXML
        // Clear the form fields
        if (waitlistGuestName != null) waitlistGuestName.clear();
        if (waitlistPhone != null) waitlistPhone.clear();
        if (waitlistEmail != null) waitlistEmail.clear();
        if (waitlistRoomType != null) waitlistRoomType.setValue(null);
        if (waitlistCheckIn != null) waitlistCheckIn.setValue(null);
        if (waitlistCheckOut != null) waitlistCheckOut.setValue(null);
        if (waitlistNotes != null) waitlistNotes.clear();
    }

    @FXML
    private void filterWaitlist() {
        String roomType = waitlistRoomTypeCombo != null ? waitlistRoomTypeCombo.getValue() : null;

        List<Waitlist> results;
        if (roomType != null && !roomType.equals("All")) {
            try {
                results = waitlistService.getEntriesByRoomType(RoomType.valueOf(roomType));
            } catch (IllegalArgumentException e) {
                results = waitlistService.getActiveEntries();
            }
        } else {
            results = waitlistService.getActiveEntries();
        }

        if (waitlistTable != null) {
            waitlistTable.setItems(FXCollections.observableArrayList(results));
        }
    }

    @FXML
    private void clearWaitlistFilter() {
        if (waitlistRoomTypeCombo != null) {
            waitlistRoomTypeCombo.setValue("All");
        }
        loadWaitlist();
    }

    @FXML
    private void clearWaitlistForm() {
        if (waitlistGuestName != null) waitlistGuestName.clear();
        if (waitlistPhone != null) waitlistPhone.clear();
        if (waitlistEmail != null) waitlistEmail.clear();
        if (waitlistRoomType != null) waitlistRoomType.setValue(null);
        if (waitlistCheckIn != null) waitlistCheckIn.setValue(null);
        if (waitlistCheckOut != null) waitlistCheckOut.setValue(null);
        if (waitlistAdults != null) waitlistAdults.getValueFactory().setValue(1);
        if (waitlistChildren != null) waitlistChildren.getValueFactory().setValue(0);
        if (waitlistNotes != null) waitlistNotes.clear();
    }

    @FXML
    private void confirmAddWaitlist() {
        // Validate required fields
        if (waitlistGuestName == null || waitlistGuestName.getText().trim().isEmpty()) {
            showError("Please enter guest name");
            return;
        }
        if (waitlistPhone == null || waitlistPhone.getText().trim().isEmpty()) {
            showError("Please enter phone number");
            return;
        }
        if (waitlistRoomType == null || waitlistRoomType.getValue() == null) {
            showError("Please select room type");
            return;
        }
        if (waitlistCheckIn == null || waitlistCheckIn.getValue() == null) {
            showError("Please select check-in date");
            return;
        }
        if (waitlistCheckOut == null || waitlistCheckOut.getValue() == null) {
            showError("Please select check-out date");
            return;
        }
        if (waitlistCheckOut.getValue().isBefore(waitlistCheckIn.getValue()) ||
            waitlistCheckOut.getValue().isEqual(waitlistCheckIn.getValue())) {
            showError("Check-out date must be after check-in date");
            return;
        }

        try {
            // Create or find guest
            String guestName = waitlistGuestName.getText().trim();
            String phone = waitlistPhone.getText().trim();
            String email = waitlistEmail != null ? waitlistEmail.getText().trim() : "";

            // Split name into first and last
            String[] nameParts = guestName.split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            // Create guest
            Guest guest = new Guest(firstName, lastName, email, phone);
            GuestRepository guestRepo = new GuestRepository();
            guest = guestRepo.save(guest);

            // Get room type
            RoomType roomType = RoomType.valueOf(waitlistRoomType.getValue().toUpperCase());

            // Get guest counts from spinners
            int adults = (waitlistAdults != null) ? waitlistAdults.getValue() : 1;
            int children = (waitlistChildren != null) ? waitlistChildren.getValue() : 0;

            // Add to waitlist
            Waitlist entry = waitlistService.addToWaitlist(
                guest,
                roomType,
                waitlistCheckIn.getValue(),
                waitlistCheckOut.getValue(),
                adults,
                children
            );

            showInfo("Success", "Added to Waitlist",
                guestName + " has been added to the waitlist for " + formatRoomType(roomType) +
                ".\nThey will be notified when a room becomes available.");

            // Refresh the list
            loadWaitlist();

            // Clear the form
            clearWaitlistForm();

        } catch (Exception e) {
            showError("Failed to add to waitlist: " + e.getMessage());
        }
    }

    private void removeFromWaitlist(Waitlist entry) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove from Waitlist");
        confirm.setHeaderText("Remove " + entry.getGuest().getFullName() + "?");
        confirm.setContentText("This will remove the guest from the waitlist. This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                waitlistService.removeEntry(entry, "Removed by admin");
                loadWaitlist();
                showInfo("Removed", "Guest Removed", entry.getGuest().getFullName() + " has been removed from the waitlist.");
            }
        });
    }

    @FXML
    private void dismissWaitlistNotification() {
        if (waitlistNotificationPanel != null) {
            waitlistNotificationPanel.setVisible(false);
            waitlistNotificationPanel.setManaged(false);
        }
    }

    // ========== Feedback ==========

    private void loadFeedbackSummary() {
        FeedbackService.FeedbackSummary summary = feedbackService.getSummary();
        int total = summary.getTotalCount();
        Map<Integer, Long> distribution = summary.getRatingDistribution();

        // Average rating
        if (avgRatingLabel != null) {
            avgRatingLabel.setText(String.format("%.1f", summary.getAverageRating()));
        }
        if (totalFeedbackLabel != null) {
            totalFeedbackLabel.setText("Based on " + total + " reviews");
        }

        // Rating distribution
        long fiveStar = distribution.getOrDefault(5, 0L);
        long fourStar = distribution.getOrDefault(4, 0L);
        long threeStar = distribution.getOrDefault(3, 0L);
        long lowStar = distribution.getOrDefault(2, 0L) + distribution.getOrDefault(1, 0L);

        if (fiveStarLabel != null) fiveStarLabel.setText(String.valueOf(fiveStar));
        if (fourStarLabel != null) fourStarLabel.setText(String.valueOf(fourStar));
        if (threeStarLabel != null) threeStarLabel.setText(String.valueOf(threeStar));
        if (lowStarLabel != null) lowStarLabel.setText(String.valueOf(lowStar));

        // Percentages
        if (total > 0) {
            if (fiveStarPercentLabel != null) fiveStarPercentLabel.setText(String.format("%.0f%%", fiveStar * 100.0 / total));
            if (fourStarPercentLabel != null) fourStarPercentLabel.setText(String.format("%.0f%%", fourStar * 100.0 / total));
            if (threeStarPercentLabel != null) threeStarPercentLabel.setText(String.format("%.0f%%", threeStar * 100.0 / total));
            if (lowStarPercentLabel != null) lowStarPercentLabel.setText(String.format("%.0f%%", lowStar * 100.0 / total));
        } else {
            if (fiveStarPercentLabel != null) fiveStarPercentLabel.setText("0%");
            if (fourStarPercentLabel != null) fourStarPercentLabel.setText("0%");
            if (threeStarPercentLabel != null) threeStarPercentLabel.setText("0%");
            if (lowStarPercentLabel != null) lowStarPercentLabel.setText("0%");
        }

        // Load feedback table
        if (feedbackTable != null) {
            List<Feedback> feedbackList = feedbackService.getAllFeedback();
            feedbackTable.setItems(FXCollections.observableArrayList(feedbackList));
        }

        // Load common tags dynamically
        if (commonTagsContainer != null) {
            // Keep the first child (the "Common Tags:" label)
            if (commonTagsContainer.getChildren().size() > 1) {
                commonTagsContainer.getChildren().subList(1, commonTagsContainer.getChildren().size()).clear();
            }

            // Get tag counts from service
            Map<String, Long> tagCounts = summary.getSentimentTagCounts();

            // Color palette for tags
            String[] colors = {"#27ae60", "#3498db", "#9b59b6", "#e74c3c", "#f39c12", "#1abc9c", "#e67e22"};
            int colorIndex = 0;

            // Sort tags by count (descending) and add as labels
            tagCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(6) // Show top 6 tags
                .forEach(entry -> {
                    Label tagLabel = new Label(entry.getKey() + " (" + entry.getValue() + ")");
                    int idx = commonTagsContainer.getChildren().size() - 1;
                    String color = colors[idx % colors.length];
                    tagLabel.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                                     "-fx-padding: 5 10; -fx-background-radius: 15;");
                    commonTagsContainer.getChildren().add(tagLabel);
                });

            // If no tags, show a placeholder
            if (tagCounts.isEmpty()) {
                Label noTags = new Label("No tags yet");
                noTags.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                commonTagsContainer.getChildren().add(noTags);
            }
        }
    }

    @FXML
    private void exportFeedback() {
        List<Feedback> feedbackList = feedbackService.getAllFeedback();
        String csv = feedbackService.exportToCSV(feedbackList);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Feedback");
        fileChooser.setInitialFileName("feedback_export.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = Main.getPrimaryStage();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(csv);
                showInfo("Export Successful", "Feedback Exported",
                    "Feedback has been exported to: " + file.getName());
                auditService.log(authService.getCurrentUsername(), "EXPORT", "Feedback", null,
                    "Exported feedback to CSV");
            } catch (Exception e) {
                showError("Export failed: " + e.getMessage());
            }
        }
    }

    private void showFeedbackDetails(Feedback feedback) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feedback Details");
        alert.setHeaderText("Feedback from " + feedback.getGuest().getFullName());

        String stars = "★".repeat(feedback.getRating()) + "☆".repeat(5 - feedback.getRating());
        String sentiment = feedback.getRating() >= 4 ? "Positive" : (feedback.getRating() == 3 ? "Neutral" : "Negative");

        StringBuilder content = new StringBuilder();
        content.append("Rating: ").append(stars).append(" (").append(feedback.getRating()).append("/5)\n");
        content.append("Sentiment: ").append(sentiment).append("\n\n");

        if (feedback.getReservation() != null) {
            content.append("Reservation: ").append(feedback.getReservation().getConfirmationNumber()).append("\n");
            content.append("Stay: ").append(feedback.getReservation().getCheckInDate())
                   .append(" - ").append(feedback.getReservation().getCheckOutDate()).append("\n");
            if (feedback.getReservation().getRooms() != null && !feedback.getReservation().getRooms().isEmpty()) {
                Room room = feedback.getReservation().getRooms().get(0);
                content.append("Room: ").append(room.getRoomNumber())
                       .append(" (").append(formatRoomType(room.getType())).append(")\n");
            }
        }

        content.append("\nSubmitted: ").append(feedback.getSubmittedAt().format(
            java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"))).append("\n");
        content.append("\nComment:\n").append(feedback.getComment());

        alert.setContentText(content.toString());
        alert.getDialogPane().setMinWidth(400);
        alert.showAndWait();
    }

    @FXML
    private void applyFeedbackFilter() {
        List<Feedback> allFeedback = feedbackService.getAllFeedback();
        List<Feedback> filtered = allFeedback;

        // Filter by rating
        if (ratingFilter != null && ratingFilter.getValue() != null && !ratingFilter.getValue().equals("All Ratings")) {
            int targetRating = switch (ratingFilter.getValue()) {
                case "5 Stars" -> 5;
                case "4 Stars" -> 4;
                case "3 Stars" -> 3;
                case "2 Stars" -> 2;
                case "1 Star" -> 1;
                default -> 0;
            };
            if (targetRating > 0) {
                final int rating = targetRating;
                filtered = filtered.stream().filter(f -> f.getRating() == rating).toList();
            }
        }

        // Filter by date range
        if (feedbackFromDate != null && feedbackFromDate.getValue() != null) {
            LocalDate from = feedbackFromDate.getValue();
            filtered = filtered.stream()
                .filter(f -> !f.getSubmittedAt().toLocalDate().isBefore(from))
                .toList();
        }
        if (feedbackToDate != null && feedbackToDate.getValue() != null) {
            LocalDate to = feedbackToDate.getValue();
            filtered = filtered.stream()
                .filter(f -> !f.getSubmittedAt().toLocalDate().isAfter(to))
                .toList();
        }

        // Filter by sentiment
        if (sentimentFilter != null && sentimentFilter.getValue() != null && !sentimentFilter.getValue().equals("All")) {
            String sentiment = sentimentFilter.getValue();
            filtered = filtered.stream().filter(f -> {
                String feedbackSentiment = f.getRating() >= 4 ? "Positive" : (f.getRating() == 3 ? "Neutral" : "Negative");
                return feedbackSentiment.equals(sentiment);
            }).toList();
        }

        // Filter by guest search
        if (feedbackGuestSearch != null && !feedbackGuestSearch.getText().trim().isEmpty()) {
            String search = feedbackGuestSearch.getText().trim().toLowerCase();
            filtered = filtered.stream()
                .filter(f -> f.getGuest().getFullName().toLowerCase().contains(search))
                .toList();
        }

        if (feedbackTable != null) {
            feedbackTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    @FXML
    private void clearFeedbackFilter() {
        if (ratingFilter != null) ratingFilter.setValue("All Ratings");
        if (feedbackFromDate != null) feedbackFromDate.setValue(null);
        if (feedbackToDate != null) feedbackToDate.setValue(null);
        if (sentimentFilter != null) sentimentFilter.setValue("All");
        if (feedbackGuestSearch != null) feedbackGuestSearch.clear();
        loadFeedbackSummary();
    }

    // ========== Loyalty ==========

    private void loadLoyaltyMembers() {
        List<Guest> members = loyaltyService.getLoyaltyMembers();
        if (loyaltyMembersTable != null) {
            loyaltyMembersTable.setItems(FXCollections.observableArrayList(members));
        }
        updateLoyaltyStats(members);
    }

    private void updateLoyaltyStats(List<Guest> members) {
        int totalMembers = members.size();
        int totalPoints = members.stream().mapToInt(Guest::getLoyaltyPoints).sum();
        double totalValue = loyaltyService.calculatePointValue(totalPoints);
        int avgPoints = totalMembers > 0 ? totalPoints / totalMembers : 0;

        if (loyaltyTotalMembersLabel != null) {
            loyaltyTotalMembersLabel.setText(String.format("%,d", totalMembers));
        }
        if (loyaltyTotalPointsLabel != null) {
            loyaltyTotalPointsLabel.setText(String.format("%,d", totalPoints));
        }
        if (loyaltyTotalValueLabel != null) {
            loyaltyTotalValueLabel.setText(String.format("$%,.2f value", totalValue));
        }
        if (loyaltyAvgPointsLabel != null) {
            loyaltyAvgPointsLabel.setText(String.format("%,d", avgPoints));
        }
        if (loyaltyEarningRateLabel != null) {
            loyaltyEarningRateLabel.setText(loyaltyService.getEarningRateDisplay());
        }
        if (loyaltyRedemptionRateLabel != null) {
            loyaltyRedemptionRateLabel.setText(loyaltyService.getRedemptionRateDisplay());
        }
    }

    @FXML
    private void searchLoyaltyMembers() {
        String searchTerm = loyaltySearchField != null ? loyaltySearchField.getText().trim().toLowerCase() : "";

        if (searchTerm.isEmpty()) {
            loadLoyaltyMembers();
            return;
        }

        List<Guest> allMembers = loyaltyService.getLoyaltyMembers();
        List<Guest> filtered = allMembers.stream()
            .filter(g -> {
                String name = g.getFullName().toLowerCase();
                String email = g.getEmail() != null ? g.getEmail().toLowerCase() : "";
                String loyaltyNum = g.getLoyaltyNumber() != null ? g.getLoyaltyNumber().toLowerCase() : "";
                return name.contains(searchTerm) || email.contains(searchTerm) || loyaltyNum.contains(searchTerm);
            })
            .toList();

        if (loyaltyMembersTable != null) {
            loyaltyMembersTable.setItems(FXCollections.observableArrayList(filtered));
        }
        updateLoyaltyStats(filtered);
    }

    @FXML
    private void clearLoyaltySearch() {
        if (loyaltySearchField != null) {
            loyaltySearchField.clear();
        }
        loadLoyaltyMembers();
    }

    /**
     * Initialize the loyalty settings fields with current values from the service.
     */
    private void initializeLoyaltySettings() {
        if (settingsPointsPerDollar != null) {
            settingsPointsPerDollar.setText(String.valueOf((int) loyaltyService.getPointsPerDollar()));
        }
        if (settingsPointsPerRedemption != null) {
            settingsPointsPerRedemption.setText(String.valueOf(loyaltyService.getPointsPerRedemptionDollar()));
        }
        if (settingsMaxRedemption != null) {
            settingsMaxRedemption.setText(String.valueOf(loyaltyService.getMaxPointsPerRedemption()));
        }
        if (settingsMinRedemption != null) {
            settingsMinRedemption.setText(String.valueOf(loyaltyService.getMinPointsToRedeem()));
        }
    }

    @FXML
    private void saveLoyaltySettings() {
        try {
            // Parse and validate input values
            double pointsPerDollar = Double.parseDouble(settingsPointsPerDollar.getText().trim());
            int pointsPerRedemption = Integer.parseInt(settingsPointsPerRedemption.getText().trim());
            int maxRedemption = Integer.parseInt(settingsMaxRedemption.getText().trim());
            int minRedemption = Integer.parseInt(settingsMinRedemption.getText().trim());

            // Validate values
            if (pointsPerDollar <= 0 || pointsPerRedemption <= 0 || maxRedemption <= 0 || minRedemption < 0) {
                showError("All values must be positive (minimum can be 0).");
                return;
            }

            if (minRedemption > maxRedemption) {
                showError("Minimum redemption cannot be greater than maximum redemption.");
                return;
            }

            // Update loyalty service settings
            loyaltyService.setPointsPerDollar(pointsPerDollar);
            loyaltyService.setPointsPerRedemptionDollar(pointsPerRedemption);
            loyaltyService.setMaxPointsPerRedemption(maxRedemption);
            loyaltyService.setMinPointsToRedeem(minRedemption);

            // Update the display labels
            if (loyaltyEarningRateLabel != null) {
                loyaltyEarningRateLabel.setText(loyaltyService.getEarningRateDisplay());
            }
            if (loyaltyRedemptionRateLabel != null) {
                loyaltyRedemptionRateLabel.setText(loyaltyService.getRedemptionRateDisplay());
            }

            // Log the change
            auditService.log(authService.getCurrentUser().getUsername(), "LOYALTY_SETTINGS_UPDATE",
                "System", "loyalty_settings",
                String.format("Updated loyalty settings: Earn=%.0f pts/$1, Redeem=%d pts=$1, Max=%d, Min=%d",
                    pointsPerDollar, pointsPerRedemption, maxRedemption, minRedemption));

            showInfo("Settings Saved", "Loyalty Program Settings Updated",
                "The loyalty program settings have been saved successfully.");

        } catch (NumberFormatException e) {
            showError("Please enter valid numeric values for all settings.");
        }
    }

    private void showLoyaltyMemberDetails(Guest member) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Loyalty Member Details");
        alert.setHeaderText(member.getFullName());

        double pointValue = loyaltyService.calculatePointValue(member.getLoyaltyPoints());

        StringBuilder content = new StringBuilder();
        content.append("Loyalty Number: ").append(member.getLoyaltyNumber()).append("\n\n");
        content.append("Contact Information:\n");
        content.append("  Email: ").append(member.getEmail() != null ? member.getEmail() : "N/A").append("\n");
        content.append("  Phone: ").append(member.getPhone() != null ? member.getPhone() : "N/A").append("\n\n");
        content.append("Points Information:\n");
        content.append("  Current Balance: ").append(String.format("%,d points", member.getLoyaltyPoints())).append("\n");
        content.append("  Dollar Value: ").append(String.format("$%.2f", pointValue)).append("\n\n");
        content.append(String.format("Earning Rate: %.0f points per $1 spent\n", loyaltyService.getPointsPerDollar()));
        content.append(String.format("Redemption: %d points = $1.00 discount", loyaltyService.getPointsPerRedemptionDollar()));

        alert.setContentText(content.toString());
        alert.getDialogPane().setMinWidth(400);
        alert.showAndWait();
    }

    @FXML
    private void enrollMember() {
        // Get all non-member guests
        GuestRepository guestRepo = new GuestRepository();
        List<Guest> nonMembers = guestRepo.findAll().stream()
            .filter(g -> g.getLoyaltyNumber() == null || g.getLoyaltyNumber().isEmpty())
            .toList();

        if (nonMembers.isEmpty()) {
            showInfo("No Eligible Guests", "All Enrolled",
                "All existing guests are already enrolled in the loyalty program.");
            return;
        }

        // Create choice dialog
        ChoiceDialog<Guest> dialog = new ChoiceDialog<>(nonMembers.get(0), nonMembers);
        dialog.setTitle("Enroll in Loyalty Program");
        dialog.setHeaderText("Select a guest to enroll");
        dialog.setContentText("Guest:");

        // Set string converter to display guest names properly instead of object references
        @SuppressWarnings("unchecked")
        javafx.scene.control.ComboBox<Guest> comboBox = (javafx.scene.control.ComboBox<Guest>) dialog.getDialogPane().lookup(".combo-box");
        if (comboBox != null) {
            comboBox.setConverter(new javafx.util.StringConverter<Guest>() {
                @Override
                public String toString(Guest guest) {
                    return guest != null ? guest.getFullName() + " (" + guest.getEmail() + ")" : "";
                }
                @Override
                public Guest fromString(String string) {
                    return null;
                }
            });
        }

        dialog.showAndWait().ifPresent(guest -> {
            String loyaltyNumber = loyaltyService.enrollGuest(guest);
            showInfo("Enrollment Successful", "New Loyalty Member",
                guest.getFullName() + " has been enrolled!\n\nLoyalty Number: " + loyaltyNumber +
                "\nStarting Points: 0\n\nThey will earn 5 points per $1 spent (5% return rate).");
            auditService.log(authService.getCurrentUsername(), "LOYALTY_ENROLL", "Guest",
                guest.getId().toString(), "Enrolled guest in loyalty: " + guest.getFullName());
            loadLoyaltyMembers();
        });
    }

    @FXML
    private void searchGuests() {
        String searchTerm = guestSearchField != null ? guestSearchField.getText().trim() : "";
        String loyaltyStatus = loyaltyFilter != null ? loyaltyFilter.getValue() : null;

        GuestRepository guestRepo = new GuestRepository();
        List<Guest> results;

        if (searchTerm.isEmpty()) {
            results = guestRepo.findAll();
        } else {
            results = guestRepo.findByName(searchTerm);
            auditService.log(authService.getCurrentUsername(), "SEARCH", "Guest", null,
                "Searched guests: " + searchTerm);
        }

        // Apply loyalty filter
        if (loyaltyStatus != null && !loyaltyStatus.equals("All Guests")) {
            if (loyaltyStatus.equals("Loyalty Members")) {
                results = results.stream()
                    .filter(g -> g.getLoyaltyNumber() != null && !g.getLoyaltyNumber().isEmpty())
                    .toList();
            } else if (loyaltyStatus.equals("Non-Members")) {
                results = results.stream()
                    .filter(g -> g.getLoyaltyNumber() == null || g.getLoyaltyNumber().isEmpty())
                    .toList();
            }
        }

        if (guestsTable != null) {
            guestsTable.setItems(FXCollections.observableArrayList(results));
        }
        if (guestCountLabel != null) {
            guestCountLabel.setText("Showing " + results.size() + " guests");
        }
    }

    @FXML
    private void clearGuestSearch() {
        if (guestSearchField != null) guestSearchField.clear();
        if (loyaltyFilter != null) loyaltyFilter.setValue("All Guests");
        loadAllGuests();
    }

    @FXML
    private void addNewGuest() {
        // Create dialog for adding new guest
        Dialog<Guest> dialog = new Dialog<>();
        dialog.setTitle("Add New Guest");
        dialog.setHeaderText("Enter guest information");

        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone (e.g., 416-555-1234)");

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Request focus on first name field
        javafx.application.Platform.runLater(firstNameField::requestFocus);

        // Enable/disable save button based on input
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Validation listener
        javafx.beans.value.ChangeListener<String> validationListener = (obs, oldVal, newVal) -> {
            boolean valid = !firstNameField.getText().trim().isEmpty()
                && !lastNameField.getText().trim().isEmpty()
                && !emailField.getText().trim().isEmpty()
                && emailField.getText().contains("@")
                && !phoneField.getText().trim().isEmpty();
            saveButton.setDisable(!valid);
        };

        firstNameField.textProperty().addListener(validationListener);
        lastNameField.textProperty().addListener(validationListener);
        emailField.textProperty().addListener(validationListener);
        phoneField.textProperty().addListener(validationListener);

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Guest(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(guest -> {
            try {
                GuestRepository guestRepo = new GuestRepository();

                // Check for duplicate email
                if (guestRepo.findByEmail(guest.getEmail()) != null) {
                    showError("A guest with this email already exists.");
                    return;
                }

                // Save guest
                guestRepo.save(guest);

                showInfo("Guest Added", "Success",
                    guest.getFullName() + " has been added to the system.");

                auditService.log(authService.getCurrentUsername(), "GUEST_CREATE", "Guest",
                    guest.getId().toString(), "Created new guest: " + guest.getFullName());

                // Refresh guest list
                loadAllGuests();

            } catch (Exception e) {
                showError("Failed to add guest: " + e.getMessage());
            }
        });
    }

    // ========== Reports ==========

    // Store current reports for export
    private ReportingService.RevenueReport currentRevenueReport;
    private ReportingService.OccupancyReport currentOccupancyReport;
    private List<AuditLog> currentActivityLogs;

    private void loadReportsData() {
        // Initialize date pickers with defaults
        LocalDate today = LocalDate.now();
        if (revenueDate != null) revenueDate.setValue(today);
        if (revenueFromDate != null) revenueFromDate.setValue(today.withDayOfMonth(1));
        if (revenueToDate != null) revenueToDate.setValue(today);
        if (occupancyDate != null) occupancyDate.setValue(today);
        if (occupancyFromDate != null) occupancyFromDate.setValue(today.minusDays(7));
        if (occupancyToDate != null) occupancyToDate.setValue(today);
        if (logFromDate != null) logFromDate.setValue(today.minusDays(30));
        if (logToDate != null) logToDate.setValue(today);

        // Set default combo values
        if (revenuePeriod != null) revenuePeriod.setValue("Daily");
        if (occupancyView != null) occupancyView.setValue("Daily");
        if (revenueRoomType != null) revenueRoomType.setValue("All Rooms");
        if (occupancyRoomType != null) occupancyRoomType.setValue("All Rooms");
        if (logActionFilter != null) logActionFilter.setValue("All Actions");
        if (logActorFilter != null) logActorFilter.setValue("All Users");

        // Set initial visibility based on default period (Daily)
        updateRevenueDatePickerVisibility("Daily");
        updateOccupancyDatePickerVisibility("Daily");

        // Setup table columns
        setupRevenueTableColumns();
        setupOccupancyTableColumns();
        setupActivityLogsTableColumns();

        // Load initial activity logs
        loadActivityLogs();
    }

    @SuppressWarnings("unchecked")
    private void setupRevenueTableColumns() {
        if (revenueTable == null) return;
        revenueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ObservableList<TableColumn<ReportingService.RevenueReport.DailyRevenue, ?>> columns = revenueTable.getColumns();
        if (columns.size() >= 6) {
            // Period/Date column
            ((TableColumn<ReportingService.RevenueReport.DailyRevenue, LocalDate>) columns.get(0))
                .setCellValueFactory(new PropertyValueFactory<>("date"));

            // Reservations column
            ((TableColumn<ReportingService.RevenueReport.DailyRevenue, Integer>) columns.get(1))
                .setCellValueFactory(new PropertyValueFactory<>("reservationCount"));

            // Subtotal column
            ((TableColumn<ReportingService.RevenueReport.DailyRevenue, Double>) columns.get(2))
                .setCellValueFactory(new PropertyValueFactory<>("subtotal"));

            // Tax column
            ((TableColumn<ReportingService.RevenueReport.DailyRevenue, Double>) columns.get(3))
                .setCellValueFactory(new PropertyValueFactory<>("tax"));

            // Discounts column
            ((TableColumn<ReportingService.RevenueReport.DailyRevenue, Double>) columns.get(4))
                .setCellValueFactory(new PropertyValueFactory<>("discounts"));

            // Total column
            ((TableColumn<ReportingService.RevenueReport.DailyRevenue, Double>) columns.get(5))
                .setCellValueFactory(new PropertyValueFactory<>("total"));

            // Format currency columns
            ((TableColumn<ReportingService.RevenueReport.DailyRevenue, Double>) columns.get(2))
                .setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Double value, boolean empty) {
                        super.updateItem(value, empty);
                        setText(empty || value == null ? "" : String.format("$%,.2f", value));
                    }
                });
            ((TableColumn<ReportingService.RevenueReport.DailyRevenue, Double>) columns.get(3))
                .setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Double value, boolean empty) {
                        super.updateItem(value, empty);
                        setText(empty || value == null ? "" : String.format("$%,.2f", value));
                    }
                });
            ((TableColumn<ReportingService.RevenueReport.DailyRevenue, Double>) columns.get(4))
                .setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Double value, boolean empty) {
                        super.updateItem(value, empty);
                        setText(empty || value == null ? "" : String.format("$%,.2f", value));
                    }
                });
            ((TableColumn<ReportingService.RevenueReport.DailyRevenue, Double>) columns.get(5))
                .setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Double value, boolean empty) {
                        super.updateItem(value, empty);
                        setText(empty || value == null ? "" : String.format("$%,.2f", value));
                    }
                });
        }
    }

    @SuppressWarnings("unchecked")
    private void setupOccupancyTableColumns() {
        if (occupancyTable == null) return;
        occupancyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ObservableList<TableColumn<ReportingService.OccupancyReport.DailyOccupancy, ?>> columns = occupancyTable.getColumns();
        if (columns.size() >= 4) {
            ((TableColumn<ReportingService.OccupancyReport.DailyOccupancy, LocalDate>) columns.get(0))
                .setCellValueFactory(new PropertyValueFactory<>("date"));
            ((TableColumn<ReportingService.OccupancyReport.DailyOccupancy, Integer>) columns.get(1))
                .setCellValueFactory(new PropertyValueFactory<>("availableRooms"));
            ((TableColumn<ReportingService.OccupancyReport.DailyOccupancy, Integer>) columns.get(2))
                .setCellValueFactory(new PropertyValueFactory<>("occupiedRooms"));
            ((TableColumn<ReportingService.OccupancyReport.DailyOccupancy, Double>) columns.get(3))
                .setCellValueFactory(new PropertyValueFactory<>("occupancyPercent"));

            // Format occupancy percent column
            ((TableColumn<ReportingService.OccupancyReport.DailyOccupancy, Double>) columns.get(3))
                .setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Double value, boolean empty) {
                        super.updateItem(value, empty);
                        setText(empty || value == null ? "" : String.format("%.1f%%", value));
                    }
                });
        }
    }

    @SuppressWarnings("unchecked")
    private void setupActivityLogsTableColumns() {
        if (activityLogsTable == null) return;
        activityLogsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ObservableList<TableColumn<AuditLog, ?>> columns = activityLogsTable.getColumns();
        if (columns.size() >= 6) {
            ((TableColumn<AuditLog, LocalDateTime>) columns.get(0))
                .setCellValueFactory(new PropertyValueFactory<>("timestamp"));
            ((TableColumn<AuditLog, String>) columns.get(1))
                .setCellValueFactory(new PropertyValueFactory<>("actor"));
            ((TableColumn<AuditLog, String>) columns.get(2))
                .setCellValueFactory(new PropertyValueFactory<>("action"));
            ((TableColumn<AuditLog, String>) columns.get(3))
                .setCellValueFactory(new PropertyValueFactory<>("entityType"));
            ((TableColumn<AuditLog, String>) columns.get(4))
                .setCellValueFactory(new PropertyValueFactory<>("entityId"));
            ((TableColumn<AuditLog, String>) columns.get(5))
                .setCellValueFactory(new PropertyValueFactory<>("message"));

            // Format timestamp
            ((TableColumn<AuditLog, LocalDateTime>) columns.get(0))
                .setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(LocalDateTime value, boolean empty) {
                        super.updateItem(value, empty);
                        setText(empty || value == null ? "" :
                            value.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    }
                });
        }
    }

    // ========== Period/View Change Handlers ==========

    @FXML
    private void onRevenuePeriodChange() {
        String period = revenuePeriod != null ? revenuePeriod.getValue() : "Daily";
        updateRevenueDatePickerVisibility(period);
    }

    @FXML
    private void onOccupancyViewChange() {
        String view = occupancyView != null ? occupancyView.getValue() : "Daily";
        updateOccupancyDatePickerVisibility(view);
    }

    private void updateRevenueDatePickerVisibility(String period) {
        boolean isCustom = "Custom".equals(period);

        // Single date picker (for Daily/Weekly/Monthly)
        if (revenueDateLabel != null) {
            revenueDateLabel.setVisible(!isCustom);
            revenueDateLabel.setManaged(!isCustom);
        }
        if (revenueDate != null) {
            revenueDate.setVisible(!isCustom);
            revenueDate.setManaged(!isCustom);
        }

        // From/To date pickers (for Custom)
        if (revenueFromLabel != null) {
            revenueFromLabel.setVisible(isCustom);
            revenueFromLabel.setManaged(isCustom);
        }
        if (revenueFromDate != null) {
            revenueFromDate.setVisible(isCustom);
            revenueFromDate.setManaged(isCustom);
        }
        if (revenueToLabel != null) {
            revenueToLabel.setVisible(isCustom);
            revenueToLabel.setManaged(isCustom);
        }
        if (revenueToDate != null) {
            revenueToDate.setVisible(isCustom);
            revenueToDate.setManaged(isCustom);
        }
    }

    private void updateOccupancyDatePickerVisibility(String view) {
        boolean isCustom = "Custom".equals(view);

        // Single date picker (for Daily/Weekly/Monthly)
        if (occupancyDateLabel != null) {
            occupancyDateLabel.setVisible(!isCustom);
            occupancyDateLabel.setManaged(!isCustom);
        }
        if (occupancyDate != null) {
            occupancyDate.setVisible(!isCustom);
            occupancyDate.setManaged(!isCustom);
        }

        // From/To date pickers (for Custom)
        if (occupancyFromLabel != null) {
            occupancyFromLabel.setVisible(isCustom);
            occupancyFromLabel.setManaged(isCustom);
        }
        if (occupancyFromDate != null) {
            occupancyFromDate.setVisible(isCustom);
            occupancyFromDate.setManaged(isCustom);
        }
        if (occupancyToLabel != null) {
            occupancyToLabel.setVisible(isCustom);
            occupancyToLabel.setManaged(isCustom);
        }
        if (occupancyToDate != null) {
            occupancyToDate.setVisible(isCustom);
            occupancyToDate.setManaged(isCustom);
        }
    }

    @FXML
    private void generateRevenueReport() {
        String period = revenuePeriod != null ? revenuePeriod.getValue() : "Daily";

        // Get the appropriate date based on period selection
        LocalDate selectedDate = revenueDate != null && revenueDate.getValue() != null
            ? revenueDate.getValue() : LocalDate.now();

        // Generate report based on period
        if ("Custom".equals(period)) {
            LocalDate fromDate = revenueFromDate != null && revenueFromDate.getValue() != null
                ? revenueFromDate.getValue() : LocalDate.now().withDayOfMonth(1);
            LocalDate toDate = revenueToDate != null && revenueToDate.getValue() != null
                ? revenueToDate.getValue() : LocalDate.now();
            currentRevenueReport = reportingService.generateRevenueReportForDateRange(fromDate, toDate);
        } else if ("Monthly".equals(period)) {
            currentRevenueReport = reportingService.generateMonthlyRevenueReport(selectedDate.getYear(), selectedDate.getMonthValue());
        } else if ("Weekly".equals(period)) {
            currentRevenueReport = reportingService.generateWeeklyRevenueReport(selectedDate);
        } else { // Daily
            currentRevenueReport = reportingService.generateDailyRevenueReport(selectedDate);
        }

        // Update stats labels
        if (revenueTotalLabel != null) {
            revenueTotalLabel.setText(String.format("$%,.2f", currentRevenueReport.getTotal()));
        }
        if (revenueReservationsLabel != null) {
            revenueReservationsLabel.setText(String.valueOf(currentRevenueReport.getReservationCount()));
        }
        if (revenueTaxLabel != null) {
            revenueTaxLabel.setText(String.format("$%,.2f", currentRevenueReport.getTax()));
        }
        if (revenueDiscountsLabel != null) {
            revenueDiscountsLabel.setText(String.format("$%,.2f", currentRevenueReport.getDiscounts()));
        }

        // Populate the revenue table with daily breakdown data
        if (revenueTable != null && currentRevenueReport.getDailyData() != null) {
            revenueTable.setItems(FXCollections.observableArrayList(currentRevenueReport.getDailyData()));
        }

        showInfo("Report Generated", currentRevenueReport.getTitle(),
            String.format("Revenue: $%,.2f from %d reservations",
                currentRevenueReport.getTotal(), currentRevenueReport.getReservationCount()));
    }

    @FXML
    private void generateOccupancyReport() {
        String view = occupancyView != null ? occupancyView.getValue() : "Daily";

        // Get the appropriate date based on view selection
        LocalDate selectedDate = occupancyDate != null && occupancyDate.getValue() != null
            ? occupancyDate.getValue() : LocalDate.now();

        // Generate report based on view type
        if ("Custom".equals(view)) {
            LocalDate fromDate = occupancyFromDate != null && occupancyFromDate.getValue() != null
                ? occupancyFromDate.getValue() : LocalDate.now().minusDays(7);
            LocalDate toDate = occupancyToDate != null && occupancyToDate.getValue() != null
                ? occupancyToDate.getValue() : LocalDate.now();
            currentOccupancyReport = reportingService.generateOccupancyReportForDateRange(fromDate, toDate);
        } else if ("Monthly".equals(view)) {
            currentOccupancyReport = reportingService.generateMonthlyOccupancyReport(selectedDate.getYear(), selectedDate.getMonthValue());
        } else if ("Weekly".equals(view)) {
            currentOccupancyReport = reportingService.generateWeeklyOccupancyReport(selectedDate);
        } else { // Daily
            currentOccupancyReport = reportingService.generateDailyOccupancyReport(selectedDate);
        }

        // Populate table
        if (occupancyTable != null) {
            occupancyTable.setItems(FXCollections.observableArrayList(currentOccupancyReport.getDailyData()));
        }

        // Update stats
        List<ReportingService.OccupancyReport.DailyOccupancy> data = currentOccupancyReport.getDailyData();
        double avgOccupancy = currentOccupancyReport.getAverageOccupancy();
        double peakOccupancy = data.stream().mapToDouble(d -> d.getOccupancyPercent()).max().orElse(0);
        double lowestOccupancy = data.stream().mapToDouble(d -> d.getOccupancyPercent()).min().orElse(0);
        int totalRooms = data.isEmpty() ? 0 : data.get(0).getTotalRooms();

        if (occupancyAvgLabel != null) occupancyAvgLabel.setText(String.format("%.0f%%", avgOccupancy));
        if (occupancyTotalRoomsLabel != null) occupancyTotalRoomsLabel.setText(String.valueOf(totalRooms));
        if (occupancyPeakLabel != null) occupancyPeakLabel.setText(String.format("%.0f%%", peakOccupancy));
        if (occupancyLowestLabel != null) occupancyLowestLabel.setText(String.format("%.0f%%", lowestOccupancy));
    }

    private void loadActivityLogs() {
        currentActivityLogs = auditService.getAllLogs();
        if (activityLogsTable != null) {
            activityLogsTable.setItems(FXCollections.observableArrayList(currentActivityLogs));
        }
    }

    @FXML
    private void filterActivityLogs() {
        List<AuditLog> allLogs = auditService.getAllLogs();

        // Apply filters
        String searchTerm = logSearchField != null ? logSearchField.getText().trim().toLowerCase() : "";
        String actionFilter = logActionFilter != null ? logActionFilter.getValue() : "All Actions";
        String actorFilter = logActorFilter != null ? logActorFilter.getValue() : "All Users";
        LocalDate fromDate = logFromDate != null ? logFromDate.getValue() : null;
        LocalDate toDate = logToDate != null ? logToDate.getValue() : null;

        currentActivityLogs = allLogs.stream()
            .filter(log -> {
                // Search filter
                if (!searchTerm.isEmpty()) {
                    boolean matches = (log.getMessage() != null && log.getMessage().toLowerCase().contains(searchTerm))
                        || (log.getAction() != null && log.getAction().toLowerCase().contains(searchTerm))
                        || (log.getActor() != null && log.getActor().toLowerCase().contains(searchTerm));
                    if (!matches) return false;
                }

                // Action filter - map display names to actual action values
                if (!"All Actions".equals(actionFilter)) {
                    String action = log.getAction() != null ? log.getAction().toUpperCase() : "";
                    String entityType = log.getEntityType() != null ? log.getEntityType() : "";
                    boolean matches = switch (actionFilter) {
                        case "Login" -> action.contains("LOGIN");
                        case "Reservation Created" -> action.contains("CREATE") && entityType.contains("Reservation");
                        case "Reservation Modified" -> action.contains("UPDATE") && entityType.contains("Reservation");
                        case "Reservation Cancelled" -> action.contains("CANCEL");
                        case "Check-in" -> action.contains("CHECK") && action.contains("IN");
                        case "Check-out" -> action.contains("CHECK") && action.contains("OUT");
                        case "Payment" -> action.contains("PAYMENT") || entityType.contains("Payment");
                        case "Refund" -> action.contains("REFUND");
                        case "Discount Applied" -> action.contains("DISCOUNT");
                        default -> true;
                    };
                    if (!matches) return false;
                }

                // Actor filter
                if (!"All Users".equals(actorFilter)) {
                    if ("Admin".equals(actorFilter) && !"admin".equalsIgnoreCase(log.getActor())) return false;
                    if ("Manager".equals(actorFilter) && !"manager".equalsIgnoreCase(log.getActor())) return false;
                    if ("System".equals(actorFilter) && !"SYSTEM".equals(log.getActor())) return false;
                }

                // Date filter
                if (fromDate != null && log.getTimestamp().toLocalDate().isBefore(fromDate)) return false;
                if (toDate != null && log.getTimestamp().toLocalDate().isAfter(toDate)) return false;

                return true;
            })
            .toList();

        if (activityLogsTable != null) {
            activityLogsTable.setItems(FXCollections.observableArrayList(currentActivityLogs));
        }
    }

    @FXML
    private void exportRevenueCSV() {
        if (currentRevenueReport == null) {
            generateRevenueReport();
        }
        String csv = reportingService.exportRevenueToCSV(currentRevenueReport);
        saveToFile(csv, "revenue_report.csv", "CSV Files", "*.csv");
    }

    @FXML
    private void exportRevenuePDF() {
        if (currentRevenueReport == null) {
            generateRevenueReport();
        }

        // Build intro lines
        List<String> introLines = new ArrayList<>();
        introLines.add("Period: " + currentRevenueReport.getStartDate().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " to " +
            currentRevenueReport.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        introLines.add("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Build body lines
        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("## Summary");
        bodyLines.add("Total Reservations: " + currentRevenueReport.getReservationCount());
        bodyLines.add(String.format("Subtotal: $%.2f", currentRevenueReport.getSubtotal()));
        bodyLines.add(String.format("Tax: $%.2f", currentRevenueReport.getTax()));
        bodyLines.add(String.format("Discounts: $%.2f", currentRevenueReport.getDiscounts()));
        bodyLines.add(String.format("Total: $%.2f", currentRevenueReport.getTotal()));
        bodyLines.add(String.format("Actual Revenue (Paid): $%.2f", currentRevenueReport.getActualRevenue()));
        if (currentRevenueReport.getDailyData() != null && !currentRevenueReport.getDailyData().isEmpty()) {
            bodyLines.add("");
            bodyLines.add("## Daily Revenue Breakdown");
            for (var day : currentRevenueReport.getDailyData()) {
                bodyLines.add(String.format("  %s: $%.2f (%d reservations)",
                    day.getDate(), day.getTotal(), day.getReservationCount()));
            }
        }

        savePdfWithPdfBox("Revenue Report", introLines, bodyLines, "revenue_report.pdf");
    }

    @FXML
    private void exportOccupancyCSV() {
        if (currentOccupancyReport == null) {
            generateOccupancyReport();
        }
        String csv = reportingService.exportOccupancyToCSV(currentOccupancyReport);
        saveToFile(csv, "occupancy_report.csv", "CSV Files", "*.csv");
    }

    @FXML
    private void exportOccupancyPDF() {
        if (currentOccupancyReport == null) {
            generateOccupancyReport();
        }

        // Build intro lines
        List<String> introLines = new ArrayList<>();
        introLines.add("Period: " + currentOccupancyReport.getStartDate() + " to " + currentOccupancyReport.getEndDate());
        introLines.add("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Build body lines
        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("## Summary");
        bodyLines.add(String.format("Average Occupancy: %.1f%%", currentOccupancyReport.getAverageOccupancy()));
        bodyLines.add("");
        bodyLines.add("## Daily Occupancy Data");
        bodyLines.add(String.format("%-12s %10s %10s %12s", "Date", "Available", "Occupied", "Occupancy %"));
        bodyLines.add("");
        for (var day : currentOccupancyReport.getDailyData()) {
            bodyLines.add(String.format("%-12s %10d %10d %11.1f%%",
                day.getDate(), day.getAvailableRooms(), day.getOccupiedRooms(), day.getOccupancyPercent()));
        }

        savePdfWithPdfBox("Occupancy Report", introLines, bodyLines, "occupancy_report.pdf");
    }

    @FXML
    private void exportLogsCSV() {
        if (currentActivityLogs == null || currentActivityLogs.isEmpty()) {
            currentActivityLogs = auditService.getAllLogs();
        }
        String csv = auditService.exportToCSV(currentActivityLogs);
        saveToFile(csv, "activity_logs.csv", "CSV Files", "*.csv");
    }

    @FXML
    private void exportLogsTXT() {
        if (currentActivityLogs == null || currentActivityLogs.isEmpty()) {
            currentActivityLogs = auditService.getAllLogs();
        }
        String txt = auditService.exportToTXT(currentActivityLogs);
        saveToFile(txt, "activity_logs.txt", "Text Files", "*.txt");
    }

    private void saveToFile(String content, String defaultName, String filterDesc, String filterExt) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName(defaultName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterDesc, filterExt));

        Stage stage = Main.getPrimaryStage();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                showInfo("Export Successful", "File Saved", "File saved to: " + file.getName());
                auditService.log(authService.getCurrentUsername(), "EXPORT", "Report", null,
                    "Exported to: " + file.getName());
            } catch (Exception e) {
                showError("Save failed: " + e.getMessage());
            }
        }
    }

    private void savePdfWithPdfBox(String title, List<String> introLines, List<String> bodyLines, String defaultName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.setInitialFileName(defaultName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Stage stage = Main.getPrimaryStage();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                PdfExportUtil.exportDocument(file.toPath(), title, introLines, bodyLines);
                showInfo("Export Successful", "PDF Saved", "File saved to: " + file.getName());
                auditService.log(authService.getCurrentUsername(), "EXPORT", "Report", null,
                    "Exported PDF to: " + file.getName());
            } catch (Exception e) {
                showError("PDF export failed: " + e.getMessage());
            }
        }
    }

    // ========== Utility Methods ==========

    /**
     * Format status enum to human-readable string.
     * E.g., "CHECKED_IN" -> "Checked In"
     */
    private String formatStatus(ReservationStatus status) {
        if (status == null) return "-";
        String name = status.name();
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) result.append(" ");
            result.append(word.charAt(0)).append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * Parse formatted status string back to enum.
     * E.g., "Checked In" -> CHECKED_IN
     */
    private ReservationStatus parseStatus(String formatted) {
        if (formatted == null || formatted.equals("All")) return null;
        String enumName = formatted.toUpperCase().replace(" ", "_");
        return ReservationStatus.valueOf(enumName);
    }

    private void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
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
            showError("Failed to load screen: " + fxmlPath + "\n" + e.getMessage());
        }
    }
}
