package com.vm.GWConnector.mapper;

import com.vm.GWConnector.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Component
public class DraftClaimMapper {
    private static final String REPORTER_REF_ID = "reporterId";

    public GWDraftClaimRequest mapToGWRequest(DraftClaimRequestDTO request, PolicyResponse policy) {
        return mapToGWRequest(request, policy, false);
    }

    public GWDraftClaimRequest mapToUKGWRequest(DraftClaimRequestDTO request, PolicyResponse policy) {
        return mapToGWRequest(request, policy, true);
    }

    private GWDraftClaimRequest mapToGWRequest(
            DraftClaimRequestDTO request, PolicyResponse policy, boolean ukRequest) {
        GWDraftClaimRequest.Attributes attributes = new GWDraftClaimRequest.Attributes();
        attributes.setPolicyNumber(policy.getPolicyNumber());
        attributes.setDescription(firstNonBlank(
                request.getDescription(), "Draft claim for policy " + policy.getPolicyNumber()));
        attributes.setLossDate(normalizeLossDate(request.getLossDate()));
        GWCodeName lossCause = mapLossCause(request.getLossCause());
        if (lossCause != null) {
            attributes.setLossCause(lossCause);
        }
        attributes.setFaultRating(code(firstNonBlank(request.getFaultRatingCode(), "1")));
        attributes.setReportedByType(code(firstNonBlank(request.getReportedByTypeCode(), "other")));
        GWDraftClaimRequest.LossLocation lossLocation = mapLossLocation(
                request.getLossLocation(),
                policy.getPolicyAddress(),
                ukRequest ? "GB" : "US",
                !ukRequest);
        if (lossLocation != null) {
            attributes.setLossLocation(lossLocation);
        }
        GWDraftClaimRequest.Reference reporter = new GWDraftClaimRequest.Reference();
        reporter.setRefid(REPORTER_REF_ID);
        attributes.setReporter(reporter);

        GWDraftClaimRequest.DataNode data = new GWDraftClaimRequest.DataNode();
        data.setAttributes(attributes);
        GWDraftClaimRequest result = new GWDraftClaimRequest();
        result.setData(data);
        result.setIncluded(included(policy.getInsuredName()));
        return result;
    }

    public DraftClaimResponseDTO mapToResponse(GWDraftClaimResponse response) {
        if (response == null || response.getData() == null || response.getData().getAttributes() == null) return null;
        GWDraftClaimResponse.Attributes attributes = response.getData().getAttributes();
        DraftClaimResponseDTO result = new DraftClaimResponseDTO();
        result.setClaimId(attributes.getId());
        result.setClaimNumber(attributes.getClaimNumber());
        result.setPolicyNumber(attributes.getPolicyNumber());
        result.setClaimStatus(attributes.getClaimStatus() != null ? attributes.getClaimStatus() : attributes.getState() != null ? attributes.getState().getName() : null);
        result.setAssignmentStatus(attributes.getAssignmentStatus() != null ? attributes.getAssignmentStatus().getName() : null);
        result.setDescription(attributes.getDescription());
        result.setLossDate(attributes.getLossDate());
        result.setLossLocation(attributes.getLossLocation());
        return result;
    }

    public DraftClaimResponseDTO mapToResponse(GWClaimResponse response) {
        if (response == null) return null;
        DraftClaimResponseDTO result = new DraftClaimResponseDTO();
        result.setClaimId(response.getId());
        result.setClaimNumber(response.getClaimNumber());
        result.setPolicyNumber(response.getPolicyNumber());
        result.setClaimStatus(response.getState() != null ? response.getState().getName() : null);
        result.setAssignmentStatus(response.getAssignmentStatus() != null ? response.getAssignmentStatus().getName() : null);
        result.setDescription(response.getDescription());
        result.setLossDate(response.getLossDate());
        result.setLossLocation(mapLossLocation(response.getLossLocation()));
        return result;
    }

