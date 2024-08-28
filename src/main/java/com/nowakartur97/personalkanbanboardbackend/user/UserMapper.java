package com.nowakartur97.personalkanbanboardbackend.user;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTConfigurationProperties;
import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final JWTUtil jwtUtil;
    private final JWTConfigurationProperties jwtConfigurationProperties;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserEntity mapToEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .username(userDTO.getUsername())
                .password(bCryptPasswordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .role(UserRole.USER)
                .build();
    }

    public UserResponse mapToResponse(UserEntity userEntity) {
        return new UserResponse(
                userEntity.getUserId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole().name()),
                jwtConfigurationProperties.getExpirationTimeInMilliseconds(),
                userEntity.getRole());
    }
}
