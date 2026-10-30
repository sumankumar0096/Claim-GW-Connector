package com.vm.GWConnector.service;

import com.vm.GWConnector.exception.ClaimServiceException;
import com.vm.GWConnector.model.*;
import com.vm.GWConnector.mapper.GWClaimAdjustorUsersMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimAdjustorServiceImpl implements ClaimAdjustorService {

    private final RestTemplate restTemplate;

    @Value("${admin-api.url}")
    private String adminApiUrl;

    @Value("${policy-api.username}")
    private String username;

    @Value("${policy-api.password}")
    private String password;

    @Override
    public GWClaimAdjustorResponse getClaimAdjustors(int pageSize) {
        String url = String.format("%s/rest/admin/v1/groups?pageSize=%d", adminApiUrl, pageSize);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("Calling GW claim adjustor API: url={}", url);
            ResponseEntity<GWClaimAdjustorResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GWClaimAdjustorResponse.class);
            log.info("Received claim adjustor response: status={} body={}", response.getStatusCode(), response.getBody());
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error fetching claim adjustors from GW", e);
            throw new ClaimServiceException("Failed to fetch claim adjustors from GW", e);
        }
    }

    @Override
    public com.vm.GWConnector.model.GWClaimAdjustorSingleResponse getClaimAdjustorById(String groupId) {
        String url = String.format("%s/rest/admin/v1/groups/%s", adminApiUrl, groupId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("Calling GW claim adjustor API (single): url={}", url);
            ResponseEntity<com.vm.GWConnector.model.GWClaimAdjustorSingleResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    com.vm.GWConnector.model.GWClaimAdjustorSingleResponse.class);
            log.info("Received single claim adjustor response: status={} bodyPresent={}", response.getStatusCode(), response.getBody() != null);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error fetching claim adjustor from GW for id={}", groupId, e);
            throw new ClaimServiceException("Failed to fetch claim adjustor from GW", e);
        }
    }

    @Override
    public ClaimAdjustorUsersResponseDTO getClaimAdjustorUsers(String groupId, int pageSize) {
        String url = String.format("%s/rest/admin/v1/groups/%s/users?pageSize=%d", adminApiUrl, groupId, pageSize);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("Calling GW claim adjustor users API: url={}", url);
            ResponseEntity<com.vm.GWConnector.model.GWClaimAdjustorUsersResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    com.vm.GWConnector.model.GWClaimAdjustorUsersResponse.class);
            log.info("Received claim adjustor users response: status={} count={}", response.getStatusCode(), response.getBody() != null ? response.getBody().getCount() : null);
            return GWClaimAdjustorUsersMapper.map(response.getBody());
        } catch (RestClientException e) {
            log.error("Error fetching claim adjustor users from GW for groupId={}", groupId, e);
            throw new ClaimServiceException("Failed to fetch claim adjustor users from GW", e);
        }
    }
}

