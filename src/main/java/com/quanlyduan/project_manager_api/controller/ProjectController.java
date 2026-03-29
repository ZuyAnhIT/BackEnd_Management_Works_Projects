package com.quanlyduan.project_manager_api.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.dto.request.CreateTaskRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteProjectMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.BoardColumnResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectBacklogResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectInvitationResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
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

/**
 * Controller xu ly cac nghiep vu lien quan den Du an (Project) va cac thuc the ben trong nhu Task, Thanh vien.
 */
@RestController
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects")
@CrossOrigin("*")
public class ProjectController {

    // Cac hang so mac dinh cho phan trang va sap xep
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE_10 = "10";
    private static final String DEFAULT_SIZE_20 = "20";
    private static final String SORT_BY_CREATED_AT = "createdAt";
    private static final String SORT_BY_JOINED_AT = "joinedAt";
    private static final String SORT_BY_ORDER = "sortOrder";
    private static final String SORT_BY_ID = "id";
    private static final String SORT_DIR_DESC = "desc";
    private static final String SORT_DIR_ASC = "asc";
    private static final String INVITE_STATUS_PENDING = "PENDING";

    // Cac thong bao tra ve (Response Messages)
    private static final String MSG_CREATE_SUCCESS = "Project created successfully.";
    private static final String MSG_LIST_SUCCESS = "Project list retrieved successfully.";
    private static final String MSG_SEARCH_SUCCESS = "Project search successful.";
    private static final String MSG_DETAIL_SUCCESS = "Project details retrieved successfully.";
    private static final String MSG_UPDATE_SUCCESS = "Project information updated successfully.";
    private static final String MSG_UPDATE_STATUS_SUCCESS = "Project status updated successfully.";
    private static final String MSG_DELETE_SUCCESS = "Project cancelled successfully.";
    private static final String MSG_BACKLOG_SUCCESS = "Project backlog data retrieved successfully.";
    private static final String MSG_CREATE_TASK_SUCCESS = "New task created successfully.";
    private static final String MSG_BOARD_SUCCESS = "Task board data retrieved successfully.";
    private static final String MSG_TASK_LIST_SUCCESS = "Project tasks fetched successfully.";
    private static final String MSG_GROUPED_TASK_SUCCESS = "Grouped task data retrieved successfully.";
    private static final String MSG_CALENDAR_SUCCESS = "Calendar tasks retrieved successfully.";
    private static final String MSG_ARCHIVE_SUCCESS = "Archived tasks retrieved successfully.";
    private static final String MSG_MEMBER_LIST_SUCCESS = "Project member list retrieved successfully.";
    private static final String MSG_MEMBER_SEARCH_SUCCESS = "Project member search successful.";
    private static final String MSG_INVITATION_LIST_SUCCESS = "Project invitations retrieved successfully.";
    private static final String MSG_CANCEL_INVITE_SUCCESS = "Invitation cancelled successfully.";
    private static final String MSG_INVITE_SUCCESS = "Operation successful.";

    private final ProjectService projectService;
    private final SecurityService securityService;
    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    // Khoi tao thu cong de tiem phu thuoc
    public ProjectController(ProjectService projectService, SecurityService securityService, 
                             TaskService taskService, ObjectMapper objectMapper) {
        this.projectService = projectService;
        this.securityService = securityService;
        this.taskService = taskService;
        this.objectMapper = objectMapper;
    }

    // ========================================================================
    // QUAN LY DU AN (PROJECT CRUD)
    // ========================================================================

    /**
     * Tao du an moi trong Workspace, ho tro tai len anh dai dien.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:create')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Parameter(schema = @Schema(implementation = ProjectRequest.class))
            @RequestPart("data") String dataString,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        ProjectRequest request = parseProjectData(dataString, ProjectRequest.class);
        Integer creatorId = securityService.getCurrentUserId();
        ProjectResponse created = projectService.createProject(companyId, workspaceId, request, creatorId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(MSG_CREATE_SUCCESS, created));
    }

    /**
     * Lay danh sach du an trong Workspace (ho tro loc theo trang thai).
     */
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<ProjectResponse>>> listProjects(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE_10) int size,
            @RequestParam(defaultValue = SORT_BY_CREATED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {

        PageResponseDTO<ProjectResponse> projects = projectService.listProjectsByWorkspace(
                companyId, workspaceId, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MSG_LIST_SUCCESS, projects));
    }

