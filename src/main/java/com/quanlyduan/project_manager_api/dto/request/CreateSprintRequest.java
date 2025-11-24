// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateSprintRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateSprintRequest {
    
    private String name; 

    private String goal;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private List<Integer> taskIds; 
}