package com.gymmanagement.service;

import com.gymmanagement.model.*;
import com.gymmanagement.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final AdminRepository adminRepo;
    private final TrainerRepository trainerRepo;
    private final CustomerRepository customerRepo;
    private final MembershipRepository membershipRepo;
    private final PaymentRepository paymentRepo;
    private final TrainerSlotRepository slotRepo;
    private final TrainerRequestRepository requestRepo;
    private final BookingRepository bookingRepo;
    private final BookingSlotRepository bookingSlotRepo;
    private final EquipmentRepository equipmentRepo;
    private final EquipmentRequestRepository equipmentRequestRepo;
    private final GymAccessQrRepository qrRepo;

    public DatabaseSeeder(AdminRepository adminRepo, TrainerRepository trainerRepo,
                          CustomerRepository customerRepo, MembershipRepository membershipRepo,
                          PaymentRepository paymentRepo, TrainerSlotRepository slotRepo,
                          TrainerRequestRepository requestRepo, BookingRepository bookingRepo,
                          BookingSlotRepository bookingSlotRepo, EquipmentRepository equipmentRepo,
                          EquipmentRequestRepository equipmentRequestRepo, GymAccessQrRepository qrRepo) {
        this.adminRepo = adminRepo;
        this.trainerRepo = trainerRepo;
        this.customerRepo = customerRepo;
        this.membershipRepo = membershipRepo;
        this.paymentRepo = paymentRepo;
        this.slotRepo = slotRepo;
        this.requestRepo = requestRepo;
        this.bookingRepo = bookingRepo;
        this.bookingSlotRepo = bookingSlotRepo;
        this.equipmentRepo = equipmentRepo;
        this.equipmentRequestRepo = equipmentRequestRepo;
        this.qrRepo = qrRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (adminRepo.count() > 0) {
            System.out.println("===> Database already initialized. Skipping auto-seeding.");
            return;
        }

        System.out.println("===> Seeding initial sample data for Gym Management System DBMS Project...");

        // 1. Admins
        Admin a1 = adminRepo.save(new Admin("Vikram Sharma", "admin@gym.com", "admin123"));
        Admin a2 = adminRepo.save(new Admin("Neha Kapoor", "neha.admin@gym.com", "admin123"));

        // 2. Trainers
        Trainer t1 = trainerRepo.save(new Trainer("Ravi Kumar", "ravi@gym.com", "trainer123", "9876543210", "Strength & Conditioning", 7, "ACE Certified Personal Trainer", new BigDecimal("55000.00"), "AVAILABLE"));
        Trainer t2 = trainerRepo.save(new Trainer("Priya Patel", "priya@gym.com", "trainer123", "9876543211", "Yoga & Aerobics", 5, "RYT 500 Certified Yoga Master", new BigDecimal("45000.00"), "AVAILABLE"));
        Trainer t3 = trainerRepo.save(new Trainer("Amit Singh", "amit@gym.com", "trainer123", "9876543212", "Bodybuilding & Hypertrophy", 8, "ISSA Elite Trainer", new BigDecimal("60000.00"), "AVAILABLE"));
        Trainer t4 = trainerRepo.save(new Trainer("Sneha Rao", "sneha@gym.com", "trainer123", "9876543213", "Crossfit & HIIT", 4, "Crossfit Level 2 Trainer", new BigDecimal("42000.00"), "AVAILABLE"));
        Trainer t5 = trainerRepo.save(new Trainer("Rajesh Verma", "rajesh@gym.com", "trainer123", "9876543214", "Cardio & Weight Loss", 6, "K11 Diploma in Personal Training", new BigDecimal("48000.00"), "AVAILABLE"));

        // 3. Customers
        Customer c1 = customerRepo.save(new Customer("Rahul Verma", "rahul@gmail.com", "customer123", "9123456780", "Flat 402, Bellandur, Bengaluru", LocalDate.now().minusMonths(6)));
        Customer c2 = customerRepo.save(new Customer("Ananya Sharma", "ananya@gmail.com", "customer123", "9123456781", "B-12, Sector 62, Noida", LocalDate.now().minusMonths(5)));
        Customer c3 = customerRepo.save(new Customer("Rohit Patil", "rohit@gmail.com", "customer123", "9123456782", "Flat 101, Kothrud, Pune", LocalDate.now().minusMonths(4)));
        Customer c4 = customerRepo.save(new Customer("Pooja Mehta", "pooja@gmail.com", "customer123", "9123456783", "14/A, Marine Drive, Mumbai", LocalDate.now().minusMonths(3)));
        Customer c5 = customerRepo.save(new Customer("Vikram Malhotra", "vikram@gmail.com", "customer123", "9123456784", "C-404, DLF Phase 5, Gurugram", LocalDate.now().minusMonths(3)));
        Customer c6 = customerRepo.save(new Customer("Sneha Gupta", "snehag@gmail.com", "customer123", "9123456785", "22, Park Street, Kolkata", LocalDate.now().minusMonths(2)));
        Customer c7 = customerRepo.save(new Customer("Arjun Reddy", "arjun@gmail.com", "customer123", "9123456786", "Plot 88, Jubilee Hills, Hyderabad", LocalDate.now().minusMonths(2)));
        Customer c8 = customerRepo.save(new Customer("Divya Nair", "divya@gmail.com", "customer123", "9123456787", "TC 15/234, Kowdiar, Trivandrum", LocalDate.now().minusMonths(1)));
        Customer c9 = customerRepo.save(new Customer("Siddharth Joshi", "siddharth@gmail.com", "customer123", "9123456788", "B-303, Sunrise Towers, Ahmedabad", LocalDate.now().minusWeeks(2)));
        Customer c10 = customerRepo.save(new Customer("Kavita Mehra", "kavita@gmail.com", "customer123", "9123456789", "H-56, Anna Nagar West, Chennai", LocalDate.now().minusWeeks(1)));

        // 4. Memberships & Payments & QRs
        LocalDate today = LocalDate.now();

        Customer[] custArr = {c1, c2, c3, c4, c5, c6, c7, c8, c9, c10};
        String[] plans = {"Annual VIP", "Quarterly Premium", "Monthly Gold", "Half-Yearly Elite", "Annual VIP", "Monthly Gold", "Quarterly Premium", "Annual VIP", "Monthly Gold", "Quarterly Premium"};
        BigDecimal[] amounts = {new BigDecimal("12000.00"), new BigDecimal("4000.00"), new BigDecimal("1500.00"), new BigDecimal("7500.00"), new BigDecimal("12000.00"), new BigDecimal("1500.00"), new BigDecimal("4000.00"), new BigDecimal("12000.00"), new BigDecimal("1500.00"), new BigDecimal("4000.00")};
        String[] statuses = {"ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "EXPIRED", "ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE"};

        for (int i = 0; i < custArr.length; i++) {
            LocalDate start = today.minusDays(10 + i * 5);
            LocalDate end = "EXPIRED".equals(statuses[i]) ? today.minusDays(2) : today.plusMonths(3);
            Membership m = membershipRepo.save(new Membership(custArr[i], plans[i], amounts[i], start, end, statuses[i]));
            
            paymentRepo.save(new Payment(custArr[i], m, amounts[i], LocalDateTime.now().minusDays(10 + i * 5), "UPI - PhonePe", "SUCCESS", "TXN_INIT_" + (1000 + i)));

            String qrStatus = "EXPIRED".equals(statuses[i]) ? "EXPIRED" : "ACTIVE";
            qrRepo.save(new GymAccessQr(custArr[i], m, "QR-GMS-CUST" + (i + 1) + "-TOKEN", start, end, qrStatus));
        }

        // 5. Trainer Slots
        List<TrainerSlot> slots = new ArrayList<>();
        for (int d = 0; d < 7; d++) {
            LocalDate slotDate = today.plusDays(d);
            slots.add(slotRepo.save(new TrainerSlot(t1, slotDate, LocalTime.of(6, 0), LocalTime.of(7, 0), d == 0 ? "BOOKED" : "AVAILABLE")));
            slots.add(slotRepo.save(new TrainerSlot(t1, slotDate, LocalTime.of(7, 0), LocalTime.of(8, 0), "AVAILABLE")));
            slots.add(slotRepo.save(new TrainerSlot(t1, slotDate, LocalTime.of(18, 0), LocalTime.of(19, 0), "AVAILABLE")));

            slots.add(slotRepo.save(new TrainerSlot(t2, slotDate, LocalTime.of(7, 0), LocalTime.of(8, 0), "AVAILABLE")));
            slots.add(slotRepo.save(new TrainerSlot(t2, slotDate, LocalTime.of(17, 0), LocalTime.of(18, 0), d == 0 ? "BOOKED" : "AVAILABLE")));

            slots.add(slotRepo.save(new TrainerSlot(t3, slotDate, LocalTime.of(6, 0), LocalTime.of(7, 0), "AVAILABLE")));
            slots.add(slotRepo.save(new TrainerSlot(t3, slotDate, LocalTime.of(19, 0), LocalTime.of(20, 0), "AVAILABLE")));

            slots.add(slotRepo.save(new TrainerSlot(t4, slotDate, LocalTime.of(6, 30), LocalTime.of(7, 30), "AVAILABLE")));
            slots.add(slotRepo.save(new TrainerSlot(t4, slotDate, LocalTime.of(18, 30), LocalTime.of(19, 30), "AVAILABLE")));

            slots.add(slotRepo.save(new TrainerSlot(t5, slotDate, LocalTime.of(6, 0), LocalTime.of(7, 0), "AVAILABLE")));
            slots.add(slotRepo.save(new TrainerSlot(t5, slotDate, LocalTime.of(17, 0), LocalTime.of(18, 0), "AVAILABLE")));
        }

        // 6. Trainer Requests
        TrainerSlot firstSlot = slots.get(0);
        TrainerSlot secondSlot = slots.get(4);
        TrainerRequest req1 = requestRepo.save(new TrainerRequest(c1, t1, firstSlot, today.minusDays(1), "Need personal deadlift and squat coaching.", "ACCEPTED"));
        TrainerRequest req2 = requestRepo.save(new TrainerRequest(c2, t2, secondSlot, today.minusDays(1), "Evening yoga flow session.", "ACCEPTED"));
        TrainerRequest req3 = requestRepo.save(new TrainerRequest(c4, t1, slots.get(2), today, "Focus on strength conditioning.", "PENDING"));

        // 7. Bookings & Booking Slots
        Booking b1 = bookingRepo.save(new Booking(c1, t1, req1, "ONE_DAY", today, today, LocalDateTime.now().minusDays(1), "CONFIRMED"));
        bookingSlotRepo.save(new BookingSlot(b1, firstSlot, today, "BOOKED"));

        Booking b2 = bookingRepo.save(new Booking(c2, t2, req2, "ONE_DAY", today, today, LocalDateTime.now().minusDays(1), "CONFIRMED"));
        bookingSlotRepo.save(new BookingSlot(b2, secondSlot, today, "BOOKED"));

        // 8. Equipment
        Equipment eq1 = equipmentRepo.save(new Equipment("Treadmill Commercial Pro", "Heavy duty motor 4.0 HP with incline control", 8, 7, "AVAILABLE"));
        Equipment eq2 = equipmentRepo.save(new Equipment("Olympic Barbell Set 20kg", "Standard 7ft chrome Olympic barbell with safety collars", 12, 10, "AVAILABLE"));
        Equipment eq3 = equipmentRepo.save(new Equipment("Adjustable Dumbbell Pair 2.5-30kg", "Quick-select dial dumbbell pair with storage rack", 10, 8, "AVAILABLE"));
        Equipment eq4 = equipmentRepo.save(new Equipment("Hex Rubber Kettlebell Set", "Cast iron kettlebells ranging from 8kg to 24kg", 15, 14, "AVAILABLE"));
        Equipment eq5 = equipmentRepo.save(new Equipment("Lat Pulldown & Low Row Combo", "Cable selectorized dual station with 100kg weight stack", 4, 3, "AVAILABLE"));
        Equipment eq6 = equipmentRepo.save(new Equipment("Smith Machine 3D", "Dual-axis guided barbell machine with safety stoppers", 2, 2, "AVAILABLE"));
        Equipment eq7 = equipmentRepo.save(new Equipment("Multi-Angle Adjustable Bench", "Incline, flat, and decline bench with wheels", 10, 9, "AVAILABLE"));
        Equipment eq8 = equipmentRepo.save(new Equipment("Cable Crossover Station", "Functional dual adjustable pulley trainer with accessories", 2, 1, "LOW_STOCK"));

        // 9. Equipment Requests
        equipmentRequestRepo.save(new EquipmentRequest(t1, eq2, 1, "Need an extra Olympic barbell set for morning batch.", "HIGH", today.minusDays(1), "APPROVED"));
        equipmentRequestRepo.save(new EquipmentRequest(t4, eq4, 2, "Additional kettlebells for HIIT circuit training.", "HIGH", today, "PENDING"));
        equipmentRequestRepo.save(new EquipmentRequest(t3, eq1, 1, "Treadmill checkup and warmup usage.", "LOW", today, "PENDING"));

        System.out.println("===> Sample data initialization complete! 12 tables populated.");
    }
}
