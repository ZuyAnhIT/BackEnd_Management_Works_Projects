// File: src/main/java/com/quanlyduan/project_manager_api/controller/SprintController.java
// (MỚI) Controller cho Sprint
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.*;
import com.quanlyduan.project_manager_api.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api") // Dùng root /api
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    /**
     * US-S3-6: Tạo Sprint mới (trong 1 project)
     */
    @PostMapping("/projects/{projectId}/sprints")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'sprint:create')")
    public ResponseEntity<ApiResponse<SprintResponse>> createSprint(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateSprintRequest request) {
        
        // Gán projectId từ Path vào DTO
        // request.setProjectId(projectId);
        SprintResponse sprint = sprintService.createSprint(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sprint created", sprint));
    }

}
