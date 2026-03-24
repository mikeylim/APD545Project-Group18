package com.hotel.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import com.hotel.app.Main;
import com.hotel.model.*;
import com.hotel.service.*;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.AddonRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for all Kiosk screens.
 * Manages the guest self-service booking flow with database persistence.
 */
public class KioskController {

    // Services
    private final ReservationService reservationService = new ReservationService();
    private final RoomRepository roomRepository = new RoomRepository();
    private final AddonRepository addonRepository = new AddonRepository();
    private final PricingService pricingService = new PricingService();

    // ==================== GUEST COUNT SCREEN ====================
    @FXML private Label adultsLabel;
    @FXML private Label childrenLabel;
    @FXML private Label totalGuestsLabel;
    @FXML private Label errorLabel;

    // ==================== DATE SELECTION SCREEN ====================
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private Label nightsLabel;
    @FXML private Label guestsSummaryLabel;
    @FXML private Label dateValidationLabel;

    // ==================== ROOM SELECTION SCREEN ====================
    @FXML private Label guestsInfo;
    @FXML private Label datesInfo;
    @FXML private Label nightsInfo;
    @FXML private Label roomValidationLabel;
    @FXML private ToggleButton suggestedToggle;
    @FXML private ToggleButton customToggle;
    @FXML private VBox suggestedSection;
    @FXML private VBox customSection;

    // Suggested section
    @FXML private Label suggestedRecommendationText;
    @FXML private Label suggestedRoomName;
    @FXML private Label suggestedRoomDesc;
    @FXML private Label suggestedRoomPrice;
    @FXML private Label suggestedTotalLabel;

    // Custom section - quantity labels
    @FXML private Label singleQtyLabel;
    @FXML private Label doubleQtyLabel;
    @FXML private Label deluxeQtyLabel;
    @FXML private Label penthouseQtyLabel;

    // Custom section - availability labels
    @FXML private Label singleAvailable;
    @FXML private Label doubleAvailable;
    @FXML private Label deluxeAvailable;
    @FXML private Label penthouseAvailable;

    // Custom section - summary labels
    @FXML private Label customCapacityLabel;
    @FXML private Label customRequiredLabel;
    @FXML private Label customSubtotalLabel;

    // Room quantity tracking
    private int singleQty = 0;
    private int doubleQty = 0;
    private int deluxeQty = 0;
    private int penthouseQty = 0;
    private boolean useCustomRooms = false;

    // ==================== ADDON SCREEN ====================
    @FXML private CheckBox wifiCheckbox;
    @FXML private CheckBox breakfastCheckbox;
    @FXML private CheckBox parkingCheckbox;
    @FXML private CheckBox spaCheckbox;
    @FXML private CheckBox airportCheckbox;
    @FXML private Label roomTotalLabel;
    @FXML private Label addonsSelectedLabel;
    @FXML private Label subtotalLabel;

    // ==================== GUEST DETAILS SCREEN ====================
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private ComboBox<String> countryCombo;
    @FXML private TextArea specialRequestsArea;
    @FXML private CheckBox loyaltyCheckbox;
    @FXML private CheckBox termsCheckbox;
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label termsError;

    // ==================== SUMMARY SCREEN ====================
    @FXML private Label summaryName;
    @FXML private Label summaryEmail;
    @FXML private Label summaryPhone;
    @FXML private Label summaryGuests;
    @FXML private Label summaryCheckIn;
    @FXML private Label summaryCheckOut;
    @FXML private Label summaryNights;
    @FXML private Label summaryRoomType;
    @FXML private Label summaryRoomPrice;
    @FXML private Label roomCharges;
    @FXML private Label subtotalAmount;
    @FXML private Label taxAmount;
    @FXML private Label totalAmount;
    @FXML private VBox addonsContainer;
    @FXML private Label noAddonsLabel;
    @FXML private VBox loyaltyEnrollmentNotice;

    // ==================== CONFIRMATION SCREEN ====================
    @FXML private Label confirmationNumber;
    @FXML private Label confGuestName;
    @FXML private Label confCheckIn;
    @FXML private Label confCheckOut;
    @FXML private Label confRoom;
    @FXML private Label confTotal;
    @FXML private Label loyaltyNumber;
    @FXML private VBox loyaltyConfirmation;

