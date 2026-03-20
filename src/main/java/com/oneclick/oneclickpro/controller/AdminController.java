package com.oneclick.oneclickpro.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin")
public class AdminController {

    private final JdbcTemplate jdbcTemplate;

    public AdminController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/estimate/detail/{guestTransId}")
    public Map<String, Object> getEstimateDetail(@PathVariable Long guestTransId) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Map<String, Object> guest = findGuestTransById(guestTransId);

            if (guest == null || guest.isEmpty()) {
                response.put("success", false);
                response.put("message", "ไม่พบข้อมูล");
                response.put("guestTransId", guestTransId);
                return response;
            }

            Object officeId = firstNonNull(
                guest.get("office_id"),
                guest.get("OFFICE_ID"),
                guest.get("location_id"),
                guest.get("LOCATION_ID")
            );

            Map<String, Object> locationInfo = findLocationInfoByOfficeId(officeId);
            String zoneName = findAreaNameByAreaId(firstNonBlank(guest, "product_area"));

            List<Map<String, Object>> vehicles = extractVehicles(guest);
            List<Map<String, Object>> signers = extractSigners(guest);

            Map<String, Object> data = new LinkedHashMap<>();

            // ============================
            // Personal
            // ============================
            data.put("guestTransId", guestTransId);

            data.put("documentType", normalizeDocumentType(firstNonBlank(
                guest, "tax_number_type", "document_type", "doc_type"
            )));

            data.put("idCardNumber", firstNonBlank(guest,
                "tax_number", "citizen_id", "id_card_number", "CITIZEN_ID"
            ));

            data.put("passportNumber", firstNonBlank(guest,
                "passport_no", "passport_number"
            ));

            data.put("taxNumber", firstNonBlank(guest,
                "company_tax_id", "organization_tax_id"
            ));

            data.put("title", firstNonBlank(guest,
                "prefix", "title"
            ));

            data.put("firstName", firstNonBlank(guest,
                "name", "first_name", "FIRST_NAME"
            ));

            data.put("lastName", firstNonBlank(guest,
                "lastname", "last_name", "LAST_NAME"
            ));

            data.put("dateOfBirth", firstNonBlank(guest,
                "date_of_birth", "birth_date", "dob"
            ));

            data.put("email", firstNonBlank(guest,
                "email1", "email", "EMAIL"
            ));

            data.put("alternateEmail", firstNonBlank(guest,
                "email2", "alt_email", "alternate_email"
            ));

            data.put("phoneNumber", firstNonBlank(guest,
                "contact_detail1", "phone", "PHONE_NUMBER"
            ));

            data.put("alternatePhoneNumber", firstNonBlank(guest,
                "contact_detail2", "alt_phone", "alternate_phone"
            ));

            data.put("occupation", firstNonBlank(guest,
                "occupation"
            ));

            data.put("lineId", firstNonBlank(guest,
                "line_id", "lineid"
            ));

            // ============================
            // Address on ID Card
            // ============================
            data.put("idCardHouseNo", firstNonBlank(guest,
                "address", "reg_houseNo", "house_no"
            ));
            data.put("idCardVillageNo", firstNonBlank(guest,
                "moo", "reg_moo"
            ));
            data.put("idCardRoad", firstNonBlank(guest,
                "street", "reg_road"
            ));
            data.put("idCardProvince", firstNonBlank(guest,
                "province", "reg_province"
            ));
            data.put("idCardSubdistrict", firstNonBlank(guest,
                "subdist", "reg_subdistrict"
            ));
            data.put("idCardDistrict", firstNonBlank(guest,
                "district", "reg_district"
            ));
            data.put("idCardPostalCode", firstNonBlank(guest,
                "postcode", "reg_zip"
            ));

            // ============================
            // Contact Address
            // ============================
            data.put("sameAsIdCardAddress", normalizeYesNoByAddress(guest));

            data.put("contactHouseNo", firstNonBlank(guest,
                "cur_address", "con_houseNo"
            ));
            data.put("contactVillageNo", firstNonBlank(guest,
                "cur_moo", "con_moo"
            ));
            data.put("contactRoad", firstNonBlank(guest,
                "cur_street", "con_road"
            ));
            data.put("contactProvince", firstNonBlank(guest,
                "cur_province", "con_province"
            ));
            data.put("contactSubdistrict", firstNonBlank(guest,
                "cur_subdist", "con_subdistrict"
            ));
            data.put("contactDistrict", firstNonBlank(guest,
                "cur_district", "con_district"
            ));
            data.put("contactPostalCode", firstNonBlank(guest,
                "cur_postcode", "con_zip"
            ));

