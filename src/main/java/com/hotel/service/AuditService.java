package com.hotel.service;

import com.hotel.model.AuditLog;
import com.hotel.repository.AuditLogRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.*;

/**
 * Audit service for activity logging.
 * Logs to both database and rotating log files.
 *
 * File logging: 1MB limit, 10 files rotation as per PDF requirements.
 */
public class AuditService {

    private static AuditService instance;
    private final AuditLogRepository auditLogRepository;
    private final Logger fileLogger;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditService() {
        this.auditLogRepository = new AuditLogRepository();
        this.fileLogger = setupFileLogger();
    }

    /**
     * Get singleton instance.
     */
    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    /**
     * Setup rotating file logger.
     * 1MB limit, 10 files max as per PDF requirements.
     */
    private Logger setupFileLogger() {
        Logger logger = Logger.getLogger("HotelAuditLog");
        logger.setUseParentHandlers(false);

        try {
            // Create logs directory if it doesn't exist
            java.io.File logsDir = new java.io.File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            // Rotating file handler: 1MB limit, 10 files max
            FileHandler fileHandler = new FileHandler(
                "logs/system_logs.%g.log",  // Pattern with generation number
                1024 * 1024,                 // 1MB limit per file
                10,                          // Keep up to 10 files
                true                         // Append mode
            );

            // Custom formatter for log entries
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("[%s] %s%n",
                        LocalDateTime.now().format(FORMATTER),
                        record.getMessage());
                }
            });

            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            System.err.println("Failed to initialize file logger: " + e.getMessage());
            // Fallback to console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            logger.addHandler(consoleHandler);
        }

        return logger;
    }

    /**
     * Log an action to both database and file.
     */
    public void log(String actor, String action, String entityType, String entityId, String message) {
        // Create database entry
        AuditLog auditLog = new AuditLog(actor, action, entityType, entityId, message);

        try {
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            System.err.println("Failed to save audit log to database: " + e.getMessage());
        }

        // Log to file
        String logEntry = String.format("%s | %s | %s | %s | %s | %s",
            auditLog.getTimestamp().format(FORMATTER),
            actor,
            action,
            entityType,
            entityId != null ? entityId : "N/A",
            message);

        fileLogger.info(logEntry);
    }

    /**
     * Log an action without entity ID.
     */
    public void log(String actor, String action, String entityType, String message) {
        log(actor, action, entityType, null, message);
    }

    /**
     * Log a system action.
     */
    public void logSystem(String action, String message) {
        log("SYSTEM", action, "System", null, message);
    }

    /**
     * Log an exception.
     */
    public void logException(String actor, String action, String entityType, Exception e) {
        String message = "Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        log(actor, action + "_ERROR", entityType, null, message);
        fileLogger.log(Level.SEVERE, message, e);
    }

    /**
     * Get recent audit logs.
     */
    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogRepository.findRecent(limit);
    }

    /**
     * Get all audit logs.
     */
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllOrderByTimestamp();
    }

    /**
     * Get logs by date range.
     */
    public List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Get logs by actor.
     */
    public List<AuditLog> getLogsByActor(String actor) {
        return auditLogRepository.findByActor(actor);
    }

    /**
     * Get logs by action type.
     */
    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    /**
     * Get login attempts.
     */
    public List<AuditLog> getLoginAttempts() {
        return auditLogRepository.findLoginAttempts();
    }

    /**
     * Search logs by message.
     */
    public List<AuditLog> searchLogs(String searchTerm) {
        return auditLogRepository.searchByMessage(searchTerm);
    }

    /**
     * Export logs to CSV format.
     */
    public String exportToCSV(List<AuditLog> logs) {
        StringBuilder csv = new StringBuilder();
        csv.append("Timestamp,Actor,Action,Entity Type,Entity ID,Message\n");

        for (AuditLog log : logs) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                log.getTimestamp().format(FORMATTER),
                log.getActor(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId() != null ? log.getEntityId() : "",
                log.getMessage() != null ? log.getMessage().replace("\"", "\"\"") : ""));
        }

        return csv.toString();
    }

    /**
     * Export logs to TXT format.
     */
    public String exportToTXT(List<AuditLog> logs) {
        StringBuilder txt = new StringBuilder();
        txt.append("=== AUDIT LOG EXPORT ===\n");
        txt.append("Generated: ").append(LocalDateTime.now().format(FORMATTER)).append("\n");
        txt.append("Total entries: ").append(logs.size()).append("\n");
        txt.append("=".repeat(80)).append("\n\n");

        for (AuditLog log : logs) {
            txt.append(String.format("[%s] %s - %s\n",
                log.getTimestamp().format(FORMATTER),
                log.getActor(),
                log.getAction()));
            txt.append(String.format("  Entity: %s (ID: %s)\n",
                log.getEntityType(),
                log.getEntityId() != null ? log.getEntityId() : "N/A"));
            txt.append(String.format("  Message: %s\n",
                log.getMessage() != null ? log.getMessage() : "N/A"));
            txt.append("-".repeat(80)).append("\n");
        }

        return txt.toString();
    }
}
