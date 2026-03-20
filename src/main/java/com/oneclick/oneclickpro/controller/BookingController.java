package com.oneclick.oneclickpro.controller;

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
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/booking")
public class BookingController {

    private final JdbcTemplate jdbcTemplate;

    public BookingController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/save")
    public String saveBooking(@RequestBody Map<String, Object> body) {
        System.out.println("payload = " + body);

        String[] carRegs = extractVehicleRegs(body.get("vehicleRegistrations"));

        Integer resolvedBuildingId = resolveBuildingLocationId(body);
        Integer resolvedOfficeId = resolveLocationId(body);

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
                contp_prefix,
                contp_auth_name,
                contp_lasname,
                contp_email,
                contp_contact,
                number_of_occupants,
                add_on_detail,
                info_channel
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            body.get("contactDetail2"),
            body.get("email1"),
            body.get("email2"),
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
            body.get("contpPrefix"),
            body.get("contpAuthName"),
            body.get("contpLastname"),
            body.get("contpEmail"),
            body.get("contpContact"),
            toOccupants(body.get("occupantsCount")),
            body.get("extraDetails"),
            body.get("discoveryChannel")
        };

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int[] types = new int[]{
                Types.VARCHAR, // lease_commencement_date
                Types.VARCHAR, // lease_end_date
                Types.INTEGER, // duration_of_contract
                Types.VARCHAR, // process_type
                Types.INTEGER, // product_area
                Types.VARCHAR, // product_type
                Types.INTEGER, // building
                Types.INTEGER, // location_id
                Types.VARCHAR, // parkinglot_req
                Types.INTEGER, // car_type
                Types.INTEGER, // car_qty
                Types.INTEGER, // mot_qty
                Types.VARCHAR, // car_license_plate1
                Types.VARCHAR, // car_license_plate2
                Types.VARCHAR, // car_license_plate3
                Types.VARCHAR, // car_license_plate4
                Types.VARCHAR, // tax_number
                Types.VARCHAR, // tax_number_type
                Types.VARCHAR, // prefix
                Types.VARCHAR, // name
                Types.VARCHAR, // lasname
                Types.VARCHAR, // date_of_birth
                Types.VARCHAR, // contact_detail1
                Types.VARCHAR, // contact_detail2
                Types.VARCHAR, // email1
                Types.VARCHAR, // email2
                Types.VARCHAR, // line_id
                Types.VARCHAR, // province
                Types.VARCHAR, // district
                Types.VARCHAR, // subdist
                Types.VARCHAR, // postcode
                Types.VARCHAR, // address
                Types.VARCHAR, // moo
                Types.VARCHAR, // street
                Types.VARCHAR, // cur_province
                Types.VARCHAR, // cur_district
                Types.VARCHAR, // cur_subdist
                Types.VARCHAR, // cur_postcode
                Types.VARCHAR, // cur_address
                Types.VARCHAR, // cur_moo
                Types.VARCHAR, // cur_street
                Types.VARCHAR, // organization_name
                Types.VARCHAR, // contp_prefix
                Types.VARCHAR, // contp_auth_name
                Types.VARCHAR, // contp_lasname
                Types.VARCHAR, // contp_email
                Types.VARCHAR, // contp_contact
                Types.INTEGER, // number_of_occupants
                Types.VARCHAR, // add_on_detail
                Types.VARCHAR  // info_channel
            };

            PreparedStatementCreatorFactory pscFactory =
                new PreparedStatementCreatorFactory(sql, types);

            pscFactory.setReturnGeneratedKeys(true);

            PreparedStatementCreator psc =
                pscFactory.newPreparedStatementCreator(Arrays.asList(params));

            jdbcTemplate.update(psc, keyHolder);

            Number generatedId = keyHolder.getKey();
            return generatedId != null ? "saved:" + generatedId.longValue() : "saved";
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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
                CONCAT(COALESCE(prefix, ''),
                       CASE WHEN prefix IS NOT NULL AND prefix <> '' THEN ' ' ELSE '' END,
                       COALESCE(name, ''),
                       CASE WHEN name IS NOT NULL AND name <> '' THEN ' ' ELSE '' END,
                       COALESCE(lasname, '')
                ) AS fullName,
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

    private Integer resolveBuildingLocationId(Map<String, Object> body) {
        return toIntOrNull(firstNonBlank(
            body.get("buildingId"),
            body.get("building_id")
        ));
    }

    private Integer resolveLocationId(Map<String, Object> body) {
        return toIntOrNull(firstNonBlank(
            body.get("officeId"),
            body.get("office_id")
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