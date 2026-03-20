package com.hotel.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Feedback {
    private final Guest guest;
    private final DoubleProperty rating;
    private final StringProperty desc = new SimpleStringProperty();

    public Feedback(Guest guest, DoubleProperty rating) {
        this.guest = guest;
        this.rating = rating;
    }

    public Guest getGuest() {
        return guest;
    }

    public Double getRating() {
        return rating.get();
    }

    public DoubleProperty getRatingProperty() {
        return rating;
    }

    public String getDesc() {
        return desc.get();
    }

    public StringProperty getDescProperty() {
        return desc;
    }
}
