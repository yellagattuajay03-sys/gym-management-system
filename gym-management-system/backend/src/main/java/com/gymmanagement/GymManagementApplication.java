package com.gymmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GymManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymManagementApplication.class, args);
        System.out.println("=========================================================");
        System.out.println(" GYM MANAGEMENT SYSTEM BACKEND STARTED SUCCESSFULLY!    ");
        System.out.println(" Port: 8080                                             ");
        System.out.println(" Web Portal: http://localhost:8080/                      ");
        System.out.println(" Admin:      http://localhost:8080/admin.html            ");
        System.out.println(" Trainer:    http://localhost:8080/trainer.html          ");
        System.out.println(" Customer:   http://localhost:8080/customer.html         ");
        System.out.println("=========================================================");
    }
}
