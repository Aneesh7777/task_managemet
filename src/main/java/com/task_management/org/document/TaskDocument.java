package com.task_management.org.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.Date;
@Data
@Document(indexName = "tasks")
public class TaskDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, name = "title")
    private String title;

    @Field(type = FieldType.Text, name = "description")
    private String description;

    @Field(type = FieldType.Keyword, name = "status")
    private String status;

    @Field(type = FieldType.Date, name = "dueDate")
    private LocalDate dueDate;

    @Field(type = FieldType.Long, name = "projectId")
    private Long projectId;

    @Field(type = FieldType.Keyword, name = "assigneeUsername")
    private String assigneeUsername;


}