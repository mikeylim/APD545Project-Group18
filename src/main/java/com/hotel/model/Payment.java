package com.hotel.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Payment {
    private final Billing billing;
    private final PaymentMethod paymentMethod;
    private final DoubleProperty amount = new SimpleDoubleProperty();

    public Payment(Billing billing, PaymentMethod paymentMethod) {
        this.billing = billing;
        this.paymentMethod = paymentMethod;
    }
}
