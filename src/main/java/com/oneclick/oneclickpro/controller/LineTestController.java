package com.oneclick.oneclickpro.controller;

import com.oneclick.oneclickpro.service.LineNotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LineTestController {

    private final LineNotificationService lineNotificationService;

    public LineTestController(LineNotificationService lineNotificationService) {
        this.lineNotificationService = lineNotificationService;
    }

    @GetMapping("/line/test")
    public String testLine() {
        lineNotificationService.sendText("🔥 ทดสอบแจ้งเตือนจากระบบ OneClick");
        return "sent";
    }
}