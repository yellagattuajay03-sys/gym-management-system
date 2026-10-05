package com.gymmanagement.repository;

import com.gymmanagement.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByStatus(String status);

    @Query("SELECT COALESCE(SUM(e.availableQuantity), 0) FROM Equipment e")
    Long sumAvailableQuantity();
}
