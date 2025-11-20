// File: src/main/java/com/quanlyduan/project_manager_api/controller/ProjectController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
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

    // API 1: LẤY DANH SÁCH (Cơ bản)
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<ProjectResponse>>> listProjects(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @RequestParam(required = false) ProjectStatus status, // Filter đơn giản
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponseDTO<ProjectResponse> projects = projectService.listProjectsByWorkspace(companyId, workspaceId, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách dự án thành công.", projects));
    }

    // API 2: TÌM KIẾM (Nâng cao - MỚI)
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<ProjectResponse>>> searchProjects(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,

            // Các tham số tìm kiếm
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String manager,
            @RequestParam(required = false) ProjectStatus status,

            // Các tham số phân trang
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        
        PageResponseDTO<ProjectResponse> results = projectService.searchProjects(
            companyId, workspaceId, name, code, manager, status,
            page, size, sortBy, sortDir
        );
        
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm dự án thành công.", results)); // Đã dịch
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


    // API 1: LAY DANH SACH THANH VIEN (Cơ bản)
    @GetMapping("/{projectId}/members")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        
        PageResponseDTO<ProjectMemberResponse> members = projectService.getProjectMembers(projectId, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành viên dự án thành công.", members));
    }

    // API 2: TIM KIEM THANH VIEN (Nâng cao)
    @GetMapping("/{projectId}/members/search")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<ProjectMemberResponse>>> searchProjectMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,

            // Các tham số tìm kiếm
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String phone,

            // Các tham số phân trang
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        
        PageResponseDTO<ProjectMemberResponse> members = projectService.searchProjectMembers(
            projectId, name, email, role, phone, 
            page, size, sortBy, sortDir
        );
        
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm thành viên dự án thành công.", members));
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