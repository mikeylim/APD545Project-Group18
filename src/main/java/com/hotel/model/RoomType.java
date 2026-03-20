package com.hotel.model;

import javafx.beans.property.*;

public class RoomType {
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final IntegerProperty capacity = new SimpleIntegerProperty();

    public RoomType(String name, double price, int capacity) {
        this.name.set(name);
        this.price.set(price);
        this.capacity.set(capacity);
    }

    public String getName() {
        return name.getValue();
    }

    public StringProperty getNameProperty() {
        return name;
    }

    public double getPrice() {
        return price.getValue();
    }

    public DoubleProperty getPriceProperty() {
        return price;
    }

    public int getCapacity() {
        return capacity.getValue();
    }

    public IntegerProperty getCapacityProperty() {
        return capacity;
    }
}
