package com.hotel.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Guest {
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty confirmationNumber = new SimpleStringProperty();

    public Guest(String firstName, String lastName, String email, String phone, String confirmationNumber) {
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.email.set(email);
        this.phone.set(phone);
        this.confirmationNumber.set(confirmationNumber);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public StringProperty getFirstNameProperty() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public StringProperty getLastNameProperty() {
        return lastName;
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty getEmailProperty() {
        return email;
    }

    public String getPhone() {
        return phone.get();
    }

    public StringProperty getPhoneProperty() {
        return phone;
    }

    public String getConfirmationNumber() {
        return confirmationNumber.get();
    }

    public StringProperty getConfirmationNumberProperty() {
        return confirmationNumber;
    }
}
