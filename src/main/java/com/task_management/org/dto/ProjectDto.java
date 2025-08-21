package com.task_management.org.dto;

import lombok.Data;

@Data
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private String createdByUsername;
}
