package com.oneclick.oneclickpro.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/booking/prefill")
public class BookingPrefillController {

    private final JdbcTemplate jdbcTemplate;

    public BookingPrefillController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/test")
    public String test() {
        return "prefill ok";
    }

    @GetMapping("/personal")
    public Map<String, Object> getPersonalPrefill(
            @RequestParam String documentType,
            @RequestParam String documentNo
    ) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = """
                SELECT *
                FROM oc_guest_trans_all
                WHERE tax_number = ?
                  AND (
                        (? = 'citizenId' AND LOWER(COALESCE(tax_number_type, '')) IN ('citizenid', 'citizen_id', 'idcard', 'id_card', 'บัตรประชาชน'))
                     OR (? = 'passport' AND LOWER(COALESCE(tax_number_type, '')) IN ('passport', 'พาสปอร์ต'))
                     OR COALESCE(tax_number_type, '') = ''
                  )
                LIMIT 1
            """;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    sql,
                    documentNo,
                    documentType,
                    documentType
            );

            if (rows.isEmpty()) {
                result.put("found", false);
                result.put("data", null);
                return result;
            }

            Map<String, Object> row = rows.get(0);
            Map<String, Object> data = new HashMap<>();

            data.put("documentType", documentType);
            data.put("citizenId", "citizenId".equalsIgnoreCase(documentType) ? value(row, "tax_number") : "");
            data.put("passport", "passport".equalsIgnoreCase(documentType) ? value(row, "tax_number") : "");

            data.put("title", value(row, "prefix"));
            data.put("firstName", value(row, "name"));
            data.put("lastName", value(row, "lasname", "lastname"));
            data.put("birthDate", value(row, "date_of_birth"));

            data.put("phone", value(row, "contact_detail1"));
            data.put("altPhone", value(row, "contact_detail2"));
            data.put("email", value(row, "email1"));
            data.put("altEmail", value(row, "email2"));
            data.put("lineId", value(row, "line_id"));

            // Registered address
            data.put("reg_houseNo", value(row, "address"));
            data.put("reg_moo", value(row, "moo"));
            data.put("reg_road", value(row, "street"));
            data.put("reg_subdistrict", value(row, "subdist"));
            data.put("reg_district", value(row, "district"));
            data.put("reg_province", value(row, "province"));
            data.put("reg_zip", value(row, "postcode"));

            // Current / contact address
            data.put("con_houseNo", value(row, "cur_address"));
            data.put("con_moo", value(row, "cur_moo"));
            data.put("con_road", value(row, "cur_street"));
            data.put("con_subdistrict", value(row, "cur_subdist"));
            data.put("con_district", value(row, "cur_district"));
            data.put("con_province", value(row, "cur_province"));
            data.put("con_zip", value(row, "cur_postcode"));

            result.put("found", true);
            result.put("data", data);
            return result;

        } catch (Exception e) {
            result.put("found", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    @GetMapping("/company")
    public Map<String, Object> getCompanyPrefill(@RequestParam String taxId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = """
                SELECT *
                FROM oc_guest_trans_all
                WHERE tax_number = ?
                LIMIT 1
            """;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, taxId);

            if (rows.isEmpty()) {
                result.put("found", false);
                result.put("data", null);
                return result;
            }

            Map<String, Object> row = rows.get(0);
            Map<String, Object> data = new HashMap<>();

            data.put("taxId", value(row, "tax_number"));
            data.put("bizType", value(row, "business_detail"));
            data.put("bizNo", value(row, "organization_name"));

            List<Map<String, Object>> directors = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                Map<String, Object> d = new HashMap<>();
                d.put("title", value(row, "auth_prefix" + i));
                d.put("firstName", value(row, "auth_name" + i));
                d.put("lastName", value(row, "auth_lasname" + i));
                d.put("email", value(row, "auth_email" + i));
                d.put("phone", value(row, "auth_contact" + i));

                Object signMode = value(row, "auth_signature_type" + i);
                d.put("signMode", "".equals(signMode) ? "SELF" : signMode);

                directors.add(d);
            }
            data.put("directors", directors);

            data.put("contactTitle", value(row, "contp_prefix"));
            data.put("contactLastName", value(row, "contp_lasname", "contp_lastname"));
            data.put("lastName", value(row, "lasname", "lastname"));
            data.put("contactPhone", value(row, "contp_contact"));
            data.put("contactEmail", value(row, "contp_email"));

            // Registered address
            data.put("reg_houseNo", value(row, "address"));
            data.put("reg_moo", value(row, "moo"));
            data.put("reg_road", value(row, "street"));
            data.put("reg_subdistrict", value(row, "subdist"));
            data.put("reg_district", value(row, "district"));
            data.put("reg_province", value(row, "province"));
            data.put("reg_zip", value(row, "postcode"));

            // Current / contact address
            data.put("con_houseNo", value(row, "cur_address"));
            data.put("con_moo", value(row, "cur_moo"));
            data.put("con_road", value(row, "cur_street"));
            data.put("con_subdistrict", value(row, "cur_subdist"));
            data.put("con_district", value(row, "cur_district"));
            data.put("con_province", value(row, "cur_province"));
            data.put("con_zip", value(row, "cur_postcode"));

            result.put("found", true);
            result.put("data", data);
            return result;

        } catch (Exception e) {
            result.put("found", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    private Object value(Map<String, Object> row, String... keys) {
    for (String key : keys) {
        if (row.containsKey(key) && row.get(key) != null) {
            String v = String.valueOf(row.get(key)).trim();
            if (!v.isEmpty()) {
                return v;
            }
        }
    }
    return "";
}

    @GetMapping("/debug-columns")
    public List<Map<String, Object>> debugColumns() {
        return jdbcTemplate.queryForList("SHOW COLUMNS FROM oc_guest_trans_all");
    }

    @GetMapping("/version")
public String version() {
    return "prefill-java-new-no-test";
}
}