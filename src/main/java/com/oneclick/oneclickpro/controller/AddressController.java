package com.oneclick.oneclickpro.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/address")
public class AddressController {

    private final JdbcTemplate jdbcTemplate;

    public AddressController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/postcode/{postcode}")
    public List<Map<String, Object>> getAddressByPostcode(@PathVariable String postcode) {
        return jdbcTemplate.queryForList(
            """
            SELECT DISTINCT
                PostCodeMain AS postcode,
                ProvinceThai AS province,
                DistrictThai AS district,
                subdist_Thai AS subdistrict
            FROM oc_addr_thai_all
            WHERE PostCodeMain = ?
              AND ProvinceThai IS NOT NULL AND ProvinceThai <> ''
              AND DistrictThai IS NOT NULL AND DistrictThai <> ''
              AND subdist_Thai IS NOT NULL AND subdist_Thai <> ''
            ORDER BY ProvinceThai, DistrictThai, subdist_Thai
            """,
            postcode
        );
    }
}