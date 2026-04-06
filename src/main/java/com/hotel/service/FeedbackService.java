package com.hotel.service;

import com.hotel.model.Feedback;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.hotel.repository.FeedbackRepository;
import com.hotel.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for feedback management.
 * Handles submission, filtering, and aggregation.
 */
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ReservationRepository reservationRepository;
    private final AuditService auditService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FeedbackService() {
        this.feedbackRepository = new FeedbackRepository();
        this.reservationRepository = new ReservationRepository();
        this.auditService = AuditService.getInstance();
    }

    /**
     * Submit feedback for a reservation.
     * Only allowed after checkout.
     */
    public Feedback submitFeedback(Reservation reservation, int rating, String comment, String sentimentTags) {
        // Validate reservation is checked out
        if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            throw new IllegalStateException("Feedback can only be submitted after checkout");
        }

        // Check if reservation is fully paid
        if (!reservation.isFullyPaid()) {
            throw new IllegalStateException("Feedback can only be submitted after balance is settled");
        }

        // Check if feedback already exists
        if (feedbackRepository.findByReservation(reservation).isPresent()) {
            throw new IllegalStateException("Feedback has already been submitted for this reservation");
        }

        // Create feedback
        Feedback feedback = new Feedback(reservation.getGuest(), reservation, rating, comment);
        feedback.setSentimentTag(sentimentTags);

        Feedback savedFeedback = feedbackRepository.save(feedback);

        auditService.log("GUEST", "FEEDBACK_SUBMIT", "Feedback", savedFeedback.getId().toString(),
            String.format("Feedback submitted for reservation %s. Rating: %d stars",
                reservation.getConfirmationNumber(), rating));

        return savedFeedback;
    }

    /**
     * Get feedback by reservation.
     */
    public Optional<Feedback> getFeedbackByReservation(Reservation reservation) {
        return feedbackRepository.findByReservation(reservation);
    }

    /**
     * Get feedback by reservation ID.
     */
    public Optional<Feedback> getFeedbackByReservationId(Long reservationId) {
        return feedbackRepository.findByReservationId(reservationId);
    }

    /**
     * Get all feedback by guest.
     */
    public List<Feedback> getFeedbackByGuest(Guest guest) {
        return feedbackRepository.findByGuest(guest);
    }

    /**
     * Get feedback by rating.
     */
    public List<Feedback> getFeedbackByRating(int rating) {
        return feedbackRepository.findByRating(rating);
    }

    /**
     * Get feedback by minimum rating.
     */
    public List<Feedback> getFeedbackByMinimumRating(int minRating) {
        return feedbackRepository.findByMinimumRating(minRating);
    }

    /**
     * Get feedback by sentiment tag.
     */
    public List<Feedback> getFeedbackBySentimentTag(String tag) {
        return feedbackRepository.findBySentimentTag(tag);
    }

    /**
     * Get feedback by date range.
     */
    public List<Feedback> getFeedbackByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return feedbackRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Get all feedback ordered by date.
     */
    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAllOrderByDate();
    }

    /**
     * Get average rating.
     */
    public double getAverageRating() {
        return feedbackRepository.getAverageRating();
    }

    /**
     * Get rating distribution.
     */
    public Map<Integer, Long> getRatingDistribution() {
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            distribution.put(i, feedbackRepository.countByRating(i));
        }
        return distribution;
    }

    /**
     * Get sentiment tag counts.
     */
    public Map<String, Long> getSentimentTagCounts() {
        Map<String, Long> counts = new HashMap<>();
        List<Feedback> allFeedback = feedbackRepository.findAll();

        for (Feedback fb : allFeedback) {
            if (fb.getSentimentTag() != null && !fb.getSentimentTag().isEmpty()) {
                String[] tags = fb.getSentimentTag().split(",");
                for (String tag : tags) {
                    tag = tag.trim();
                    counts.merge(tag, 1L, Long::sum);
                }
            }
        }

        return counts;
    }

    /**
     * Get feedback summary statistics.
     */
    public FeedbackSummary getSummary() {
        List<Feedback> allFeedback = feedbackRepository.findAll();
        return new FeedbackSummary(
            allFeedback.size(),
            getAverageRating(),
            getRatingDistribution(),
            getSentimentTagCounts()
        );
    }

    /**
     * Export feedback to CSV format.
     */
    public String exportToCSV(List<Feedback> feedbackList) {
        StringBuilder csv = new StringBuilder();
        csv.append("Reservation ID,Confirmation Number,Guest Name,Rating,Comment,Sentiment Tags,Submitted At\n");

        for (Feedback fb : feedbackList) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\"\n",
                fb.getReservation().getId(),
                fb.getReservation().getConfirmationNumber(),
                fb.getGuest().getFullName(),
                fb.getRating(),
                fb.getComment() != null ? fb.getComment().replace("\"", "\"\"") : "",
                fb.getSentimentTag() != null ? fb.getSentimentTag() : "",
                fb.getSubmittedAt().format(FORMATTER)));
        }

        return csv.toString();
    }

    /**
     * Feedback summary class.
     */
    public static class FeedbackSummary {
        private final int totalCount;
        private final double averageRating;
        private final Map<Integer, Long> ratingDistribution;
        private final Map<String, Long> sentimentTagCounts;

        public FeedbackSummary(int totalCount, double averageRating,
                              Map<Integer, Long> ratingDistribution, Map<String, Long> sentimentTagCounts) {
            this.totalCount = totalCount;
            this.averageRating = averageRating;
            this.ratingDistribution = ratingDistribution;
            this.sentimentTagCounts = sentimentTagCounts;
        }

        public int getTotalCount() { return totalCount; }
        public double getAverageRating() { return averageRating; }
        public Map<Integer, Long> getRatingDistribution() { return ratingDistribution; }
        public Map<String, Long> getSentimentTagCounts() { return sentimentTagCounts; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== FEEDBACK SUMMARY ===\n");
            sb.append("Total Feedback: ").append(totalCount).append("\n");
            sb.append(String.format("Average Rating: %.2f / 5.0\n", averageRating));
            sb.append("\nRating Distribution:\n");
            for (Map.Entry<Integer, Long> entry : ratingDistribution.entrySet()) {
                sb.append(String.format("  %d stars: %d\n", entry.getKey(), entry.getValue()));
            }
            sb.append("\nSentiment Tags:\n");
            for (Map.Entry<String, Long> entry : sentimentTagCounts.entrySet()) {
                sb.append(String.format("  %s: %d\n", entry.getKey(), entry.getValue()));
            }
            return sb.toString();
        }
    }
}
