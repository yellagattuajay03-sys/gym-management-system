package com.gymmanagement.service;

import com.gymmanagement.dto.DashboardStatsDto;
import com.gymmanagement.dto.EquipmentDto;
import com.gymmanagement.dto.TrainerDto;
import com.gymmanagement.model.*;
import com.gymmanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final TrainerRepository trainerRepository;
    private final CustomerRepository customerRepository;
    private final MembershipRepository membershipRepository;
    private final BookingRepository bookingRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final TrainerSlotRepository trainerSlotRepository;
    private final TrainerRequestRepository trainerRequestRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentRequestRepository equipmentRequestRepository;
    private final PaymentRepository paymentRepository;

    public AdminService(TrainerRepository trainerRepository,
                        CustomerRepository customerRepository,
                        MembershipRepository membershipRepository,
                        BookingRepository bookingRepository,
                        BookingSlotRepository bookingSlotRepository,
                        TrainerSlotRepository trainerSlotRepository,
                        TrainerRequestRepository trainerRequestRepository,
                        EquipmentRepository equipmentRepository,
                        EquipmentRequestRepository equipmentRequestRepository,
                        PaymentRepository paymentRepository) {
        this.trainerRepository = trainerRepository;
        this.customerRepository = customerRepository;
        this.membershipRepository = membershipRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSlotRepository = bookingSlotRepository;
        this.trainerSlotRepository = trainerSlotRepository;
        this.trainerRequestRepository = trainerRequestRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentRequestRepository = equipmentRequestRepository;
        this.paymentRepository = paymentRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalTrainers(trainerRepository.count());
        stats.setTotalCustomers(customerRepository.count());
        stats.setActiveMembers(membershipRepository.countByStatus("ACTIVE"));
        stats.setTodayBookings(bookingRepository.countByStartDate(LocalDate.now()));
        stats.setPendingTrainerRequests(trainerRequestRepository.countByStatus("PENDING"));
        stats.setPendingEquipmentRequests(equipmentRequestRepository.countByStatus("PENDING"));
        stats.setAvailableEquipment(equipmentRepository.sumAvailableQuantity());
        stats.setTotalRevenue(paymentRepository.sumTotalSuccessfulRevenue());
        return stats;
    }

    // --- Trainer Management ---
    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    public Trainer addTrainer(TrainerDto dto) {
        if (trainerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Trainer with email " + dto.getEmail() + " already exists.");
        }
        Trainer trainer = new Trainer(
                dto.getName(),
                dto.getEmail(),
                dto.getPassword() != null ? dto.getPassword() : "trainer123",
                dto.getPhone(),
                dto.getSpecialization(),
                dto.getExperienceYears(),
                dto.getQualification(),
                dto.getSalary(),
                dto.getAvailabilityStatus() != null ? dto.getAvailabilityStatus() : "AVAILABLE"
        );
        return trainerRepository.save(trainer);
    }

    public Trainer updateTrainer(Long trainerId, TrainerDto dto) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with ID: " + trainerId));
        
        if (dto.getName() != null) trainer.setName(dto.getName());
        if (dto.getEmail() != null) trainer.setEmail(dto.getEmail());
        if (dto.getPhone() != null) trainer.setPhone(dto.getPhone());
        if (dto.getSpecialization() != null) trainer.setSpecialization(dto.getSpecialization());
        if (dto.getExperienceYears() != null) trainer.setExperienceYears(dto.getExperienceYears());
        if (dto.getQualification() != null) trainer.setQualification(dto.getQualification());
        if (dto.getSalary() != null) trainer.setSalary(dto.getSalary());
        if (dto.getAvailabilityStatus() != null) trainer.setAvailabilityStatus(dto.getAvailabilityStatus());

        return trainerRepository.save(trainer);
    }

    public void deleteTrainer(Long trainerId) {
        if (!trainerRepository.existsById(trainerId)) {
            throw new IllegalArgumentException("Trainer not found with ID: " + trainerId);
        }
        trainerRepository.deleteById(trainerId);
    }

    // --- Customer Management ---
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Map<String, Object> getCustomerFullDetails(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));
        
        List<Membership> memberships = membershipRepository.findByCustomer_CustomerId(customerId);
        List<Payment> payments = paymentRepository.findByCustomer_CustomerIdOrderByPaymentDateDesc(customerId);
        List<Booking> bookings = bookingRepository.findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId);

        Map<String, Object> details = new HashMap<>();
        details.put("customer", customer);
        details.put("memberships", memberships);
        details.put("payments", payments);
        details.put("bookings", bookings);
        return details;
    }

    // --- Schedule & Booking Management ---
    public List<TrainerSlot> getAllTrainerSlots() {
        return trainerSlotRepository.findAll();
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<BookingSlot> getAllBookingSlots() {
        return bookingSlotRepository.findAll();
    }

    // --- Equipment Management ---
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public Equipment addEquipment(EquipmentDto dto) {
        String status = "AVAILABLE";
        if (dto.getAvailableQuantity() == 0) {
            status = "OUT_OF_STOCK";
        } else if (dto.getAvailableQuantity() <= 2) {
            status = "LOW_STOCK";
        }

        Equipment equipment = new Equipment(
                dto.getEquipmentName(),
                dto.getDescription(),
                dto.getTotalQuantity(),
                dto.getAvailableQuantity(),
                status
        );
        return equipmentRepository.save(equipment);
    }

    public Equipment updateEquipment(Long equipmentId, EquipmentDto dto) {
        Equipment eq = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + equipmentId));

        if (dto.getEquipmentName() != null) eq.setEquipmentName(dto.getEquipmentName());
        if (dto.getDescription() != null) eq.setDescription(dto.getDescription());
        if (dto.getTotalQuantity() != null) eq.setTotalQuantity(dto.getTotalQuantity());
        if (dto.getAvailableQuantity() != null) {
            eq.setAvailableQuantity(dto.getAvailableQuantity());
            if (eq.getAvailableQuantity() == 0) {
                eq.setStatus("OUT_OF_STOCK");
            } else if (eq.getAvailableQuantity() <= 2) {
                eq.setStatus("LOW_STOCK");
            } else {
                eq.setStatus("AVAILABLE");
            }
        }
        return equipmentRepository.save(eq);
    }

    public List<EquipmentRequest> getAllEquipmentRequests() {
        return equipmentRequestRepository.findAllByOrderByRequestDateDesc();
    }

    @Transactional
    public EquipmentRequest reviewEquipmentRequest(Long requestId, String action) {
        EquipmentRequest req = equipmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment request not found: " + requestId));

        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new IllegalStateException("Request is already " + req.getStatus());
        }

        if ("APPROVE".equalsIgnoreCase(action)) {
            Equipment eq = req.getEquipment();
            if (eq.getAvailableQuantity() < req.getQuantity()) {
                throw new IllegalStateException("Insufficient stock! Available: " + eq.getAvailableQuantity() + 
                        ", Requested: " + req.getQuantity());
            }

            eq.setAvailableQuantity(eq.getAvailableQuantity() - req.getQuantity());
            if (eq.getAvailableQuantity() == 0) {
                eq.setStatus("OUT_OF_STOCK");
            } else if (eq.getAvailableQuantity() <= 2) {
                eq.setStatus("LOW_STOCK");
            }
            equipmentRepository.save(eq);

            req.setStatus("APPROVED");
        } else if ("REJECT".equalsIgnoreCase(action)) {
            req.setStatus("REJECTED");
        } else {
            throw new IllegalArgumentException("Invalid action: " + action + ". Must be APPROVE or REJECT.");
        }

        return equipmentRequestRepository.save(req);
    }
}
