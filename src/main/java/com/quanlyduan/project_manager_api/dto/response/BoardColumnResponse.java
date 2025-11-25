// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/BoardColumnResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnResponse {
    private Integer statusId;
    private String statusName;
    private String color;
    private Integer order; 
    
    private Boolean isCompleted;
    private List<TaskResponse> tasks; 
}
