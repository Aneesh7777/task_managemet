package com.task_management.org.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private String assigneeUsername;
    private Long projectId;
    private Long assigneeId; // Used for creating/updating a task
}
