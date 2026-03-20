package com.hotel.model;

import javafx.beans.property.SimpleStringProperty;

public class User {
    private final String username;
    private final String passwHash;
    private final Role role;

    public User(String username, String passwHash, Role role) {
        this.username = username;
        this.passwHash = passwHash;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswHash() {
        return passwHash;
    }

    public Role getRole() {
        return role;
    }
}
