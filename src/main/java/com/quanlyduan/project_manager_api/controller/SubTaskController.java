// File: src/main/java/com/quanlyduan/project_manager_api/controller/SubTaskController.java
package com.quanlyduan.project_manager_api.controller;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.request.CreateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;
import com.quanlyduan.project_manager_api.service.SubTaskService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/subtasks")
@CrossOrigin("*")
public class SubTaskController {

    private final SubTaskService subTaskService;

    public SubTaskController(SubTaskService subTaskService) {
        this.subTaskService = subTaskService;
    }

    @Operation(summary = "Get list of subtasks")
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:view')")
    public ResponseEntity<ApiResponse<List<SubTaskResponse>>> getSubTasks(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer taskId) {

        List<SubTaskResponse> subTasks = subTaskService.getSubTasks(companyId, workspaceId, projectId, taskId);
        return ResponseEntity.ok(ApiResponse.success("Success", subTasks));
    }

    @Operation(summary = "Get subtask detail")
    @GetMapping("/{subTaskId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:view')")
    public ResponseEntity<ApiResponse<SubTaskResponse>> getSubTaskDetail(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer taskId,
            @PathVariable Integer subTaskId) {

        SubTaskResponse subTask = subTaskService.getSubTaskDetail(companyId, workspaceId, projectId, taskId, subTaskId);
        return ResponseEntity.ok(ApiResponse.success("Success", subTask));
    }

    @Operation(summary = "Create new subtask")
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:edit')")
    public ResponseEntity<ApiResponse<SubTaskResponse>> createSubTask(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer taskId,
            @Valid @RequestBody CreateSubTaskRequest request) {

        SubTaskResponse subTask = subTaskService.createSubTask(companyId, workspaceId, projectId, taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created successfully", subTask));
    }

    @Operation(summary = "Update subtask")
    @PutMapping("/{subTaskId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:edit')")
    public ResponseEntity<ApiResponse<SubTaskResponse>> updateSubTask(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer taskId,
            @PathVariable Integer subTaskId,
            @Valid @RequestBody UpdateSubTaskRequest request) {

        SubTaskResponse subTask = subTaskService.updateSubTask(companyId, workspaceId, projectId, taskId, subTaskId, request);
        return ResponseEntity.ok(ApiResponse.success("Updated successfully", subTask));
    }

    @Operation(summary = "Delete subtask")
    @DeleteMapping("/{subTaskId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteSubTask(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer taskId,
            @PathVariable Integer subTaskId) {

        subTaskService.deleteSubTask(companyId, workspaceId, projectId, taskId, subTaskId);
        return ResponseEntity.ok(ApiResponse.success("Deleted successfully", null));
    }
}
