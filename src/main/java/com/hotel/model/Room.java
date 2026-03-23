package com.hotel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column
    private int floor;

    // Default constructor required by JPA
    public Room() {}

    public Room(String roomNumber, RoomType type) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.status = RoomStatus.AVAILABLE;
    }

    public Room(String roomNumber, RoomType type, int floor) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.floor = floor;
        this.status = RoomStatus.AVAILABLE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public double getBasePrice() {
        return type.getBasePrice();
    }

    public int getMaxOccupancy() {
        return type.getMaxOccupancy();
    }

    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }
}
