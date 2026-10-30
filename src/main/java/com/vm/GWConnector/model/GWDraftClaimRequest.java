package com.vm.GWConnector.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GWDraftClaimRequest {
    private DataNode data;
    private Included included;

    @Data public static class DataNode { private Attributes attributes; }
    @Data public static class Attributes {
        private String policyNumber;
        private String description;
        private String lossDate;
        private GWCodeName lossCause;
        private LossLocation lossLocation;
        private GWCodeName faultRating;
        private GWCodeName reportedByType;
        private Reference reporter;
    }
    @Data public static class LossLocation {
        private String addressLine1;
        private String city;
        private String country;
        private String policyLabel;
        private String postalCode;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private GWCodeName state;
    }
    @Data public static class Reference { private String refid; }
    @Data public static class Included { @JsonProperty("ClaimContact") private List<ClaimContact> claimContact; }
    @Data public static class ClaimContact {
        private ContactAttributes attributes;
        private String method;
        private String refid;
        private String uri;
    }
    @Data public static class ContactAttributes {
        private String firstName;
        private String lastName;
        private String contactSubtype;
    }
}
