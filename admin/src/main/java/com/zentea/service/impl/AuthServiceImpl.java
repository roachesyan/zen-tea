package com.zentea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zentea.common.constant.Constants;
import com.zentea.common.exception.BusinessException;
import com.zentea.common.result.ResultCode;
import com.zentea.config.JwtProperties;
import com.zentea.dto.request.LoginRequest;
import com.zentea.dto.request.RegisterRequest;
import com.zentea.dto.response.LoginResponse;
import com.zentea.dto.response.UserInfoResponse;
import com.zentea.entity.RefreshToken;
import com.zentea.entity.User;
import com.zentea.mapper.RefreshTokenMapper;
import com.zentea.mapper.UserMapper;
import com.zentea.security.JwtTokenProvider;
import com.zentea.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        Long existing = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (existing > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setRole(Constants.ROLE_CUSTOMER);
        user.setStatus(Constants.STATUS_ENABLED);
        userMapper.insert(user);

        log.info("User registered: {}", user.getUsername());
        return generateLoginResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        if (user.getStatus() != Constants.STATUS_ENABLED) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        log.info("User logged in: {}", user.getUsername());
        return generateLoginResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(String refreshTokenStr) {
        if (!jwtTokenProvider.validateToken(refreshTokenStr)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        RefreshToken storedToken = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getToken, refreshTokenStr));
        if (storedToken == null || storedToken.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshTokenStr);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        refreshTokenMapper.deleteById(storedToken.getId());
        log.info("Token refreshed for user: {}", user.getUsername());
        return generateLoginResponse(user);
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        refreshTokenMapper.delete(
                new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getUserId, userId));
        log.info("User logged out: userId={}", userId);
    }

    private LoginResponse generateLoginResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setExpireTime(LocalDateTime.now().plusNanos(jwtProperties.getRefreshTokenExpiration() * 1_000_000));
        refreshTokenMapper.insert(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000)
                .user(UserInfoResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .phone(user.getPhone())
                        .avatarUrl(user.getAvatarUrl())
                        .role(user.getRole())
                        .build())
                .build();
    }
}
