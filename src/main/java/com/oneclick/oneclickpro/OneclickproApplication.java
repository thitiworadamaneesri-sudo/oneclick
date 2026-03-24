package com.oneclick.oneclickpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class OneclickproApplication {

    public static void main(String[] args) {
        SpringApplication.run(OneclickproApplication.class, args);
    }

    @GetMapping("/ping-20260324")
    public String ping() {
        return "pong-20260324";
    }
}