package com.hotel.model;

import java.time.LocalDate;

public class AuditLog {
    private final LocalDate timestamp;
    private final User actor;
    private final String action;
    private final String entityType;

    public AuditLog(LocalDate timestamp, User actor, String action, String entityType) {
        this.timestamp = timestamp;
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public User getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }
}
