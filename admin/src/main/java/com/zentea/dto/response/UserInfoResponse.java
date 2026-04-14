package com.zentea.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResponse {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatarUrl;
    private String role;
}
