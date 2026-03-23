package com.hotel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private int rating; // 1-5 stars

    @Column(length = 1000)
    private String comment;

    @Column
    private String sentimentTag; // e.g., "cleanliness", "service", "value"

    @Column
    private LocalDateTime submittedAt;

    // Default constructor required by JPA
    public Feedback() {
        this.submittedAt = LocalDateTime.now();
    }

    public Feedback(Guest guest, Reservation reservation, int rating, String comment) {
        this();
        this.guest = guest;
        this.reservation = reservation;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1) rating = 1;
        if (rating > 5) rating = 5;
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSentimentTag() {
        return sentimentTag;
    }

    public void setSentimentTag(String sentimentTag) {
        this.sentimentTag = sentimentTag;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
