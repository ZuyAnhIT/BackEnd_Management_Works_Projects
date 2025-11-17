// File: src/main/java/com/quanlyduan/project_manager_api/controller/ProjectController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.security.SecurityService; 
import com.quanlyduan.project_manager_api.service.ProjectService;
import com.quanlyduan.project_manager_api.service.TaskService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;

@RestController
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects")
@CrossOrigin("*")

public class ProjectController {

    private final ProjectService projectService;
    private final SecurityService securityService; 
    private final TaskService taskService;
    
    public ProjectController(ProjectService projectService, SecurityService securityService, TaskService taskService) {
        this.projectService = projectService;
        this.securityService = securityService;
        this.taskService = taskService;
    }

    /**
     * US7 – API tạo Project mới trong Workspace.
     * Quyền truy cập:
     * @PreAuthorize("@securityService.hasWorkspacePermission(#workspaceId, 'project:create')")
     * Nghiệp vụ tóm tắt:
     * - Nhận ProjectRequest (name, projectCode bắt buộc; các trường khác tùy chọn).
     * - Lấy user hiện tại từ securityService để gán createdBy.
     * - Ủy quyền cho ProjectService xử lý (validate, unique, reference mapping, save).
     * Kết quả:
     * - 201 Created + ApiResponse<ProjectResponse> chứa thông tin project vừa tạo.
     */
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:create')") // Đã sửa
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody ProjectRequest request) {

        Integer creatorId = securityService.getCurrentUserId(); // Đã sửa
        ProjectResponse created = projectService.createProject(companyId, workspaceId, request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo dự án thành công.", created)); // Đã dịch
    }

    /**
     * US8 – API xem danh sách Project trong một Workspace.
     * Quyền truy cập:
     * @PreAuthorize("@securityService.hasWorkspacePermission(#workspaceId, 'workspace:view')")
     * Nghiệp vụ tóm tắt:
     * - Xác thực (ở service) rằng workspace thuộc companyId để ngăn truy cập chéo công ty.
     * - Lấy danh sách dự án, loại bỏ dự án CANCELLED (ẩn dự án đã hủy) và trả về dạng DTO.
     * Kết quả:
     * - 200 OK + ApiResponse<List<ProjectResponse>>.
     */
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:view')") // Đã sửa
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> listProjects(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        List<ProjectResponse> data = projectService.listProjectsByWorkspace(companyId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách dự án thành công.", data)); // Đã dịch
    }

    /**
     * Project Trash – API xem danh sách dự án bị hủy (CANCELLED) trong một Workspace.
     * Quyền truy cập:
     * @PreAuthorize("@securityService.hasWorkspacePermission(#workspaceId, 'workspace:view')")
     * Nghiệp vụ tóm tắt:
     * - Xác thực workspace thuộc companyId ở tầng service.
     * - Trả về chỉ các dự án có status = CANCELLED.
     * Kết quả:
     * - 200 OK + ApiResponse<List<ProjectResponse>>.
     */
    @GetMapping("/trash")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:view')") // Đã sửa
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> listTrashedProjects(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        List<ProjectResponse> data = projectService.listCancelledProjectsByWorkspace(companyId, workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách dự án đã hủy thành công.", data)); // Đã dịch
    }

    /**
     * US9 – API xóa (soft delete) Project: chuyển trạng thái dự án sang CANCELLED.
     * Quyền truy cập:
     * @PreAuthorize("@securityService.hasProjectPermission(#projectId, 'project:delete')")
     * Nghiệp vụ tóm tắt:
     * - Xác thực workspace thuộc companyId, và project thuộc workspace (service làm).
     * - Đặt status = CANCELLED, không xóa cứng.
     * Kết quả:
     * - 200 OK + ApiResponse null-data với message thành công.
     */
    @DeleteMapping("/{projectId}")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:delete')") // Đã sửa
    public ResponseEntity<ApiResponse<Object>> deleteProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {

        projectService.deleteProject(companyId, workspaceId, projectId);
        return ResponseEntity.ok(ApiResponse.success("Hủy dự án thành công.", null)); // Đã dịch
    }
    
    @GetMapping("/{projectId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')") // Đã sửa
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {
        ProjectResponse response = projectService.getProjectDetails(companyId, workspaceId, projectId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết dự án thành công.", response)); // Đã dịch
    }

    @PutMapping("/{projectId}/status")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')") // Đã sửa
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Valid @RequestBody UpdateProjectStatusRequest request) {

        ProjectResponse updated = projectService.updateProjectStatus(companyId, workspaceId, projectId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái dự án thành công.", updated)); // Đã dịch
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')") // Đã sửa
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Valid @RequestBody UpdateProjectRequest request) {

        ProjectResponse updated = projectService.updateProject(companyId, workspaceId, projectId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin dự án thành công.", updated)); // Đã dịch
    }

    // API LAY DANH SACH BACKLOG CUA DU AN
    @GetMapping("/{projectId}/backlog")
    // Bảo vệ: Chỉ thành viên dự án (project:view) mới được xem
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<TaskSummaryResponse>>> getProjectBacklog(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {
        
        List<TaskSummaryResponse> backlog = projectService.getProjectBacklog(companyId, workspaceId, projectId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy backlog dự án thành công.", backlog)); 
    }

    // API TAO TASK MOI
    @PostMapping("/{projectId}/tasks") 
    // Bảo vệ: Yêu cầu quyền 'task:create' trong dự án
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:create')")
    public ResponseEntity<ApiResponse<TaskSummaryResponse>> createTask(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateTaskRequest request) {
                
        // Chúng ta không cần companyId và workspaceId ở đây
        // vì projectService.createTask sẽ tự tìm
        TaskSummaryResponse newTask = taskService.createTask(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo công việc mới thành công.", newTask));
    }


    // API LAY DANH SACH THANH VIEN TRONG DU AN
    @GetMapping("/{projectId}/members")
    // Bảo vệ: Chỉ thành viên dự án (project:view) mới được xem
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {
        
        List<ProjectMemberResponse> members = projectService.getProjectMembers(projectId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành viên dự án thành công.", members)); // Đã dịch
    }


    // API CAP NHAT VAI TRO THANH VIEN DU AN
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:manage_roles')")
    @PutMapping("/{projectId}/members/{memberId}/role")
    public ResponseEntity<ApiResponse<Object>> updateProjectMemberRole(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer memberId,
            @Valid @RequestBody RoleUpdateRequest request) { // Tái sử dụng DTO

        // 1. Gọi service
        ProjectMemberResponse updatedMember = projectService.updateProjectMemberRole(projectId, memberId, request.getRoleCode());

        // 2. Tạo message động
        String message = String.format("Cập nhật vai trò cho người dùng '%s' (ID: %d) thành '%s' thành công.",
            updatedMember.getFullName(),
            updatedMember.getUserId(),
            updatedMember.getRoleName()
        );

        // 3. Tạo data trả về
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", updatedMember.getUserId());
        responseData.put("fullName", updatedMember.getFullName());
        responseData.put("newRoleCode", request.getRoleCode());
        responseData.put("newRoleName", updatedMember.getRoleName());

        return ResponseEntity.ok(ApiResponse.success(message, responseData));


    }
}