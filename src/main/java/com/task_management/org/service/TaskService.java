// src/main/java/com/task_management/org/service/TaskService.java
package com.task_management.org.service;

import com.task_management.org.document.TaskDocument;
import com.task_management.org.dto.TaskDto;
import com.task_management.org.entity.Project;
import com.task_management.org.entity.Task;
import com.task_management.org.entity.User;
import com.task_management.org.repository.ProjectRepository;
import com.task_management.org.repository.TaskRepository;
import com.task_management.org.repository.TaskSearchRepository;
import com.task_management.org.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskSearchRepository taskSearchRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository, TaskSearchRepository taskSearchRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskSearchRepository = taskSearchRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskDto createTask(TaskDto taskDto) {
        Project project = projectRepository.findById(taskDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User assignee = userRepository.findById(taskDto.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Assignee user not found"));

        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setStatus(taskDto.getStatus());
        task.setDueDate(taskDto.getDueDate());
        task.setProject(project);
        task.setAssignee(assignee);

        Task savedTask = taskRepository.save(task);
        TaskDocument taskDocument = new TaskDocument();
        taskDocument.setId(savedTask.getId());
        taskDocument.setTitle(savedTask.getTitle());
        taskDocument.setDescription(savedTask.getDescription());
        taskDocument.setStatus(savedTask.getStatus());
        taskDocument.setDueDate(savedTask.getDueDate());
        taskDocument.setProjectId(savedTask.getProject().getId());
        if (savedTask.getAssignee() != null) {
            taskDocument.setAssigneeUsername(savedTask.getAssignee().getUsername());
        }
        taskSearchRepository.save(taskDocument);

        return convertToDto(savedTask);

    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#id")
    public TaskDto getTaskById(Long id) {
        System.out.println("--- Fetching task " + id + " from DATABASE ---");
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        return convertToDto(task);
    }

    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate());
        dto.setProjectId(task.getProject().getId());
        if (task.getAssignee() != null) {
            dto.setAssigneeId(task.getAssignee().getId());
            dto.setAssigneeUsername(task.getAssignee().getUsername());
        }
        return dto;
    }


    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '-mytasks'")
    public List<TaskDto> getMyTasks() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return taskRepository.findByAssignee(currentUser).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#id"),
            @CacheEvict(value = "tasks", key = "#result.assigneeUsername + '-mytasks'")
    })
    public TaskDto updateTask(Long id, TaskDto taskDto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        User assignee = userRepository.findById(taskDto.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Assignee user not found with id: " + taskDto.getAssigneeId()));
        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setDueDate(taskDto.getDueDate());
        existingTask.setAssignee(assignee);

        Task updatedTask = taskRepository.save(existingTask);
        TaskDocument taskDocument = new TaskDocument();
        taskDocument.setId(updatedTask.getId());
        taskDocument.setTitle(updatedTask.getTitle());
        taskDocument.setDescription(updatedTask.getDescription());
        taskDocument.setStatus(updatedTask.getStatus());
        taskDocument.setDueDate(updatedTask.getDueDate());
        taskDocument.setProjectId(updatedTask.getProject().getId());
        if (updatedTask.getAssignee() != null) {
            taskDocument.setAssigneeUsername(updatedTask.getAssignee().getUsername());
        }
        taskSearchRepository.save(taskDocument);

        return convertToDto(updatedTask);
    }





    }
