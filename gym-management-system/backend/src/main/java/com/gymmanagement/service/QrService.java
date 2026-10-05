package com.gymmanagement.service;

import com.gymmanagement.dto.QrVerifyResponse;
import com.gymmanagement.model.Customer;
import com.gymmanagement.model.GymAccessQr;
import com.gymmanagement.model.Membership;
import com.gymmanagement.repository.GymAccessQrRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class QrService {

    private final GymAccessQrRepository qrRepository;

    public QrService(GymAccessQrRepository qrRepository) {
        this.qrRepository = qrRepository;
    }

    public QrVerifyResponse verifyAccess(String token) {
        if (token == null || token.trim().isEmpty()) {
            return QrVerifyResponse.denied("No QR token provided.", token);
        }

        String cleanedToken = token.trim();
        Optional<GymAccessQr> qrOpt = qrRepository.findByQrToken(cleanedToken);

        // 1. QR exists
        if (qrOpt.isEmpty()) {
            return QrVerifyResponse.denied("Invalid QR Token: Access record not found in system.", cleanedToken);
        }

        GymAccessQr qr = qrOpt.get();

        // 2. QR is active
        if (!"ACTIVE".equalsIgnoreCase(qr.getStatus())) {
            return QrVerifyResponse.denied("QR Access is " + qr.getStatus() + ". Entry denied.", cleanedToken);
        }

        // 3. Membership is active
        Membership membership = qr.getMembership();
        if (membership == null || !"ACTIVE".equalsIgnoreCase(membership.getStatus())) {
            return QrVerifyResponse.denied("Membership is Inactive or Expired. Please renew membership.", cleanedToken);
        }

        // 4. Current date within validity
        LocalDate today = LocalDate.now();
        if (today.isBefore(qr.getValidFrom())) {
            return QrVerifyResponse.denied("Membership validity starts on " + qr.getValidFrom() + ". Early access denied.", cleanedToken);
        }
        if (today.isAfter(qr.getValidUntil())) {
            return QrVerifyResponse.denied("Membership validity expired on " + qr.getValidUntil() + ". Access denied.", cleanedToken);
        }

        Customer customer = qr.getCustomer();
        return QrVerifyResponse.granted(
                "Access Granted! Welcome to the gym, " + customer.getName() + "!",
                customer.getName(),
                customer.getEmail(),
                membership.getPlanName(),
                qr.getValidFrom(),
                qr.getValidUntil(),
                cleanedToken
        );
    }
}