    private GWDraftClaimRequest.LossLocation mapLossLocation(DraftClaimRequestDTO.LossLocationDTO source, String policyAddress) {
        PolicyAddressParts policyParts = parsePolicyAddress(policyAddress);
        GWDraftClaimRequest.LossLocation result = new GWDraftClaimRequest.LossLocation();

        String addressLine1 = firstNonBlank(source != null ? source.getAddressLine1() : null, policyParts.addressLine1);
        String city = firstNonBlank(source != null ? source.getCity() : null, policyParts.city);
        String postalCode = firstNonBlank(source != null ? source.getPostalCode() : null, policyParts.postalCode);
        String country = normalizeCountryCode(firstNonBlank(source != null ? source.getCountry() : null, policyParts.country));
        String stateCode = normalizeStateCode(firstNonBlank(source != null ? source.getStateCode() : null, policyParts.stateCode));

        if (isBlank(addressLine1) && isBlank(city) && isBlank(postalCode) && isBlank(country) && isBlank(stateCode)) {
            return null;
        }

        if (isBlank(country)) {
            country = "US";
        }
        if (isBlank(stateCode)) {
            stateCode = policyParts.stateCode;
        }

        result.setAddressLine1(addressLine1);
        result.setCity(city);
        result.setCountry(country);
        result.setPostalCode(postalCode);
        result.setPolicyLabel(source != null ? source.getPolicyLabel() : null);
        if (!isBlank(stateCode)) {
            GWCodeName state = code(stateCode);
            state.setName(source != null ? source.getStateName() : null);
            result.setState(state);
        }
        return result;
    }

    private GWDraftClaimRequest.LossLocation mapLossLocation(
            DraftClaimRequestDTO.LossLocationDTO source,
            String policyAddress,
            String defaultCountry,
            boolean includeState) {
        PolicyAddressParts policyParts = parsePolicyAddress(policyAddress);
        GWDraftClaimRequest.LossLocation result = new GWDraftClaimRequest.LossLocation();

        String addressLine1 = firstNonBlank(source != null ? source.getAddressLine1() : null, policyParts.addressLine1);
        String city = firstNonBlank(source != null ? source.getCity() : null, policyParts.city);
        String postalCode = firstNonBlank(source != null ? source.getPostalCode() : null, policyParts.postalCode);
        String country = normalizeCountryCode(source != null ? source.getCountry() : null);
        if (isBlank(country)) {
            country = includeState ? normalizeCountryCode(policyParts.country) : defaultCountry;
        }
        String stateCode = normalizeStateCode(firstNonBlank(source != null ? source.getStateCode() : null, policyParts.stateCode));

        if (isBlank(addressLine1) && isBlank(city) && isBlank(postalCode) && isBlank(country) && isBlank(stateCode)) {
            return null;
        }

        if (isBlank(country)) {
            country = defaultCountry;
        }
        if (isBlank(stateCode)) {
            stateCode = policyParts.stateCode;
        }

        result.setAddressLine1(addressLine1);
        result.setCity(city);
        result.setCountry(country);
        result.setPostalCode(postalCode);
        result.setPolicyLabel(firstNonBlank(
                source != null ? source.getPolicyLabel() : null,
                "Policy Contact Address (Insured)"));
        if (includeState && !isBlank(stateCode)) {
            GWCodeName state = code(stateCode);
            state.setName(source != null ? source.getStateName() : null);
            result.setState(state);
        }
        return result;
    }


    private GWDraftClaimRequest.LossLocation mapLossLocation(GWLossLocation source) {
        if (source == null) return null;
        GWDraftClaimRequest.LossLocation result = new GWDraftClaimRequest.LossLocation();
        result.setAddressLine1(source.getAddressLine1()); result.setCity(source.getCity()); result.setCountry(source.getCountry());
        result.setPostalCode(source.getPostalCode());
        if (source.getState() != null) result.setState(code(source.getState().getCode(), source.getState().getName()));
        return result;
    }

    private GWDraftClaimRequest.Included included(String insuredName) {
        String[] names = insuredName == null || insuredName.isBlank() ? new String[]{"System", "User"} : insuredName.trim().split("\\s+", 2);
        GWDraftClaimRequest.ContactAttributes attributes = new GWDraftClaimRequest.ContactAttributes();
        attributes.setFirstName(names[0]); attributes.setLastName(names.length > 1 ? names[1] : "User"); attributes.setContactSubtype("Person");
        GWDraftClaimRequest.ClaimContact contact = new GWDraftClaimRequest.ClaimContact();
        contact.setAttributes(attributes); contact.setMethod("post"); contact.setRefid(REPORTER_REF_ID); contact.setUri("/claim/v1/claims/this/contacts");
        GWDraftClaimRequest.Included included = new GWDraftClaimRequest.Included(); included.setClaimContact(List.of(contact));
        return included;
    }

    private GWCodeName code(String value) { return code(value, null); }

    private GWCodeName code(String value, String name) {
        GWCodeName code = new GWCodeName();
        code.setCode(value);
        code.setName(name);
        return code;
    }

    private GWCodeName mapLossCause(DraftClaimRequestDTO.CodeDTO source) {
        if (source == null) {
            return null;
        }

        String code = normalizeLossCauseCode(source.getCode() != null ? source.getCode() : source.getName());
        if (isBlank(code)) {
            return null;
        }

        GWCodeName result = new GWCodeName();
        result.setCode(code);
        result.setName(source.getName() != null ? source.getName() : source.getCode());
        return result;
    }