    // Date formatter
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private final DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    public void initialize() {
        BookingSession session = BookingSession.getInstance();

        // Initialize based on which screen we're on (check which elements exist)
        initializeGuestCountScreen(session);
        initializeDateSelectionScreen(session);
        initializeRoomSelectionScreen(session);
        initializeAddonsScreen(session);
        initializeGuestDetailsScreen(session);
        initializeSummaryScreen(session);
        initializeConfirmationScreen(session);
    }

    // ==================== SCREEN INITIALIZERS ====================

    private void initializeGuestCountScreen(BookingSession session) {
        if (adultsLabel != null) {
            adultsLabel.setText(String.valueOf(session.getAdults()));
        }
        if (childrenLabel != null) {
            childrenLabel.setText(String.valueOf(session.getChildren()));
        }
        if (totalGuestsLabel != null) {
            totalGuestsLabel.setText(String.valueOf(session.getTotalGuests()));
        }
    }

    private void initializeDateSelectionScreen(BookingSession session) {
        if (checkInPicker != null) {
            // Set initial value
            LocalDate checkIn = session.getCheckInDate() != null ? session.getCheckInDate() : LocalDate.now().plusDays(1);
            checkInPicker.setValue(checkIn);

            // Disable past dates
            checkInPicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(LocalDate.now()));
                }
            });

            // Update nights when changed
            checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateNightsDisplay());
        }

        if (checkOutPicker != null) {
            // Set initial value
            LocalDate checkOut = session.getCheckOutDate() != null ? session.getCheckOutDate() : LocalDate.now().plusDays(3);
            checkOutPicker.setValue(checkOut);

            // Disable dates before check-in
            checkOutPicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate checkIn = checkInPicker != null ? checkInPicker.getValue() : LocalDate.now();
                    setDisable(empty || date.isBefore(checkIn.plusDays(1)));
                }
            });

            // Update nights when changed
            checkOutPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateNightsDisplay());
        }

        // Initialize display
        if (nightsLabel != null) {
            updateNightsDisplay();
        }
        if (guestsSummaryLabel != null) {
            guestsSummaryLabel.setText(session.getAdults() + " Adults, " + session.getChildren() + " Children");
        }
    }

    private void initializeRoomSelectionScreen(BookingSession session) {
        // Skip if not on room selection screen
        if (guestsInfo == null) return;

        int totalGuests = session.getTotalGuests();
        int nights = session.getNights();

        // Booking summary bar
        guestsInfo.setText(session.getAdults() + " Adults, " + session.getChildren() + " Children");
        if (datesInfo != null && session.getCheckInDate() != null && session.getCheckOutDate() != null) {
            datesInfo.setText(session.getCheckInDate().format(shortDateFormatter) + " - " + session.getCheckOutDate().format(shortDateFormatter));
        }
        if (nightsInfo != null) {
            nightsInfo.setText(String.valueOf(nights));
        }

        // Determine recommended room type based on guest count
        RoomType recommendedType;
        if (totalGuests <= 2) {
            recommendedType = RoomType.SINGLE;
        } else if (totalGuests <= 4) {
            recommendedType = RoomType.DOUBLE;
        } else {
            recommendedType = RoomType.PENTHOUSE;
        }
        session.setSelectedRoomType(recommendedType);

        // Update suggested section
        if (suggestedRoomName != null) {
            suggestedRoomName.setText("1 x " + recommendedType.getDisplayName());
        }
        if (suggestedRoomDesc != null) {
            suggestedRoomDesc.setText("Perfect for up to " + recommendedType.getMaxOccupancy() + " guests");
        }
        if (suggestedRoomPrice != null) {
            suggestedRoomPrice.setText(String.format("$%.0f", recommendedType.getBasePrice()));
        }
        if (suggestedTotalLabel != null) {
            double total = recommendedType.getBasePrice() * nights;
            suggestedTotalLabel.setText(String.format("Total for %d night%s: $%.2f", nights, nights == 1 ? "" : "s", total));
        }

        // Initialize custom section
        if (customRequiredLabel != null) {
            customRequiredLabel.setText("Required: " + totalGuests + " guests");
        }
        updateCustomRoomSummary();

        // Setup toggle buttons for suggested vs custom room selection
        if (suggestedToggle != null && customToggle != null) {
            suggestedToggle.setOnAction(e -> {
                useCustomRooms = false;
                suggestedToggle.setSelected(true);
                customToggle.setSelected(false);
                if (suggestedSection != null) {
                    suggestedSection.setVisible(true);
                    suggestedSection.setManaged(true);
                }
                if (customSection != null) {
                    customSection.setVisible(false);
                    customSection.setManaged(false);
                }
            });
            customToggle.setOnAction(e -> {
                useCustomRooms = true;
                customToggle.setSelected(true);
                suggestedToggle.setSelected(false);
                if (suggestedSection != null) {
                    suggestedSection.setVisible(false);
                    suggestedSection.setManaged(false);
                }
                if (customSection != null) {
                    customSection.setVisible(true);
                    customSection.setManaged(true);
                }
            });
        }
    }

    private void updateCustomRoomSummary() {
        BookingSession session = BookingSession.getInstance();
        int nights = session.getNights();
        if (nights == 0) nights = 1;

        // Calculate total capacity
        int totalCapacity = (singleQty * 2) + (doubleQty * 4) + (deluxeQty * 4) + (penthouseQty * 6);

        // Calculate subtotal
        double subtotal = (singleQty * 100.0 * nights) +
                          (doubleQty * 150.0 * nights) +
                          (deluxeQty * 200.0 * nights) +
                          (penthouseQty * 350.0 * nights);

        // Update labels
        if (customCapacityLabel != null) {
            customCapacityLabel.setText("Total Capacity: " + totalCapacity + " guests");
        }
        if (customSubtotalLabel != null) {
            customSubtotalLabel.setText(String.format("$%.2f", subtotal));
        }

        // Update quantity labels
        if (singleQtyLabel != null) singleQtyLabel.setText(String.valueOf(singleQty));
        if (doubleQtyLabel != null) doubleQtyLabel.setText(String.valueOf(doubleQty));
        if (deluxeQtyLabel != null) deluxeQtyLabel.setText(String.valueOf(deluxeQty));
        if (penthouseQtyLabel != null) penthouseQtyLabel.setText(String.valueOf(penthouseQty));
    }

    private void initializeAddonsScreen(BookingSession session) {
        if (roomTotalLabel != null) {
            double roomCost = session.calculateRoomCost();
            roomTotalLabel.setText(String.format("$%.2f", roomCost));
        }

        // Setup checkbox listeners to update pricing
        setupAddonCheckboxListener(wifiCheckbox);
        setupAddonCheckboxListener(breakfastCheckbox);
        setupAddonCheckboxListener(parkingCheckbox);
        setupAddonCheckboxListener(spaCheckbox);
        setupAddonCheckboxListener(airportCheckbox);

        // Initialize addon totals
        updateAddonPricing();
    }

    private void setupAddonCheckboxListener(CheckBox checkbox) {
        if (checkbox != null) {
            checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> updateAddonPricing());
        }
    }

    private void updateAddonPricing() {
        BookingSession session = BookingSession.getInstance();
        int nights = session.getNights();
        if (nights == 0) nights = 2; // Default

        double addonTotal = 0;

        // Calculate addon costs based on selections
        if (wifiCheckbox != null && wifiCheckbox.isSelected()) {
            addonTotal += 10; // Wi-Fi per reservation
        }
        if (breakfastCheckbox != null && breakfastCheckbox.isSelected()) {
            addonTotal += 25 * nights; // Breakfast per night
        }
        if (parkingCheckbox != null && parkingCheckbox.isSelected()) {
            addonTotal += 15 * nights; // Parking per night
        }
        if (spaCheckbox != null && spaCheckbox.isSelected()) {
            addonTotal += 150; // Spa per reservation
        }
        if (airportCheckbox != null && airportCheckbox.isSelected()) {
            addonTotal += 50; // Airport shuttle per reservation
        }

        // Update labels
        if (addonsSelectedLabel != null) {
            addonsSelectedLabel.setText(String.format("$%.2f", addonTotal));
        }

        double roomCost = session.calculateRoomCost();
        if (subtotalLabel != null) {
            subtotalLabel.setText(String.format("$%.2f", roomCost + addonTotal));
        }
    }

    private void initializeGuestDetailsScreen(BookingSession session) {
        // Pre-fill if data exists in session
        if (firstNameField != null && session.getFirstName() != null) {
            firstNameField.setText(session.getFirstName());
        }
        if (lastNameField != null && session.getLastName() != null) {
            lastNameField.setText(session.getLastName());
        }
        if (emailField != null && session.getEmail() != null) {
            emailField.setText(session.getEmail());
        }
        if (phoneField != null && session.getPhone() != null) {
            phoneField.setText(session.getPhone());
        }
    }

    private void initializeSummaryScreen(BookingSession session) {
        if (summaryName == null) return; // Not on summary screen

        // Guest info
        summaryName.setText(session.getFullName() != null ? session.getFullName() : "");
        if (summaryEmail != null) {
            summaryEmail.setText(session.getEmail() != null ? session.getEmail() : "");
        }
        if (summaryPhone != null) {
            summaryPhone.setText(session.getPhone() != null ? session.getPhone() : "");
        }
        if (summaryGuests != null) {
            summaryGuests.setText(session.getAdults() + " Adults, " + session.getChildren() + " Children");
        }

        // Stay details
        if (summaryCheckIn != null && session.getCheckInDate() != null) {
            summaryCheckIn.setText(session.getCheckInDate().format(dateFormatter) + " (3:00 PM)");
        }
        if (summaryCheckOut != null && session.getCheckOutDate() != null) {
            summaryCheckOut.setText(session.getCheckOutDate().format(dateFormatter) + " (11:00 AM)");
        }
        if (summaryNights != null) {
            summaryNights.setText(session.getNights() + " Nights");
        }

        // Room info
        if (summaryRoomType != null) {
            if (session.hasCustomRooms()) {
                StringBuilder roomText = new StringBuilder();
                if (session.getSingleRoomCount() > 0) {
                    roomText.append(session.getSingleRoomCount()).append(" x Single Room\n");
                }
                if (session.getDoubleRoomCount() > 0) {
                    roomText.append(session.getDoubleRoomCount()).append(" x Double Room\n");
                }
                if (session.getDeluxeRoomCount() > 0) {
                    roomText.append(session.getDeluxeRoomCount()).append(" x Deluxe Room\n");
                }
                if (session.getPenthouseRoomCount() > 0) {
                    roomText.append(session.getPenthouseRoomCount()).append(" x Penthouse Suite");
                }
                summaryRoomType.setText(roomText.toString().trim());
            } else {
                summaryRoomType.setText("1 x " + session.getSelectedRoomType().getDisplayName());
            }
        }
        if (summaryRoomPrice != null) {
            if (session.hasCustomRooms()) {
                summaryRoomPrice.setText("(Multiple rooms)");
            } else {
                summaryRoomPrice.setText(String.format("$%.2f/night", session.getSelectedRoomType().getBasePrice()));
            }
        }

        // Pricing calculation
        int nights = session.getNights();
        double roomCost = session.calculateRoomCost();
        double addonCost = calculateAddonCost(session);
        double subtotal = roomCost + addonCost;
        double tax = subtotal * 0.13;
        double total = subtotal + tax;

        if (roomCharges != null) {
            roomCharges.setText(String.format("$%.2f", roomCost));
        }
        if (subtotalAmount != null) {
            subtotalAmount.setText(String.format("$%.2f", subtotal));
        }
        if (taxAmount != null) {
            taxAmount.setText(String.format("$%.2f", tax));
        }
        if (totalAmount != null) {
            totalAmount.setText(String.format("$%.2f", total));
        }

        // Loyalty notice visibility
        if (loyaltyEnrollmentNotice != null) {
            loyaltyEnrollmentNotice.setVisible(session.isJoinLoyalty());
            loyaltyEnrollmentNotice.setManaged(session.isJoinLoyalty());
        }
    }

    private void initializeConfirmationScreen(BookingSession session) {
        Reservation reservation = session.getCompletedReservation();
        if (reservation == null || confirmationNumber == null) return;

        confirmationNumber.setText(reservation.getConfirmationNumber());

        if (confGuestName != null) {
            confGuestName.setText(reservation.getGuest().getFullName());
        }
        if (confCheckIn != null) {
            confCheckIn.setText(reservation.getCheckInDate().format(dateFormatter));
        }
        if (confCheckOut != null) {
            confCheckOut.setText(reservation.getCheckOutDate().format(dateFormatter));
        }
        if (confRoom != null && !reservation.getRooms().isEmpty()) {
            Room room = reservation.getRooms().get(0);
            confRoom.setText("1 x " + room.getType().getDisplayName());
        }
        if (confTotal != null) {
            confTotal.setText(String.format("$%.2f", reservation.getTotal()));
        }

        // Loyalty confirmation
        if (loyaltyConfirmation != null) {
            loyaltyConfirmation.setVisible(session.isJoinLoyalty());
            loyaltyConfirmation.setManaged(session.isJoinLoyalty());
        }
        if (loyaltyNumber != null && session.isJoinLoyalty()) {
            loyaltyNumber.setText("NR-" + String.format("%06d", reservation.getGuest().getId()));
        }
    }

    // ==================== HELPER METHODS ====================

    private void updateNightsDisplay() {
        if (nightsLabel != null && checkInPicker != null && checkOutPicker != null) {
            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();
            if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                int nights = (int) (checkOut.toEpochDay() - checkIn.toEpochDay());
                nightsLabel.setText(String.valueOf(nights));
            } else {
                nightsLabel.setText("0");
            }
        }
    }

    private double calculateAddonCost(BookingSession session) {
        double cost = 0;
        int nights = session.getNights();
        for (Addon addon : session.getSelectedAddons()) {
            cost += addon.calculateCost(nights);
        }
        return cost;
    }

    private void updateGuestCounts() {
        BookingSession session = BookingSession.getInstance();
        if (adultsLabel != null) {
            adultsLabel.setText(String.valueOf(session.getAdults()));
        }
        if (childrenLabel != null) {
            childrenLabel.setText(String.valueOf(session.getChildren()));
        }
        if (totalGuestsLabel != null) {
            totalGuestsLabel.setText(String.valueOf(session.getTotalGuests()));
        }
    }

    // ==================== NAVIGATION METHODS ====================

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
        BookingSession.getInstance().reset();
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
        // Save guest count to session
        BookingSession session = BookingSession.getInstance();
        if (adultsLabel != null) {
            session.setAdults(Integer.parseInt(adultsLabel.getText()));
        }
        if (childrenLabel != null) {
            session.setChildren(Integer.parseInt(childrenLabel.getText()));
        }
        loadScreen("/view/kiosk/KioskDateSelection.fxml", "NewnhamNexus - Select Dates");
    }

    @FXML
    private void goToRoomSelection() {
        // Save dates to session
        BookingSession session = BookingSession.getInstance();
        if (checkInPicker != null && checkInPicker.getValue() != null) {
            session.setCheckInDate(checkInPicker.getValue());
        }
        if (checkOutPicker != null && checkOutPicker.getValue() != null) {
            session.setCheckOutDate(checkOutPicker.getValue());
        }

        // Validate dates
        if (session.getCheckInDate() == null || session.getCheckOutDate() == null) {
            if (dateValidationLabel != null) {
                dateValidationLabel.setText("Please select both check-in and check-out dates");
            }
            return;
        }
        if (!session.getCheckOutDate().isAfter(session.getCheckInDate())) {
            if (dateValidationLabel != null) {
                dateValidationLabel.setText("Check-out must be after check-in");
            }
            return;
        }

        loadScreen("/view/kiosk/KioskRoomSelection.fxml", "NewnhamNexus - Select Room");
    }

    @FXML
    private void goToAddOns() {
        BookingSession session = BookingSession.getInstance();

        if (useCustomRooms) {
            // Validate custom room selection
            int totalCapacity = (singleQty * 2) + (doubleQty * 4) + (deluxeQty * 4) + (penthouseQty * 6);
            int totalRooms = singleQty + doubleQty + deluxeQty + penthouseQty;

            if (totalRooms == 0) {
                if (roomValidationLabel != null) {
                    roomValidationLabel.setText("Please select at least one room");
                }
                return;
            }

            if (totalCapacity < session.getTotalGuests()) {
                if (roomValidationLabel != null) {
                    roomValidationLabel.setText("Selected rooms cannot accommodate all guests. Need capacity for " + session.getTotalGuests() + " guests.");
                }
                return;
            }

            // Store custom room counts in session
            session.setCustomRoomCounts(singleQty, doubleQty, deluxeQty, penthouseQty);

            // Set the primary room type (the one with most rooms, or highest tier if equal)
            if (penthouseQty > 0) {
                session.setSelectedRoomType(RoomType.PENTHOUSE);
            } else if (deluxeQty > 0) {
                session.setSelectedRoomType(RoomType.DELUXE);
            } else if (doubleQty > 0) {
                session.setSelectedRoomType(RoomType.DOUBLE);
            } else {
                session.setSelectedRoomType(RoomType.SINGLE);
            }
        } else {
            // Using suggested room - already set in initializeRoomSelectionScreen
            // Clear any custom selection
            session.setCustomRoomCounts(0, 0, 0, 0);
        }

        loadScreen("/view/kiosk/KioskAddOns.fxml", "NewnhamNexus - Add-Ons");
    }

    @FXML
    private void skipAddOns() {
        BookingSession.getInstance().clearAddons();
        loadScreen("/view/kiosk/KioskGuestDetails.fxml", "NewnhamNexus - Guest Details");
    }

    @FXML
    private void goToGuestDetails() {
        // Save addon selections to session
        saveAddonSelections();
        loadScreen("/view/kiosk/KioskGuestDetails.fxml", "NewnhamNexus - Guest Details");
    }

    private void saveAddonSelections() {
        BookingSession session = BookingSession.getInstance();
        session.clearAddons();

        try {
            List<Addon> allAddons = addonRepository.findAll();

            if (wifiCheckbox != null && wifiCheckbox.isSelected()) {
                findAddonByName(allAddons, "Wi-Fi").ifPresent(session::addAddon);
            }
            if (breakfastCheckbox != null && breakfastCheckbox.isSelected()) {
                findAddonByName(allAddons, "Breakfast").ifPresent(session::addAddon);
            }
            if (parkingCheckbox != null && parkingCheckbox.isSelected()) {
                findAddonByName(allAddons, "Parking").ifPresent(session::addAddon);
            }
            if (spaCheckbox != null && spaCheckbox.isSelected()) {
                findAddonByName(allAddons, "Spa").ifPresent(session::addAddon);
            }
            if (airportCheckbox != null && airportCheckbox.isSelected()) {
                findAddonByName(allAddons, "Airport").ifPresent(session::addAddon);
            }
        } catch (Exception e) {
            System.err.println("Error loading addons: " + e.getMessage());
        }
    }

    private java.util.Optional<Addon> findAddonByName(List<Addon> addons, String name) {
        return addons.stream()
            .filter(a -> a.getName().toLowerCase().contains(name.toLowerCase()))
            .findFirst();
    }

    @FXML
    private void goToSummary() {
        // Validate guest details
        if (!validateGuestDetails()) {
            return;
        }

        // Save guest details to session
        BookingSession session = BookingSession.getInstance();
        session.setFirstName(firstNameField.getText().trim());
        session.setLastName(lastNameField.getText().trim());
        session.setEmail(emailField.getText().trim());
        session.setPhone(phoneField.getText().trim());
        session.setJoinLoyalty(loyaltyCheckbox != null && loyaltyCheckbox.isSelected());

        loadScreen("/view/kiosk/KioskSummary.fxml", "NewnhamNexus - Review Booking");
    }

    private boolean validateGuestDetails() {
        boolean valid = true;

        // Clear previous errors
        clearError(firstNameError);
        clearError(lastNameError);
        clearError(emailError);
        clearError(phoneError);
        clearError(termsError);

        // Validate first name
        if (firstNameField == null || firstNameField.getText().trim().isEmpty()) {
            setError(firstNameError, "First name is required");
            valid = false;
        }

        // Validate last name
        if (lastNameField == null || lastNameField.getText().trim().isEmpty()) {
            setError(lastNameError, "Last name is required");
            valid = false;
        }

        // Validate email
        if (emailField == null || emailField.getText().trim().isEmpty()) {
            setError(emailError, "Email is required");
            valid = false;
        } else if (!emailField.getText().contains("@") || !emailField.getText().contains(".")) {
            setError(emailError, "Please enter a valid email");
            valid = false;
        }

        // Validate phone
        if (phoneField == null || phoneField.getText().trim().isEmpty()) {
            setError(phoneError, "Phone number is required");
            valid = false;
        }

        // Validate terms
        if (termsCheckbox == null || !termsCheckbox.isSelected()) {
            setError(termsError, "You must agree to the terms and conditions");
            valid = false;
        }

        return valid;
    }

    private void clearError(Label label) {
        if (label != null) label.setText("");
    }

    private void setError(Label label, String message) {
        if (label != null) label.setText(message);
    }

    @FXML
    private void confirmBooking() {
        BookingSession session = BookingSession.getInstance();

        try {
            // Create guest from session
            Guest guest = session.createGuest();

            // Get an available room of the selected type
            List<Room> availableRooms = roomRepository.findAvailableByTypeForDateRange(
                session.getSelectedRoomType(),
                session.getCheckInDate(),
                session.getCheckOutDate()
            );

            if (availableRooms.isEmpty()) {
                // Fallback to any available room
                availableRooms = roomRepository.findAvailableForDateRange(
                    session.getCheckInDate(),
                    session.getCheckOutDate()
                );
            }

            if (availableRooms.isEmpty()) {
                showError("No Rooms Available",
                    "Sorry, no rooms are available for your selected dates.\nPlease try different dates.");
                return;
            }

            // Select first available room
            List<Room> selectedRooms = List.of(availableRooms.get(0));

            // Create and save reservation to database
            Reservation reservation = reservationService.createReservation(
                guest,
                session.getAdults(),
                session.getChildren(),
                session.getCheckInDate(),
                session.getCheckOutDate(),
                selectedRooms,
                session.getSelectedAddons()
            );

            // Store completed reservation in session for confirmation screen
            session.setCompletedReservation(reservation);

            // Handle loyalty enrollment
            if (session.isJoinLoyalty()) {
                Guest savedGuest = reservation.getGuest();
                if (savedGuest.getLoyaltyNumber() == null) {
                    savedGuest.setLoyaltyNumber("NR-" + String.format("%06d", savedGuest.getId()));
                    savedGuest.setLoyaltyPoints(100); // Welcome bonus
                }
            }

            System.out.println("===========================================");
            System.out.println("RESERVATION SAVED TO DATABASE!");
            System.out.println("Confirmation Number: " + reservation.getConfirmationNumber());
            System.out.println("Guest: " + reservation.getGuest().getFullName());
            System.out.println("Room: " + reservation.getRooms().get(0).getRoomNumber());
            System.out.println("Total: $" + String.format("%.2f", reservation.getTotal()));
            System.out.println("===========================================");

            // Navigate to confirmation screen
            loadScreen("/view/kiosk/KioskConfirmation.fxml", "NewnhamNexus - Booking Confirmed");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Booking Error", "An error occurred while processing your booking:\n" + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void startNewBooking() {
        BookingSession.getInstance().reset();
        loadScreen("/view/kiosk/KioskWelcome.fxml", "NewnhamNexus - Kiosk");
    }

    @FXML
    private void printConfirmation() {
        BookingSession session = BookingSession.getInstance();
        Reservation reservation = session.getCompletedReservation();

        if (reservation == null) {
            showError("Print Error", "No reservation data available to print.");
            return;
        }

        // Create a printable summary
        StringBuilder printContent = new StringBuilder();
        printContent.append("═══════════════════════════════════════════\n");
        printContent.append("           NEWNHAM NEXUS HOTEL\n");
        printContent.append("           RESERVATION CONFIRMATION\n");
        printContent.append("═══════════════════════════════════════════\n\n");
        printContent.append("Confirmation Number: ").append(reservation.getConfirmationNumber()).append("\n\n");
        printContent.append("Guest: ").append(reservation.getGuest().getFullName()).append("\n");
        printContent.append("Email: ").append(reservation.getGuest().getEmail()).append("\n");
        printContent.append("Phone: ").append(reservation.getGuest().getPhone()).append("\n\n");
        printContent.append("Check-in:  ").append(reservation.getCheckInDate().format(dateFormatter)).append(" (3:00 PM)\n");
        printContent.append("Check-out: ").append(reservation.getCheckOutDate().format(dateFormatter)).append(" (11:00 AM)\n");
        printContent.append("Duration:  ").append(session.getNights()).append(" night(s)\n\n");

        if (!reservation.getRooms().isEmpty()) {
            Room room = reservation.getRooms().get(0);
            printContent.append("Room: ").append(room.getRoomNumber()).append(" - ").append(room.getType().getDisplayName()).append("\n");
        }

        printContent.append("\n───────────────────────────────────────────\n");
        printContent.append(String.format("Subtotal:      $%.2f\n", reservation.getSubtotal()));
        printContent.append(String.format("Tax (13%% HST): $%.2f\n", reservation.getTax()));
        printContent.append(String.format("TOTAL:         $%.2f\n", reservation.getTotal()));
        printContent.append("───────────────────────────────────────────\n\n");
        printContent.append("Payment due at check-in/check-out.\n");
        printContent.append("Please present valid ID upon arrival.\n\n");
        printContent.append("Thank you for choosing NewnhamNexus!\n");
        printContent.append("═══════════════════════════════════════════\n");

        // Show print preview dialog
        Alert printDialog = new Alert(Alert.AlertType.INFORMATION);
        printDialog.setTitle("Print Confirmation");
        printDialog.setHeaderText("Reservation Confirmation - Print Preview");

        TextArea textArea = new TextArea(printContent.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(450);
        textArea.setPrefHeight(400);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        printDialog.getDialogPane().setContent(textArea);
        printDialog.getDialogPane().setPrefWidth(500);

        // Add Print button
        ButtonType printButton = new ButtonType("Print", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        printDialog.getButtonTypes().setAll(printButton, closeButton);

        printDialog.showAndWait().ifPresent(response -> {
            if (response == printButton) {
                // In a real application, this would send to printer
                // For demo purposes, show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Print Sent");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Confirmation has been sent to the printer.\n\nIn a kiosk environment, this would print to the receipt printer.");
                successAlert.showAndWait();
            }
        });
    }

    // ==================== GUEST COUNT BUTTONS ====================

    @FXML
    private void increaseAdults() {
        BookingSession session = BookingSession.getInstance();
        if (session.getAdults() < 10) {
            session.setAdults(session.getAdults() + 1);
            updateGuestCounts();
        }
    }

    @FXML
    private void decreaseAdults() {
        BookingSession session = BookingSession.getInstance();
        if (session.getAdults() > 1) {
            session.setAdults(session.getAdults() - 1);
            updateGuestCounts();
        }
    }

    @FXML
    private void increaseChildren() {
        BookingSession session = BookingSession.getInstance();
        if (session.getChildren() < 10) {
            session.setChildren(session.getChildren() + 1);
            updateGuestCounts();
        }
    }

    @FXML
    private void decreaseChildren() {
        BookingSession session = BookingSession.getInstance();
        if (session.getChildren() > 0) {
            session.setChildren(session.getChildren() - 1);
            updateGuestCounts();
        }
    }

    @FXML
    private void showRoomPolicies() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Room Booking Policies");
        alert.setHeaderText("Occupancy Limits");
        alert.setContentText(
            "Single Room: Up to 2 guests - $100/night\n" +
            "Double Room: Up to 4 guests - $150/night\n" +
            "Deluxe Room: Up to 4 guests - $200/night\n" +
            "Penthouse Suite: Up to 6 guests - $350/night\n\n" +
            "Please ensure your selection meets the occupancy requirements."
        );
        alert.showAndWait();
    }

    // ==================== ROOM QUANTITY BUTTONS ====================

    @FXML
    private void increaseSingleRoom() {
        if (singleQty < 5) {
            singleQty++;
            updateCustomRoomSummary();
        }
    }

    @FXML
    private void decreaseSingleRoom() {
        if (singleQty > 0) {
            singleQty--;
            updateCustomRoomSummary();
        }
    }

    @FXML
    private void increaseDoubleRoom() {
        if (doubleQty < 5) {
            doubleQty++;
            updateCustomRoomSummary();
        }
    }

    @FXML
    private void decreaseDoubleRoom() {
        if (doubleQty > 0) {
            doubleQty--;
            updateCustomRoomSummary();
        }
    }

    @FXML
    private void increaseDeluxeRoom() {
        if (deluxeQty < 3) {
            deluxeQty++;
            updateCustomRoomSummary();
        }
    }

    @FXML
    private void decreaseDeluxeRoom() {
        if (deluxeQty > 0) {
            deluxeQty--;
            updateCustomRoomSummary();
        }
    }

    @FXML
    private void increasePenthouseRoom() {
        if (penthouseQty < 2) {
            penthouseQty++;
            updateCustomRoomSummary();
        }
    }

    @FXML
    private void decreasePenthouseRoom() {
        if (penthouseQty > 0) {
            penthouseQty--;
            updateCustomRoomSummary();
        }
    }

    // ==================== SCREEN LOADER ====================

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
