package com.gymmanagement.controller;

import com.gymmanagement.dto.EquipmentRequestDto;
import com.gymmanagement.model.*;
import com.gymmanagement.service.TrainerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer")
@CrossOrigin(origins = "*")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(trainerService.getProfile(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile/{id}/availability")
    public ResponseEntity<?> updateAvailability(@PathVariable("id") Long id, @RequestParam("status") String status) {
        try {
            return ResponseEntity.ok(trainerService.updateAvailability(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/schedule/{id}")
    public ResponseEntity<List<TrainerSlot>> getSchedule(@PathVariable("id") Long id) {
        return ResponseEntity.ok(trainerService.getTrainerSlots(id));
    }

    @PostMapping("/slots/{id}")
    public ResponseEntity<?> addSlot(@PathVariable("id") Long id,
                                     @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                     @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                     @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        try {
            TrainerSlot slot = trainerService.addSlot(id, date, startTime, endTime);
            return ResponseEntity.ok(slot);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<List<TrainerRequest>> getCustomerRequests(@PathVariable("id") Long id) {
        return ResponseEntity.ok(trainerService.getCustomerRequests(id));
    }

    @PostMapping("/requests/{id}/handle")
    public ResponseEntity<?> handleRequest(@PathVariable("id") Long id, @RequestParam("action") String action) {
        try {
            TrainerRequest req = trainerService.handleRequest(id, action);
            return ResponseEntity.ok(Map.of("success", true, "message", "Request marked as " + req.getStatus(), "data", req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<List<Customer>> getMyCustomers(@PathVariable("id") Long id) {
        return ResponseEntity.ok(trainerService.getMyCustomers(id));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<List<Booking>> getMyBookings(@PathVariable("id") Long id) {
        return ResponseEntity.ok(trainerService.getMyBookings(id));
    }

    @PostMapping("/equipment-requests")
    public ResponseEntity<?> submitEquipmentRequest(@RequestBody EquipmentRequestDto dto) {
        try {
            EquipmentRequest req = trainerService.submitEquipmentRequest(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "Equipment request submitted to admin.", "data", req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/equipment-requests/{id}")
    public ResponseEntity<List<EquipmentRequest>> getMyEquipmentRequests(@PathVariable("id") Long id) {
        return ResponseEntity.ok(trainerService.getMyEquipmentRequests(id));
    }
}
