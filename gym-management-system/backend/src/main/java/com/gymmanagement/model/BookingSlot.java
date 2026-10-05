package com.gymmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_slot", uniqueConstraints = {
    @UniqueConstraint(name = "uk_booking_slot_date", columnNames = {"slot_id", "booking_date"})
})
public class BookingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_slot_id")
    private Long bookingSlotId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = false)
    private TrainerSlot slot;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "BOOKED"; // BOOKED, CANCELLED, COMPLETED

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public BookingSlot() {}

    public BookingSlot(Booking booking, TrainerSlot slot, LocalDate bookingDate, String status) {
        this.booking = booking;
        this.slot = slot;
        this.bookingDate = bookingDate;
        this.status = status != null ? status : "BOOKED";
    }

    public Long getBookingSlotId() {
        return bookingSlotId;
    }

    public void setBookingSlotId(Long bookingSlotId) {
        this.bookingSlotId = bookingSlotId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public TrainerSlot getSlot() {
        return slot;
    }

    public void setSlot(TrainerSlot slot) {
        this.slot = slot;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
