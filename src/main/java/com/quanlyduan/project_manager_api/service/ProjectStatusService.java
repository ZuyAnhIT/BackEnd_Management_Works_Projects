// File: src/main/java/com/quanlyduan/project_manager_api/service/ProjectStatusService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.ProjectStatusResponse;
import java.util.List;

public interface ProjectStatusService {
    
    /**
     * Lấy danh sách các trạng thái (cột) của một dự án.
     * @param projectId ID dự án
     * @return Danh sách DTO đã sắp xếp
     */
    List<ProjectStatusResponse> getProjectStatuses(Integer projectId);
    
    // (Các hàm create, reorder, delete sẽ làm ở bước sau)
}