package com.gymmanagement.repository;

import com.gymmanagement.model.EquipmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRequestRepository extends JpaRepository<EquipmentRequest, Long> {
    List<EquipmentRequest> findByTrainer_TrainerIdOrderByRequestDateDesc(Long trainerId);
    List<EquipmentRequest> findAllByOrderByRequestDateDesc();
    long countByStatus(String status);
}
