// File: src/main/java/com/quanlyduan/project_manager_api/service/SprintService.java
// (MỚI)
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.SprintDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintResponse;
import java.util.List;

public interface SprintService {
    // US-S3-6
    SprintResponse createSprint(Integer projectId, CreateSprintRequest request);

    // US-S3-8
    SprintResponse startSprint(Integer projectId, Integer sprintId);
    SprintResponse completeSprint(Integer projectId, Integer sprintId);
    SprintResponse cancelSprint(Integer projectId, Integer sprintId);
    // lay danh sach sprint theo project     
    List<SprintResponse> getSprintsByProject(Integer projectId, String status); // <-- THÊM STATUS
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
}
