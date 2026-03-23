package com.oneclick.oneclickpro.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/line")
public class LineWebhookController {

    @Value("${line.channel-secret}")
    private String channelSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String body,
            @RequestHeader(value = "x-line-signature", required = false) String signature
    ) {
        try {
            if (signature == null || !verifySignature(body, signature, channelSecret)) {
                return ResponseEntity.badRequest().body("invalid signature");
            }

            System.out.println("===== LINE WEBHOOK =====");
            System.out.println(body);

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("error");
        }
    }

    private boolean verifySignature(String body, String signature, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        String encoded = Base64.getEncoder().encodeToString(hash);
        return encoded.equals(signature);
    }
}