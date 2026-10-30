package com.vm.GWConnector.service;

import com.vm.GWConnector.mapper.ClaimMapper;
import com.vm.GWConnector.mapper.ClaimActivityAssigneesMapper;
import com.vm.GWConnector.mapper.ClaimAssignmentMapper;
import com.vm.GWConnector.mapper.DraftClaimMapper;
import com.vm.GWConnector.mapper.ClaimSubmissionMapper;
import com.vm.GWConnector.mapper.PolicyMapper;
import com.vm.GWConnector.model.*;
import com.vm.GWConnector.exception.ClaimServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final RestTemplate restTemplate;
    private final ClaimActivityAssigneesMapper claimActivityAssigneesMapper;
    private final ClaimAssignmentMapper claimAssignmentMapper;
    private final DraftClaimMapper draftClaimMapper;
    private final PolicyService policyService;
    private final PolicyMapper policyMapper;
    private final ClaimSubmissionMapper claimSubmissionMapper;

    @Value("${claim-api.url}")
    private String baseUrl;

    @Value("${policy-api.username}")
    private String username;

    @Value("${policy-api.password}")
    private String password;

    @Override
    public GWClaimResponse getClaim(String claimNumber) {

        String url =
                baseUrl +
                "/rest/claim/v1/claims/claimNumber/" +
                claimNumber;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("Fetching claim from GW: claimNumber={}", claimNumber);
            ResponseEntity<GWClaimResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            GWClaimResponse.class);
            log.info("Received claim response: claimNumber={} status={} body={}",
                    claimNumber,
                    response.getStatusCode(),
                    response.getBody());
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error fetching claim from GW: claimNumber={}", claimNumber, e);
            throw new ClaimServiceException("Failed to fetch claim from GW", e);
        }
    }

    @Override
    public GWClaimResponse getClaimById(String claimId) {
        String url = baseUrl + "/rest/claim/v1/claims/" + claimId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            log.info("Fetching claim from GW: claimId={}", claimId);
            ResponseEntity<GWClaimSubmitResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<Void>(headers), GWClaimSubmitResponse.class);
            GWClaimAttributes attributes = response.getBody() != null && response.getBody().getData() != null
                    ? response.getBody().getData().getAttributes() : null;
            log.info("Received claim response: claimId={} status={} attributesPresent={}",
                    claimId, response.getStatusCode(), attributes != null);
            return mapToClaimResponse(attributes);
        } catch (RestClientException e) {
            log.error("Error fetching claim from GW: claimId={}", claimId, e);
            throw new ClaimServiceException("Failed to fetch claim from GW", e);
        }
    }

    @Override
    public GWClaimResponse getClaimByNumber(String claimNumber) {

        String url = baseUrl
                + "/rest/claim/v1/claims/claimNumber/"
                + claimNumber;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            log.info("Fetching claim from GW by claim number: claimNumber={}",claimNumber);

            ResponseEntity<GWClaimAttributes> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            new HttpEntity<Void>(headers),
                            GWClaimAttributes.class
                    );

            GWClaimAttributes attributes = response.getBody();

            log.info(
                    "Received claim response by claim number: " +
                            "claimNumber={} status={} attributesPresent={}",
                    claimNumber,
                    response.getStatusCode(),
                    attributes != null
            );

            if (attributes == null) {
                throw new ClaimServiceException(
                        "Guidewire returned empty claim details for claimNumber="
                                + claimNumber
                );
            }

            return mapToClaimResponse(attributes);

        } catch (HttpClientErrorException e) {

            log.error(
                    "Guidewire rejected claim lookup: claimNumber={} " +
                            "status={} response={}",
                    claimNumber,
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

            throw new ClaimServiceException(
                    "Failed to fetch claim by number from GW: "
                            + claimNumber,
                    e
            );

        } catch (RestClientException e) {

            log.error(
                    "Error fetching claim by number from GW: claimNumber={}",
                    claimNumber,
                    e
            );

            throw new ClaimServiceException(
                    "Failed to fetch claim by number from GW",
                    e
            );
        }
    }

    private GWClaimResponse mapToClaimResponse(GWClaimAttributes attributes) {
        if (attributes == null) {
            return null;
        }

        GWClaimResponse response = new GWClaimResponse();
        response.setAssignedGroup(attributes.getAssignedGroup());
        response.setAssignmentStatus(attributes.getAssignmentStatus());
        response.setClaimNumber(attributes.getClaimNumber());
        response.setDescription(attributes.getDescription());
        response.setFlagged(attributes.getFlagged());
        response.setId(attributes.getId());
        response.setLobCode(attributes.getLobCode());
        response.setLossCause(attributes.getLossCause());
        response.setLossDate(attributes.getLossDate());
        response.setLossLocation(attributes.getLossLocation());
        response.setLossType(attributes.getLossType());
        response.setPolicyNumber(attributes.getPolicyNumber());
        response.setReportedDate(attributes.getReportedDate());
        response.setState(attributes.getState());
        return response;
    }

    public List<ClaimResponseDTO> getClaimsByPolicyNumber(String policyNumber) {

        String url = String.format(
                "%s/rest/claim/v1/claims/policyNumber/%s",
                baseUrl,
                policyNumber);
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("Fetching claims by policy number from GW: policyNumber={}", policyNumber);
            ResponseEntity<ClaimDetailsByPolicyNumber[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ClaimDetailsByPolicyNumber[].class);
            log.info("Received claims by policy response: policyNumber={} status={} count={}",
                    policyNumber,
                    response.getStatusCode(),
                    response.getBody() != null ? response.getBody().length : 0);
            return Arrays.stream(response.getBody())
                    .map(ClaimMapper::toResponse)
                    .toList();
        } catch (RestClientException e) {
            log.error("Error fetching claims by policy number from GW: policyNumber={}", policyNumber, e);
            throw new ClaimServiceException("Failed to fetch claims by policy number from GW", e);
        }
    }

    @Override
    public GWClaimActivityAssigneesResponse getClaimActivityAssignees(String claimId) {
        String url = String.format("%s/rest/claim/v1/claims/%s/activity-assignees", baseUrl, claimId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("Fetching claim activity assignees from GW: claimId={}", claimId);
            ResponseEntity<GWClaimActivityAssigneesResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GWClaimActivityAssigneesResponse.class);
            log.info("Received claim activity assignees response: claimId={} status={} bodyPresent={}",
                    claimId,
                    response.getStatusCode(),
                    response.getBody() != null);
            return claimActivityAssigneesMapper.mapToResponse(response.getBody());
        } catch (RestClientException e) {
            log.error("Error fetching claim activity assignees from GW: claimId={}", claimId, e);
            throw new ClaimServiceException("Failed to fetch claim activity assignees from GW", e);
        }
    }

    @Override
    public ClaimAssignmentResponseDTO assignClaim(ClaimAssignmentRequestDTO request) {
        String claimId = request.getClaimId();
        String url = String.format("%s/rest/claim/v1/claims/%s/assign", baseUrl, claimId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        GWClaimAssignmentRequest gwRequest = claimAssignmentMapper.mapToGWRequest(request);

        try {
            log.info("Assigning Guidewire claim: claimId={}", claimId);
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(gwRequest, headers),
                    Void.class);
            log.info("Assigned Guidewire claim: claimId={} status={}", claimId, response.getStatusCode());
            return claimAssignmentMapper.mapToResponse(request, response.getStatusCode());
        } catch (RestClientException e) {
            log.error("Error assigning Guidewire claim: claimId={}", claimId, e);
            throw new ClaimServiceException("Failed to assign claim in GW", e);
        }
    }

    @Override
    public DraftClaimResponseDTO createDraftClaim(DraftClaimRequestDTO request) {
        if (request.hasClaimReference()) {
            return getExistingClaim(request);
        }

        PolicyResponse policy = resolvePolicy(request);
        GWDraftClaimRequest gwRequest = request.getLossLocation() != null
                && "GB".equalsIgnoreCase(request.getLossLocation().getCountry())
                ? draftClaimMapper.mapToUKGWRequest(request, policy)
                : draftClaimMapper.mapToGWRequest(request, policy);
//        GWDraftClaimRequest gwRequest = draftClaimMapper.mapToUKGWRequest(request, policy);
        String url = baseUrl + "/rest/claim/v1/claims";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            log.info("Creating draft claim in Guidewire: request={}", gwRequest);
            ResponseEntity<GWDraftClaimResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(gwRequest, headers), GWDraftClaimResponse.class);
            DraftClaimResponseDTO result = draftClaimMapper.mapToResponse(response.getBody());
            if (result == null) {
                throw new ClaimServiceException("Guidewire returned an empty draft-claim response");
            }
            log.info("Created draft claim: claimId={} claimNumber={}", result.getClaimId(), result.getClaimNumber());
            return result;
        } catch (RestClientException e) {
            log.error("Error creating draft claim in Guidewire: policyNumber={}", request.getPolicyNumber(), e);
            throw new ClaimServiceException("Failed to create draft claim in GW", e);
        }
    }

    @Override
    public ClaimSubmissionResponseDTO submitClaim(ClaimSubmissionRequestDTO request) {
        String url = String.format("%s/rest/claim/v1/claims/%s/submit", baseUrl, request.getClaimId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        // Guidewire requires this header even when the submit action has no request body.
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            log.info("Submitting Guidewire claim: claimId={}", request.getClaimId());
            ResponseEntity<GWClaimSubmitResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<Void>(null, headers), GWClaimSubmitResponse.class);
            log.info("Submitted Guidewire claim: claimId={} status={} body={}",
                    request.getClaimId(), response.getStatusCode(), response.getBody());
            return claimSubmissionMapper.mapToResponse(request, response.getBody(), response.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.error("Guidewire rejected claim submission: claimId={} status={} response={}",
                    request.getClaimId(), e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new ClaimServiceException("Only draft claims can be submitted", e);
        } catch (RestClientException e) {
            log.error("Error submitting Guidewire claim: claimId={}", request.getClaimId(), e);
            throw new ClaimServiceException("Failed to submit claim in GW", e);
        }
    }

    private PolicyResponse resolvePolicy(DraftClaimRequestDTO request) {
        log.info("Looking up policy before draft-claim creation: policyNumber={}", request.getPolicyNumber());
        PolicyResponse policy = policyMapper.mapToPolicyResponse(
                policyService.searchPolicies(policyService.buildRequest(request.getPolicyNumber())));
        if (policy == null) {
            throw new ClaimServiceException("No policy found for policy number " + request.getPolicyNumber());
        }
        return policy;
    }

    private DraftClaimResponseDTO getExistingClaim(DraftClaimRequestDTO request) {
        log.info("Fetching existing claim: claimNumber={} policyNumber={}", request.getClaimNumber(), request.getPolicyNumber());
        GWClaimResponse claim = getClaim(request.getClaimNumber());
        if (claim == null) {
            throw new ClaimServiceException("No claim found for claim number " + request.getClaimNumber());
        }
        if (claim.getPolicyNumber() != null && !claim.getPolicyNumber().equals(request.getPolicyNumber())) {
            throw new ClaimServiceException("Claim number does not belong to the supplied policy number");
        }
        return draftClaimMapper.mapToResponse(claim);
    }

    @Override
    public CreateClaimResponse createClaim(CreateClaimRequest request) {

        String url = baseUrl + "/rest/composite/v1/composite";


        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        CompositeCreateClaimRequest compositeRequest = buildCompositeRequest(request);

        HttpEntity<CompositeCreateClaimRequest> entity =
                new HttpEntity<>(compositeRequest, headers);

        try {
            log.info("Creating claim in GW composite API: request={}", compositeRequest);
            ResponseEntity<CompositeCreateClaimResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    CompositeCreateClaimResponse.class);
            log.info("Received composite create response: status={} body={}",
                    response.getStatusCode(),
                    response.getBody());
            return mapToCreateClaimResponse(response.getBody());
        } catch (RestClientException e) {
            log.error("Error creating claim in GW composite API", e);
            throw new ClaimServiceException("Failed to create claim in GW", e);
        }
    }

    private CompositeCreateClaimRequest buildCompositeRequest(CreateClaimRequest request) {
        CompositeCreateClaimRequest.CompositeRequest policyRequest = new CompositeCreateClaimRequest.CompositeRequest();
        policyRequest.setMethod("post");
        policyRequest.setUri("/claim/v1/unverified-policies");

        CompositeCreateClaimRequest.CompositeRequestAttributes policyAttributes = new CompositeCreateClaimRequest.CompositeRequestAttributes();
        policyAttributes.setPolicyNumber(request.getPolicyNumber());
        GWCodeName policyType = new GWCodeName();
        policyType.setCode(request.getPolicyTypeCode() != null ? request.getPolicyTypeCode() : "BusinessAuto");
        policyAttributes.setPolicyType(policyType);
        policyAttributes.setEffectiveDate(request.getEffectiveDate());
        policyAttributes.setExpirationDate(request.getExpirationDate());
        GWCodeName currency = new GWCodeName();
        currency.setCode(request.getCurrencyCode() != null ? request.getCurrencyCode() : "usd");
        policyAttributes.setCurrency(currency);

        CompositeCreateClaimRequest.CompositeRequestBody policyBody = new CompositeCreateClaimRequest.CompositeRequestBody();
        policyBody.setData(new CompositeCreateClaimRequest.CompositeRequestData(policyAttributes));
        policyRequest.setBody(policyBody);
        policyRequest.setVars(List.of(new CompositeCreateClaimRequest.CompositeVar("uvNum", "$.data.attributes.policyNumber")));

        CompositeCreateClaimRequest.CompositeRequest claimRequest = new CompositeCreateClaimRequest.CompositeRequest();
        claimRequest.setMethod("post");
        claimRequest.setUri("/claim/v1/claims");

        CompositeCreateClaimRequest.CompositeRequestAttributes claimAttributes = new CompositeCreateClaimRequest.CompositeRequestAttributes();
        claimAttributes.setPolicyNumber("${uvNum}");
        claimAttributes.setLossDate(request.getLossDate());
        GWCodeName lossCause = new GWCodeName();
        lossCause.setCode(request.getLossCauseCode() != null ? request.getLossCauseCode() : "otherobjcoll");
        claimAttributes.setLossCause(lossCause);

        if (request.getLossLocation() != null) {
            CompositeCreateClaimRequest.CompositeLossLocation lossLocation = new CompositeCreateClaimRequest.CompositeLossLocation();
            lossLocation.setAddressLine1(request.getLossLocation().getAddressLine1());
            lossLocation.setCity(request.getLossLocation().getCity());
            GWState state = new GWState();
            state.setCode(request.getLossLocation().getStateCode());
            lossLocation.setState(state);
            lossLocation.setPostalCode(request.getLossLocation().getPostalCode());
            lossLocation.setCountry(request.getLossLocation().getCountry());
            claimAttributes.setLossLocation(lossLocation);
        }

        claimAttributes.setDescription(request.getDescription());

        CompositeCreateClaimRequest.CompositeRequestBody claimBody = new CompositeCreateClaimRequest.CompositeRequestBody();
        claimBody.setData(new CompositeCreateClaimRequest.CompositeRequestData(claimAttributes));
        claimRequest.setBody(claimBody);
        claimRequest.setVars(List.of(
                new CompositeCreateClaimRequest.CompositeVar("claimId", "$.data.attributes.id"),
                new CompositeCreateClaimRequest.CompositeVar("claimNumber", "$.data.attributes.claimNumber")
        ));

        return new CompositeCreateClaimRequest(List.of(policyRequest, claimRequest));
    }

    private CreateClaimResponse mapToCreateClaimResponse(CompositeCreateClaimResponse compositeResponse) {
        if (compositeResponse == null || compositeResponse.getResponses() == null) {
            log.error("Composite create claim response is empty or invalid");
            throw new ClaimServiceException("Invalid response returned from GW composite create claim API");
        }

        CreateClaimResponse result = new CreateClaimResponse();
        boolean found = compositeResponse.getResponses().stream()
                .filter(resp -> resp.getBody() != null && resp.getBody().getData() != null)
                .map(CompositeCreateClaimResponse.CompositeResponse::getBody)
                .map(CompositeCreateClaimResponse.CompositeResponseBody::getData)
                .peek(data -> log.debug("Composite response data node={}", data))
                .map(data -> data.getAttributes())
                .reduce((first, second) -> second)
                .map(attributes -> {
                    result.setClaimId(attributes.getId());
                    result.setClaimNumber(attributes.getClaimNumber());
                    result.setPolicyNumber(attributes.getPolicyNumber());
                    result.setStatus("CREATED");
                    return true;
                })
                .orElse(false);

        if (!found) {
            log.error("No claim attributes were found in composite create claim response");
            throw new ClaimServiceException("GW composite create claim response did not contain claim details");
        }

        return result;
    }
}
