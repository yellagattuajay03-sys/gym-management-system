package com.gymmanagement.dto;

import java.math.BigDecimal;

public class PaymentRequestDto {
    private Long customerId;
    private String planName; // Monthly Gold, Quarterly Premium, Half-Yearly Elite, Annual VIP
    private BigDecimal amount;
    private String paymentMethod; // UPI, Credit Card, Debit Card, Net Banking, Cash

    public PaymentRequestDto() {}

    public PaymentRequestDto(Long customerId, String planName, BigDecimal amount, String paymentMethod) {
        this.customerId = customerId;
        this.planName = planName;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
