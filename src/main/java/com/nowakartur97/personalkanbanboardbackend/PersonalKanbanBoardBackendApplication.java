package com.nowakartur97.personalkanbanboardbackend;

import com.nowakartur97.personalkanbanboardbackend.auth.JWTUtil;
import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskService;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserRole;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Random;

@SpringBootApplication
@ConfigurationPropertiesScan
@RequiredArgsConstructor
@Slf4j
public class PersonalKanbanBoardBackendApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PersonalKanbanBoardBackendApplication.class, args);
    }

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    private final UserService userService;
    private final TaskService taskService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTUtil jwtUtil;

    @Override
    public void run(String... args) {
        if (activeProfile.equals("local")) {
            UserEntity user = createTestUser();
            createTestTask(user);
        }
    }

    private UserEntity createTestUser() {

        UserEntity user;

        if (userService.existsByUsernameOrEmail("admin", "admin@domain.com").block()) {
            user = userService.findByUsername("admin").block();
        } else {
            user = UserEntity.builder()
                    .username("admin")
                    .password(bCryptPasswordEncoder.encode("admin123"))
                    .email("admin@domain.com")
                    .role(UserRole.ADMIN)
                    .build();

            userService.save(user).block();
        }

        log.info("Token: {}", jwtUtil.generateToken(user.getUsername(), user.getRole().name()));
        return user;
    }

    private void createTestTask(UserEntity user) {
        taskService.save(TaskEntity.builder()
                        .title("task1")
                        .description("desc1")
                        .assignedTo(user.getUserId())
                        .status(TaskStatus.values()[new Random().nextInt(TaskStatus.values().length)])
                        .priority(TaskPriority.values()[new Random().nextInt(TaskPriority.values().length)])
                        .targetEndDate(LocalDate.now().plusDays(new Random().nextInt(3)))
                        // TODO: Check in Postgres to see if the date is auto-populated
                        .createdOn(Instant.now())
                        .createdBy(user.getUserId())
                        .build())
                .block();
    }
}
