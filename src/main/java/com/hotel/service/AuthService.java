package com.hotel.service;

import com.hotel.model.User;
import com.hotel.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Authentication service for admin login/logout.
 * Uses BCrypt for password verification.
 */
public class AuthService {

    private final UserRepository userRepository;
    private final AuditService auditService;
    private User currentUser;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.auditService = AuditService.getInstance();
    }

    /**
     * Authenticate user with username and password.
     * @return true if login successful, false otherwise
     */
    public boolean login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            auditService.log("UNKNOWN", "LOGIN_FAILED", "User", null,
                "Failed login attempt for username: " + username);
            return false;
        }

        User user = userOpt.get();

        // Check if user is active
        if (!user.isActive()) {
            auditService.log(username, "LOGIN_FAILED", "User", user.getId().toString(),
                "Login attempt for inactive account");
            return false;
        }

        // Verify password with BCrypt
        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            this.currentUser = user;
            auditService.log(username, "LOGIN", "User", user.getId().toString(),
                "User logged in successfully");
            return true;
        } else {
            auditService.log(username, "LOGIN_FAILED", "User", user.getId().toString(),
                "Invalid password");
            return false;
        }
    }

    /**
     * Logout current user.
     */
    public void logout() {
        if (currentUser != null) {
            auditService.log(currentUser.getUsername(), "LOGOUT", "User",
                currentUser.getId().toString(), "User logged out");
            currentUser = null;
        }
    }

    /**
     * Get currently logged in user.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Get current user's username.
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "SYSTEM";
    }

    /**
     * Get current user's maximum discount percentage.
     */
    public double getMaxDiscountPercent() {
        return currentUser != null ? currentUser.getMaxDiscountPercent() : 0.0;
    }

    /**
     * Check if current user can apply a given discount percentage.
     */
    public boolean canApplyDiscount(double discountPercent) {
        if (currentUser == null) return false;
        return discountPercent <= currentUser.getMaxDiscountPercent();
    }

    /**
     * Create a new admin/manager user.
     */
    public User createUser(String username, String password, com.hotel.model.Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(username, hashedPassword, role);
        User savedUser = userRepository.save(user);

        auditService.log(getCurrentUsername(), "CREATE", "User", savedUser.getId().toString(),
            "Created new user: " + username + " with role: " + role);

        return savedUser;
    }

    /**
     * Change user password.
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        // Verify old password
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            return false;
        }

        // Update with new password
        user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.update(user);

        auditService.log(getCurrentUsername(), "UPDATE", "User", user.getId().toString(),
            "Password changed for user: " + username);

        return true;
    }

    /**
     * Deactivate a user account.
     */
    public void deactivateUser(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            userRepository.update(user);

            auditService.log(getCurrentUsername(), "DEACTIVATE", "User", user.getId().toString(),
                "Deactivated user: " + username);
        }
    }
}
