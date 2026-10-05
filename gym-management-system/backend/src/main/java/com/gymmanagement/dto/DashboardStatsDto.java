package com.gymmanagement.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {
    private long totalTrainers;
    private long totalCustomers;
    private long activeMembers;
    private long todayBookings;
    private long pendingTrainerRequests;
    private long pendingEquipmentRequests;
    private long availableEquipment;
    private BigDecimal totalRevenue;

    public DashboardStatsDto() {}

    public long getTotalTrainers() {
        return totalTrainers;
    }

    public void setTotalTrainers(long totalTrainers) {
        this.totalTrainers = totalTrainers;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(long activeMembers) {
        this.activeMembers = activeMembers;
    }

    public long getTodayBookings() {
        return todayBookings;
    }

    public void setTodayBookings(long todayBookings) {
        this.todayBookings = todayBookings;
    }

    public long getPendingTrainerRequests() {
        return pendingTrainerRequests;
    }

    public void setPendingTrainerRequests(long pendingTrainerRequests) {
        this.pendingTrainerRequests = pendingTrainerRequests;
    }

    public long getPendingEquipmentRequests() {
        return pendingEquipmentRequests;
    }

    public void setPendingEquipmentRequests(long pendingEquipmentRequests) {
        this.pendingEquipmentRequests = pendingEquipmentRequests;
    }

    public long getAvailableEquipment() {
        return availableEquipment;
    }

    public void setAvailableEquipment(long availableEquipment) {
        this.availableEquipment = availableEquipment;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
