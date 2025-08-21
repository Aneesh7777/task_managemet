package com.task_management.org.repository;


import com.task_management.org.document.ProjectDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProjectSearchRepository extends ElasticsearchRepository<ProjectDocument, Long> {

    List<ProjectDocument> findByNameContainingOrDescriptionContaining(String name, String description);
}