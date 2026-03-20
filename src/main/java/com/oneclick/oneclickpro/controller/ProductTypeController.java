package com.oneclick.oneclickpro.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/product-types")
public class ProductTypeController {

    private final JdbcTemplate jdbcTemplate;

    public ProductTypeController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> getProductTypesByZone(@RequestParam Integer areaId) {
        String sql = """
            SELECT DISTINCT
                pt.product_type_id,
                pt.product_type_name,
                pt.product_type_name_eng,
                pt.active
            FROM oc_properties_all p
            JOIN oc_product_type_all pt
                ON p.product_type_id = pt.product_type_id
            WHERE p.area_id = ?
              AND pt.active = 'Y'
            ORDER BY pt.product_type_id
        """;

        return jdbcTemplate.queryForList(sql, areaId);
    }
}