package com.gymmanagement.repository;

import com.gymmanagement.model.BookingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingSlotRepository extends JpaRepository<BookingSlot, Long> {
    List<BookingSlot> findByBooking_BookingId(Long bookingId);
    List<BookingSlot> findBySlot_SlotId(Long slotId);
    List<BookingSlot> findByBookingDate(LocalDate bookingDate);
    boolean existsBySlot_SlotIdAndBookingDate(Long slotId, LocalDate bookingDate);
}
