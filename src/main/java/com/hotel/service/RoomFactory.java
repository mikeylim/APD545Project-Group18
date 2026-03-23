package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.model.RoomType;

/**
 * Factory Pattern implementation for creating Room instances.
 * Generates different room types (Single, Double, Deluxe, Penthouse)
 * with their configured attributes.
 */
public class RoomFactory {

    /**
     * Create a room of the specified type.
     *
     * @param roomNumber The room number (e.g., "101", "202")
     * @param type The type of room to create
     * @return A new Room instance with configured attributes
     */
    public static Room createRoom(String roomNumber, RoomType type) {
        return new Room(roomNumber, type);
    }

    /**
     * Create a room with floor information.
     *
     * @param roomNumber The room number
     * @param type The type of room
     * @param floor The floor number
     * @return A new Room instance
     */
    public static Room createRoom(String roomNumber, RoomType type, int floor) {
        return new Room(roomNumber, type, floor);
    }

    /**
     * Create a Single room (max 2 guests, base price $100).
     */
    public static Room createSingleRoom(String roomNumber) {
        return createRoom(roomNumber, RoomType.SINGLE);
    }

    /**
     * Create a Single room with floor.
     */
    public static Room createSingleRoom(String roomNumber, int floor) {
        return createRoom(roomNumber, RoomType.SINGLE, floor);
    }

    /**
     * Create a Double room (max 4 guests, base price $150).
     */
    public static Room createDoubleRoom(String roomNumber) {
        return createRoom(roomNumber, RoomType.DOUBLE);
    }

    /**
     * Create a Double room with floor.
     */
    public static Room createDoubleRoom(String roomNumber, int floor) {
        return createRoom(roomNumber, RoomType.DOUBLE, floor);
    }

    /**
     * Create a Deluxe room (max 2 guests, base price $200).
     */
    public static Room createDeluxeRoom(String roomNumber) {
        return createRoom(roomNumber, RoomType.DELUXE);
    }

    /**
     * Create a Deluxe room with floor.
     */
    public static Room createDeluxeRoom(String roomNumber, int floor) {
        return createRoom(roomNumber, RoomType.DELUXE, floor);
    }

    /**
     * Create a Penthouse room (max 2 guests, base price $350).
     */
    public static Room createPenthouseRoom(String roomNumber) {
        return createRoom(roomNumber, RoomType.PENTHOUSE);
    }

    /**
     * Create a Penthouse room with floor.
     */
    public static Room createPenthouseRoom(String roomNumber, int floor) {
        return createRoom(roomNumber, RoomType.PENTHOUSE, floor);
    }
}
