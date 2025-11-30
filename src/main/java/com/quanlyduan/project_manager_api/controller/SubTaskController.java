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
// Endpoint phụ thuộc vào các cấp cha: Company -> Workspace -> Project -> Task
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/subtasks")
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến SubTask (Công việc con) của một Task cụ thể.
 * Tất cả các thao tác đều yêu cầu kiểm tra tính hợp lệ của hệ thống phân cấp (Hierarchy).
 */
public class SubTaskController {

    private final SubTaskService subTaskService;

    public SubTaskController(SubTaskService subTaskService) {
        this.subTaskService = subTaskService;
    }

    // ======================================================
    // 1. LẤY DANH SÁCH SUBTASK (LIST SUBTASKS)
    // ======================================================
    @Operation(summary = "Get list of subtasks")
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:view')")
    public ResponseEntity<ApiResponse<List<SubTaskResponse>>> getSubTasks(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer taskId) {

        List<SubTaskResponse> subTasks = subTaskService.getSubTasks(companyId, workspaceId, projectId, taskId);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved subtask list.", subTasks));
    }

    // ======================================================
    // 2. XEM CHI TIẾT SUBTASK (GET DETAIL)
    // ======================================================
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
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved subtask.", subTask));
    }

    // ======================================================
    // 3. TẠO SUBTASK MỚI (CREATE SUBTASK)
    // ======================================================
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
        // Sửa thông báo trả về sang tiếng Anh (201 Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Subtask created successfully.", subTask));
    }

    // ======================================================
    // 4. CẬP NHẬT SUBTASK (UPDATE SUBTASK)
    // ======================================================
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
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Subtask updated successfully.", subTask));
    }

    // ======================================================
    // 5. XÓA SUBTASK (DELETE SUBTASK)
    // ======================================================
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
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Subtask deleted successfully.", null));
    }
}