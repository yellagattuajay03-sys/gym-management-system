package com.gymmanagement.dto;

import java.time.LocalDate;

public class QrVerifyResponse {
    private boolean granted;
    private String status; // ACCESS GRANTED or ACCESS DENIED
    private String message;
    private String customerName;
    private String customerEmail;
    private String planName;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private String qrToken;

    public QrVerifyResponse() {}

    public static QrVerifyResponse denied(String message, String qrToken) {
        QrVerifyResponse resp = new QrVerifyResponse();
        resp.setGranted(false);
        resp.setStatus("ACCESS DENIED");
        resp.setMessage(message);
        resp.setQrToken(qrToken);
        return resp;
    }

    public static QrVerifyResponse granted(String message, String customerName, String customerEmail, 
                                           String planName, LocalDate validFrom, LocalDate validUntil, String qrToken) {
        QrVerifyResponse resp = new QrVerifyResponse();
        resp.setGranted(true);
        resp.setStatus("ACCESS GRANTED");
        resp.setMessage(message);
        resp.setCustomerName(customerName);
        resp.setCustomerEmail(customerEmail);
        resp.setPlanName(planName);
        resp.setValidFrom(validFrom);
        resp.setValidUntil(validUntil);
        resp.setQrToken(qrToken);
        return resp;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}
