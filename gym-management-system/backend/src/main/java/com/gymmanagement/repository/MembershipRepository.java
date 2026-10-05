package com.gymmanagement.repository;

import com.gymmanagement.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByCustomer_CustomerId(Long customerId);
    Optional<Membership> findFirstByCustomer_CustomerIdAndStatusOrderByEndDateDesc(Long customerId, String status);
    Optional<Membership> findFirstByCustomer_CustomerIdOrderByMembershipIdDesc(Long customerId);
    List<Membership> findByStatus(String status);
    long countByStatus(String status);
}
