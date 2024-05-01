package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RequiredArgsConstructor
public abstract class UserController {

    protected final UserService userService;
    protected final BCryptPasswordEncoder bCryptPasswordEncoder;
    protected final JWTUtil jwtUtil;
    protected final JWTConfigurationProperties jwtConfigurationProperties;

    protected UserResponse mapToUserResponse(UserEntity userEntity) {
        return new UserResponse(
                userEntity.getUserId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name()),
                jwtConfigurationProperties.getExpirationTimeInMilliseconds());
    }
}
