package com.vm.GWConnector.mapper;

import com.vm.GWConnector.model.ClaimAdjustorResponse;
import com.vm.GWConnector.model.GWClaimAdjustorResponse;
import org.springframework.stereotype.Component;
import com.vm.GWConnector.model.ClaimAdjustorUsersResponseDTO;

import java.util.Collections;
import java.util.List;

@Component
public class ClaimAdjustorMapper {

    public List<ClaimAdjustorResponse> mapToClaimAdjustorResponses(
            GWClaimAdjustorResponse gwResponse) {

        if (gwResponse == null || gwResponse.getData() == null || gwResponse.getData().isEmpty()) {
            return Collections.emptyList();
        }

        return gwResponse.getData().stream()
                .map(this::mapToClaimAdjustorResponse)
                .toList();
    }

    private ClaimAdjustorResponse mapToClaimAdjustorResponse(
            GWClaimAdjustorResponse.GroupData groupData) {

        ClaimAdjustorResponse response = new ClaimAdjustorResponse();

        if (groupData.getAttributes() != null) {
            response.setId(groupData.getAttributes().getId());
            response.setName(firstNonBlank(groupData.getAttributes().getDisplayName(), groupData.getAttributes().getName()));
            response.setDescription(groupData.getAttributes().getDescription());
            response.setActive(groupData.getAttributes().getActive());
            response.setGroupType(extractStringValue(groupData.getAttributes().getGroupType()));
            response.setSecurityZone(extractStringValue(groupData.getAttributes().getSecurityZone()));
        }

        response.setType(groupData.getType());

        if (groupData.getLinks() != null && groupData.getLinks().getSelf() != null) {
            response.setSelfLink(groupData.getLinks().getSelf().getHref());
        }

        return response;
    }

    private String extractStringValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof java.util.Map<?, ?> map) {
            for (String key : new String[]{"displayName", "name", "value", "code", "id"}) {
                Object nested = map.get(key);
                if (nested instanceof String nestedString && !nestedString.isBlank()) {
                    return nestedString;
                }
            }
            return map.toString();
        }
        return value.toString();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
    
}

