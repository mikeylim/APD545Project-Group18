package com.hotel.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Addon {
    private final StringProperty name = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final PricingModel pricingModel;

    public Addon(String name, double price, PricingModel pricingModel) {
        this.name.setValue(name);
        this.price.setValue(price);
        this.pricingModel = pricingModel;
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

    public PricingModel getPricingModel() {
        return pricingModel;
    }
}
