package com.vm.GWConnector.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DraftClaimRequestDTO {

    @NotBlank
    private String policyNumber;

    private String claimNumber;

    private String description;

    private String lossDate;

    @Valid
    private CodeDTO lossCause;
    private String faultRatingCode = "1";
    private String reportedByTypeCode = "other";

    @Valid
    private LossLocationDTO lossLocation;

    @AssertTrue(message = "lossDate, lossCause, and lossLocation are required when claimNumber is not provided")
    public boolean isValidForRequestedOperation() {
        if (hasClaimReference()) {
            return true;
        }
        return lossDate != null && !lossDate.isBlank()
                && lossCause != null && lossCause.getCode() != null && !lossCause.getCode().isBlank()
                && lossLocation != null;
    }

    public boolean hasClaimReference() {
        return claimNumber != null && !claimNumber.isBlank();
    }

    @Data
    public static class LossLocationDTO {
        @NotBlank private String addressLine1;
        @NotBlank private String city;
        @NotBlank private String country;
        @NotBlank private String postalCode;
        @NotBlank private String stateCode;
        private String stateName;
        private String policyLabel = "Policy Contact Address (Insured)";
    }

    @Data
    public static class CodeDTO {
        @NotBlank private String code;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String name;
    }
}
