package com.quanlyduan.project_manager_api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.request.CreateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSubTaskRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.SubTaskResponse;
import com.quanlyduan.project_manager_api.service.SubTaskService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * Controller xử lý các nghiệp vụ liên quan đến Công việc con (SubTask) của một Task.
 * Các thao tác yêu cầu tuân thủ hệ thống phân cấp: Company -> Workspace -> Project -> Task.
 */
@RestController
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/subtasks")
@CrossOrigin("*")
public class SubTaskController {

    // Khai báo các hằng số thông báo trả về (Response Messages)
    private static final String MSG_FETCH_LIST_SUCCESS = "Successfully retrieved subtask list.";
    private static final String MSG_FETCH_DETAIL_SUCCESS = "Successfully retrieved subtask.";
    private static final String MSG_CREATE_SUCCESS = "Subtask created successfully.";
    private static final String MSG_UPDATE_SUCCESS = "Subtask updated successfully.";
    private static final String MSG_DELETE_SUCCESS = "Subtask deleted successfully.";

    private final SubTaskService subTaskService;

    // Khởi tạo thủ công để tiêm phụ thuộc (Dependency Injection)
    public SubTaskController(SubTaskService subTaskService) {
        this.subTaskService = subTaskService;
    }

    /**
     * Lấy danh sách tất cả các công việc con thuộc một Task cụ thể.
     */
    @Operation(summary = "Get list of subtasks")
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:view')")
    public ResponseEntity<ApiResponse<List<SubTaskResponse>>> getSubTasks(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer taskId) {

        List<SubTaskResponse> subTasks = subTaskService.getSubTasks(companyId, workspaceId, projectId, taskId);
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_LIST_SUCCESS, subTasks));
    }

    /**
     * Xem thông tin chi tiết của một công việc con.
     */
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
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_DETAIL_SUCCESS, subTask));
    }

    /**
     * Tạo mới một công việc con cho Task hiện tại.
     */
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MSG_CREATE_SUCCESS, subTask));
    }

    /**
     * Cập nhật thông tin của một công việc con đã tồn tại.
     */
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
        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_SUCCESS, subTask));
    }

    /**
     * Xóa bỏ một công việc con khỏi Task.
     */
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
        return ResponseEntity.ok(ApiResponse.success(MSG_DELETE_SUCCESS, null));
    }
}