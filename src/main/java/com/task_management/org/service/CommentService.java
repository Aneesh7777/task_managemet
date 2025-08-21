//// src/main/java/com/task_management/org/service/CommentService.java
//package com.task_management.org.service;
//
//import com.task_management.org.dto.CommentDto;
//import com.task_management.org.entity.Comment;
//import com.task_management.org.entity.Task;
//import com.task_management.org.entity.User;
//import com.task_management.org.repository.CommentRepository;
//import com.task_management.org.repository.TaskRepository;
//import com.task_management.org.repository.UserRepository;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class CommentService {
//
//    private final CommentRepository commentRepository;
//    private final TaskRepository taskRepository;
//    private final UserRepository userRepository;
//
//    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository, UserRepository userRepository) {
//        this.commentRepository = commentRepository;
//        this.taskRepository = taskRepository;
//        this.userRepository = userRepository;
//    }
//
//    @Transactional
//    @CacheEvict(value = "comments", key = "#commentDto.taskId")
//    public CommentDto createComment(CommentDto commentDto) {
//        Task task = taskRepository.findById(commentDto.getTaskId())
//                .orElseThrow(() -> new RuntimeException("Task not found"));
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        User author = userRepository.findByUsername(username)
//                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
//
//        Comment comment = new Comment();
//        comment.setContent(commentDto.getContent());
//        comment.setTask(task);
//        comment.setAuthor(author);
//
//        Comment savedComment = commentRepository.save(comment);
//        return convertToDto(savedComment);
//    }
//
//    @Transactional(readOnly = true)
//    @Cacheable(value = "comments", key = "#taskId")
//    public List<CommentDto> getCommentsByTaskId(Long taskId) {
//        System.out.println("--- Fetching comments for task " + taskId + " from DATABASE ---");
//        return commentRepository.findByTaskId(taskId).stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }
//
//    private CommentDto convertToDto(Comment comment) {
//        CommentDto dto = new CommentDto();
//        dto.setId(comment.getId());
//        dto.setContent(comment.getContent());
//        dto.setCreatedAt(comment.getCreatedAt());
//        dto.setTaskId(comment.getTask().getId());
//        if (comment.getAuthor() != null) {
//            dto.setAuthorUsername(comment.getAuthor().getUsername());
//        }
//        return dto;
//    }
//}
// src/main/java/com/task_management/org/service/CommentService.java
package com.task_management.org.service;

import com.task_management.org.dto.CommentDto;
import com.task_management.org.entity.Comment;
import com.task_management.org.entity.Task;
import com.task_management.org.entity.User;
import com.task_management.org.repository.CommentRepository;
import com.task_management.org.repository.TaskRepository;
import com.task_management.org.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @CacheEvict(value = "comments", key = "#commentDto.taskId")
    public CommentDto createComment(CommentDto commentDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User author = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Task task = taskRepository.findById(commentDto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + commentDto.getTaskId()));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().equals(author);

        if (!isAdmin && !isAssignee) {
            throw new AccessDeniedException("You must be an admin or the assignee of the task to comment.");
        }

        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setTask(task);
        comment.setAuthor(author);

        Comment savedComment = commentRepository.save(comment);
        return convertToDto(savedComment);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#taskId")
    public List<CommentDto> getCommentsByTaskId(Long taskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // --- AUTHORIZATION LOGIC ---
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().equals(currentUser);

        if (!isAdmin && !isAssignee) {
            throw new AccessDeniedException("You can only view comments for tasks you are assigned to.");
        }
        // --- END AUTHORIZATION LOGIC ---

        System.out.println("--- Fetching comments for task " + taskId + " from DATABASE ---");
        return commentRepository.findByTaskId(taskId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setTaskId(comment.getTask().getId());
        if (comment.getAuthor() != null) {
            dto.setAuthorUsername(comment.getAuthor().getUsername());
        }
        return dto;
    }
}
