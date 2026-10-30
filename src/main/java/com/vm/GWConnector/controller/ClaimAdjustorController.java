package com.vm.GWConnector.controller;

import com.vm.GWConnector.model.*;
import com.vm.GWConnector.model.GWClaimAdjustorUsersResponse;
import com.vm.GWConnector.service.ClaimAdjustorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/claim-adjustors")
@RequiredArgsConstructor
@Tag(name = "Claim adjustors", description = "Guidewire ClaimCenter group and user operations")
public class ClaimAdjustorController {

    private final ClaimAdjustorService claimAdjustorService;

    @GetMapping("/groups")
    @Operation(summary = "List claim-adjustor groups")
    public ResponseEntity<GWClaimAdjustorResponse> getClaimAdjustors(
            @RequestParam(name = "pageSize", defaultValue = "100") int pageSize) {

        log.info("Received request to fetch claim adjustors with pageSize={}", pageSize);
        GWClaimAdjustorResponse response = claimAdjustorService.getClaimAdjustors(pageSize);
        log.info("Returning claim adjustors response with dataCount={}", response != null && response.getData() != null ? response.getData().size() : 0);
        return ResponseEntity.ok(response);
    }

    // Keep the original /groups/... path available while accepting the path used by clients.
    @GetMapping({"/{groupId}/users", "/groups/{groupId}/users"})
    @Operation(summary = "List users in a claim-adjustor group")
    public ResponseEntity<ClaimAdjustorUsersResponseDTO> getClaimAdjustorUsers(
            @PathVariable String groupId,
            @RequestParam(name = "pageSize", defaultValue = "100") int pageSize) {

        log.info("Received request to fetch claim adjustor users for groupId={} pageSize={}", groupId, pageSize);
        ClaimAdjustorUsersResponseDTO response = claimAdjustorService.getClaimAdjustorUsers(groupId, pageSize);
        log.info("Returning claim adjustor users response with count={}", response != null ? response.getCount() : 0);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Get a claim-adjustor group")
    public ResponseEntity<com.vm.GWConnector.model.GWClaimAdjustorSingleResponse> getClaimAdjustorById(
            @org.springframework.web.bind.annotation.PathVariable String groupId) {

        log.info("Received request to fetch claim adjustor by id={}", groupId);
        com.vm.GWConnector.model.GWClaimAdjustorSingleResponse resp = claimAdjustorService.getClaimAdjustorById(groupId);
        return ResponseEntity.ok(resp);
    }
}
