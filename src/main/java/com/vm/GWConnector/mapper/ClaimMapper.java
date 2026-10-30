package com.vm.GWConnector.mapper;

import com.vm.GWConnector.model.ClaimDetailsByPolicyNumber;
import com.vm.GWConnector.model.ClaimResponse;
import com.vm.GWConnector.model.ClaimResponseDTO;
import com.vm.GWConnector.model.GWClaimResponse;
import org.springframework.stereotype.Component;

@Component
public class ClaimMapper {

    public ClaimResponse mapToClaimResponse(
            GWClaimResponse gwClaimResponse) {

        if (gwClaimResponse == null) {
            return null;
        }

        ClaimResponse response = new ClaimResponse();

        response.setClaimId(gwClaimResponse.getId());
        response.setClaimNumber(gwClaimResponse.getClaimNumber());
        response.setDescription(gwClaimResponse.getDescription());
        response.setPolicyNumber(gwClaimResponse.getPolicyNumber());

        if (gwClaimResponse.getAssignmentStatus() != null) {
            response.setAssignmentStatus(
                    gwClaimResponse.getAssignmentStatus().getName());

            response.setAssignmentStatusCode(
                    gwClaimResponse.getAssignmentStatus().getCode());
        }

        if (gwClaimResponse.getFlagged() != null) {
            response.setFlaggedStatus(
                    gwClaimResponse.getFlagged().getName());

            response.setFlaggedStatusCode(
                    gwClaimResponse.getFlagged().getCode());
        }

        if (gwClaimResponse.getLobCode() != null) {
            response.setLobCode(
                    gwClaimResponse.getLobCode().getCode());

            response.setLobName(
                    gwClaimResponse.getLobCode().getName());
        }

        if (gwClaimResponse.getLossCause() != null) {
            response.setLossCauseCode(
                    gwClaimResponse.getLossCause().getCode());

            response.setLossCauseName(
                    gwClaimResponse.getLossCause().getName());
        }

        if (gwClaimResponse.getLossType() != null) {
            response.setLossTypeCode(
                    gwClaimResponse.getLossType().getCode());

            response.setLossTypeName(
                    gwClaimResponse.getLossType().getName());
        }

        if (gwClaimResponse.getState() != null) {
            response.setClaimStateCode(
                    gwClaimResponse.getState().getCode());

            response.setClaimStateName(
                    gwClaimResponse.getState().getName());
        }
        
        if (gwClaimResponse.getAssignedGroup() != null) {
                response.setAssignedGroup(gwClaimResponse.getAssignedGroup().getId());
        }       

        response.setLossDate(gwClaimResponse.getLossDate());
        response.setReportedDate(gwClaimResponse.getReportedDate());

        if (gwClaimResponse.getLossLocation() != null) {

            response.setAddress(
                    gwClaimResponse.getLossLocation().getDisplayName());

            response.setCity(
                    gwClaimResponse.getLossLocation().getCity());

            response.setPostalCode(
                    gwClaimResponse.getLossLocation().getPostalCode());

            if (gwClaimResponse.getLossLocation().getState() != null) {
                response.setState(
                        gwClaimResponse.getLossLocation()
                                .getState()
                                .getName());
            }
        }

        return response;
    }

    public static ClaimResponseDTO toResponse(
            ClaimDetailsByPolicyNumber claim) {

        return ClaimResponseDTO.builder()
                .claimNumber(claim.getClaimNumber())
                .policyNumber(claim.getPolicyNumber())
                .description(claim.getDescription())
                .lossDate(claim.getLossDate())
                .reportedDate(claim.getReportedDate())
                .assignmentStatus(
                        claim.getAssignmentStatus() != null
                                ? claim.getAssignmentStatus().getName()
                                : null)
                .claimState(
                        claim.getState() != null
                                ? claim.getState().getName()
                                : null)
                .lob(
                        claim.getLobCode() != null
                                ? claim.getLobCode().getName()
                                : null)
                .lossCause(
                        claim.getLossCause() != null
                                ? claim.getLossCause().getName()
                                : null)
                .lossType(
                        claim.getLossType() != null
                                ? claim.getLossType().getName()
                                : null)
                .build();
    }
}