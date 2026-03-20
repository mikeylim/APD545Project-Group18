package com.hotel.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

public class Waitlist {
    private final Guest guest;
    private final RoomType roomType;
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();

    public Waitlist(Guest guest, RoomType roomType, LocalDate startDate, LocalDate endDate) {
        this.guest = guest;
        this.roomType = roomType;
        this.startDate.set(startDate);
        this.endDate.set(endDate);
    }

    public Guest getGuest() {
        return guest;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public LocalDate getStartDate() {
        return startDate.getValue();
    }

    public ObjectProperty<LocalDate> getStartDateProperty() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate.getValue();
    }

    public ObjectProperty<LocalDate> getEndDateProperty() {
        return endDate;
    }
}
