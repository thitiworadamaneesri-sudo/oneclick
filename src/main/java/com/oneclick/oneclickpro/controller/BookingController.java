package com.oneclick.oneclickpro.controller;

import com.oneclick.oneclickpro.service.LineNotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {
    "https://ppavis.com",
    "http://localhost:5173"
})
@RequestMapping("/booking")
public class BookingController {

    private final JdbcTemplate jdbcTemplate;
    private final LineNotificationService lineNotificationService;

    public BookingController(JdbcTemplate jdbcTemplate,
                             LineNotificationService lineNotificationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.lineNotificationService = lineNotificationService;
    }

    @PostMapping("/save")
    public String saveBooking(@RequestBody Map<String, Object> body) {
        System.out.println("payload = " + body);

        String[] carRegs = extractVehicleRegs(body.get("vehicleRegistrations"));

        Integer resolvedAreaId = toIntOrNull(
            firstNonBlank(
                body.get("areaId"),
                body.get("area_id"),
                body.get("productArea"),
                body.get("product_area")
            )
        );

        Integer resolvedBuildingId = resolveBuildingLocationId(body);
        Integer resolvedOfficeId = resolveLocationId(body);

        System.out.println("payload = " + body);
        System.out.println("buildingId raw = " + body.get("buildingId"));
        System.out.println("building_id raw = " + body.get("building_id"));
        System.out.println("officeId raw = " + body.get("officeId"));
        System.out.println("office_id raw = " + body.get("office_id"));
        System.out.println("resolvedBuildingId = " + resolvedBuildingId);
        System.out.println("resolvedOfficeId = " + resolvedOfficeId);

        if (resolvedBuildingId == null) {
            throw new IllegalArgumentException("buildingId/building_id is required");
        }

        if (resolvedOfficeId == null) {
            throw new IllegalArgumentException("officeId/office_id is required");
        }

        System.out.println("resolvedBuildingId = " + resolvedBuildingId);
        System.out.println("resolvedOfficeId = " + resolvedOfficeId);

        String sql = """
            INSERT INTO oc_guest_trans_all (
                lease_commencement_date,
                lease_end_date,
                duration_of_contract,
                process_type,
                product_area,
                product_type,
                building,
                location_id,
                parkinglot_req,
                car_type,
                car_qty,
                mot_qty,
                car_license_plate1,
                car_license_plate2,
                car_license_plate3,
                car_license_plate4,
                tax_number,
                tax_number_type,
                prefix,
                name,
                lasname,
                date_of_birth,
                contact_detail1,
                contact_detail2,
                email1,
                email2,
                line_id,
                province,
                district,
                subdist,
                postcode,
                address,
                moo,
                street,
                cur_province,
                cur_district,
                cur_subdist,
                cur_postcode,
                cur_address,
                cur_moo,
                cur_street,
                organization_name,
                business_detail,
                contp_prefix,
                contp_auth_name,
                contp_lasname,
                contp_email,
                contp_contact,
                auth_prefix1,
                auth_name1,
                auth_lasname1,
                auth_email1,
                auth_contact1,
                auth_signature_type1,

                auth_prefix2,
                auth_name2,
                auth_lasname2,
                auth_email2,
                auth_contact2,
                auth_signature_type2,

                auth_prefix3,
                auth_name3,
                auth_lasname3,
                auth_email3,
                auth_contact3,
                auth_signature_type3,
                number_of_occupants,
                add_on_detail,
                info_channel
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Object[] params = new Object[]{
            normalizeDate(body.get("leaseCommencementDate")),
            normalizeDate(body.get("leaseEndDate")),
            toIntOrNull(body.get("durationOfContract")),
            resolveProcessType(body),
            toIntOrNull(firstNonBlank(body.get("areaId"), body.get("area_id"), body.get("productArea"), body.get("product_area"))),
            body.get("productType"),
            resolvedBuildingId,
            resolvedOfficeId,
            toParkingReq(body.get("needParking")),
            toCarType(body.get("parkingType")),
            toCarQty(body.get("parkingType"), body.get("parkingCount")),
            toMotQty(body.get("parkingType"), body.get("parkingCount")),
            carRegs[0],
            carRegs[1],
            carRegs[2],
            carRegs[3],
            resolveTaxNumber(body),
            resolveTaxNumberType(body),
            body.get("prefix"),
            body.get("name"),
            body.get("lastname"),
            normalizeDate(body.get("dateOfBirth")),
            body.get("contactDetail1"),
            pick(body, "contactDetail2", "altPhone"),
            body.get("email1"),
            pick(body, "email2", "altEmail"),
            body.get("lineId"),
            body.get("province"),
            body.get("district"),
            body.get("subdist"),
            body.get("postcode"),
            body.get("address"),
            body.get("moo"),
            body.get("street"),
            body.get("curProvince"),
            body.get("curDistrict"),
            body.get("curSubdist"),
            body.get("curPostcode"),
            body.get("curAddress"),
            body.get("curMoo"),
            body.get("curStreet"),
            body.get("organizationName"),

            pick(body, "business_detail", "businessType", "business_type", "bizType", "companyType"),
            pick(body, "contpPrefix", "contp_prefix", "contactTitle"),
            pick(body, "contpAuthName", "contp_name", "contactFirstName"),
            pick(body, "contpLastname", "contp_lastname", "contactLastName"),
            pick(body, "contpEmail", "contp_email", "contactEmail"),
            pick(body, "contpContact", "contp_contact", "contactPhone"),

            pick(body, "auth_prefix1"),
            pick(body, "auth_name1", "auth_firstname1"),
            pick(body, "auth_lasname1", "auth_lastname1"),
            pick(body, "auth_email1"),
            pick(body, "auth_contact1"),
            pick(body, "auth_signature_type1", "auth_sign_mode1"),

            pick(body, "auth_prefix2"),
            pick(body, "auth_name2", "auth_firstname2"),
            pick(body, "auth_lasname2", "auth_lastname2"),
            pick(body, "auth_email2"),
            pick(body, "auth_contact2"),
            pick(body, "auth_signature_type2", "auth_sign_mode2"),

            pick(body, "auth_prefix3"),
            pick(body, "auth_name3", "auth_firstname3"),
            pick(body, "auth_lasname3", "auth_lastname3"),
            pick(body, "auth_email3"),
            pick(body, "auth_contact3"),
            pick(body, "auth_signature_type3", "auth_sign_mode3"),

            toOccupants(body.get("occupantsCount")),
            body.get("extraDetails"),
            body.get("discoveryChannel")
        };

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int[] types = new int[]{
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.INTEGER,
                Types.VARCHAR,
                Types.INTEGER,
                Types.VARCHAR,
                Types.INTEGER,
                Types.INTEGER,
                Types.VARCHAR,
                Types.INTEGER,
                Types.INTEGER,
                Types.INTEGER,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.INTEGER,
                Types.VARCHAR,
                Types.VARCHAR
            };

            PreparedStatementCreatorFactory pscFactory =
                new PreparedStatementCreatorFactory(sql);

            pscFactory.setReturnGeneratedKeys(true);

            PreparedStatementCreator psc =
                pscFactory.newPreparedStatementCreator(Arrays.asList(params));

            System.out.println("resolvedAreaId = " + resolvedAreaId);
            System.out.println("resolvedBuildingId = " + resolvedBuildingId);
            System.out.println("resolvedOfficeId = " + resolvedOfficeId);
            System.out.println("params.length = " + params.length);
            System.out.println("params = " + Arrays.toString(params));

            jdbcTemplate.update(psc, keyHolder);

            Number generatedId = keyHolder.getKey();

            try {
    String zoneDisplay = resolveZoneDisplay(body, resolvedAreaId);
    String buildingDisplay = resolveBuildingDisplay(body, resolvedBuildingId);
    String roomDisplay = resolveRoomDisplay(body, resolvedOfficeId);

    String displayName = resolveLineDisplayName(body);

    String message = """
📢 แจ้งเตือน OneClick

มีผู้ลงทะเบียนใหม่

เลขรายการ: %s
📌 ประเภท: %s
👤 ชื่อ: %s
🏢 โซน: %s
🏢 อาคาร: %s
🚪 ห้อง: %s
📅 เริ่ม: %s
📅 สิ้นสุด: %s

👉 กรุณาตรวจสอบในระบบ
""".formatted(
        generatedId != null ? generatedId.longValue() : "-",
        resolveProcessType(body) != null ? resolveProcessType(body) : "-",
        displayName != null ? displayName : "-",
        zoneDisplay != null ? zoneDisplay : "-",
        buildingDisplay != null ? buildingDisplay : "-",
        roomDisplay != null ? roomDisplay : "-",
        normalizeDate(body.get("leaseCommencementDate")) != null
            ? normalizeDate(body.get("leaseCommencementDate"))
            : "-",
        normalizeDate(body.get("leaseEndDate")) != null
            ? normalizeDate(body.get("leaseEndDate"))
            : "-"
    ) + "\n\nhttps://ppavis.com/registers/#/admin/leads";

    System.out.println("==== LINE MESSAGE START ====");
    System.out.println(message);
    System.out.println("==== LINE MESSAGE END ====");

    lineNotificationService.sendText(message);
} catch (Exception lineEx) {
    lineEx.printStackTrace();
}

            return generatedId != null ? "saved:" + generatedId.longValue() : "saved";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }

    @GetMapping("/admin-list")
    public List<Map<String, Object>> getAdminList() {
        String sql = """
            SELECT
                guest_trans_id AS id,
                DATE_FORMAT(lease_commencement_date, '%Y-%m-%d') AS createdAt,
                0 AS processed,

                prefix AS title,
                name AS firstName,
                lasname AS lastName,
                CASE
                    WHEN organization_name IS NOT NULL AND TRIM(organization_name) <> ''
                        THEN organization_name
                    ELSE CONCAT(
                        COALESCE(prefix, ''),
                        CASE WHEN prefix IS NOT NULL AND TRIM(prefix) <> '' THEN ' ' ELSE '' END,
                        COALESCE(name, ''),
                        CASE WHEN name IS NOT NULL AND TRIM(name) <> '' THEN ' ' ELSE '' END,
                        COALESCE(lasname, '')
                    )
                END AS fullName,
                tax_number AS idCard,
                NULL AS passportNo,
                NULL AS occupation,
                line_id AS lineId,
                date_of_birth AS birthDate,
                email1 AS email,
                email2 AS email2,
                contact_detail1 AS phone,
                contact_detail2 AS phone2,
                add_on_detail AS note,

                address AS reg_houseNo,
                moo AS reg_moo,
                street AS reg_road,
                province AS reg_province,
                subdist AS reg_subdistrict,
                district AS reg_district,
                postcode AS reg_zip,

                cur_address AS con_houseNo,
                cur_moo AS con_moo,
                cur_street AS con_road,
                cur_province AS con_province,
                cur_subdist AS con_subdistrict,
                cur_district AS con_district,
                cur_postcode AS con_zip,

                process_type AS processType,
                product_area AS projectZone,
                product_type AS propertyType,
                building AS building,
                location_id AS roomNo,
                lease_commencement_date AS startDate,
                NULL AS contractMonths,
                NULL AS rentPrice,
                add_on_detail AS remark,
                info_channel AS sourceChannel,
                1 AS consentAccepted

            FROM oc_guest_trans_all
            ORDER BY guest_trans_id DESC
            """;

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/latest")
    public List<Map<String, Object>> getLatestBookings() {
        return jdbcTemplate.queryForList(
            "SELECT * FROM oc_guest_trans_all ORDER BY guest_trans_id DESC LIMIT 10"
        );
    }

    private String firstNonBlank(Object... values) {
        for (Object value : values) {
            if (value != null) {
                String text = String.valueOf(value).trim();
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }
        return null;
    }

    private Object pick(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            if (key == null || key.trim().isEmpty()) {
                continue;
            }
            Object value = body.get(key);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private Integer resolveBuildingLocationId(Map<String, Object> body) {
        return toIntOrNull(firstNonBlank(
            body.get("buildingId"),
            body.get("building_id"),
            body.get("building"),
            body.get("buildingLocationId")
        ));
    }

    private Integer resolveLocationId(Map<String, Object> body) {
        return toIntOrNull(firstNonBlank(
            body.get("officeId"),
            body.get("office_id"),
            body.get("location_id"),
            body.get("roomId"),
            body.get("selectedOfficeId")
        ));
    }

    private String resolveProcessType(Map<String, Object> body) {
        String raw = firstNonBlank(
            body.get("processType"),
            body.get("process_type")
        );

        if (raw == null) {
            return null;
        }

        String value = raw.trim().toUpperCase();

        if ("BOOKING".equals(value) || "จอง".equals(raw.trim())) {
            return "BOOKING";
        }

        if ("CONTRACT".equals(value) || "ทำสัญญา".equals(raw.trim())) {
            return "CONTRACT";
        }

        return raw.trim();
    }

    private String resolveLineDisplayName(Map<String, Object> body) {
        String organizationName = firstNonBlank(
            body.get("organizationName"),
            body.get("organization_name"),
            body.get("companyName"),
            body.get("businessName")
        );

        if (organizationName != null) {
            return organizationName;
        }

        String prefix = firstNonBlank(body.get("prefix"));
        String firstName = firstNonBlank(body.get("name"));
        String lastName = firstNonBlank(body.get("lastname"));

        String fullName = (
            (prefix != null ? prefix + " " : "") +
            (firstName != null ? firstName + " " : "") +
            (lastName != null ? lastName : "")
        ).trim();

        return fullName.isEmpty() ? "-" : fullName;
    }

    private String resolveZoneDisplay(Map<String, Object> body, Integer areaId) {
        String direct = firstNonBlank(
            body.get("zoneDp"),
            body.get("zoneDP"),
            body.get("zoneName"),
            body.get("zone"),
            body.get("areaDp"),
            body.get("areaDP"),
            body.get("areaName"),
            body.get("projectZoneName")
        );

        if (direct != null && !isNumericLike(direct)) {
            return direct;
        }

        if (areaId == null) {
            return "-";
        }

        try {
            String sql = """
                SELECT area_name
                FROM ppavis_oneclickpro.oc_areas_all
                WHERE area_id = ?
                LIMIT 1
                """;

            String result = jdbcTemplate.queryForObject(sql, String.class, areaId);

            if (result != null && !result.trim().isEmpty()) {
                return result.trim();
            }
        } catch (Exception ignored) {
        }

        return String.valueOf(areaId);
    }

    private String resolveBuildingDisplay(Map<String, Object> body, Integer buildingId) {
        String direct = firstNonBlank(
            body.get("buildingDp"),
            body.get("buildingDP"),
            body.get("buildingName"),
            body.get("buildingLabel"),
            body.get("building")
        );

        if (direct != null && !isNumericLike(direct)) {
            return direct;
        }

        if (buildingId == null) {
            return "-";
        }

        try {
            String sql = """
                SELECT BUILDING_DP
                FROM oc_properties_locs_all
                WHERE LOCATION_ID = ?
                LIMIT 1
                """;

            String result = jdbcTemplate.queryForObject(sql, String.class, buildingId);

            if (result != null && !result.trim().isEmpty()) {
                return result.trim();
            }
        } catch (Exception ignored) {
        }

        return String.valueOf(buildingId);
    }

    private String resolveRoomDisplay(Map<String, Object> body, Integer officeId) {
        String direct = firstNonBlank(
            body.get("officeDp"),
            body.get("officeDP"),
            body.get("officeDisplay"),
            body.get("roomDisplay"),
            body.get("roomNo")
        );

        if (direct != null && !isNumericLike(direct)) {
            return direct;
        }

        if (officeId == null) {
            return "-";
        }

        try {
            String sql = """
                SELECT OFFICE_DP
                FROM oc_properties_locs_all
                WHERE LOCATION_ID = ?
                LIMIT 1
                """;

            String result = jdbcTemplate.queryForObject(sql, String.class, officeId);

            if (result != null && !result.trim().isEmpty()) {
                return result.trim();
            }
        } catch (Exception ignored) {
        }

        return String.valueOf(officeId);
    }

    private boolean isNumericLike(String value) {
        if (value == null) return false;
        return value.trim().matches("^\\d+$");
    }

    private String resolveTaxNumber(Map<String, Object> body) {
        String applicantType = firstNonBlank(
            body.get("applicantType"),
            body.get("customerType"),
            body.get("tenantType")
        );

        String documentType = firstNonBlank(
            body.get("documentType"),
            body.get("docType")
        );

        String applicantTypeLower = applicantType == null ? "" : applicantType.trim().toLowerCase();
        String documentTypeLower = documentType == null ? "" : documentType.trim().toLowerCase();

        String companyTax = firstNonBlank(
            body.get("taxId"),
            body.get("taxNumber"),
            body.get("tax_number"),
            body.get("organizationTaxId"),
            body.get("companyTaxId"),
            body.get("companyTaxNumber"),
            body.get("organizationTaxNumber"),
            body.get("businessTaxId"),
            body.get("businessTaxNumber")
        );

        String passportNo = firstNonBlank(
            body.get("passportNo"),
            body.get("passportNumber")
        );

        String citizenId = firstNonBlank(
            body.get("citizenId"),
            body.get("citizenID"),
            body.get("idCardNumber")
        );

        if (
            "company".equals(applicantTypeLower) ||
            "corporate".equals(applicantTypeLower) ||
            "juristic".equals(applicantTypeLower) ||
            "juristic_person".equals(applicantTypeLower) ||
            "นิติบุคคล".equals(applicantTypeLower)
        ) {
            return companyTax;
        }

        if (
            documentTypeLower.contains("passport") ||
            documentTypeLower.contains("พาสปอร์ต")
        ) {
            return passportNo;
        }

        if (
            documentTypeLower.contains("citizen") ||
            documentTypeLower.contains("citizenid") ||
            documentTypeLower.contains("id_card") ||
            documentTypeLower.contains("id card") ||
            documentTypeLower.contains("บัตรประชาชน")
        ) {
            return citizenId;
        }

        if (passportNo != null) {
            return passportNo;
        }

        if (citizenId != null) {
            return citizenId;
        }

        if (companyTax != null) {
            return companyTax;
        }

        return null;
    }

    private String resolveTaxNumberType(Map<String, Object> body) {
        String applicantType = firstNonBlank(
            body.get("applicantType"),
            body.get("customerType"),
            body.get("tenantType")
        );

        String documentType = firstNonBlank(
            body.get("documentType"),
            body.get("docType")
        );

        String organizationName = firstNonBlank(
            body.get("organizationName"),
            body.get("organization_name"),
            body.get("companyName"),
            body.get("bizNo"),
            body.get("businessName")
        );

        String companyTax = firstNonBlank(
            body.get("taxId"),
            body.get("taxNumber"),
            body.get("tax_number"),
            body.get("organizationTaxId"),
            body.get("companyTaxId"),
            body.get("companyTaxNumber"),
            body.get("organizationTaxNumber"),
            body.get("businessTaxId"),
            body.get("businessTaxNumber")
        );

        String passportNo = firstNonBlank(
            body.get("passportNo"),
            body.get("passportNumber")
        );

        String citizenId = firstNonBlank(
            body.get("citizenId"),
            body.get("citizenID"),
            body.get("idCardNumber")
        );

        String applicantTypeLower = applicantType == null ? "" : applicantType.trim().toLowerCase();
        String documentTypeLower = documentType == null ? "" : documentType.trim().toLowerCase();

        if (
            "company".equals(applicantTypeLower) ||
            "corporate".equals(applicantTypeLower) ||
            "juristic".equals(applicantTypeLower) ||
            "juristic_person".equals(applicantTypeLower) ||
            "นิติบุคคล".equals(applicantTypeLower)
        ) {
            return companyTax != null ? "COMPANY_TAX" : null;
        }

        if (organizationName != null && companyTax != null) {
            return "COMPANY_TAX";
        }

        if (
            documentTypeLower.contains("passport") ||
            documentTypeLower.contains("พาสปอร์ต")
        ) {
            return passportNo != null ? "PASSPORT" : null;
        }

        if (
            documentTypeLower.contains("citizen") ||
            documentTypeLower.contains("citizenid") ||
            documentTypeLower.contains("id_card") ||
            documentTypeLower.contains("id card") ||
            documentTypeLower.contains("บัตรประชาชน")
        ) {
            return citizenId != null ? "ID_CARD" : null;
        }

        if (passportNo != null) {
            return "PASSPORT";
        }

        if (citizenId != null) {
            return "ID_CARD";
        }

        if (companyTax != null) {
            return "COMPANY_TAX";
        }

        return null;
    }

    private static String toParkingReq(Object needParking) {
        if (needParking instanceof Boolean b) {
            return b ? "Y" : "N";
        }
        if (needParking != null) {
            String text = String.valueOf(needParking).trim().toLowerCase();
            if ("true".equals(text) || "y".equals(text) || "yes".equals(text) || "1".equals(text)) {
                return "Y";
            }
        }
        return "N";
    }

    private static Integer toCarType(Object parkingType) {
        if (parkingType == null) return null;
        return "car".equalsIgnoreCase(String.valueOf(parkingType).trim()) ? 1 : 0;
    }

    private static Integer toCarQty(Object parkingType, Object parkingCount) {
        if (!"car".equalsIgnoreCase(String.valueOf(parkingType).trim())) {
            return 0;
        }
        return toInt(parkingCount);
    }

    private static Integer toMotQty(Object parkingType, Object parkingCount) {
        if (!"motorcycle".equalsIgnoreCase(String.valueOf(parkingType).trim())) {
            return 0;
        }
        return toInt(parkingCount);
    }

    private static Integer toInt(Object value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static Integer toIntOrNull(Object value) {
        if (value == null) return null;
        try {
            String text = String.valueOf(value).trim();
            if (text.isEmpty()) return null;
            return Integer.parseInt(text);
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer toOccupants(Object value) {
        if (value == null) return null;

        String text = String.valueOf(value).trim();

        if ("1".equals(text)) return 1;
        if ("2".equals(text)) return 2;
        if ("MORE_THAN_2".equals(text)) return 3;

        try {
            return Integer.parseInt(text);
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeDate(Object value) {
        if (value == null) return null;

        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return null;

        try {
            return LocalDate.parse(text).toString();
        } catch (DateTimeParseException ignored) {
        }

        try {
            DateTimeFormatter thaiStyle = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(text, thaiStyle).toString();
        } catch (DateTimeParseException ignored) {
        }

        return text;
    }

    private static String[] extractVehicleRegs(Object raw) {
        String[] result = new String[]{null, null, null, null};

        if (raw instanceof List<?> list) {
            for (int i = 0; i < list.size() && i < 4; i++) {
                Object item = list.get(i);
                result[i] = item == null ? null : String.valueOf(item).trim();
            }
        }

        return result;
    }
}