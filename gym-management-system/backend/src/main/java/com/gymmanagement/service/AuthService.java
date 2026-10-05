package com.gymmanagement.service;

import com.gymmanagement.dto.LoginRequest;
import com.gymmanagement.dto.LoginResponse;
import com.gymmanagement.dto.RegisterRequest;
import com.gymmanagement.model.Admin;
import com.gymmanagement.model.Customer;
import com.gymmanagement.model.Trainer;
import com.gymmanagement.repository.AdminRepository;
import com.gymmanagement.repository.CustomerRepository;
import com.gymmanagement.repository.TrainerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AuthService {

    private final AdminRepository adminRepository;
    private final TrainerRepository trainerRepository;
    private final CustomerRepository customerRepository;

    public AuthService(AdminRepository adminRepository, 
                       TrainerRepository trainerRepository, 
                       CustomerRepository customerRepository) {
        this.adminRepository = adminRepository;
        this.trainerRepository = trainerRepository;
        this.customerRepository = customerRepository;
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null || request.getRole() == null) {
            return LoginResponse.failure("Email, password, and role are required.");
        }

        String role = request.getRole().toUpperCase().trim();
        String email = request.getEmail().trim();
        String password = request.getPassword().trim();

        switch (role) {
            case "ADMIN":
                Optional<Admin> admin = adminRepository.findByEmail(email);
                if (admin.isPresent() && admin.get().getPassword().equals(password)) {
                    return LoginResponse.success("Admin login successful", admin.get().getAdminId(), 
                            admin.get().getName(), admin.get().getEmail(), "ADMIN");
                }
                break;

            case "TRAINER":
                Optional<Trainer> trainer = trainerRepository.findByEmail(email);
                if (trainer.isPresent() && trainer.get().getPassword().equals(password)) {
                    return LoginResponse.success("Trainer login successful", trainer.get().getTrainerId(), 
                            trainer.get().getName(), trainer.get().getEmail(), "TRAINER");
                }
                break;

            case "CUSTOMER":
                Optional<Customer> customer = customerRepository.findByEmail(email);
                if (customer.isPresent() && customer.get().getPassword().equals(password)) {
                    return LoginResponse.success("Customer login successful", customer.get().getCustomerId(), 
                            customer.get().getName(), customer.get().getEmail(), "CUSTOMER");
                }
                break;

            default:
                return LoginResponse.failure("Invalid user role specified.");
        }

        return LoginResponse.failure("Invalid email or password.");
    }

    public Customer registerCustomer(RegisterRequest req) {
        if (customerRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + req.getEmail());
        }

        Customer customer = new Customer(
                req.getName(),
                req.getEmail(),
                req.getPassword(),
                req.getPhone(),
                req.getAddress(),
                LocalDate.now()
        );
        return customerRepository.save(customer);
    }
}
