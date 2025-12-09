// File: src/main/java/com/quanlyduan/project_manager_api/controller/ProjectController.java
package com.quanlyduan.project_manager_api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteProjectMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ActivityLogResponse;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.BoardColumnResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectBacklogResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.ProjectService;
import com.quanlyduan.project_manager_api.service.TaskService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects")
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến Dự án (Project) trong Workspace.
 */
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

    // ========================================================================
    // A. QUẢN LÝ PROJECT (CRUD)
    // ========================================================================

    // API TẠO DỰ ÁN (TICH HOP UPLOAD)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
            // Sửa thông báo trả về sang tiếng Anh
            throw new BadRequestException("Invalid JSON data: " + e.getMessage());
        }

        Integer creatorId = securityService.getCurrentUserId();
        ProjectResponse created = projectService.createProject(companyId, workspaceId, request, creatorId, file);

        // Sửa thông báo trả về sang tiếng Anh (201 Created)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully.", created));
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
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project list retrieved successfully.", projects));
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

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project search successful.", results));
    }

    // API LẤY CHI TIẾT DỰ ÁN
    @GetMapping("/{projectId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {
        ProjectResponse response = projectService.getProjectDetails(companyId, workspaceId, projectId);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project details retrieved successfully.", response));
    }

    // API CẬP NHẬT DỰ ÁN (TICH HOP UPLOAD)
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
            // Sửa thông báo trả về sang tiếng Anh
            throw new BadRequestException("Invalid JSON data: " + e.getMessage());
        }

        ProjectResponse updated = projectService.updateProject(companyId, workspaceId, projectId, request, file);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project information updated successfully.", updated));
    }

    // API CẬP NHẬT TRẠNG THÁI DỰ ÁN (STATUS)
    @PutMapping("/{projectId}/status")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Valid @RequestBody UpdateProjectStatusRequest request) {

        ProjectResponse updated = projectService.updateProjectStatus(companyId, workspaceId, projectId, request);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project status updated successfully.", updated));
    }

    // API XÓA (soft delete) Project: chuyển trạng thái dự án sang CANCELLED.
    @DeleteMapping("/{projectId}")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {

        projectService.deleteProject(companyId, workspaceId, projectId);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project cancelled successfully.", null));
    }

    // ========================================================================
    // B. QUẢN LÝ TASK (BACKLOG, BOARD, LIST)
    // ========================================================================

    // API XEM MÀN HÌNH BACKLOG (PHAN TRANG + SORT + FILTER NANG CAO)
    @GetMapping("/{projectId}/backlog")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<ProjectBacklogResponse>> getProjectBacklog(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,

            // Các tham số tìm kiếm/lọc (Optional)
            @RequestParam(required = false) String keyword,      // Tìm chung (Tên/Mã)
            @RequestParam(required = false) Integer assigneeId, // Tìm theo người làm
            @RequestParam(required = false) TaskPriority priority, // Tìm theo độ ưu tiên
            @RequestParam(required = false) TaskType taskType,      // Tìm theo loại

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

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project backlog data retrieved successfully.", backlogData));
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
        TaskSummaryResponse newTask = taskService.createTask(projectId, request);

        // Sửa thông báo trả về sang tiếng Anh (201 Created)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("New task created successfully.", newTask));
    }

    // --- XEM BOARD (KÈM FILTER NÂNG CAO) ---
    @GetMapping("/{projectId}/board")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<BoardColumnResponse>>> getProjectBoard(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,

            // Filter Sprint
            @RequestParam(required = false) Integer sprintId, // null = auto active, 0 = backlog
            
            // Filter Tìm kiếm nâng cao
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType
    ) {

        List<BoardColumnResponse> board = projectService.getProjectBoard(
            companyId, workspaceId, projectId,
            sprintId, keyword, assigneeId, priority, taskType
        );

        
        return ResponseEntity.ok(ApiResponse.success("Task board data retrieved successfully.", board));
    }

    // ======================================================
    // API LẤY DANH SÁCH TASK (LIST VIEW) - ĐÃ NÂNG CẤP
    // ======================================================
    @GetMapping("/{projectId}/tasks")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<TaskSummaryResponse>>> getProjectTasks(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,

            // Filter Params
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) List<Integer> statusIds,

            // Pagination & Sorting
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Gọi Service trả về Page<TaskSummaryResponse>
        PageResponseDTO<TaskSummaryResponse> tasks = projectService.getProjectTaskList(
                companyId, workspaceId, projectId,
                sprintId, search, assigneeId, priority, statusIds,
                page, size, sortBy, sortDir
        );

        // Sửa thông báo sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project tasks fetched successfully.", tasks));
    }

    // ======================================================
    // API NHÓM TASK (GROUPING VIEW) 
    // ======================================================
    @GetMapping("/{projectId}/tasks/grouped")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<Map<String, List<TaskSummaryResponse>>>> getTasksGrouped(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam String groupBy, // "assignee", "priority", "status", "sprint"

            // Filter Params
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) String search
    ) {
        // Gọi Service trả về Map<String, List<TaskSummaryResponse>>
        Map<String, List<TaskSummaryResponse>> data = projectService.getTasksGroupedBy(
            companyId, workspaceId, projectId, groupBy, sprintId, search
        );

        // Sửa thông báo sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Grouped task data retrieved successfully.", data));
    }

    // ======================================================
    // API XEM LỊCH (CALENDAR VIEW)
    // ======================================================
    @GetMapping("/{projectId}/calendar")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<TaskSummaryResponse>>> getProjectCalendar(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,

            // Thời gian view (Bắt buộc cho Calendar)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, // ex: 2025-10-01
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,   // ex: 2025-11-01

            // Filter Params (Optional)
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType
    ) {

        List<TaskSummaryResponse> calendarTasks = projectService.getTaskCalendar(
            companyId, workspaceId, projectId,
            from, to, keyword, assigneeId, priority, taskType
        );

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Calendar tasks retrieved successfully.", calendarTasks));
    }

    // API XEM DANH SÁCH TASK ĐÃ LƯU TRỮ (ARCHIVE BIN) - CÓ LỌC & TÌM KIẾM
    @GetMapping("/{projectId}/archived-tasks")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<TaskSummaryResponse>>> getArchivedTasks(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            
            // --- CÁC BỘ LỌC MỚI ---
            @RequestParam(required = false) String keyword,      // Tìm theo tên/mã
            @RequestParam(required = false) Integer assigneeId,  // Tìm theo người làm cũ
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType,
            
            // Phân trang
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        
        PageResponseDTO<TaskSummaryResponse> archivedTasks = projectService.getArchivedTasks(
            projectId, 
            keyword, assigneeId, priority, taskType, // Truyền bộ lọc vào Service
            page, size
        );
        
        return ResponseEntity.ok(ApiResponse.success("Archived tasks retrieved successfully.", archivedTasks));
    }

    // ========================================================================
    // C. QUẢN LÝ THÀNH VIÊN DỰ ÁN
    // ========================================================================

    // API LẤY DANH SÁCH THÀNH VIÊN (Cơ bản)
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

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project member list retrieved successfully.", members));
    }

    // API TÌM KIẾM THÀNH VIÊN (Nâng cao)
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

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Project member search successful.", members));
    }


    // API CẬP NHẬT VAI TRÒ THÀNH VIÊN DỰ ÁN
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

        // 2. Tạo message động (Sửa thông báo trả về sang tiếng Anh)
        String message = String.format("Role for user '%s' (ID: %d) successfully updated to '%s'.",
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

    // API MỜI THÀNH VIÊN (NỘI BỘ + NGOÀI)
    @PostMapping("/{projectId}/members")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:invite_member')")
    public ResponseEntity<ApiResponse<Object>> inviteMember(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Valid @RequestBody InviteProjectMemberRequest request) {

        projectService.inviteMemberToProject(projectId, request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Operation successful.", null));
    }

}