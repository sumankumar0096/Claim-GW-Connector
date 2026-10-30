package com.vm.GWConnector.model;

import lombok.Data;

import java.util.List;

@Data
public class ClaimAdjustorUsersResponseDTO {
    private long count;
    private List<ClaimAdjustorUserDTO> adjustors;
}