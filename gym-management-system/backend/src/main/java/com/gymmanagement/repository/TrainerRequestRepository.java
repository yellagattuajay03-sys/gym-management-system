package com.gymmanagement.repository;

import com.gymmanagement.model.TrainerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainerRequestRepository extends JpaRepository<TrainerRequest, Long> {
    List<TrainerRequest> findByTrainer_TrainerIdOrderByRequestDateDesc(Long trainerId);
    List<TrainerRequest> findByCustomer_CustomerIdOrderByRequestDateDesc(Long customerId);
    long countByStatus(String status);
}
