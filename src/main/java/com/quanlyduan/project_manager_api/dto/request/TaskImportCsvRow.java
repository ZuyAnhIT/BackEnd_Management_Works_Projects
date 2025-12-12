// src/main/java/com/quanlyduan/project_manager_api/dto/request/TaskImportCsvRow.java
package com.quanlyduan.project_manager_api.dto.request;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class TaskImportCsvRow {
    
    @CsvBindByName(column = "Title", required = true)
    private String title;

    @CsvBindByName(column = "Description")
    private String description;

    @CsvBindByName(column = "Assignee Email")
    private String assigneeEmail; 
    @CsvBindByName(column = "Priority") 
    private String priority;

    @CsvBindByName(column = "Status") 
    private String statusName;

    @CsvBindByName(column = "Due Date") 
    private String dueDate;

    @CsvBindByName(column = "Story Points")
    private Integer storyPoints;
    
    @CsvBindByName(column = "Estimated Hours")
    private Double estimatedHours;
}