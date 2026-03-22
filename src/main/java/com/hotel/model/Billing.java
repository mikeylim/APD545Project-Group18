package com.hotel.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Billing {
    private final Reservation reserv;
    private final DoubleProperty roomCost = new SimpleDoubleProperty();
    private final DoubleProperty addonCost = new SimpleDoubleProperty();
    private final DoubleProperty tax = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();

    public Billing(Reservation reserv, double roomCost, double addonCost, double tax, double total) {
        this.reserv = reserv;
        this.roomCost.set(roomCost);
        this.addonCost.set(addonCost);
        this.tax.set(tax);
        this.total.set(total);
    }

    public Reservation getReserv() {
        return reserv;
    }

    public double getRoomCost() {
        return roomCost.getValue();
    }

    public DoubleProperty getRoomCostProperty() {
        return roomCost;
    }

    public double getAddonCost() {
        return addonCost.getValue();
    }

    public DoubleProperty getAddonCostProperty() {
        return addonCost;
    }

    public double getTax() {
        return tax.getValue();
    }

    public DoubleProperty getTaxProperty() {
        return tax;
    }

    public double getTotal() {
        return total.getValue();
    }

    public DoubleProperty getTotalProperty() {
        return total;
    }
}