    /**
     * Tim kiem du an nang cao theo ten, ma, nguoi quan ly.
     */
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<ProjectResponse>>> searchProjects(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String manager,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE_10) int size,
            @RequestParam(defaultValue = SORT_BY_CREATED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {

        PageResponseDTO<ProjectResponse> results = projectService.searchProjects(
                companyId, workspaceId, name, code, manager, status, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(MSG_SEARCH_SUCCESS, results));
    }

    /**
     * Lay thong tin chi tiet cua mot du an.
     */
    @GetMapping("/{projectId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {
        ProjectResponse response = projectService.getProjectDetails(companyId, workspaceId, projectId);
        return ResponseEntity.ok(ApiResponse.success(MSG_DETAIL_SUCCESS, response));
    }

    /**
     * Cap nhat thong tin du an va hinh anh minh hoa.
     */
    @PutMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Parameter(schema = @Schema(implementation = UpdateProjectRequest.class))
            @RequestPart("data") String dataString,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        UpdateProjectRequest request = parseProjectData(dataString, UpdateProjectRequest.class);
        ProjectResponse updated = projectService.updateProject(companyId, workspaceId, projectId, request, file);

        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_SUCCESS, updated));
    }

    /**
     * Thay doi trang thai hien tai cua du an.
     */
    @PutMapping("/{projectId}/status")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Valid @RequestBody UpdateProjectStatusRequest request) {

        ProjectResponse updated = projectService.updateProjectStatus(companyId, workspaceId, projectId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_STATUS_SUCCESS, updated));
    }

    /**
     * Huy du an (Soft delete).
     */
    @DeleteMapping("/{projectId}")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'project:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteProject(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId) {

        projectService.deleteProject(companyId, workspaceId, projectId);
        return ResponseEntity.ok(ApiResponse.success(MSG_DELETE_SUCCESS, null));
    }

    // ========================================================================
    // QUAN LY CONG VIEC (TASK VIEWS)
    // ========================================================================

    /**
     * Lay du lieu man hinh Backlog cua du an.
     */
    @GetMapping("/{projectId}/backlog")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<ProjectBacklogResponse>> getProjectBacklog(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE_20) int size,
            @RequestParam(defaultValue = SORT_BY_ORDER) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_ASC) String sortDir) {

        ProjectBacklogResponse backlogData = projectService.getProjectBacklog(
            companyId, workspaceId, projectId, keyword, assigneeId, priority, taskType,
            page, size, sortBy, sortDir
        );

        return ResponseEntity.ok(ApiResponse.success(MSG_BACKLOG_SUCCESS, backlogData));
    }

    /**
     * Tao cong viec moi (Task).
     */
    @PostMapping("/{projectId}/tasks")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:create')")
    public ResponseEntity<ApiResponse<TaskSummaryResponse>> createTask(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateTaskRequest request) {

        TaskSummaryResponse newTask = taskService.createTask(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(MSG_CREATE_TASK_SUCCESS, newTask));
    }

    /**
     * Lay du lieu bang cong viec (Board view).
     */
    @GetMapping("/{projectId}/board")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<BoardColumnResponse>>> getProjectBoard(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType) {

        List<BoardColumnResponse> board = projectService.getProjectBoard(
            companyId, workspaceId, projectId, sprintId, keyword, assigneeId, priority, taskType
        );

        return ResponseEntity.ok(ApiResponse.success(MSG_BOARD_SUCCESS, board));
    }

    /**
     * Lay danh sach cong viec (List view) kem phan trang va bo loc.
     */
    @GetMapping("/{projectId}/tasks")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<TaskSummaryResponse>>> getProjectTasks(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) List<Integer> statusIds,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE_20) int size,
            @RequestParam(defaultValue = SORT_BY_ID) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {

        PageResponseDTO<TaskSummaryResponse> tasks = projectService.getProjectTaskList(
                companyId, workspaceId, projectId, sprintId, search, assigneeId, priority, statusIds,
                page, size, sortBy, sortDir
        );

        return ResponseEntity.ok(ApiResponse.success(MSG_TASK_LIST_SUCCESS, tasks));
    }

    /**
     * Gom nhom cong viec theo tieu chi (Nguoi lam, do uu tien, trang thai, sprint).
     */
    @GetMapping("/{projectId}/tasks/grouped")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<Map<String, List<TaskSummaryResponse>>>> getTasksGrouped(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam String groupBy,
            @RequestParam(required = false) Integer sprintId,
            @RequestParam(required = false) String search) {

        Map<String, List<TaskSummaryResponse>> data = projectService.getTasksGroupedBy(
            companyId, workspaceId, projectId, groupBy, sprintId, search
        );

        return ResponseEntity.ok(ApiResponse.success(MSG_GROUPED_TASK_SUCCESS, data));
    }

    /**
     * Lay danh sach cong viec hien thi tren lich (Calendar view).
     */
    @GetMapping("/{projectId}/calendar")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<TaskSummaryResponse>>> getProjectCalendar(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType) {

        List<TaskSummaryResponse> calendarTasks = projectService.getTaskCalendar(
            companyId, workspaceId, projectId, from, to, keyword, assigneeId, priority, taskType
        );

        return ResponseEntity.ok(ApiResponse.success(MSG_CALENDAR_SUCCESS, calendarTasks));
    }

    /**
     * Truy xuat thung rac (Cac cong viec da luu tru).
     */
    @GetMapping("/{projectId}/archived-tasks")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<TaskSummaryResponse>>> getArchivedTasks(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE_20) int size) {
        
        PageResponseDTO<TaskSummaryResponse> archivedTasks = projectService.getArchivedTasks(
            projectId, keyword, assigneeId, priority, taskType, page, size
        );
        
        return ResponseEntity.ok(ApiResponse.success(MSG_ARCHIVE_SUCCESS, archivedTasks));
    }

    // ========================================================================
    // QUAN LY THANH VIEN DU AN
    // ========================================================================

    /**
     * Lay danh sach thanh vien tham gia du an.
     */
    @GetMapping("/{projectId}/members")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE_10) int size,
            @RequestParam(defaultValue = SORT_BY_JOINED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {

        PageResponseDTO<ProjectMemberResponse> members = projectService.getProjectMembers(projectId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MSG_MEMBER_LIST_SUCCESS, members));
    }

    /**
     * Tim kiem thanh vien du an theo ten, email, vai tro.
     */
    @GetMapping("/{projectId}/members/search")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<ProjectMemberResponse>>> searchProjectMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE_10) int size,
            @RequestParam(defaultValue = SORT_BY_JOINED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {

        PageResponseDTO<ProjectMemberResponse> members = projectService.searchProjectMembers(
            projectId, name, email, role, phone, page, size, sortBy, sortDir
        );

        return ResponseEntity.ok(ApiResponse.success(MSG_MEMBER_SEARCH_SUCCESS, members));
    }

    /**
     * Thay doi vai tro cua thanh vien trong du an.
     */
    @PutMapping("/{projectId}/members/{memberId}/role")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:manage_roles')")
    public ResponseEntity<ApiResponse<Object>> updateProjectMemberRole(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer memberId,
            @Valid @RequestBody RoleUpdateRequest request) {

        ProjectMemberResponse updatedMember = projectService.updateProjectMemberRole(projectId, memberId, request.getRoleCode());

        String message = String.format("Role for user '%s' (ID: %d) successfully updated to '%s'.",
            updatedMember.getFullName(), updatedMember.getUserId(), updatedMember.getRoleName()
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", updatedMember.getUserId());
        responseData.put("fullName", updatedMember.getFullName());
        responseData.put("newRoleCode", request.getRoleCode());
        responseData.put("newRoleName", updatedMember.getRoleName());

        return ResponseEntity.ok(ApiResponse.success(message, responseData));
    }

    /**
     * Moi thanh vien moi (trong cong ty hoac ben ngoai) vao du an.
     */
    @PostMapping("/{projectId}/members")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:invite_member')")
    public ResponseEntity<ApiResponse<Object>> inviteMember(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @Valid @RequestBody InviteProjectMemberRequest request) {

        projectService.inviteMemberToProject(projectId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_INVITE_SUCCESS, null));
    }

    /**
     * Lay danh sach cac loi moi tham gia du an da gui.
     */
    @GetMapping("/{projectId}/invitations")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<PageResponseDTO<ProjectInvitationResponse>>> getProjectInvitations(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = INVITE_STATUS_PENDING) String status,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE_10) int size,
            @RequestParam(defaultValue = SORT_BY_CREATED_AT) String sortBy,
            @RequestParam(defaultValue = SORT_DIR_DESC) String sortDir) {
        
        PageResponseDTO<ProjectInvitationResponse> data = projectService.getProjectInvitations(
                projectId, keyword, status, page, size, sortBy, sortDir
        );
        
        return ResponseEntity.ok(ApiResponse.success(MSG_INVITATION_LIST_SUCCESS, data));
    }

    /**
     * Huy mot loi moi tham gia du an da gui truoc do.
     */
    @DeleteMapping("/{projectId}/invitations/{invitationId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<Object>> cancelInvitation(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer invitationId) {
            
        projectService.cancelProjectInvitation(projectId, invitationId);
        return ResponseEntity.ok(ApiResponse.success(MSG_CANCEL_INVITE_SUCCESS, null));
    }

    // ========================================================================
    // CAC HAM HO TRO (UTILITIES)
    // ========================================================================

    /**
     * Chuyen doi du lieu JSON chuoi sang Doi tuong DTO tuong ung.
     */
    private <T> T parseProjectData(String dataString, Class<T> clazz) {
        try {
            return objectMapper.readValue(dataString, clazz);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid JSON data: " + e.getMessage());
        }
    }
}