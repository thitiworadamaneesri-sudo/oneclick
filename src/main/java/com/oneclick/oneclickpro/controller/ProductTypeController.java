package com.oneclick.oneclickpro.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/systems/types")
public class ProductTypeController {

    private final JdbcTemplate jdbcTemplate;

    public ProductTypeController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<?> getProductTypesByZone(@RequestParam Integer areaId) {
        try {
            String sql = """
                SELECT DISTINCT
                    pt.product_type_id AS productTypeId,
                    pt.product_type_name AS name,
                    pt.product_type_name_eng AS eng
                FROM oc_properties_all p
                JOIN oc_product_type_all pt
                    ON p.product_type_id = pt.product_type_id
                WHERE p.area_id = ?
                  AND pt.active = 'Y'
                ORDER BY pt.product_type_id
            """;

            return ResponseEntity.ok(jdbcTemplate.queryForList(sql, areaId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("PRODUCT TYPE API ERROR: " + e.getMessage());
        }
    }
}