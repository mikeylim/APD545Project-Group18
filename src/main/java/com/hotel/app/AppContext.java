package com.hotel.app;

import com.hotel.controller.AdminController;
import com.hotel.controller.FeedbackController;
import com.hotel.controller.KioskController;
import com.hotel.controller.LauncherController;
import com.hotel.repository.AddonRepository;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.FeedbackRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.UserRepository;
import com.hotel.repository.WaitlistRepository;
import com.hotel.service.AuditService;
import com.hotel.service.LoyaltyService;
import com.hotel.service.ReportingService;
import com.hotel.service.AuthService;
import com.hotel.service.FeedbackService;
import com.hotel.service.PaymentService;
import com.hotel.service.PricingService;
import com.hotel.service.ReservationService;
import com.hotel.service.WaitlistService;

/**
 * Lightweight application composition root inspired by the DI examples from WK8/WK11.
 * It centralizes service/repository creation and allows controllers to use constructor injection.
 */
public final class AppContext {
    private static final AppContext INSTANCE = new AppContext();

    private final UserRepository userRepository = new UserRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final GuestRepository guestRepository = new GuestRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final AddonRepository addonRepository = new AddonRepository();
    private final AuditLogRepository auditLogRepository = new AuditLogRepository();
    private final FeedbackRepository feedbackRepository = new FeedbackRepository();
    private final PaymentRepository paymentRepository = new PaymentRepository();
    private final WaitlistRepository waitlistRepository = new WaitlistRepository();

    // Services - using default constructors (they create their own dependencies)
    private final PricingService pricingService = new PricingService();
    private final AuthService authService = new AuthService();
    private final ReservationService reservationService = new ReservationService();
    private final PaymentService paymentService = new PaymentService();
    private final FeedbackService feedbackService = new FeedbackService();
    private final WaitlistService waitlistService = new WaitlistService();
    private final ReportingService reportingService = new ReportingService();

    private AppContext() {
    }

    public static AppContext getInstance() {
        return INSTANCE;
    }

    public Object createController(Class<?> controllerType) {
        // Try to create controller with AppContext injection first, then fallback to default constructor
        try {
            // Try AppContext constructor
            return controllerType.getDeclaredConstructor(AppContext.class).newInstance(this);
        } catch (NoSuchMethodException e) {
            // Fallback to default constructor
            try {
                return controllerType.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to create controller " + controllerType.getName(), ex);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create controller " + controllerType.getName(), e);
        }
    }

    public AuthService authService() { return authService; }
    public ReservationRepository reservationRepository() { return reservationRepository; }
    public GuestRepository guestRepository() { return guestRepository; }
    public RoomRepository roomRepository() { return roomRepository; }
    public AddonRepository addonRepository() { return addonRepository; }
    public AuditLogRepository auditLogRepository() { return auditLogRepository; }
    public FeedbackRepository feedbackRepository() { return feedbackRepository; }
    public PaymentRepository paymentRepository() { return paymentRepository; }
    public PaymentService paymentService() { return paymentService; }
    public PricingService pricingService() { return pricingService; }
    public ReservationService reservationService() { return reservationService; }
    public WaitlistService waitlistService() { return waitlistService; }
    public AuditService auditService() { return AuditService.getInstance(); }
    public AuditService auditLogService() { return AuditService.getInstance(); } // Alias for compatibility
    public FeedbackService feedbackService() { return feedbackService; }
    public LoyaltyService loyaltyService() { return LoyaltyService.getInstance(); }
    public ReportingService reportingService() { return reportingService; }
}
