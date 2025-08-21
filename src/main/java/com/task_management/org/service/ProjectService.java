// src/main/java/com/task_management/org/service/ProjectService.java
package com.task_management.org.service;

import com.task_management.org.dto.ProjectDto;
import com.task_management.org.dto.ProjectRequestDto;
import com.task_management.org.entity.Project;
import com.task_management.org.entity.User;
import com.task_management.org.repository.ProjectRepository;
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

import com.task_management.org.document.ProjectDocument;
import com.task_management.org.repository.ProjectSearchRepository;


@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectSearchRepository projectSearchRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, ProjectSearchRepository projectSearchRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectSearchRepository = projectSearchRepository;
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectDto createProject(ProjectRequestDto requestDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Project project = new Project();
        project.setName(requestDto.getName());
        project.setDescription(requestDto.getDescription());
        project.setCreatedBy(currentUser);

        Project savedProject = projectRepository.save(project);
        ProjectDocument projectDocument = new ProjectDocument();
        projectDocument.setId(savedProject.getId());
        projectDocument.setName(savedProject.getName());
        projectDocument.setDescription(savedProject.getDescription());
        projectDocument.setCreatedByUsername(savedProject.getCreatedBy().getUsername());
        projectSearchRepository.save(projectDocument);

        return convertToDto(savedProject);
    }

    @Transactional(readOnly = true)
    @Cacheable("projects")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProjectDto> getAllProjects() {
        System.out.println("--- Fetching all projects from DATABASE ---");
        return projectRepository.findAllWithCreatedBy().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#id")
    public ProjectDto getProjectById(Long id) {
        System.out.println("--- Fetching project " + id + " from DATABASE ---");
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return convertToDto(project);
    }

    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projects", allEntries = true)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectDto updateProject(Long id, ProjectRequestDto projectDetails) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());

        Project updatedProject = projectRepository.save(project);
        ProjectDocument projectDocument = new ProjectDocument();
        projectDocument.setId(updatedProject.getId());
        projectDocument.setName(updatedProject.getName());
        projectDocument.setDescription(updatedProject.getDescription());
        if (updatedProject.getCreatedBy() != null) {
            projectDocument.setCreatedByUsername(updatedProject.getCreatedBy().getUsername());
        }
        projectSearchRepository.save(projectDocument);

        return convertToDto(updatedProject);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projects", allEntries = true)
    })
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
        projectSearchRepository.deleteById(id);
    }

    private ProjectDto convertToDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        if (project.getCreatedBy() != null) {
            dto.setCreatedByUsername(project.getCreatedBy().getUsername());
        }
        return dto;
    }



}
