package com.hotel.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public class Reservation {
    private final Guest guest;
    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ReservationStatus status;
    private final IntegerProperty adults = new SimpleIntegerProperty();
    private final IntegerProperty children = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> checkIn = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> checkOut = new SimpleObjectProperty<>();
    private final ObservableList<Addon> addons = FXCollections.observableArrayList();

    public Reservation(Guest guest, ReservationStatus status, int adults, int children, LocalDate checkIn, LocalDate checkOut) {
        this.guest = guest;
        this.status = status;
        this.adults.set(adults);
        this.children.set(children);
        this.checkIn.set(checkIn);
        this.checkOut.set(checkOut);
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public Guest getGuest() {
        return guest;
    }

    public ObservableList<Room> getRooms() {
        return rooms;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public int getAdults() {
        return adults.getValue();
    }

    public IntegerProperty getAdultsProperty() {
        return adults;
    }

    public int getChildren() {
        return children.getValue();
    }

    public IntegerProperty getChildrenProperty() {
        return children;
    }

    public ObjectProperty<LocalDate> getCheckInProperty() {
        return checkIn;
    }

    public ObjectProperty<LocalDate> getCheckOutProperty() {
        return checkOut;
    }
}
