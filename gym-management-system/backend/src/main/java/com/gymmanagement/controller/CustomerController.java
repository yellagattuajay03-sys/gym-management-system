package com.gymmanagement.controller;

import com.gymmanagement.dto.BookingRequestDto;
import com.gymmanagement.dto.PaymentRequestDto;
import com.gymmanagement.model.*;
import com.gymmanagement.service.CustomerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(customerService.getProfile(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trainers")
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        return ResponseEntity.ok(customerService.getAllTrainers());
    }

    @GetMapping("/trainers/{id}/slots")
    public ResponseEntity<List<TrainerSlot>> getAvailableSlots(
            @PathVariable("id") Long id,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(customerService.getTrainerAvailableSlots(id, date));
    }

    @PostMapping("/requests")
    public ResponseEntity<?> sendRequest(@RequestBody BookingRequestDto dto) {
        try {
            TrainerRequest request = customerService.sendTrainerRequest(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Trainer request sent successfully! Awaiting trainer confirmation.",
                    "data", request
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<List<TrainerRequest>> getMyRequests(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getMyRequests(id));
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDto dto) {
        try {
            Booking booking = customerService.createBooking(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Booking confirmed successfully!",
                    "booking", booking
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<List<Booking>> getMyBookings(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getMyBookings(id));
    }

    @GetMapping("/membership/{id}")
    public ResponseEntity<?> getMembership(@PathVariable("id") Long id) {
        var activeOpt = customerService.getActiveMembership(id);
        var allList = customerService.getAllMemberships(id);
        return ResponseEntity.ok(Map.of(
                "activeMembership", activeOpt.orElse(null),
                "allMemberships", allList
        ));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<List<Payment>> getMyPayments(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getMyPayments(id));
    }

    @PostMapping("/pay")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequestDto dto) {
        try {
            Map<String, Object> result = customerService.processMembershipPayment(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/qr/{id}")
    public ResponseEntity<?> getMyQr(@PathVariable("id") Long id) {
        var qrOpt = customerService.getActiveQrCode(id);
        if (qrOpt.isPresent()) {
            return ResponseEntity.ok(Map.of("hasQr", true, "qr", qrOpt.get()));
        } else {
            return ResponseEntity.ok(Map.of("hasQr", false, "message", "No active QR code. Please purchase or renew membership."));
        }
    }
}
