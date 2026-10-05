package com.gymmanagement.controller;

import com.gymmanagement.dto.LoginRequest;
import com.gymmanagement.dto.LoginResponse;
import com.gymmanagement.dto.RegisterRequest;
import com.gymmanagement.model.Customer;
import com.gymmanagement.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Customer customer = authService.registerCustomer(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Customer registration successful!",
                    "customerId", customer.getCustomerId(),
                    "name", customer.getName(),
                    "email", customer.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
