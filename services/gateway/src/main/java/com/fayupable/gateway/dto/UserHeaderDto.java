package com.fayupable.gateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserHeaderDto {
    private String userId;
    private String username;
    private String role;
}
