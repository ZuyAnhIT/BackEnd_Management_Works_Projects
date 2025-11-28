// File: src/main/java/com/quanlyduan/project_manager_api/controller/SubTaskController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;
import com.quanlyduan.project_manager_api.service.SubTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks/{taskId}/subtasks")
@CrossOrigin("*")
public class SubTaskController {

    private final SubTaskService subTaskService;

    public SubTaskController(SubTaskService subTaskService) {
        this.subTaskService = subTaskService;
    }

    /**
     * TẠO SUBTASK MỚI
     * POST /api/tasks/{taskId}/subtasks
     */
    @PostMapping
    @PreAuthorize("@securityService.hasTaskPermission(#taskId, 'task:edit')")
    public ResponseEntity<ApiResponse<SubTaskResponse>> createSubTask(
            @PathVariable Integer taskId,
            @Valid @RequestBody CreateSubTaskRequest request) {
        
        SubTaskResponse newSubTask = subTaskService.createSubTask(taskId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Create subtask successful.", newSubTask));
    }
}