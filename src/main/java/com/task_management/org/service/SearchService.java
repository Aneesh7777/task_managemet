package com.task_management.org.service;
import com.task_management.org.document.ProjectDocument;
import com.task_management.org.document.TaskDocument;
import com.task_management.org.repository.ProjectSearchRepository;
import com.task_management.org.repository.TaskSearchRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final ProjectSearchRepository projectSearchRepository;
    private final TaskSearchRepository taskSearchRepository;

    public SearchService(ProjectSearchRepository projectSearchRepository, TaskSearchRepository taskSearchRepository) {
        this.projectSearchRepository = projectSearchRepository;
        this.taskSearchRepository = taskSearchRepository;
    }

    public Map<String, Object> search(String query) {
        List<ProjectDocument> projects = projectSearchRepository.findByNameContainingOrDescriptionContaining(query, query);
        List<TaskDocument> tasks = taskSearchRepository.findByTitleContainingOrDescriptionContaining(query, query);

        Map<String, Object> results = new HashMap<>();
        results.put("projects", projects);
        results.put("tasks", tasks);

        return results;
    }
}