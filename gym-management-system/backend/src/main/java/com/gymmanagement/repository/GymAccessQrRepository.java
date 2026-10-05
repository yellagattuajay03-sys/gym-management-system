package com.gymmanagement.repository;

import com.gymmanagement.model.GymAccessQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GymAccessQrRepository extends JpaRepository<GymAccessQr, Long> {
    Optional<GymAccessQr> findByQrToken(String qrToken);
    List<GymAccessQr> findByCustomer_CustomerIdOrderByValidUntilDesc(Long customerId);
    Optional<GymAccessQr> findFirstByCustomer_CustomerIdAndStatusOrderByValidUntilDesc(Long customerId, String status);
}
