package com.oneclick.oneclickpro.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {
    "http://localhost:5173",
    "https://ppavis.com"
})
@RestController
@RequestMapping("/api")
public class HealthController {

    public HealthController() {
        System.out.println("🔥 HealthController LOADED");
    }

    @GetMapping("/health")
    public String health() {
        System.out.println("=== /api/health called ===");
        return "OK";
    }
}