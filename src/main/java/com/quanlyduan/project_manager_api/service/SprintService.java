// File: src/main/java/com/quanlyduan/project_manager_api/service/SprintService.java
// (MỚI)
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.SprintResponse;
import java.util.List;

public interface SprintService {
    // US-S3-6
    SprintResponse createSprint(Integer projectId, CreateSprintRequest request);

    // US-S3-8
    SprintResponse startSprint(Integer sprintId);

    // (Helper cho Security)
    Integer getProjectIdBySprint(Integer sprintId);
}
