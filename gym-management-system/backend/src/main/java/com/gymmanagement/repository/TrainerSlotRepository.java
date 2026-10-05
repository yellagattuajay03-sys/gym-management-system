package com.gymmanagement.repository;

import com.gymmanagement.model.TrainerSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerSlotRepository extends JpaRepository<TrainerSlot, Long> {
    List<TrainerSlot> findByTrainer_TrainerIdOrderBySlotDateAscStartTimeAsc(Long trainerId);
    List<TrainerSlot> findByTrainer_TrainerIdAndSlotDateOrderByStartTimeAsc(Long trainerId, LocalDate slotDate);
    List<TrainerSlot> findByTrainer_TrainerIdAndSlotDateAndStatus(Long trainerId, LocalDate slotDate, String status);
    Optional<TrainerSlot> findByTrainer_TrainerIdAndSlotDateAndStartTime(Long trainerId, LocalDate slotDate, LocalTime startTime);
    List<TrainerSlot> findBySlotDateOrderByStartTimeAsc(LocalDate slotDate);
    List<TrainerSlot> findByStatus(String status);
}
