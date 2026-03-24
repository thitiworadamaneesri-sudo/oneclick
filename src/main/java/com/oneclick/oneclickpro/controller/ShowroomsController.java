package com.oneclick.oneclickpro.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ShowroomsController {

    private final JdbcTemplate jdbcTemplate;

    public ShowroomsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/types")
    public List<Map<String, Object>> getTypesByArea(
            @RequestParam Integer areaId
    ) {
        String sql = """
            SELECT DISTINCT
                p.product_type_id AS productTypeId,
                t.product_type_name AS name,
                t.product_type_name_eng AS eng
            FROM oc_properties_all p
            JOIN oc_product_type_all t
                ON p.product_type_id = t.product_type_id
            WHERE p.area_id = ?
              AND t.active = 'Y'
            ORDER BY t.product_type_name
        """;

        return jdbcTemplate.queryForList(sql, areaId);
    }

    @GetMapping("/buildings")
    public List<Map<String, Object>> getBuildings(
            @RequestParam Integer areaId,
            @RequestParam Integer productTypeId
    ) {
        String sql = """
            SELECT DISTINCT
                p.PROPERTY_ID AS propertyId,
                o.LOCATION_ID AS buildingLocationId,
                o.BUILDING_DP AS BUILDING_DP,
                o.BUILDING_DP AS building,
                o.BUILDING_DP AS label,
                o.BUILDING_DP AS value
            FROM oc_properties_all p
            JOIN oc_properties_locs_all o
                ON p.PROPERTY_ID = o.PROPERTY_ID
            WHERE p.area_id = ?
              AND p.product_type_id = ?
              AND o.LOCATION_TYPE_LOOKUP_CODE = 'BUILDING'
              AND o.BUILDING_DP IS NOT NULL
              AND o.BUILDING_DP <> ''
            ORDER BY o.BUILDING_DP
        """;

        return jdbcTemplate.queryForList(sql, areaId, productTypeId);
    }

    @GetMapping("/floors")
    public List<Map<String, Object>> getFloors(
            @RequestParam Integer propertyId,
            @RequestParam String building
    ) {
        String sql = """
            SELECT DISTINCT
                o.PROPERTY_ID AS propertyId,
                o.BUILDING_DP AS BUILDING_DP,
                o.BUILDING_DP AS building,
                o.FLOOR_DP AS FLOOR_DP,
                o.FLOOR_DP AS floor,
                o.FLOOR_DP AS label,
                o.FLOOR_DP AS value
            FROM oc_properties_locs_all o
            WHERE o.PROPERTY_ID = ?
              AND o.BUILDING_DP = ?
              AND o.LOCATION_TYPE_LOOKUP_CODE = 'FLOOR'
              AND o.FLOOR_DP IS NOT NULL
              AND o.FLOOR_DP <> ''
            ORDER BY o.FLOOR_DP
        """;

        return jdbcTemplate.queryForList(sql, propertyId, building);
    }

    @GetMapping("/rooms")
    public List<Map<String, Object>> getRooms(
            @RequestParam Integer propertyId,
            @RequestParam String building,
            @RequestParam String floor
    ) {
        String sql = """
            SELECT DISTINCT
                o.LOCATION_ID AS locationId,
                o.OFFICE_ID AS officeId,
                o.OFFICE_DP AS id,
                o.OFFICE_DP AS roomId,
                o.OFFICE_DP AS roomNo,
                o.OFFICE_DP AS office,
                o.OFFICE_DP AS OFFICE_DP,
                o.PROPERTY_ID AS propertyId,
                o.BUILDING_DP AS BUILDING_DP,
                o.BUILDING_DP AS building,
                o.FLOOR_DP AS FLOOR_DP,
                o.FLOOR_DP AS floor,
                CASE
                    WHEN o.STATUS = 'Y' THEN 'available'
                    WHEN o.STATUS = 'R' THEN 'occupied'
                    ELSE 'occupied'
                END AS status
            FROM oc_properties_locs_all o
            WHERE o.PROPERTY_ID = ?
              AND o.BUILDING_DP = ?
              AND o.FLOOR_DP = ?
              AND o.LOCATION_TYPE_LOOKUP_CODE = 'OFFICE'
              AND o.OFFICE_DP IS NOT NULL
              AND o.OFFICE_DP <> ''
            ORDER BY o.OFFICE_DP
        """;

        return jdbcTemplate.queryForList(sql, propertyId, building, floor);
    }

    @GetMapping
    public List<Map<String, Object>> getShowrooms(
            @RequestParam Integer areaId,
            @RequestParam Integer productTypeId
    ) {
        String sql = """
            SELECT
                o.LOCATION_ID AS locationId,
                o.OFFICE_ID AS officeId,
                o.OFFICE_DP AS id,
                o.OFFICE_DP AS roomId,
                o.OFFICE_DP AS roomNo,
                o.OFFICE_DP AS office,
                o.OFFICE_DP AS OFFICE_DP,
                o.PROPERTY_ID AS propertyId,
                o.BUILDING_DP AS BUILDING_DP,
                o.BUILDING_DP AS building,
                o.FLOOR_DP AS FLOOR_DP,
                o.FLOOR_DP AS floor,
                CASE
                    WHEN o.STATUS = 'Y' THEN 'available'
                    WHEN o.STATUS = 'R' THEN 'occupied'
                    ELSE 'occupied'
                END AS status
            FROM oc_properties_all p
            JOIN oc_properties_locs_all o
                ON p.PROPERTY_ID = o.PROPERTY_ID
            WHERE p.area_id = ?
              AND p.product_type_id = ?
              AND o.LOCATION_TYPE_LOOKUP_CODE = 'OFFICE'
              AND o.OFFICE_DP IS NOT NULL
              AND o.OFFICE_DP <> ''
            ORDER BY o.BUILDING_DP, o.FLOOR_DP, o.OFFICE_DP
        """;

        return jdbcTemplate.queryForList(sql, areaId, productTypeId);
    }
}