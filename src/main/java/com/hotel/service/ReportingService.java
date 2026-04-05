package com.hotel.service;

import com.hotel.model.*;
import com.hotel.repository.*;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service for generating reports.
 * Supports revenue, occupancy, feedback, and activity reports.
 * Exports to CSV, PDF (text), and TXT formats.
 */
public class ReportingService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;
    private final FeedbackRepository feedbackRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportingService() {
        this.reservationRepository = new ReservationRepository();
        this.paymentRepository = new PaymentRepository();
        this.roomRepository = new RoomRepository();
        this.feedbackRepository = new FeedbackRepository();
        this.auditLogRepository = new AuditLogRepository();
        this.auditService = AuditService.getInstance();
    }

    // ========== Revenue Reports ==========

    /**
     * Generate daily revenue report.
     */
    public RevenueReport generateDailyRevenueReport(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return generateRevenueReport(startOfDay, endOfDay, "Daily Report: " + date);
    }

    /**
     * Generate weekly revenue report.
     */
    public RevenueReport generateWeeklyRevenueReport(LocalDate weekStart) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(6).atTime(23, 59, 59);
        return generateRevenueReport(start, end, "Weekly Report: " + weekStart + " to " + weekStart.plusDays(6));
    }

    /**
     * Generate monthly revenue report.
     */
    public RevenueReport generateMonthlyRevenueReport(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = lastDay.atTime(23, 59, 59);
        return generateRevenueReport(start, end, "Monthly Report: " + firstDay.getMonth() + " " + year);
    }

    private RevenueReport generateRevenueReport(LocalDateTime start, LocalDateTime end, String title) {
        List<Reservation> reservations = reservationRepository.findByDateRange(
            start.toLocalDate(), end.toLocalDate());

        // Calculate totals
        int reservationCount = reservations.size();
        double subtotal = 0;
        double tax = 0;
        double discounts = 0;
        double total = 0;

        for (Reservation r : reservations) {
            subtotal += r.getSubtotal();
            tax += r.getTax();
            discounts += r.getDiscountAmount();
            total += r.getTotal();
        }

        // Get actual payments in period
        double actualRevenue = paymentRepository.getTotalRevenueByDateRange(start, end);

        // Calculate daily breakdown
        List<RevenueReport.DailyRevenue> dailyData = new ArrayList<>();
        LocalDate currentDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            final LocalDate dateToCheck = currentDate;

            // Filter reservations for this date (by check-in date)
            List<Reservation> dailyReservations = reservations.stream()
                .filter(r -> r.getCheckInDate().equals(dateToCheck))
                .toList();

            int dayReservations = dailyReservations.size();
            double daySubtotal = dailyReservations.stream().mapToDouble(Reservation::getSubtotal).sum();
            double dayTax = dailyReservations.stream().mapToDouble(Reservation::getTax).sum();
            double dayDiscounts = dailyReservations.stream().mapToDouble(Reservation::getDiscountAmount).sum();
            double dayTotal = dailyReservations.stream().mapToDouble(Reservation::getTotal).sum();

            dailyData.add(new RevenueReport.DailyRevenue(currentDate, dayReservations, daySubtotal, dayTax, dayDiscounts, dayTotal));
            currentDate = currentDate.plusDays(1);
        }

        return new RevenueReport(title, start, end, reservationCount, subtotal, tax, discounts, total, actualRevenue, dailyData);
    }

    /**
     * Generate revenue report for a custom date range.
     */
    public RevenueReport generateRevenueReportForDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        String title = "Revenue Report: " + startDate + " to " + endDate;
        return generateRevenueReport(start, end, title);
    }

    /**
     * Generate occupancy report for a custom date range.
     */
    public OccupancyReport generateOccupancyReportForDateRange(LocalDate startDate, LocalDate endDate) {
        String title = "Occupancy Report: " + startDate + " to " + endDate;
        return generateOccupancyReport(startDate, endDate, title);
    }

    // ========== Occupancy Reports ==========

    /**
     * Generate daily occupancy report.
     */
    public OccupancyReport generateDailyOccupancyReport(LocalDate date) {
        return generateOccupancyReport(date, date, "Daily Occupancy: " + date);
    }

    /**
     * Generate weekly occupancy report.
     */
    public OccupancyReport generateWeeklyOccupancyReport(LocalDate weekStart) {
        return generateOccupancyReport(weekStart, weekStart.plusDays(6),
            "Weekly Occupancy: " + weekStart + " to " + weekStart.plusDays(6));
    }

    /**
     * Generate monthly occupancy report.
     */
    public OccupancyReport generateMonthlyOccupancyReport(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        return generateOccupancyReport(firstDay, lastDay,
            "Monthly Occupancy: " + firstDay.getMonth() + " " + year);
    }

    private OccupancyReport generateOccupancyReport(LocalDate startDate, LocalDate endDate, String title) {
        long totalRooms = roomRepository.count();
        List<OccupancyReport.DailyOccupancy> dailyData = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            // Count occupied rooms for this date
            List<Reservation> activeReservations = reservationRepository.findActiveReservations();
            int occupiedRooms = 0;

            for (Reservation r : activeReservations) {
                if (!current.isBefore(r.getCheckInDate()) && current.isBefore(r.getCheckOutDate())) {
                    occupiedRooms += r.getRooms().size();
                }
            }

            int availableRooms = (int) totalRooms - occupiedRooms;
            double occupancyPercent = (totalRooms > 0) ? (occupiedRooms * 100.0 / totalRooms) : 0;

            dailyData.add(new OccupancyReport.DailyOccupancy(current, (int) totalRooms, availableRooms, occupiedRooms, occupancyPercent));
            current = current.plusDays(1);
        }

        return new OccupancyReport(title, startDate, endDate, dailyData);
    }

    // ========== Export Methods ==========

    /**
     * Export revenue report to CSV.
     */
    public String exportRevenueToCSV(RevenueReport report) {
        StringBuilder csv = new StringBuilder();
        csv.append("Revenue Report\n");
        csv.append("Title,").append(report.getTitle()).append("\n");
        csv.append("Period,").append(report.getStartDate().format(DATETIME_FORMATTER))
           .append(" to ").append(report.getEndDate().format(DATETIME_FORMATTER)).append("\n");
        csv.append("\n");
        csv.append("Metric,Value\n");
        csv.append("Reservation Count,").append(report.getReservationCount()).append("\n");
        csv.append("Subtotal,$").append(String.format("%.2f", report.getSubtotal())).append("\n");
        csv.append("Tax,$").append(String.format("%.2f", report.getTax())).append("\n");
        csv.append("Discounts,$").append(String.format("%.2f", report.getDiscounts())).append("\n");
        csv.append("Total,$").append(String.format("%.2f", report.getTotal())).append("\n");
        csv.append("Actual Revenue,$").append(String.format("%.2f", report.getActualRevenue())).append("\n");
        return csv.toString();
    }

    /**
     * Export occupancy report to CSV.
     */
    public String exportOccupancyToCSV(OccupancyReport report) {
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Total Rooms,Available,Occupied,Occupancy %\n");

        for (OccupancyReport.DailyOccupancy day : report.getDailyData()) {
            csv.append(day.getDate().format(DATE_FORMATTER)).append(",");
            csv.append(day.getTotalRooms()).append(",");
            csv.append(day.getAvailableRooms()).append(",");
            csv.append(day.getOccupiedRooms()).append(",");
            csv.append(String.format("%.1f", day.getOccupancyPercent())).append("\n");
        }

        return csv.toString();
    }

    /**
     * Export report to PDF format (text-based for simplicity).
     */
    public String exportToPDF(RevenueReport report) {
        // For simplicity, return formatted text that can be written to PDF
        StringBuilder pdf = new StringBuilder();
        pdf.append("═".repeat(60)).append("\n");
        pdf.append("            REVENUE REPORT\n");
        pdf.append("═".repeat(60)).append("\n\n");
        pdf.append(report.getTitle()).append("\n\n");
        pdf.append("Period: ").append(report.getStartDate().format(DATETIME_FORMATTER))
           .append("\n        to ").append(report.getEndDate().format(DATETIME_FORMATTER)).append("\n\n");
        pdf.append("─".repeat(40)).append("\n");
        pdf.append(String.format("%-25s %15s\n", "Metric", "Value"));
        pdf.append("─".repeat(40)).append("\n");
        pdf.append(String.format("%-25s %15d\n", "Reservation Count", report.getReservationCount()));
        pdf.append(String.format("%-25s %15s\n", "Subtotal", "$" + String.format("%.2f", report.getSubtotal())));
        pdf.append(String.format("%-25s %15s\n", "Tax (13% HST)", "$" + String.format("%.2f", report.getTax())));
        pdf.append(String.format("%-25s %15s\n", "Discounts", "$" + String.format("%.2f", report.getDiscounts())));
        pdf.append("─".repeat(40)).append("\n");
        pdf.append(String.format("%-25s %15s\n", "TOTAL", "$" + String.format("%.2f", report.getTotal())));
        pdf.append(String.format("%-25s %15s\n", "Actual Revenue", "$" + String.format("%.2f", report.getActualRevenue())));
        pdf.append("═".repeat(60)).append("\n");
        pdf.append("\nGenerated: ").append(LocalDateTime.now().format(DATETIME_FORMATTER)).append("\n");
        return pdf.toString();
    }

    /**
     * Save report to file.
     */
    public void saveToFile(String content, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
        }
        auditService.log("SYSTEM", "EXPORT", "Report", null, "Exported report to: " + filename);
    }

    // ========== Report Data Classes ==========

    public static class RevenueReport {
        private final String title;
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final int reservationCount;
        private final double subtotal;
        private final double tax;
        private final double discounts;
        private final double total;
        private final double actualRevenue;
        private final List<DailyRevenue> dailyData;

        public RevenueReport(String title, LocalDateTime startDate, LocalDateTime endDate,
                            int reservationCount, double subtotal, double tax, double discounts,
                            double total, double actualRevenue, List<DailyRevenue> dailyData) {
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.reservationCount = reservationCount;
            this.subtotal = subtotal;
            this.tax = tax;
            this.discounts = discounts;
            this.total = total;
            this.actualRevenue = actualRevenue;
            this.dailyData = dailyData != null ? dailyData : new ArrayList<>();
        }

        public String getTitle() { return title; }
        public LocalDateTime getStartDate() { return startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public int getReservationCount() { return reservationCount; }
        public double getSubtotal() { return subtotal; }
        public double getTax() { return tax; }
        public double getDiscounts() { return discounts; }
        public double getTotal() { return total; }
        public double getActualRevenue() { return actualRevenue; }
        public List<DailyRevenue> getDailyData() { return dailyData; }

        public static class DailyRevenue {
            private final LocalDate date;
            private final int reservationCount;
            private final double subtotal;
            private final double tax;
            private final double discounts;
            private final double total;

            public DailyRevenue(LocalDate date, int reservationCount, double subtotal,
                               double tax, double discounts, double total) {
                this.date = date;
                this.reservationCount = reservationCount;
                this.subtotal = subtotal;
                this.tax = tax;
                this.discounts = discounts;
                this.total = total;
            }

            public LocalDate getDate() { return date; }
            public int getReservationCount() { return reservationCount; }
            public double getSubtotal() { return subtotal; }
            public double getTax() { return tax; }
            public double getDiscounts() { return discounts; }
            public double getTotal() { return total; }
        }
    }

    public static class OccupancyReport {
        private final String title;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final List<DailyOccupancy> dailyData;

        public OccupancyReport(String title, LocalDate startDate, LocalDate endDate, List<DailyOccupancy> dailyData) {
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.dailyData = dailyData;
        }

        public String getTitle() { return title; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public List<DailyOccupancy> getDailyData() { return dailyData; }

        public double getAverageOccupancy() {
            if (dailyData.isEmpty()) return 0;
            return dailyData.stream().mapToDouble(DailyOccupancy::getOccupancyPercent).average().orElse(0);
        }

        public static class DailyOccupancy {
            private final LocalDate date;
            private final int totalRooms;
            private final int availableRooms;
            private final int occupiedRooms;
            private final double occupancyPercent;

            public DailyOccupancy(LocalDate date, int totalRooms, int availableRooms, int occupiedRooms, double occupancyPercent) {
                this.date = date;
                this.totalRooms = totalRooms;
                this.availableRooms = availableRooms;
                this.occupiedRooms = occupiedRooms;
                this.occupancyPercent = occupancyPercent;
            }

            public LocalDate getDate() { return date; }
            public int getTotalRooms() { return totalRooms; }
            public int getAvailableRooms() { return availableRooms; }
            public int getOccupiedRooms() { return occupiedRooms; }
            public double getOccupancyPercent() { return occupancyPercent; }
        }
    }
}
