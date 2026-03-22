package com.hotel.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Room {
    private final IntegerProperty roomNumber = new SimpleIntegerProperty();
    private final RoomType type;
    private final RoomStatus status;

    public Room(int roomNumber, RoomType type, RoomStatus status) {
        this.roomNumber.set(roomNumber);
        this.type = type;
        this.status = status;
    }

    public int getRoomNumber() {
        return roomNumber.get();
    }

    public IntegerProperty getRoomNumberProperty() {
        return roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public RoomStatus getStatus() {
        return status;
    }
}
