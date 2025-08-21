package com.task_management.org.repository;

import com.task_management.org.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Inherits all standard CRUD methods
    @Query("SELECT p FROM Project p JOIN FETCH p.createdBy")
    List<Project> findAllWithCreatedBy();

}
