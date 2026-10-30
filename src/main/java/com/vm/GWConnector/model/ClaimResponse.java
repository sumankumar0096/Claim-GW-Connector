package com.vm.GWConnector.model;

import lombok.Data;

@Data
public class ClaimResponse {

    private String claimNumber;
    private String claimId;
    private String description;
    private String policyNumber;

    private String assignmentStatus;
    private String assignmentStatusCode;

    private String flaggedStatus;
    private String flaggedStatusCode;

    private String lobCode;
    private String lobName;

    private String lossCauseCode;
    private String lossCauseName;

    private String lossTypeCode;
    private String lossTypeName;

    private String lossDate;
    private String reportedDate;

    private String claimStateCode;
    private String claimStateName;

    private String city;
    private String state;
    private String postalCode;
    private String address;
    private String assignedGroup;
    private ClaimAdjustorUsersResponseDTO adjustors;
}