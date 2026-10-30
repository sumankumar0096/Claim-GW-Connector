package com.vm.GWConnector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FnolProcessRequest {

    @NotBlank
    private String policyNumber;

    private String claimNumber;

    private String dateOfLoss;

    private String timeOfLoss;

    private String lossDescription;

    private String lossCause;

    @Valid
    private LossLocation lossLocation;

    public boolean hasClaimReference() {
        return claimNumber != null && !claimNumber.isBlank();
    }

    public DraftClaimRequestDTO toDraftClaimRequest() {
        DraftClaimRequestDTO request = new DraftClaimRequestDTO();
        request.setPolicyNumber(policyNumber);
        request.setClaimNumber(claimNumber);
        request.setDescription(lossDescription);
        request.setLossDate(normalizeLossDate(dateOfLoss, timeOfLoss));

        if (lossCause != null && !lossCause.isBlank()) {
            DraftClaimRequestDTO.CodeDTO cause = new DraftClaimRequestDTO.CodeDTO();
            cause.setCode(lossCause);
            request.setLossCause(cause);
        }

        if (lossLocation != null) {
            DraftClaimRequestDTO.LossLocationDTO location = new DraftClaimRequestDTO.LossLocationDTO();
            location.setAddressLine1(lossLocation.getAddress1());
            location.setCity(lossLocation.getCity());
            location.setStateCode(lossLocation.getState());
            location.setPostalCode(lossLocation.getZipCode() == null || lossLocation.getZipCode().isBlank()
                    ? lossLocation.getPostalCode()
                    : lossLocation.getZipCode());
            location.setCountry(lossLocation.getCountry());
            request.setLossLocation(location);
        }

        return request;
    }

    private String normalizeLossDate(String dateValue, String timeValue) {
        if (dateValue == null || dateValue.isBlank()) {
            return null;
        }

        String trimmedDate = dateValue.trim();
        String trimmedTime = timeValue == null ? null : timeValue.trim();
        LocalTime time = parseTime(trimmedTime);

        try {
            OffsetDateTime parsed = OffsetDateTime.parse(trimmedDate);
            if (time != null) {
                parsed = parsed.toLocalDate().atTime(time).atOffset(ZoneOffset.UTC);
            }
            return parsed.withOffsetSameInstant(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDate date = LocalDate.parse(trimmedDate, DateTimeFormatter.ofPattern("dd MMM uuuu"));
            LocalDateTime combined = LocalDateTime.of(date, time != null ? time : LocalTime.MIDNIGHT);
            return combined.atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDate date = LocalDate.parse(trimmedDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDateTime combined = LocalDateTime.of(date, time != null ? time : LocalTime.MIDNIGHT);
            return combined.atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDate date = LocalDate.parse(trimmedDate, DateTimeFormatter.ofPattern("dd/MM/uuuu"));
            LocalDateTime combined = LocalDateTime.of(date, time != null ? time : LocalTime.MIDNIGHT);
            return combined.atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        } catch (DateTimeParseException ignored) {
        }

        return trimmedDate;
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LossLocation {
        private String address1;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String postalCode;
    }
}