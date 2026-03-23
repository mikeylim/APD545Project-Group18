package com.hotel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "addons")
public class Addon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PricingModel pricingModel;

    // Default constructor required by JPA
    public Addon() {}

    public Addon(String name, double price, PricingModel pricingModel) {
        this.name = name;
        this.price = price;
        this.pricingModel = pricingModel;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public PricingModel getPricingModel() {
        return pricingModel;
    }

    public void setPricingModel(PricingModel pricingModel) {
        this.pricingModel = pricingModel;
    }

    /**
     * Calculate addon cost based on number of nights
     */
    public double calculateCost(int nights) {
        if (pricingModel == PricingModel.PER_NIGHT) {
            return price * nights;
        }
        return price; // PER_RESERVATION
    }
}
