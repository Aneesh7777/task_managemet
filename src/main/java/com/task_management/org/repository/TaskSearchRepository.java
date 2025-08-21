package com.task_management.org.repository;


import com.task_management.org.document.TaskDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface TaskSearchRepository extends ElasticsearchRepository<TaskDocument, Long> {

    List<TaskDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
}