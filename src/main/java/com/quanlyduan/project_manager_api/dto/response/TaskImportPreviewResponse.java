// src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskImportPreviewResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TaskImportPreviewResponse {
    private int rowIndex;
    // Dữ liệu thô để hiển thị input
    private String title;
    private String description;
    private String assigneeEmail;
    private String priority;
    private String statusName;
    private String dueDate;
    private Integer storyPoints;
    private Double estimatedHours;
    
    // Trạng thái hợp lệ
    private boolean isValid;
    private List<String> errors; // Danh sách lỗi của dòng này
}