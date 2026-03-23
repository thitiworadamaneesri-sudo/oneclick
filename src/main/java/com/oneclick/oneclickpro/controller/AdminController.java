package com.oneclick.oneclickpro.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

            boolean isCorporate = isCorporateGuest(guest);
            String resolvedBillingType = resolveBillingAddressType(guest, isCorporate);

            Map<String, Object> data = new LinkedHashMap<>();

            // ============================
            // Personal / Common
            // ============================
            data.put("guestTransId", guestTransId);

            data.put("documentType", normalizeDocumentType(firstNonBlank(
                guest, "tax_number_type", "document_type", "doc_type"
            )));

            data.put("idCardNumber", firstNonBlank(guest,
                "citizen_id", "id_card_number", "CITIZEN_ID"
            ));

            data.put("passportNumber", firstNonBlank(guest,
                "passport_no", "passport_number"
            ));

            data.put("taxNumber", firstNonBlank(guest,
                "tax_number", "company_tax_id", "organization_tax_id"
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
            // ID card / certificate address
            // ============================
            data.put("idCardHouseNo", firstNonBlank(guest, "address"));
            data.put("idCardVillageNo", firstNonBlank(guest, "moo"));
            data.put("idCardRoad", firstNonBlank(guest, "street"));
            data.put("idCardProvince", firstNonBlank(guest, "province"));
            data.put("idCardDistrict", firstNonBlank(guest, "district"));
            data.put("idCardSubdistrict", firstNonBlank(guest, "subdist"));
            data.put("idCardPostalCode", firstNonBlank(guest, "postcode"));

            // ============================
            // Contact address
            // ============================
            data.put("sameAsIdCardAddress", normalizeYesNoByAddress(guest));
            data.put("contactHouseNo", firstNonBlank(guest, "cur_address"));
            data.put("contactVillageNo", firstNonBlank(guest, "cur_moo"));
            data.put("contactRoad", firstNonBlank(guest, "cur_street"));
            data.put("contactProvince", firstNonBlank(guest, "cur_province"));
            data.put("contactDistrict", firstNonBlank(guest, "cur_district"));
            data.put("contactSubdistrict", firstNonBlank(guest, "cur_subdist"));
            data.put("contactPostalCode", firstNonBlank(guest, "cur_postcode"));

            // ============================
            // Billing address
            // ============================
            data.put("billingAddressType", resolvedBillingType);
            populateBillingAddress(data, guest, resolvedBillingType);

            // ============================
            // Booking / contract
            // ============================
            data.put("processType", normalizeProcessType(firstNonBlank(
                guest, "process_type", "transaction_type"
            )));

            data.put("startDate", firstNonBlank(
                guest, "lease_commencement_date", "start_date"
            ));

            data.put("endDate", firstNonBlank(
                guest, "lease_end_date", "end_date"
            ));

            data.put("zone", zoneName);

            data.put("propertyType", firstNonBlank(
                guest, "product_type", "property_type"
            ));

            data.put("building", firstNonBlank(
                locationInfo, "buildingName", "building_name"
            ));

            data.put("floor", firstNonBlank(
                locationInfo, "floorName", "floor_name"
            ));

            data.put("roomNo", firstNonBlank(
                locationInfo, "officeName", "office_name"
            ));

            data.put("officeId", officeId);

            data.put("contractMonths", firstNonBlank(
                guest, "duration_of_contract"
            ));

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
            // Company / Signers / Contact person
            // ============================
            data.put("applicantType", firstNonBlank(
                guest, "applicant_type", "applicantType", "customer_type", "customerType", "tenant_type", "tenantType"
            ));
            data.put("customerType", firstNonBlank(
                guest, "customer_type", "customerType"
            ));
            data.put("tenantType", firstNonBlank(
                guest, "tenant_type", "tenantType"
            ));

            data.put("businessType", firstNonBlank(
                guest, "business_detail"
            ));
            data.put("companyName", firstNonBlank(
                guest, "organization_name", "organizationName", "company_name", "companyName"
            ));
            data.put("companyTaxId", firstNonBlank(
                guest, "company_tax_id", "organization_tax_id", "tax_number"
            ));

            data.put("contactPersonTitle", firstNonBlank(
                guest, "contp_prefix"
            ));
            data.put("contactPersonFirstName", firstNonBlank(
                guest, "contp_auth_name", "contp_name"
            ));
            data.put("contactPersonLastName", firstNonBlank(
                guest, "contp_auth_lastname", "contp_lastname", "contp_lasname"
            ));
            data.put("contactPersonEmail", firstNonBlank(
                guest, "contp_email"
            ));
            data.put("contactPersonPhone", firstNonBlank(
                guest, "contp_contact"
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

    private void populateBillingAddress(
        Map<String, Object> data,
        Map<String, Object> guest,
        String billingType
    ) {
        String normalized = trimSafe(billingType).toLowerCase();

        if ("contact".equals(normalized) || normalized.contains("contact")) {
            data.put("billingHouseNo", firstNonBlank(guest, "cur_address"));
            data.put("billingVillageNo", firstNonBlank(guest, "cur_moo"));
            data.put("billingRoad", firstNonBlank(guest, "cur_street"));
            data.put("billingProvince", firstNonBlank(guest, "cur_province"));
            data.put("billingDistrict", firstNonBlank(guest, "cur_district"));
            data.put("billingSubdistrict", firstNonBlank(guest, "cur_subdist"));
            data.put("billingPostalCode", firstNonBlank(guest, "cur_postcode"));
            return;
        }

        if ("signer1".equals(normalized) || normalized.contains("authorized director #1")) {
            data.put("billingHouseNo", firstNonBlank(guest, "address"));
            data.put("billingVillageNo", firstNonBlank(guest, "moo"));
            data.put("billingRoad", firstNonBlank(guest, "street"));
            data.put("billingProvince", firstNonBlank(guest, "province"));
            data.put("billingDistrict", firstNonBlank(guest, "district"));
            data.put("billingSubdistrict", firstNonBlank(guest, "subdist"));
            data.put("billingPostalCode", firstNonBlank(guest, "postcode"));
            return;
        }

        if ("other".equals(normalized) || normalized.contains("other")) {
            data.put("billingHouseNo", firstNonBlank(guest, "billing_address", "billing_house_no"));
            data.put("billingVillageNo", firstNonBlank(guest, "billing_moo", "billing_village_no"));
            data.put("billingRoad", firstNonBlank(guest, "billing_street", "billing_road"));
            data.put("billingProvince", firstNonBlank(guest, "billing_province"));
            data.put("billingDistrict", firstNonBlank(guest, "billing_district"));
            data.put("billingSubdistrict", firstNonBlank(guest, "billing_subdist", "billing_subdistrict"));
            data.put("billingPostalCode", firstNonBlank(guest, "billing_postcode", "billing_postal_code"));
            return;
        }

        data.put("billingHouseNo", firstNonBlank(guest, "address"));
        data.put("billingVillageNo", firstNonBlank(guest, "moo"));
        data.put("billingRoad", firstNonBlank(guest, "street"));
        data.put("billingProvince", firstNonBlank(guest, "province"));
        data.put("billingDistrict", firstNonBlank(guest, "district"));
        data.put("billingSubdistrict", firstNonBlank(guest, "subdist"));
        data.put("billingPostalCode", firstNonBlank(guest, "postcode"));
    }

    private String resolveBillingAddressType(Map<String, Object> guest, boolean isCorporate) {
        String rawType = firstNonBlank(
            guest,
            "billing_source",
            "billingSource",
            "billing_address_type",
            "billingAddressType"
        );

        String normalized = trimSafe(rawType).toLowerCase();

        if ("registered".equals(normalized)) {
            return "registered";
        }
        if ("contact".equals(normalized)) {
            return "contact";
        }
        if ("signer1".equals(normalized)) {
            return "signer1";
        }
        if ("other".equals(normalized)) {
            return "other";
        }

        return isCorporate ? "signer1" : "registered";
    }

    private boolean isCorporateGuest(Map<String, Object> guest) {
        String applicantType = firstNonBlank(
            guest,
            "applicant_type", "applicantType",
            "customer_type", "customerType",
            "tenant_type", "tenantType"
        ).toLowerCase();

        String taxNumberType = firstNonBlank(
            guest,
            "tax_number_type", "document_type", "doc_type"
        ).toLowerCase();

        return "company".equals(applicantType)
            || "corporate".equals(applicantType)
            || "juristic".equals(applicantType)
            || "juristic_person".equals(applicantType)
            || "นิติบุคคล".equals(applicantType)
            || "company_tax".equals(taxNumberType)
            || hasRealValue(guest.get("organization_name"))
            || hasRealValue(guest.get("company_name"))
            || hasRealValue(guest.get("auth_name1"))
            || hasRealValue(guest.get("auth_firstname1"));
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

            return result;
        } catch (Exception e) {
            return result;
        }
    }

    private String findAreaNameByAreaId(String areaId) {
        if (isBlank(areaId)) return "";

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT area_name
                FROM oc_areas_all
                WHERE area_id = ?
            """, areaId);

            if (rows.isEmpty()) return areaId;

            return str(rows.get(0).get("area_name"));
        } catch (Exception e) {
            return areaId;
        }
    }

    private List<Map<String, Object>> extractVehicles(Map<String, Object> guest) {
        List<Map<String, Object>> list = new ArrayList<>();

        addVehicle(list, "รถยนต์ (Car)", guest.get("car_license_plate1"));
        addVehicle(list, "รถยนต์ (Car)", guest.get("car_license_plate2"));
        addVehicle(list, "รถยนต์ (Car)", guest.get("car_license_plate3"));
        addVehicle(list, "รถยนต์ (Car)", guest.get("car_license_plate4"));

        addVehicle(list, "จักรยานยนต์ (Motorcycle)", guest.get("mot_license_plate1"));
        addVehicle(list, "จักรยานยนต์ (Motorcycle)", guest.get("mot_license_plate2"));
        addVehicle(list, "จักรยานยนต์ (Motorcycle)", guest.get("mot_license_plate3"));
        addVehicle(list, "จักรยานยนต์ (Motorcycle)", guest.get("mot_license_plate4"));

        return list;
    }

    private void addVehicle(List<Map<String, Object>> list, String type, Object reg) {
        if (hasRealValue(reg)) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("type", type);
            v.put("registration", String.valueOf(reg).trim());
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
            String name = firstNonBlank(guest, "auth_name" + i, "auth_firstname" + i);
            String lastname = firstNonBlank(guest, "auth_lastname" + i, "auth_lasname" + i);
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
        if ("COMPANY_TAX".equals(v)) return "เลขประจำตัวผู้เสียภาษี / Company Tax ID";
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
        if (map == null || keys == null) return "";
        for (String key : keys) {
            Object v = map.get(key);
            if (hasRealValue(v)) {
                return String.valueOf(v).trim();
            }
        }
        return "";
    }

    private Object firstNonNull(Object... values) {
        if (values == null) return null;
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

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}