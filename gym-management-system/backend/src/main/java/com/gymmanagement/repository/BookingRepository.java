package com.gymmanagement.repository;

import com.gymmanagement.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Booking> findByTrainer_TrainerIdOrderByCreatedAtDesc(Long trainerId);
    List<Booking> findAllByOrderByCreatedAtDesc();
    long countByStartDate(LocalDate startDate);
}
