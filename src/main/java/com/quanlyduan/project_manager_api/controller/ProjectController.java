// File: src/main/java/com/quanlyduan/project_manager_api/controller/ProjectController.java
package com.quanlyduan.project_manager_api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.BoardColumnResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectBacklogResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.security.SecurityService; 
import com.quanlyduan.project_manager_api.service.ProjectService;
import com.quanlyduan.project_manager_api.service.TaskService;
import com.fasterxml.jackson.core.JsonProcessingException; 
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile; 
import io.swagger.v3.oas.annotations.Parameter; 
import io.swagger.v3.oas.annotations.media.Schema; 

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final ObjectMapper objectMapper;

    public ProjectController(ProjectService projectService, SecurityService securityService, TaskService taskService, ObjectMapper objectMapper) {
        this.projectService = projectService;
        this.securityService = securityService;
        this.taskService = taskService;
        this.objectMapper = objectMapper;
    }

    // API TAO DU AN (TICH HOP UPLOAD)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Thêm consumes
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:create')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            
            // Nhận JSON String
            @Parameter(schema = @Schema(implementation = ProjectRequest.class))
            @RequestPart("data") String dataString,
            
            // Nhận file ảnh (Optional)
            @RequestPart(value = "file", required = false) MultipartFile file) {

        // Convert String -> DTO
        ProjectRequest request;
        try {
            request = objectMapper.readValue(dataString, ProjectRequest.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Dữ liệu JSON không hợp lệ: " + e.getMessage()); // Đã dịch
        }

        Integer creatorId = securityService.getCurrentUserId();
        ProjectResponse created = projectService.createProject(companyId, workspaceId, request, creatorId, file);
        
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

    // API CAP NHAT DU AN (TICH HOP UPLOAD)
    @PutMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            
            // Nhận JSON String
            @Parameter(schema = @Schema(implementation = UpdateProjectRequest.class))
            @RequestPart("data") String dataString,
            
            // Nhận file ảnh
            @RequestPart(value = "file", required = false) MultipartFile file) {

        // Convert String -> DTO
        UpdateProjectRequest request;
        try {
            request = objectMapper.readValue(dataString, UpdateProjectRequest.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Dữ liệu JSON không hợp lệ: " + e.getMessage()); // Đã dịch
        }

        ProjectResponse updated = projectService.updateProject(companyId, workspaceId, projectId, request, file);
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin dự án thành công.", updated)); // Đã dịch
    }

    // API XEM MAN HINH BACKLOG (PHAN TRANG + SORT + FILTER NANG CAO)
    @GetMapping("/{projectId}/backlog")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<ProjectBacklogResponse>> getProjectBacklog(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            
            // Các tham số tìm kiếm/lọc (Optional)
            @RequestParam(required = false) String keyword,     // Tìm chung (Tên/Mã)
            @RequestParam(required = false) Integer assigneeId, // Tìm theo người làm
            @RequestParam(required = false) TaskPriority priority, // Tìm theo độ ưu tiên (HIGH, MEDIUM...)
            @RequestParam(required = false) TaskType taskType,     // Tìm theo loại (BUG, STORY...)

            // Các tham số phân trang cho phần Backlog
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        
        ProjectBacklogResponse backlogData = projectService.getProjectBacklog(
            companyId, workspaceId, projectId, keyword, assigneeId, priority, taskType, 
            page, size, sortBy, sortDir
        );
        
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu backlog dự án thành công.", backlogData));
    }

    // API TAO TASK (DÙNG CHUNG CHO CẢ BACKLOG VÀ SPRINT)
    // URL: POST .../projects/{projectId}/tasks
    @PostMapping("/{projectId}/tasks") 
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:create')")
    public ResponseEntity<ApiResponse<TaskSummaryResponse>> createTask(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateTaskRequest request) {
                
        // Service sẽ tự lo việc task này thuộc Sprint nào hay thuộc Backlog
        // dựa trên request.sprintId
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

    // --- US-S4-2 & US-S4-4: XEM BOARD (KÈM FILTER NÂNG CAO) ---
    @GetMapping("/{projectId}/board")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')") // Đã sửa
    public ResponseEntity<ApiResponse<List<BoardColumnResponse>>> getProjectBoard(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            
            // Filter Sprint
            @RequestParam(required = false) Integer sprintId, // null = auto active, 0 = backlog
            
            // Filter Tìm kiếm nâng cao
            @RequestParam(required = false) String keyword,    // Thay cho 'search'
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority, // Dùng Enum
            @RequestParam(required = false) TaskType taskType      // *** ĐÃ THÊM THAM SỐ THIẾU ***
    ) {
        
        List<BoardColumnResponse> board = projectService.getProjectBoard(
            companyId, workspaceId, projectId, 
            sprintId, keyword, assigneeId, priority, taskType
        );
        
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu bảng công việc thành công.", board)); // Đã dịch
    }
   // --- US-S4-8, 9, 11: Xem List Task (Advanced) ---
    @GetMapping("/{projectId}/tasks")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<TaskResponse>>> getProjectTasks(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            
            // Filter Params
            @RequestParam(required = false) Integer sprintId, // 0 = backlog
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority, // Đổi từ String -> Enum
            
            // Pagination & Sorting
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        PageResponseDTO<TaskResponse> tasks = projectService.getProjectTaskList(
                companyId, workspaceId, projectId, 
                sprintId, search, assigneeId, priority, 
                page, size, sortBy, sortDir
        );
        
        return ResponseEntity.ok(ApiResponse.success("Fetched project tasks", tasks));
    }
}