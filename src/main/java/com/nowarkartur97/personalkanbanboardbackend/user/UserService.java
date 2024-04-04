package com.nowarkartur97.personalkanbanboardbackend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Mono<UserEntity> saveUser(UserEntity User){
        return userRepository.save(User);
    }
}
