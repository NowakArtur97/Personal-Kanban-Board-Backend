package com.nowakartur97.personalkanbanboardbackend;

import com.nowakartur97.personalkanbanboardbackend.task.TaskEntity;
import com.nowakartur97.personalkanbanboardbackend.task.TaskPriority;
import com.nowakartur97.personalkanbanboardbackend.task.TaskService;
import com.nowakartur97.personalkanbanboardbackend.task.TaskStatus;
import com.nowakartur97.personalkanbanboardbackend.user.UserEntity;
import com.nowakartur97.personalkanbanboardbackend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
public class PersonalKanbanBoardBackendApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(PersonalKanbanBoardBackendApplication.class, args);
	}

	private final UserService userService;
	private final TaskService taskService;

	@Override
	public void run(String... args) {

		UserEntity user = new UserEntity();
		user.setUsername("user-" + UUID.randomUUID());
		user.setPassword("pass1");
		user.setEmail("user" + UUID.randomUUID() + "@domain.com");

		userService.saveUser(user).block();

		TaskEntity task = new TaskEntity();
		task.setTitle("task1");
		task.setDescription("desc1");
		task.setStatus(TaskStatus.IN_PROGRESS);
		task.setPriority(TaskPriority.MEDIUM);
		task.setTargetEndDate(new Date());
		task.setAssignedTo(user.getUserId());
		task.setCreatedOn(new Date());
		task.setCreatedBy(user.getUserId());

		taskService.saveTask(task).block();
	}
}
