package com.vm.GWConnector.service;

import com.vm.GWConnector.exception.ClaimServiceException;
import com.vm.GWConnector.mapper.ClaimMapper;
import com.vm.GWConnector.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class FnolProcessServiceImpl implements FnolProcessService {

    private static final int ADJUSTOR_PAGE_SIZE = 100;

    private final ClaimService claimService;
    private final ClaimMapper claimMapper;
    private final ClaimAdjustorService claimAdjustorService;

    @Override
    public ClaimResponse processFnol(FnolProcessRequest request) {

        log.info("Processing FNOL for policy={} claim={}",
                request.getPolicyNumber(),
                request.getClaimNumber());

        GWClaimResponse claimDetails;

        // ==========================================
        // Scenario 1 : Existing Claim
        // ==========================================
        if (StringUtils.hasText(request.getClaimNumber())) {

            log.info("Existing claim number provided. Fetching claim by number={}",
                    request.getClaimNumber());

            claimDetails = claimService.getClaimByNumber(request.getClaimNumber());

            if (claimDetails == null) {
                throw new ClaimServiceException(
                        "Unable to retrieve claim details for claimNumber="
                                + request.getClaimNumber());
            }

        } else {

            // ==========================================
            // Scenario 2 : New FNOL
            // ==========================================
            DraftClaimRequestDTO draftRequest = request.toDraftClaimRequest();

            DraftClaimResponseDTO draftClaim =
                    claimService.createDraftClaim(draftRequest);

            if (draftClaim == null ||
                    !StringUtils.hasText(draftClaim.getClaimId())) {

                throw new ClaimServiceException(
                        "Draft claim response did not contain a claim ID");
            }

            log.info("Draft claim created. claimId={}", draftClaim.getClaimId());

            ClaimSubmissionRequestDTO submissionRequest =
                    new ClaimSubmissionRequestDTO();

            submissionRequest.setClaimId(draftClaim.getClaimId());

            ClaimSubmissionResponseDTO submittedClaim =
                    claimService.submitClaim(submissionRequest);

            if (submittedClaim == null) {
                throw new ClaimServiceException(
                        "Claim submission did not return claim details");
            }

            log.info("Claim submitted successfully. claimId={}",
                    draftClaim.getClaimId());

            claimDetails = claimService.getClaimById(draftClaim.getClaimId());

            if (claimDetails == null) {
                throw new ClaimServiceException(
                        "Unable to retrieve claim details for claimId="
                                + draftClaim.getClaimId());
            }
        }

        // ==========================================
        // Common processing
        // ==========================================
        ClaimResponse response = claimMapper.mapToClaimResponse(claimDetails);

        String assignedGroupId = getAssignedGroupId(claimDetails);

        if (StringUtils.hasText(assignedGroupId)) {

            log.info("Fetching claim adjustor users for assignedGroupId={}",
                    assignedGroupId);

            try {

                ClaimAdjustorUsersResponseDTO adjustors =
                        claimAdjustorService.getClaimAdjustorUsers(
                                assignedGroupId,
                                ADJUSTOR_PAGE_SIZE);

                response.setAdjustors(adjustors);

                log.info("Claim adjustor users added successfully.");

            } catch (Exception ex) {

                log.error("Failed to fetch adjustor users for group={}",
                        assignedGroupId,
                        ex);

                // Don't fail FNOL if adjustor lookup fails
            }

        } else {

            log.warn("Assigned group not found for claim={}",
                    claimDetails.getClaimNumber());
        }

        return response;
    }

    /**
     * Extract assignedGroup.id from claim details.
     */
    private String getAssignedGroupId(GWClaimResponse claimDetails) {

        if (claimDetails == null ||
                claimDetails.getAssignedGroup() == null) {
            return null;
        }

        return claimDetails.getAssignedGroup().getId();
    }
}