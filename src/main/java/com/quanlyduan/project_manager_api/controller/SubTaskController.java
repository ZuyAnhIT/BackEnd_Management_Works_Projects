package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.*;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;
import com.quanlyduan.project_manager_api.service.SubTaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/subtasks")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "SubTask Management", description = "API quản lý công việc con")
public class SubTaskController {

    private final SubTaskService subTaskService;

    @PostMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:edit')")
    public ResponseEntity<ApiResponse<SubTaskResponse>> createSubTask(
            @PathVariable Integer companyId, @PathVariable Integer workspaceId, @PathVariable Integer projectId, @PathVariable Integer taskId, @Valid @RequestBody CreateSubTaskRequest request) {
        SubTaskResponse subTask = subTaskService.createSubTask(companyId, workspaceId, projectId, taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo thành công", subTask));
    }

    @PutMapping("/{subTaskId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:edit')")
    public ResponseEntity<ApiResponse<SubTaskResponse>> updateSubTask(
            @PathVariable Integer companyId, @PathVariable Integer workspaceId, @PathVariable Integer projectId, @PathVariable Integer taskId, @PathVariable Integer subTaskId, @Valid @RequestBody UpdateSubTaskRequest request) {
        SubTaskResponse subTask = subTaskService.updateSubTask(companyId, workspaceId, projectId, taskId, subTaskId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", subTask));
    }

}
