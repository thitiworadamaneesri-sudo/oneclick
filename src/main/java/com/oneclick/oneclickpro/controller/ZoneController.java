package com.oneclick.oneclickpro.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ZoneController {

    private final JdbcTemplate jdbcTemplate;

    public ZoneController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/zones")
    public ResponseEntity<?> getZones() {
        try {
            String sql = """
                SELECT
                    area_id AS id,
                    area_name AS th,
                    area_name_eng AS en
                FROM ppavis_oneclickpro.oc_areas_all
                WHERE active = 'Y'
                ORDER BY area_id
            """;

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("ZONE API ERROR: " + e.getMessage());
        }
    }
}