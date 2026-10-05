package com.gymmanagement.controller;

import com.gymmanagement.dto.QrVerifyResponse;
import com.gymmanagement.service.QrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/qr")
@CrossOrigin(origins = "*")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping("/verify")
    public ResponseEntity<QrVerifyResponse> verifyQrGet(@RequestParam("token") String token) {
        QrVerifyResponse resp = qrService.verifyAccess(token);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/verify")
    public ResponseEntity<QrVerifyResponse> verifyQrPost(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        QrVerifyResponse resp = qrService.verifyAccess(token);
        return ResponseEntity.ok(resp);
    }
}
