// File: src/main/java/com/quanlyduan/project_manager_api/service/SprintService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.SprintDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintResponse;
import java.util.List;

public interface SprintService {
    // US-S3-6
    SprintResponse createSprint(Integer projectId, CreateSprintRequest request);

    // US-S3-8
    SprintResponse startSprint(Integer projectId, Integer sprintId);
    
    SprintResponse completeSprint(Integer projectId, Integer sprintId);

    // lay danh sach sprint theo project     
    List<SprintResponse> getSprintsByProject(Integer projectId, String status); 
    // (Helper cho Security)
    Integer getProjectIdBySprint(Integer sprintId);

   /**
     * API XEM CHI TIẾT SPRINT
     * (Bao gồm danh sách task trong Sprint đó)
     * @param projectId ID của Project (để kiểm tra bảo mật)
     * @param sprintId ID của Sprint
     * @return Chi tiết Sprint
     */
    SprintDetailsResponse getSprintDetails(Integer projectId, Integer sprintId);

    /**
     * Cập nhật thông tin Sprint (Tên, Mục tiêu, Ngày tháng).
     */
    SprintResponse updateSprint(Integer projectId, Integer sprintId, UpdateSprintRequest request);

    /**
     * Xử lý xóa Sprint thông minh (Hard Delete hoặc Cancel).
     */
    void deleteSprint(Integer projectId, Integer sprintId);
}