            // ============================
            // Billing Address
            // ============================
            data.put("billingAddressType", "ที่อยู่ตามบัตรประชาชน / Address on ID Card");
            data.put("billingHouseNo", firstNonBlank(guest, "address"));
            data.put("billingVillageNo", firstNonBlank(guest, "moo"));
            data.put("billingRoad", firstNonBlank(guest, "street"));
            data.put("billingProvince", firstNonBlank(guest, "province"));
            data.put("billingSubdistrict", firstNonBlank(guest, "subdist"));
            data.put("billingDistrict", firstNonBlank(guest, "district"));
            data.put("billingPostalCode", firstNonBlank(guest, "postcode"));

            // ============================
            // Property / Booking
            // ============================
            data.put("propertyType", firstNonBlank(guest,
                "product_type", "PROPERTY_TYPE"
            ));

            data.put("building", firstNonBlank(locationInfo,
                "buildingName"
            ));

            data.put("floor", firstNonBlank(locationInfo,
                "floorName"
            ));

            data.put("roomNo", firstNonBlank(locationInfo,
                "officeName"
            ));

            data.put("officeId", firstNonBlank(guest,
                "location_id"
            ));

            data.put("startDate", firstNonBlank(guest,
                "lease_commencement_date"
            ));

            data.put("endDate", firstNonBlank(guest,
                "lease_end_date"
            ));

            data.put("contractMonths", firstNonBlank(guest,
                "duration_of_contract"
            ));

            data.put("processType", normalizeProcessType(firstNonBlank(
                guest, "process_type", "transaction_type"
            )));

            data.put("zone", zoneName);

            data.put("discoveryChannel", firstNonBlank(
                guest, "info_channel", "discovery_channel"
            ));

            data.put("additionalDetails", firstNonBlank(
                guest, "add_on_detail", "remark", "remarks", "note"
            ));

            data.put("numberOfOccupants", firstNonBlank(
                guest, "number_of_occupants"
            ));

            // ============================
            // Vehicle
            // ============================
            data.put("parkingRequirement", normalizeParkingRequirement(firstNonBlank(
                guest, "parkinglot_req"
            )));
            data.put("vehicleType", normalizeVehicleType(guest));
            data.put("numberOfVehicles", calculateVehicleCount(guest));
            data.put("vehicleRegistration", buildVehicleRegistrationText(guest));
            data.put("vehicles", vehicles);

            // ============================
            // Company / Signers
            // ============================
            data.put("businessType", firstNonBlank(
                guest, "business_detail"
            ));
            data.put("companyName", firstNonBlank(
                guest, "organization_name"
            ));

            data.put("signerCount", signers.size());
            data.put("signers", signers);

            data.put("raw", guest);

            response.put("success", true);
            response.put("data", data);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    private Map<String, Object> findGuestTransById(Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM oc_guest_trans_all WHERE guest_trans_id = ?", id
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Map<String, Object> findLocationInfoByOfficeId(Object officeId) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (officeId == null || String.valueOf(officeId).trim().isEmpty()) {
            return result;
        }

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT LOCATION_CODE, LOCATION_ALIAS, LOCATION_TYPE_LOOKUP_CODE
                FROM oc_properties_locs_all
                WHERE location_id = ?
            """, officeId);

            if (rows.isEmpty()) {
                return result;
            }

            Map<String, Object> office = rows.get(0);

            String locationCode = str(office.get("LOCATION_CODE"));
            String alias = str(office.get("LOCATION_ALIAS"));

            // ตัวอย่าง LOCATION_CODE = CYA032-2F-2205
            if (!isBlank(locationCode)) {
                String[] parts = locationCode.split("-");

                if (parts.length >= 3) {
                    result.put("buildingName", parts[0]);
                    result.put("floorName", parts[1]);
                    result.put("officeName", parts[2]);
                }
            }

            if (!result.containsKey("officeName") || isBlank(str(result.get("officeName")))) {
                result.put("officeName", alias);
            }

            if (!result.containsKey("buildingName")) {
                result.put("buildingName", "");
            }

            if (!result.containsKey("floorName")) {
                result.put("floorName", "");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String findAreaNameByAreaId(String areaId) {
        if (isBlank(areaId)) return "";

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT area_name
                FROM oc_areas_all
                WHERE area_id = ?
            """, areaId);

            if (rows.isEmpty()) {
                return "";
            }

