package com.oneclick.oneclickpro.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class DbController {

    private final JdbcTemplate jdbcTemplate;

    public DbController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db/test")
    public String testDb() {
        String dbName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        return "Connected to DB: " + dbName;
    }
}