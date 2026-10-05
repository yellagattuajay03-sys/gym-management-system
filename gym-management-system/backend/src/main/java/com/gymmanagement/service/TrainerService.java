package com.gymmanagement.service;

import com.gymmanagement.dto.EquipmentRequestDto;
import com.gymmanagement.model.*;
import com.gymmanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainerSlotRepository trainerSlotRepository;
    private final TrainerRequestRepository trainerRequestRepository;
    private final BookingRepository bookingRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentRequestRepository equipmentRequestRepository;

    public TrainerService(TrainerRepository trainerRepository,
                          TrainerSlotRepository trainerSlotRepository,
                          TrainerRequestRepository trainerRequestRepository,
                          BookingRepository bookingRepository,
                          BookingSlotRepository bookingSlotRepository,
                          EquipmentRepository equipmentRepository,
                          EquipmentRequestRepository equipmentRequestRepository) {
        this.trainerRepository = trainerRepository;
        this.trainerSlotRepository = trainerSlotRepository;
        this.trainerRequestRepository = trainerRequestRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSlotRepository = bookingSlotRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentRequestRepository = equipmentRequestRepository;
    }

    public Trainer getProfile(Long trainerId) {
        return trainerRepository.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + trainerId));
    }

    public Trainer updateAvailability(Long trainerId, String availabilityStatus) {
        Trainer trainer = getProfile(trainerId);
        trainer.setAvailabilityStatus(availabilityStatus);
        return trainerRepository.save(trainer);
    }

    public List<TrainerSlot> getTrainerSlots(Long trainerId) {
        return trainerSlotRepository.findByTrainer_TrainerIdOrderBySlotDateAscStartTimeAsc(trainerId);
    }

    public TrainerSlot addSlot(Long trainerId, LocalDate slotDate, LocalTime startTime, LocalTime endTime) {
        Trainer trainer = getProfile(trainerId);
        
        trainerSlotRepository.findByTrainer_TrainerIdAndSlotDateAndStartTime(trainerId, slotDate, startTime)
                .ifPresent(s -> {
                    throw new IllegalStateException("Slot already exists for trainer at this date and time.");
                });

        TrainerSlot slot = new TrainerSlot(trainer, slotDate, startTime, endTime, "AVAILABLE");
        return trainerSlotRepository.save(slot);
    }

    public List<TrainerRequest> getCustomerRequests(Long trainerId) {
        return trainerRequestRepository.findByTrainer_TrainerIdOrderByRequestDateDesc(trainerId);
    }

    @Transactional
    public TrainerRequest handleRequest(Long requestId, String action) {
        TrainerRequest request = trainerRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Request is already " + request.getStatus());
        }

        if ("ACCEPT".equalsIgnoreCase(action)) {
            TrainerSlot slot = request.getSlot();
            if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
                throw new IllegalStateException("Slot is no longer available! Status: " + slot.getStatus());
            }

            // Mark slot booked
            slot.setStatus("BOOKED");
            trainerSlotRepository.save(slot);

            // Update request
            request.setStatus("ACCEPTED");
            trainerRequestRepository.save(request);

            // Create booking master record
            Booking booking = new Booking(
                    request.getCustomer(),
                    request.getTrainer(),
                    request,
                    "ONE_DAY",
                    slot.getSlotDate(),
                    slot.getSlotDate(),
                    LocalDateTime.now(),
                    "CONFIRMED"
            );
            Booking savedBooking = bookingRepository.save(booking);

            // Create booking slot record
            BookingSlot bookingSlot = new BookingSlot(savedBooking, slot, slot.getSlotDate(), "BOOKED");
            bookingSlotRepository.save(bookingSlot);

        } else if ("REJECT".equalsIgnoreCase(action)) {
            request.setStatus("REJECTED");
            trainerRequestRepository.save(request);
        } else {
            throw new IllegalArgumentException("Invalid action: " + action + ". Must be ACCEPT or REJECT.");
        }

        return request;
    }

    public List<Customer> getMyCustomers(Long trainerId) {
        List<Booking> bookings = bookingRepository.findByTrainer_TrainerIdOrderByCreatedAtDesc(trainerId);
        Set<Long> seen = new HashSet<>();
        List<Customer> customers = new ArrayList<>();
        for (Booking b : bookings) {
            Customer c = b.getCustomer();
            if (c != null && seen.add(c.getCustomerId())) {
                customers.add(c);
            }
        }
        return customers;
    }

    public List<Booking> getMyBookings(Long trainerId) {
        return bookingRepository.findByTrainer_TrainerIdOrderByCreatedAtDesc(trainerId);
    }

    public EquipmentRequest submitEquipmentRequest(EquipmentRequestDto dto) {
        Trainer trainer = getProfile(dto.getTrainerId());
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found: " + dto.getEquipmentId()));

        if (dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        EquipmentRequest req = new EquipmentRequest(
                trainer,
                equipment,
                dto.getQuantity(),
                dto.getReason(),
                dto.getPriority() != null ? dto.getPriority().toUpperCase() : "MEDIUM",
                LocalDate.now(),
                "PENDING"
        );
        return equipmentRequestRepository.save(req);
    }

    public List<EquipmentRequest> getMyEquipmentRequests(Long trainerId) {
        return equipmentRequestRepository.findByTrainer_TrainerIdOrderByRequestDateDesc(trainerId);
    }
}
