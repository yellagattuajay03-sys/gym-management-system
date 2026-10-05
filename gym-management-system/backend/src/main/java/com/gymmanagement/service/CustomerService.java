package com.gymmanagement.service;

import com.gymmanagement.dto.BookingRequestDto;
import com.gymmanagement.dto.PaymentRequestDto;
import com.gymmanagement.model.*;
import com.gymmanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TrainerRepository trainerRepository;
    private final TrainerSlotRepository trainerSlotRepository;
    private final TrainerRequestRepository trainerRequestRepository;
    private final BookingRepository bookingRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;
    private final GymAccessQrRepository gymAccessQrRepository;

    public CustomerService(CustomerRepository customerRepository,
                           TrainerRepository trainerRepository,
                           TrainerSlotRepository trainerSlotRepository,
                           TrainerRequestRepository trainerRequestRepository,
                           BookingRepository bookingRepository,
                           BookingSlotRepository bookingSlotRepository,
                           MembershipRepository membershipRepository,
                           PaymentRepository paymentRepository,
                           GymAccessQrRepository gymAccessQrRepository) {
        this.customerRepository = customerRepository;
        this.trainerRepository = trainerRepository;
        this.trainerSlotRepository = trainerSlotRepository;
        this.trainerRequestRepository = trainerRequestRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSlotRepository = bookingSlotRepository;
        this.membershipRepository = membershipRepository;
        this.paymentRepository = paymentRepository;
        this.gymAccessQrRepository = gymAccessQrRepository;
    }

    public Customer getProfile(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
    }

    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    public List<TrainerSlot> getTrainerAvailableSlots(Long trainerId, LocalDate date) {
        if (date != null) {
            return trainerSlotRepository.findByTrainer_TrainerIdAndSlotDateAndStatus(trainerId, date, "AVAILABLE");
        }
        return trainerSlotRepository.findByTrainer_TrainerIdAndSlotDateAndStatus(trainerId, LocalDate.now(), "AVAILABLE");
    }

    // --- Trainer Coaching Request ---
    public TrainerRequest sendTrainerRequest(BookingRequestDto dto) {
        Customer customer = getProfile(dto.getCustomerId());
        Trainer trainer = trainerRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + dto.getTrainerId()));
        TrainerSlot slot = trainerSlotRepository.findById(dto.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer slot not found: " + dto.getSlotId()));

        if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
            throw new IllegalStateException("Slot Unavailable: The selected time slot is already " + slot.getStatus());
        }

        TrainerRequest request = new TrainerRequest(
                customer,
                trainer,
                slot,
                LocalDate.now(),
                dto.getMessage() != null ? dto.getMessage() : "Personal training requested.",
                "PENDING"
        );
        return trainerRequestRepository.save(request);
    }

    public List<TrainerRequest> getMyRequests(Long customerId) {
        return trainerRequestRepository.findByCustomer_CustomerIdOrderByRequestDateDesc(customerId);
    }

    // --- Direct Booking (ONE_DAY and DAILY) ---
    @Transactional
    public Booking createBooking(BookingRequestDto dto) {
        Customer customer = getProfile(dto.getCustomerId());
        Trainer trainer = trainerRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + dto.getTrainerId()));

        String type = dto.getBookingType() != null ? dto.getBookingType().toUpperCase() : "ONE_DAY";

        if ("ONE_DAY".equals(type)) {
            TrainerSlot slot;
            if (dto.getSlotId() != null) {
                slot = trainerSlotRepository.findById(dto.getSlotId())
                        .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + dto.getSlotId()));
            } else if (dto.getStartDate() != null && dto.getStartTime() != null) {
                slot = trainerSlotRepository.findByTrainer_TrainerIdAndSlotDateAndStartTime(
                        trainer.getTrainerId(), dto.getStartDate(), dto.getStartTime())
                        .orElseGet(() -> {
                            LocalTime end = dto.getEndTime() != null ? dto.getEndTime() : dto.getStartTime().plusHours(1);
                            return trainerSlotRepository.save(new TrainerSlot(trainer, dto.getStartDate(), dto.getStartTime(), end, "AVAILABLE"));
                        });
            } else {
                throw new IllegalArgumentException("Slot or Date & Time required for ONE_DAY booking.");
            }

            // CRUCIAL: Double booking check
            if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
                throw new IllegalStateException("Slot Unavailable: Trainer " + trainer.getName() + " is already booked for this slot!");
            }

            slot.setStatus("BOOKED");
            trainerSlotRepository.save(slot);

            Booking booking = new Booking(
                    customer,
                    trainer,
                    null,
                    "ONE_DAY",
                    slot.getSlotDate(),
                    slot.getSlotDate(),
                    LocalDateTime.now(),
                    "CONFIRMED"
            );
            Booking savedBooking = bookingRepository.save(booking);

            BookingSlot bookingSlot = new BookingSlot(savedBooking, slot, slot.getSlotDate(), "BOOKED");
            bookingSlotRepository.save(bookingSlot);

            return savedBooking;

        } else if ("DAILY".equals(type)) {
            // Recurring Daily Booking
            LocalDate start = dto.getStartDate();
            LocalDate end = dto.getEndDate();
            LocalTime startTime = dto.getStartTime() != null ? dto.getStartTime() : LocalTime.of(6, 0);
            LocalTime endTime = dto.getEndTime() != null ? dto.getEndTime() : startTime.plusHours(1);

            if (start == null || end == null || end.isBefore(start)) {
                throw new IllegalArgumentException("Valid start and end dates are required for daily booking.");
            }

            // Step 1: Pre-verify availability across ALL requested dates
            List<LocalDate> datesToBook = new ArrayList<>();
            List<TrainerSlot> slotsToBook = new ArrayList<>();

            for (LocalDate curr = start; !curr.isAfter(end); curr = curr.plusDays(1)) {
                LocalDate date = curr;
                Optional<TrainerSlot> existingOpt = trainerSlotRepository
                        .findByTrainer_TrainerIdAndSlotDateAndStartTime(trainer.getTrainerId(), date, startTime);

                TrainerSlot slot;
                if (existingOpt.isPresent()) {
                    slot = existingOpt.get();
                    if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
                        throw new IllegalStateException("Slot Unavailable on " + date + ": Trainer " + 
                                trainer.getName() + " is already booked at " + startTime + "!");
                    }
                } else {
                    slot = new TrainerSlot(trainer, date, startTime, endTime, "AVAILABLE");
                    slot = trainerSlotRepository.save(slot);
                }
                datesToBook.add(date);
                slotsToBook.add(slot);
            }

            // Step 2: Create Master Booking
            Booking booking = new Booking(
                    customer,
                    trainer,
                    null,
                    "DAILY",
                    start,
                    end,
                    LocalDateTime.now(),
                    "CONFIRMED"
            );
            Booking savedBooking = bookingRepository.save(booking);

            // Step 3: Create individual booking_slot entries for each day
            for (int i = 0; i < datesToBook.size(); i++) {
                TrainerSlot slot = slotsToBook.get(i);
                slot.setStatus("BOOKED");
                trainerSlotRepository.save(slot);

                BookingSlot bookingSlot = new BookingSlot(savedBooking, slot, datesToBook.get(i), "BOOKED");
                bookingSlotRepository.save(bookingSlot);
            }

            return savedBooking;

        } else {
            throw new IllegalArgumentException("Unknown booking type: " + type);
        }
    }

    public List<Booking> getMyBookings(Long customerId) {
        return bookingRepository.findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId);
    }

    // --- Membership & Payment Transaction ---
    public Optional<Membership> getActiveMembership(Long customerId) {
        return membershipRepository.findFirstByCustomer_CustomerIdAndStatusOrderByEndDateDesc(customerId, "ACTIVE");
    }

    public List<Membership> getAllMemberships(Long customerId) {
        return membershipRepository.findByCustomer_CustomerId(customerId);
    }

    public List<Payment> getMyPayments(Long customerId) {
        return paymentRepository.findByCustomer_CustomerIdOrderByPaymentDateDesc(customerId);
    }

    public Optional<GymAccessQr> getActiveQrCode(Long customerId) {
        return gymAccessQrRepository.findFirstByCustomer_CustomerIdAndStatusOrderByValidUntilDesc(customerId, "ACTIVE");
    }

    /**
     * ATOMIC DBMS TRANSACTION:
     * 1. Create/Update Membership Record
     * 2. Insert Payment Record
     * 3. Generate QR Access Token
     * If any operation fails, the entire transaction is rolled back.
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> processMembershipPayment(PaymentRequestDto dto) {
        Customer customer = getProfile(dto.getCustomerId());

        LocalDate startDate = LocalDate.now();
        LocalDate endDate;

        String plan = dto.getPlanName() != null ? dto.getPlanName() : "Monthly Gold";
        if (plan.toLowerCase().contains("annual")) {
            endDate = startDate.plusYears(1).minusDays(1);
        } else if (plan.toLowerCase().contains("half")) {
            endDate = startDate.plusMonths(6).minusDays(1);
        } else if (plan.toLowerCase().contains("quarter")) {
            endDate = startDate.plusMonths(3).minusDays(1);
        } else {
            endDate = startDate.plusMonths(1).minusDays(1);
        }

        // 1. Create active membership
        Membership membership = new Membership(
                customer,
                plan,
                dto.getAmount(),
                startDate,
                endDate,
                "ACTIVE"
        );
        Membership savedMembership = membershipRepository.save(membership);

        // 2. Create payment record
        String txnRef = "TXN_GMS_" + System.currentTimeMillis() + "_" + (1000 + new Random().nextInt(9000));
        Payment payment = new Payment(
                customer,
                savedMembership,
                dto.getAmount(),
                LocalDateTime.now(),
                dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "UPI - Simulated",
                "SUCCESS",
                txnRef
        );
        Payment savedPayment = paymentRepository.save(payment);

        // 3. Generate / Update Gym Access QR
        String qrToken = "QR-GMS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + customer.getCustomerId();
        GymAccessQr qr = new GymAccessQr(
                customer,
                savedMembership,
                qrToken,
                startDate,
                endDate,
                "ACTIVE"
        );
        GymAccessQr savedQr = gymAccessQrRepository.save(qr);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Payment processed and membership activated successfully!");
        result.put("membership", savedMembership);
        result.put("payment", savedPayment);
        result.put("qr", savedQr);

        return result;
    }
}
