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
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@SpringBootApplication
@ConfigurationPropertiesScan
@RequiredArgsConstructor
@Slf4j
public class PersonalKanbanBoardBackendApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PersonalKanbanBoardBackendApplication.class, args);
    }

    private final UserService userService;
    private final TaskService taskService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTUtil jwtUtil;

    @Override
    public void run(String... args) {

//        UserEntity user = createUser();
//        createTask(user);
    }

    private UserEntity createUser() {
        UserEntity user = new UserEntity("user", bCryptPasswordEncoder.encode("pass1"),
                "user@domain.com", UserRole.USER);

        userService.saveUser(user).block();

//        UserEntity user = userService.findByUsername("user").block();
        log.info("Token: {}", jwtUtil.generateToken(user.getUsername(), user.getRole().name()));
        return user;
    }

    private void createTask(UserEntity user) {
        TaskEntity task = new TaskEntity();
        task.setTitle("task1");
        task.setDescription("desc1");
        task.setStatus(TaskStatus.values()[new Random().nextInt(TaskStatus.values().length)]);
        task.setPriority(TaskPriority.values()[new Random().nextInt(TaskPriority.values().length)]);
        task.setTargetEndDate(LocalDate.now().plus(new Random().nextInt(3), ChronoUnit.DAYS));
        task.setAssignedTo(user.getUserId());
        task.setCreatedOn(LocalDate.now());
        task.setCreatedBy(user.getUserId());

        taskService.saveTask(task).block();
    }
}
