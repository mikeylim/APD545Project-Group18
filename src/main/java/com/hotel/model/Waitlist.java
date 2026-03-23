package com.hotel.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "waitlist")
public class Waitlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private LocalDate desiredCheckIn;

    @Column(nullable = false)
    private LocalDate desiredCheckOut;

    @Column
    private LocalDateTime createdAt;

    @Column
    private boolean notified = false;

    @Column
    private boolean converted = false; // Converted to reservation

    // Default constructor required by JPA
    public Waitlist() {
        this.createdAt = LocalDateTime.now();
    }

    public Waitlist(Guest guest, RoomType roomType, LocalDate desiredCheckIn, LocalDate desiredCheckOut) {
        this();
        this.guest = guest;
        this.roomType = roomType;
        this.desiredCheckIn = desiredCheckIn;
        this.desiredCheckOut = desiredCheckOut;
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

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public LocalDate getDesiredCheckIn() {
        return desiredCheckIn;
    }

    public void setDesiredCheckIn(LocalDate desiredCheckIn) {
        this.desiredCheckIn = desiredCheckIn;
    }

    public LocalDate getDesiredCheckOut() {
        return desiredCheckOut;
    }

    public void setDesiredCheckOut(LocalDate desiredCheckOut) {
        this.desiredCheckOut = desiredCheckOut;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public boolean isConverted() {
        return converted;
    }

    public void setConverted(boolean converted) {
        this.converted = converted;
    }
}
