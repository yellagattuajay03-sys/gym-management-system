package com.gymmanagement.controller;

import com.gymmanagement.dto.DashboardStatsDto;
import com.gymmanagement.dto.EquipmentDto;
import com.gymmanagement.dto.TrainerDto;
import com.gymmanagement.model.*;
import com.gymmanagement.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // --- Trainer Endpoints ---
    @GetMapping("/trainers")
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        return ResponseEntity.ok(adminService.getAllTrainers());
    }

    @PostMapping("/trainers")
    public ResponseEntity<?> addTrainer(@RequestBody TrainerDto dto) {
        try {
            Trainer trainer = adminService.addTrainer(dto);
            return ResponseEntity.ok(trainer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/trainers/{id}")
    public ResponseEntity<?> updateTrainer(@PathVariable("id") Long id, @RequestBody TrainerDto dto) {
        try {
            Trainer trainer = adminService.updateTrainer(id, dto);
            return ResponseEntity.ok(trainer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/trainers/{id}")
    public ResponseEntity<?> deleteTrainer(@PathVariable("id") Long id) {
        try {
            adminService.deleteTrainer(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Trainer deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Customer Endpoints ---
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(adminService.getAllCustomers());
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<?> getCustomerDetails(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(adminService.getCustomerFullDetails(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Schedule & Booking Endpoints ---
    @GetMapping("/slots")
    public ResponseEntity<List<TrainerSlot>> getAllSlots() {
        return ResponseEntity.ok(adminService.getAllTrainerSlots());
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    @GetMapping("/booking-slots")
    public ResponseEntity<List<BookingSlot>> getAllBookingSlots() {
        return ResponseEntity.ok(adminService.getAllBookingSlots());
    }

    // --- Equipment Endpoints ---
    @GetMapping("/equipment")
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        return ResponseEntity.ok(adminService.getAllEquipment());
    }

    @PostMapping("/equipment")
    public ResponseEntity<?> addEquipment(@RequestBody EquipmentDto dto) {
        try {
            return ResponseEntity.ok(adminService.addEquipment(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/equipment/{id}")
    public ResponseEntity<?> updateEquipment(@PathVariable("id") Long id, @RequestBody EquipmentDto dto) {
        try {
            return ResponseEntity.ok(adminService.updateEquipment(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/equipment-requests")
    public ResponseEntity<List<EquipmentRequest>> getEquipmentRequests() {
        return ResponseEntity.ok(adminService.getAllEquipmentRequests());
    }

    @PostMapping("/equipment-requests/{id}/review")
    public ResponseEntity<?> reviewEquipmentRequest(@PathVariable("id") Long id, @RequestParam("action") String action) {
        try {
            EquipmentRequest req = adminService.reviewEquipmentRequest(id, action);
            return ResponseEntity.ok(Map.of("success", true, "message", "Request " + action.toUpperCase() + "ED successfully", "data", req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