    private String normalizeLossDate(String value) {
        if (isBlank(value)) {
            return null;
        }

        String trimmed = value.trim();
        try {
            return OffsetDateTime.parse(trimmed)
                    .withOffsetSameInstant(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH))
                    .atStartOfDay(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        } catch (DateTimeParseException ignored) {
        }

        return trimmed;
    }

    private String normalizeLossCauseCode(String value) {
        if (isBlank(value)) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.matches("[A-Za-z0-9._-]+")) {
            return trimmed;
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.contains("collision")) {
            return "otherobjcoll";
        }
        if (lower.contains("theft")) {
            return "theft";
        }
        if (lower.contains("fire")) {
            return "fire";
        }
        if (lower.contains("hail") || lower.contains("storm") || lower.contains("weather")) {
            return "weather";
        }
        if (lower.contains("water") || lower.contains("flood")) {
            return "water";
        }
        if (lower.contains("glass")) {
            return "glass";
        }
        return "otherobjcoll";
    }

    private String normalizeStateCode(String value) {
        if (isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() == 2) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        return switch (trimmed.toLowerCase(Locale.ROOT)) {
            case "alabama" -> "AL";
            case "alaska" -> "AK";
            case "arizona" -> "AZ";
            case "arkansas" -> "AR";
            case "california" -> "CA";
            case "colorado" -> "CO";
            case "connecticut" -> "CT";
            case "delaware" -> "DE";
            case "district of columbia" -> "DC";
            case "florida" -> "FL";
            case "georgia" -> "GA";
            case "hawaii" -> "HI";
            case "idaho" -> "ID";
            case "illinois" -> "IL";
            case "indiana" -> "IN";
            case "iowa" -> "IA";
            case "kansas" -> "KS";
            case "kentucky" -> "KY";
            case "louisiana" -> "LA";
            case "maine" -> "ME";
            case "maryland" -> "MD";
            case "massachusetts" -> "MA";
            case "michigan" -> "MI";
            case "minnesota" -> "MN";
            case "mississippi" -> "MS";
            case "missouri" -> "MO";
            case "montana" -> "MT";
            case "nebraska" -> "NE";
            case "nevada" -> "NV";
            case "new hampshire" -> "NH";
            case "new jersey" -> "NJ";
            case "new mexico" -> "NM";
            case "new york" -> "NY";
            case "north carolina" -> "NC";
            case "north dakota" -> "ND";
            case "ohio" -> "OH";
            case "oklahoma" -> "OK";
            case "oregon" -> "OR";
            case "pennsylvania" -> "PA";
            case "rhode island" -> "RI";
            case "south carolina" -> "SC";
            case "south dakota" -> "SD";
            case "tennessee" -> "TN";
            case "texas" -> "TX";
            case "utah" -> "UT";
            case "vermont" -> "VT";
            case "virginia" -> "VA";
            case "washington" -> "WA";
            case "west virginia" -> "WV";
            case "wisconsin" -> "WI";
            case "wyoming" -> "WY";
            default -> trimmed.toUpperCase(Locale.ROOT);
        };
    }

    private String normalizeCountryCode(String value) {
        if (isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() == 2) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        if ("united states".equalsIgnoreCase(trimmed) || "usa".equalsIgnoreCase(trimmed) || "us".equalsIgnoreCase(trimmed)) {
            return "US";
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private PolicyAddressParts parsePolicyAddress(String policyAddress) {
        PolicyAddressParts result = new PolicyAddressParts();
        if (isBlank(policyAddress)) {
            return result;
        }

        String[] parts = policyAddress.split("\\s*,\\s*");
        if (parts.length > 0) {
            result.addressLine1 = emptyToNull(parts[0]);
        }
        if (parts.length > 1) {
            result.city = emptyToNull(parts[1]);
        }
        if (parts.length > 2) {
            String[] statePostal = parts[2].trim().split("\\s+");
            if (statePostal.length > 0) {
                result.stateCode = normalizeStateCode(statePostal[0]);
            }
            if (statePostal.length > 1) {
                result.postalCode = emptyToNull(statePostal[statePostal.length - 1]);
            }
        }
        if (parts.length > 3) {
            result.country = normalizeCountryCode(parts[3]);
        }
        return result;
    }

    private String emptyToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static class PolicyAddressParts {
        private String addressLine1;
        private String city;
        private String stateCode;
        private String postalCode;
        private String country;
    }
}
