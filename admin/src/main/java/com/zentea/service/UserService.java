package com.zentea.service;

import com.zentea.dto.response.UserInfoResponse;
import com.zentea.entity.User;

public interface UserService {

    User getById(Long id);

    User getByUsername(String username);

    UserInfoResponse getUserInfo(Long id);
}