            return firstNonBlank(rows.get(0), "area_name", "AREA_NAME");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private List<Map<String, Object>> extractVehicles(Map<String, Object> guest) {
        List<Map<String, Object>> list = new ArrayList<>();

        addVehicle(list, "รถยนต์", guest.get("car_license_plate1"));
        addVehicle(list, "รถยนต์", guest.get("car_license_plate2"));
        addVehicle(list, "รถยนต์", guest.get("car_license_plate3"));
        addVehicle(list, "รถยนต์", guest.get("car_license_plate4"));

        addVehicle(list, "จักรยานยนต์", guest.get("mot_license_plate1"));
        addVehicle(list, "จักรยานยนต์", guest.get("mot_license_plate2"));
        addVehicle(list, "จักรยานยนต์", guest.get("mot_license_plate3"));
        addVehicle(list, "จักรยานยนต์", guest.get("mot_license_plate4"));

        return list;
    }

    private void addVehicle(List<Map<String, Object>> list, String type, Object registrationValue) {
        String reg = str(registrationValue);
        if (!isBlank(reg) && !"null".equalsIgnoreCase(reg)) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("type", type);
            v.put("registration", reg);
            list.add(v);
        }
    }

    private int calculateVehicleCount(Map<String, Object> guest) {
        int carQty = toInt(firstNonBlank(guest, "car_qty"));
        int motQty = toInt(firstNonBlank(guest, "mot_qty"));

        if (carQty > 0 || motQty > 0) {
            return carQty + motQty;
        }

        int count = 0;
        if (hasRealValue(guest.get("car_license_plate1"))) count++;
        if (hasRealValue(guest.get("car_license_plate2"))) count++;
        if (hasRealValue(guest.get("car_license_plate3"))) count++;
        if (hasRealValue(guest.get("car_license_plate4"))) count++;
        if (hasRealValue(guest.get("mot_license_plate1"))) count++;
        if (hasRealValue(guest.get("mot_license_plate2"))) count++;
        if (hasRealValue(guest.get("mot_license_plate3"))) count++;
        if (hasRealValue(guest.get("mot_license_plate4"))) count++;
        return count;
    }

    private String buildVehicleRegistrationText(Map<String, Object> guest) {
        List<String> regs = new ArrayList<>();

        addIfReal(regs, guest.get("car_license_plate1"));
        addIfReal(regs, guest.get("car_license_plate2"));
        addIfReal(regs, guest.get("car_license_plate3"));
        addIfReal(regs, guest.get("car_license_plate4"));
        addIfReal(regs, guest.get("mot_license_plate1"));
        addIfReal(regs, guest.get("mot_license_plate2"));
        addIfReal(regs, guest.get("mot_license_plate3"));
        addIfReal(regs, guest.get("mot_license_plate4"));

        return regs.isEmpty() ? "" : String.join(", ", regs);
    }

    private String normalizeVehicleType(Map<String, Object> guest) {
        int carQty = toInt(firstNonBlank(guest, "car_qty"));
        int motQty = toInt(firstNonBlank(guest, "mot_qty"));

        if (carQty > 0 && motQty > 0) return "รถยนต์และจักรยานยนต์ (Car & Motorcycle)";
        if (carQty > 0) return "รถยนต์ (Car)";
        if (motQty > 0) return "จักรยานยนต์ (Motorcycle)";

        boolean hasCar = hasRealValue(guest.get("car_license_plate1"))
            || hasRealValue(guest.get("car_license_plate2"))
            || hasRealValue(guest.get("car_license_plate3"))
            || hasRealValue(guest.get("car_license_plate4"));

        boolean hasMot = hasRealValue(guest.get("mot_license_plate1"))
            || hasRealValue(guest.get("mot_license_plate2"))
            || hasRealValue(guest.get("mot_license_plate3"))
            || hasRealValue(guest.get("mot_license_plate4"));

        if (hasCar && hasMot) return "รถยนต์และจักรยานยนต์ (Car & Motorcycle)";
        if (hasCar) return "รถยนต์ (Car)";
        if (hasMot) return "จักรยานยนต์ (Motorcycle)";

        return "";
    }

    private List<Map<String, Object>> extractSigners(Map<String, Object> guest) {
        List<Map<String, Object>> signers = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            String prefix = firstNonBlank(guest, "auth_prefix" + i);
            String name = firstNonBlank(guest, "auth_name" + i);
            String lastname = firstNonBlank(guest, "auth_lastname" + i);
            String email = firstNonBlank(guest, "auth_email" + i);
            String phone = firstNonBlank(guest, "auth_contact" + i);

            if (isBlank(prefix) && isBlank(name) && isBlank(lastname) && isBlank(email) && isBlank(phone)) {
                continue;
            }

            Map<String, Object> signer = new LinkedHashMap<>();
            signer.put("title", prefix);
            signer.put("firstName", name);
            signer.put("lastName", lastname);
            signer.put("email", email);
            signer.put("phone", phone);
            signer.put("signMode", "");
            signer.put("position", "");
            signers.add(signer);
        }

        String contpName = firstNonBlank(guest, "contp_auth_name");
        String contpLastname = firstNonBlank(guest, "contp_auth_lastname");
        String contpEmail = firstNonBlank(guest, "contp_email");
        String contpPhone = firstNonBlank(guest, "contp_contact");
        String contpPrefix = firstNonBlank(guest, "contp_prefix");

        if (!isBlank(contpPrefix) || !isBlank(contpName) || !isBlank(contpLastname) || !isBlank(contpEmail) || !isBlank(contpPhone)) {
            Map<String, Object> signer = new LinkedHashMap<>();
            signer.put("title", contpPrefix);
            signer.put("firstName", contpName);
            signer.put("lastName", contpLastname);
            signer.put("email", contpEmail);
            signer.put("phone", contpPhone);
            signer.put("signMode", "");
            signer.put("position", "ผู้ติดต่อ");
            signers.add(signer);
        }

        return signers;
    }

    private String normalizeProcessType(String value) {
        if (isBlank(value)) return "";
        String v = value.toLowerCase();

        if (v.contains("booking") || v.contains("reserve") || v.contains("จอง")) return "จอง";
        if (v.contains("contract") || v.contains("ทำสัญญา")) return "ทำสัญญา";

        return value;
    }

    private String normalizeDocumentType(String value) {
        if (isBlank(value)) return "";
        String v = value.trim().toUpperCase();

        if ("ID_CARD".equals(v)) return "บัตรประชาชน / ID Card";
        if ("PASSPORT".equals(v)) return "หนังสือเดินทาง / Passport";
        return value;
    }

    private String normalizeParkingRequirement(String value) {
        if (isBlank(value)) return "";
        String v = value.trim().toUpperCase();

        if ("Y".equals(v) || "YES".equals(v)) return "ต้องการ (Required)";
        if ("N".equals(v) || "NO".equals(v)) return "ไม่ต้องการ (Not Required)";
        return value;
    }

    private String normalizeYesNoByAddress(Map<String, Object> guest) {
        String addr1 = firstNonBlank(guest, "address");
        String moo1 = firstNonBlank(guest, "moo");
        String street1 = firstNonBlank(guest, "street");
        String province1 = firstNonBlank(guest, "province");
        String district1 = firstNonBlank(guest, "district");
        String subdist1 = firstNonBlank(guest, "subdist");
        String postcode1 = firstNonBlank(guest, "postcode");

        String addr2 = firstNonBlank(guest, "cur_address");
        String moo2 = firstNonBlank(guest, "cur_moo");
        String street2 = firstNonBlank(guest, "cur_street");
        String province2 = firstNonBlank(guest, "cur_province");
        String district2 = firstNonBlank(guest, "cur_district");
        String subdist2 = firstNonBlank(guest, "cur_subdist");
        String postcode2 = firstNonBlank(guest, "cur_postcode");

        boolean same =
            Objects.equals(trimSafe(addr1), trimSafe(addr2)) &&
            Objects.equals(trimSafe(moo1), trimSafe(moo2)) &&
            Objects.equals(trimSafe(street1), trimSafe(street2)) &&
            Objects.equals(trimSafe(province1), trimSafe(province2)) &&
            Objects.equals(trimSafe(district1), trimSafe(district2)) &&
            Objects.equals(trimSafe(subdist1), trimSafe(subdist2)) &&
            Objects.equals(trimSafe(postcode1), trimSafe(postcode2));

        return same ? "ใช่ / Yes" : "ไม่ใช่ / No";
    }

    private String firstNonBlank(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (hasRealValue(v)) {
                return String.valueOf(v).trim();
            }
        }
        return "";
    }

    private Object firstNonNull(Object... values) {
        for (Object v : values) {
            if (v != null && !String.valueOf(v).trim().isEmpty()) return v;
        }
        return null;
    }

    private int toInt(String value) {
        try {
            if (isBlank(value)) return 0;
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void addIfReal(List<String> list, Object value) {
        if (hasRealValue(value)) {
            list.add(String.valueOf(value).trim());
        }
    }

    private boolean hasRealValue(Object value) {
        if (value == null) return false;
        String s = String.valueOf(value).trim();
        return !s.isEmpty() && !"null".equalsIgnoreCase(s);
    }

    private String trimSafe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}