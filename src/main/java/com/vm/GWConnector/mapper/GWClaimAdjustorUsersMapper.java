package com.vm.GWConnector.mapper;

import com.vm.GWConnector.model.ClaimAdjustorUserDTO;
import com.vm.GWConnector.model.ClaimAdjustorUsersResponseDTO;
import com.vm.GWConnector.model.GWClaimAdjustorUsersResponse;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import java.util.stream.Collectors;

public class GWClaimAdjustorUsersMapper {

    private GWClaimAdjustorUsersMapper() {
        // Utility class
    }

    public static ClaimAdjustorUsersResponseDTO map(
            GWClaimAdjustorUsersResponse source) {

        ClaimAdjustorUsersResponseDTO target =
                new ClaimAdjustorUsersResponseDTO();

        if (source == null || source.getData() == null) {
            target.setAdjustors(Collections.emptyList());
            return target;
        }
        target.setCount(source.getCount());
        List<ClaimAdjustorUserDTO> adjustors = source.getData()
                .stream()
                .filter(Objects::nonNull)
                .map(GWClaimAdjustorUsersMapper::mapUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        target.setAdjustors(adjustors);

        return target;
    }

    private static ClaimAdjustorUserDTO mapUser(
            GWClaimAdjustorUsersResponse.UserData userData) {

        if (userData.getAttributes() == null ||
                userData.getAttributes().getUser() == null) {
            return null;
        }

        GWClaimAdjustorUsersResponse.UserReference user =
                userData.getAttributes().getUser();

        ClaimAdjustorUserDTO target = new ClaimAdjustorUserDTO();

        target.setUserId(user.getId());
        target.setDisplayName(user.getDisplayName());

        return target;
    }
}