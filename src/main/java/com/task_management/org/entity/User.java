package com.task_management.org.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;


    // A list of all tasks assigned to this user.
    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    private List<Task> assignedTasks;

    // A list of all projects created by this user (only applies to ADMINs).
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Project> createdProjects;
}
