// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/ProjectServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quanlyduan.project_manager_api.aop.ActivityLogContext;
import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.InviteProjectMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ActivityLogResponse;
import com.quanlyduan.project_manager_api.dto.response.BoardColumnResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectBacklogResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectInvitationDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectInvitationResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.QuotaExceededException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.ActivityLog;
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.ProjectInvitation;
import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.model.ProjectType;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.repository.ActivityLogRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.CompanySubscriptionRepository;
import com.quanlyduan.project_manager_api.repository.EpicRepository;
import com.quanlyduan.project_manager_api.repository.ProjectInvitationRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.ProjectStatusRepository;
import com.quanlyduan.project_manager_api.repository.ProjectTypeRepository;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.SprintRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.repository.specification.ProjectMemberSpecification;
import com.quanlyduan.project_manager_api.repository.specification.ProjectSpecification;
import com.quanlyduan.project_manager_api.repository.specification.TaskSpecification;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.FileStorageService;
import com.quanlyduan.project_manager_api.service.ProjectService;
import com.quanlyduan.project_manager_api.service.TaskService;
import com.quanlyduan.project_manager_api.util.SortUtils;
import com.quanlyduan.project_manager_api.validation.ProjectHierarchyValidator;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final ObjectMapper objectMapper;
    private final RoleRepository roleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final SecurityService securityService;
    private final FileStorageService fileStorageService;

    private final SprintRepository sprintRepository;
    private final EpicRepository epicRepository;
    private final ProjectStatusRepository projectStatusRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final ProjectInvitationRepository projectInvitationRepository;
    private final ProjectHierarchyValidator hierarchyValidator;
    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final QuotaValidationServiceImpl quotaValidationService;

    private final EmailService emailService;


    @Value("${app.frontend.url}")
    private String frontendUrl;

    // ========================================================================
    // CONSTRUCTOR (Dependency Injection)
    // ========================================================================
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              WorkspaceRepository workspaceRepository,
                              ActivityLogRepository activityLogRepository,
                              UserRepository userRepository,
                              ProjectTypeRepository projectTypeRepository,
                              ObjectMapper objectMapper,
                              RoleRepository roleRepository,
                              ProjectMemberRepository projectMemberRepository,
                              TaskRepository taskRepository,
                              TaskService taskService,
                              SecurityService securityService,
                              SprintRepository sprintRepository,
                              EpicRepository epicRepository,
                              ProjectStatusRepository projectStatusRepository,
                              FileStorageService fileStorageService,
                              EmailService emailService,
                              CompanyMemberRepository companyMemberRepository,
                              ProjectInvitationRepository projectInvitationRepository,
                              ProjectHierarchyValidator hierarchyValidator,
                              CompanySubscriptionRepository companySubscriptionRepository,
                              QuotaValidationServiceImpl quotaValidationService

                              ) {
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.projectTypeRepository = projectTypeRepository;
        this.objectMapper = objectMapper;
        this.roleRepository = roleRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.securityService = securityService;
        this.sprintRepository = sprintRepository;
        this.epicRepository = epicRepository;
        this.projectStatusRepository = projectStatusRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
        this.companyMemberRepository = companyMemberRepository;
        this.projectInvitationRepository = projectInvitationRepository;
        this.hierarchyValidator = hierarchyValidator;
        this.companySubscriptionRepository = companySubscriptionRepository;
        this.quotaValidationService = quotaValidationService;
    }

    /**
     * Helper: Kiểm tra giá trị chuỗi có được cung cấp (khác null, khác rỗng, không phải "string").
     */
    private boolean isProvided(String value) {
        return value != null && !value.isBlank() && !"string".equalsIgnoreCase(value.trim());
    }
    
    // ------------------------------------------------------------------------
    // NHÓM CHỨC NĂNG: CRUD & DETAIL PROJECT
    // ------------------------------------------------------------------------
    
    // 📂 LOGIC TẠO DỰ ÁN (KÈM UPLOAD ẢNH BÌA & SAAS QUOTA GUARD)
    @Override
    @Transactional
    @LogActivity(action = "CREATE", entityType = "PROJECT", description = "Create new Project")
    public ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request, Integer creatorId, MultipartFile coverImageFile) {
        
        // BỨC TƯỜNG LỬA: Kiểm tra hạn mức trước khi làm bất cứ điều gì
        quotaValidationService.validateProjectCreationQuota(companyId);
        // (1) Kiểm tra workspace tồn tại và thuộc đúng companyId
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));

        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace does not belong to the specified company.");
        }

        // =====================================================================
        // 🚀 BƯỚC 1.5: QUOTA GUARD (Kiểm tra giới hạn Dự án của TOÀN BỘ CÔNG TY)
        // =====================================================================
        CompanySubscription currentSubscription = companySubscriptionRepository.findByCompany_Id(companyId)
                .orElseThrow(() -> new BadRequestException("System Error: Company does not have an active subscription."));

        // Đếm TỔNG số dự án đang tồn tại trong TOÀN BỘ các Workspace của Công ty
        long currentProjectCount = projectRepository.countByCompanyId(companyId);
        
        Integer maxAllowedProjects = currentSubscription.getPlan().getMaxProjects();

        // Kiểm tra giới hạn (Bỏ qua nếu max = -1 tức là Gói Enterprise / Không giới hạn)
        if (maxAllowedProjects != null && maxAllowedProjects != -1) {
            if (currentProjectCount >= maxAllowedProjects) {
                throw new QuotaExceededException(
                    String.format("Upgrade required! Your current '%s' plan allows a maximum of %d projects. " +
                                  "Your company currently has %d projects across all workspaces. Please upgrade your plan to create more.", 
                    currentSubscription.getPlan().getName(), maxAllowedProjects, currentProjectCount)
                );
            }
        }
        // =====================================================================

        // (2) Kiểm tra unique projectCode
        if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, request.getProjectCode())) {
            throw new BadRequestException("Project code already exists in this workspace.");
        }

        // (3) Lấy createdBy
        User createdBy = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // (4) Khởi tạo Project (Giữ nguyên toàn bộ logic cũ của bạn)
        Project project = new Project();
        project.setWorkspace(workspace);
        project.setCreatedBy(createdBy);

        project.setName(request.getName());
        project.setProjectCode(request.getProjectCode());
        project.setDescription(request.getDescription());
        project.setGoal(request.getGoal());

        // XỬ LÝ UPLOAD ẢNH BÌA
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String coverPath = fileStorageService.storeFile(coverImageFile, "project-covers");
            project.setCoverImageUrl(coverPath);
        } else if (request.getCoverImageUrl() != null) {
            project.setCoverImageUrl(request.getCoverImageUrl());
        }

        // boardConfig
        if (request.getBoardConfig() != null) {
            try {
                project.setBoardConfig(objectMapper.writeValueAsString(request.getBoardConfig()));
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Invalid JSON boardConfig.");
            }
        }
        project.setStartDate(request.getStartDate());
        project.setDueDate(request.getDueDate());

        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        } else {
            project.setPriority(ProjectPriority.MEDIUM);
        }

        if (request.getManagerId() != null && request.getManagerId() > 0) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager user not found."));
            project.setManager(manager);
        }

        if (request.getProjectTypeId() != null && request.getProjectTypeId() > 0) {
            ProjectType type = projectTypeRepository.findById(request.getProjectTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project type not found."));
            project.setProjectType(type);
        }

        if (project.getProgress() == null) {
            project.setProgress(BigDecimal.ZERO);
        }

        // (5) Lưu Project
        Project saved = projectRepository.save(project);

        // (6) Gán người tạo làm Project Admin
        Role projectAdminRole = roleRepository.findFirstByRoleCode(RoleCode.PROJECT_ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + RoleCode.PROJECT_ADMIN.name() + ". Please configure in the database."
                ));

        ProjectMember projectMember = ProjectMember.builder()
                .project(saved)
                .user(createdBy)
                .role(projectAdminRole)
                .status(MemberStatus.ACTIVE)
                .build();

        projectMemberRepository.save(projectMember);

        // (7) KHỞI TẠO TRẠNG THÁI MẶC ĐỊNH
        initDefaultStatuses(saved); 

        // (8) Trả response
        return toResponse(saved);
    }

    /**
     * US9: Xóa dự án (soft delete) bằng cách chuyển trạng thái sang CANCELLED.
     */
    @Override
    @Transactional
    @LogActivity(action = "DELETE", entityType = "PROJECT", description = "Delete Project")
    public void deleteProject(Integer companyId, Integer workspaceId, Integer projectId) {
        // 1. Kiểm tra Workspace Hierarchy
        Workspace workspace = workspaceRepository.findById(workspaceId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace does not belong to the specified company.");
        }

        // 2. Kiểm tra Project Hierarchy
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project does not belong to the specified workspace.");
        }
        
        // 3. Thực hiện Soft Delete
        project.setStatus(ProjectStatus.CANCELLED);
        projectRepository.save(project);
    }

    /**
     * Cập nhật trạng thái Project (trừ CANCELLED).
     */
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "PROJECT", description = "Update Project Status")
    public ProjectResponse updateProjectStatus(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectStatusRequest request) {
        // 1. Kiểm tra Workspace Hierarchy
        Workspace workspace = workspaceRepository.findById(workspaceId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace does not belong to the specified company.");
        }

        // 2. Kiểm tra Project Hierarchy
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project does not belong to the specified workspace.");
        }

        // 3. Validate trạng thái
        ProjectStatus newStatus = request.getNewStatus();
        if (newStatus == null) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("New status cannot be empty.");
        }
        if (newStatus == ProjectStatus.CANCELLED) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Cannot update status to CANCELLED. Please use the delete API instead.");
        }
        if (project.getStatus() == newStatus) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project is already in the requested status.");
        }

        // 4. Cập nhật và lưu
        project.setStatus(newStatus);
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    /**
     * LOGIC CẬP NHẬT DỰ ÁN (TICH HOP UPLOAD ẢNH).
     */
    // ======================================================
    // LOGIC CẬP NHẬT DỰ ÁN - FULL CODE
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "PROJECT", description = "Update project information")
    public ProjectResponse updateProject(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectRequest request, MultipartFile coverImageFile) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Workspace does not belong to the specified company.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the specified workspace.");
        }

        StringBuilder changes = new StringBuilder();

        // 1. Name & Code
        if (isProvided(request.getName()) && !request.getName().equals(project.getName())) {
            if (changes.length() > 0) changes.append(", ");
            changes.append(String.format("renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"", project.getName(), request.getName()));
            project.setName(request.getName());
        }

        if (isProvided(request.getProjectCode()) && !request.getProjectCode().equals(project.getProjectCode())) {
            if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, request.getProjectCode())) {
                throw new BadRequestException("Project code already exists.");
            }
            if (changes.length() > 0) changes.append(", ");
            changes.append(String.format("changed code from <strong>%s</strong> to <strong>%s</strong>", project.getProjectCode(), request.getProjectCode()));
            project.setProjectCode(request.getProjectCode());
        }

        // 2. Description & Goal
        if (isProvided(request.getDescription()) && !request.getDescription().equals(project.getDescription())) {
            if (changes.length() > 0) changes.append(", ");
            changes.append("updated description");
            project.setDescription(request.getDescription());
        }
        if (isProvided(request.getGoal()) && !request.getGoal().equals(project.getGoal())) {
            if (changes.length() > 0) changes.append(", ");
            changes.append("updated goal");
            project.setGoal(request.getGoal());
        }

        // 3. Priority
        if (request.getPriority() != null && request.getPriority() != project.getPriority()) {
            if (changes.length() > 0) changes.append(", ");
            changes.append(String.format("changed priority to <strong>%s</strong>", request.getPriority()));
            project.setPriority(request.getPriority());
        }

        // 4. Dates
        if (request.getStartDate() != null && !request.getStartDate().equals(project.getStartDate())) {
            if (changes.length() > 0) changes.append(", ");
            changes.append("changed start date");
            project.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null && !request.getDueDate().equals(project.getDueDate())) {
            if (changes.length() > 0) changes.append(", ");
            changes.append("changed due date");
            project.setDueDate(request.getDueDate());
        }
        if (request.getCompletedAt() != null && !request.getCompletedAt().equals(project.getCompletedAt())) {
             // Logic riêng cho completed
             project.setCompletedAt(request.getCompletedAt());
        }

        // 5. Manager
        if (request.getManagerId() != null) {
            Integer oldManagerId = project.getManager() != null ? project.getManager().getId() : 0;
            if (!request.getManagerId().equals(oldManagerId)) {
                if (request.getManagerId() == 0) {
                     if (changes.length() > 0) changes.append(", ");
                     changes.append("removed manager");
                     project.setManager(null);
                } else {
                    User manager = userRepository.findById(request.getManagerId())
                            .orElseThrow(() -> new ResourceNotFoundException("Manager not found."));
                    
                    if (changes.length() > 0) changes.append(", ");
                    changes.append(String.format("changed manager to <strong>%s</strong>", manager.getFullName()));
                    project.setManager(manager);
                }
            }
        }

        // 6. Cover Image
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String coverPath = fileStorageService.storeFile(coverImageFile, "project-covers");
            if (changes.length() > 0) changes.append(", ");
            changes.append("updated cover image");
            project.setCoverImageUrl(coverPath);
        } else if (request.getCoverImageUrl() != null && !request.getCoverImageUrl().equals(project.getCoverImageUrl())) {
             if (changes.length() > 0) changes.append(", ");
             changes.append("updated cover image");
             project.setCoverImageUrl(request.getCoverImageUrl().isBlank() ? null : request.getCoverImageUrl());
        }

        // Set log
        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        } else {
             // Nếu không có thay đổi gì (hoặc chỉ đổi field không quan trọng)
            //  ActivityLogContext.setDetail("updated project details");
        }

        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    /**
     * Lấy chi tiết Project (bao gồm kiểm tra Hierarchy).
     */
    @Override
    public ProjectResponse getProjectDetails(Integer companyId, Integer workspaceId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));

        // Kiểm tra Hierarchy
        if (!project.getWorkspace().getId().equals(workspaceId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project does not belong to the specified workspace.");
        }

        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace does not belong to the specified company.");
        }

        // Dùng mapper chung để đảm bảo đầy đủ field như khi tạo/list
        return toResponse(project);
    }

    // ------------------------------------------------------------------------
    // NHÓM CHỨC NĂNG: LISTING & SEARCHING
    // ------------------------------------------------------------------------

    // 1. LẤY DANH SÁCH DỰ ÁN (Cơ bản)
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectResponse> listProjectsByWorkspace(
            Integer companyId, Integer workspaceId, ProjectStatus status,
            int page, int size, String sortBy, String sortDir) {

        // 1. Kiểm tra Workspace Hierarchy
        Workspace workspace = workspaceRepository.findById(workspaceId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace does not belong to the specified company.");
        }

        // 2. Định nghĩa Map sắp xếp cho PROJECT
        Map<String, String> sortMapping = Map.of(
            "createdAt", "createdAt",
            "name", "name",
            "code", "projectCode",
            "status", "status",
            "dueDate", "dueDate",
            "manager", "manager.fullName"
        );

        // 3. Tạo Pageable
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "createdAt", sortMapping);

        // 4. Query DB
        Page<Project> projectPage;
        if (status != null) {
            projectPage = projectRepository.findByWorkspace_IdAndStatus(workspaceId, status, pageable);
        } else {
            projectPage = projectRepository.findByWorkspace_Id(workspaceId, pageable);
        }

        // 5. Map và trả về
        Page<ProjectResponse> dtoPage = projectPage.map(this::toResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // 2. TÌM KIẾM DỰ ÁN (Nâng cao)
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectResponse> searchProjects(
            Integer companyId, Integer workspaceId,
            String searchName, String searchCode, String searchManager, ProjectStatus searchStatus,
            int page, int size, String sortBy, String sortDir) {

        // 1. Kiểm tra Workspace Hierarchy
        Workspace workspace = workspaceRepository.findById(workspaceId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace does not belong to the specified company.");
        }

        // 2. Định nghĩa Map sắp xếp cho PROJECT
        Map<String, String> sortMapping = Map.of(
            "createdAt", "createdAt",
            "name", "name",
            "code", "projectCode",
            "status", "status",
            "dueDate", "dueDate",
            "manager", "manager.fullName"
        );

        // 3. Tạo Pageable
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "createdAt", sortMapping);

        // 4. Tạo Specification (Bộ lọc động)
        Specification<Project> spec = ProjectSpecification.filterProjects(
            workspaceId, searchName, searchCode, searchManager, searchStatus
        );

        // 5. Query DB, Map và trả về
        Page<Project> projectPage = projectRepository.findAll(spec, pageable);
        Page<ProjectResponse> dtoPage = projectPage.map(this::toResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // ------------------------------------------------------------------------
    // NHÓM CHỨC NĂNG: BACKLOG & BOARD & LIST TASKS
    // ------------------------------------------------------------------------

    /**
     * LOGIC LẤY DỮ LIỆU MÀN HÌNH BACKLOG (Active Sprints + Paginated Backlog).
     */
    @Override
    @Transactional(readOnly = true)
    public ProjectBacklogResponse getProjectBacklog(
            Integer companyId, Integer workspaceId, Integer projectId,
            String keyword, Integer assigneeId,
            TaskPriority priority, TaskType taskType,
            int page, int size, String sortBy, String sortDir) {

        // 1. Validate Project Hierarchy
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
        if (!project.getWorkspace().getId().equals(workspaceId) ||
            !project.getWorkspace().getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project does not belong to this workspace or company.");
        }

        // 2. PHẦN A: ACTIVE SPRINTS
        List<Sprint> activeSprints = sprintRepository.findActiveSprintsByProjectId(
            projectId, Arrays.asList(SprintStatus.NOT_STARTED, SprintStatus.IN_PROGRESS)
        );

        List<SprintDetailsResponse> sprintDtos = activeSprints.stream().map(sprint -> {
            // Lọc Task trong Sprint
            Specification<Task> sprintTaskSpec = TaskSpecification.filterTasks(
                projectId, sprint.getId(), false, keyword, assigneeId, priority, taskType, null, 
                false // *** QUAN TRỌNG: isArchived = false ***
            );

            // Task trong Sprint luôn sắp xếp theo thứ tự hiển thị (sortOrder)
            List<Task> tasks = taskRepository.findAll(sprintTaskSpec, Sort.by("sortOrder").ascending());

            // TÍNH TOÁN THỐNG KÊ (trên danh sách đã lọc)
            long totalPoints = tasks.stream()
                    .mapToLong(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                    .sum();

            int count = tasks.size();

            // Map task
            List<TaskSummaryResponse> taskDtos = tasks.stream()
                    .map(this::mapToTaskSummaryResponse)
                    .collect(Collectors.toList());

            return SprintDetailsResponse.builder()
                    .id(sprint.getId())
                    .name(sprint.getName())
                    .goal(sprint.getGoal())
                    .status(sprint.getStatus())
                    .startDate(sprint.getStartDate())
                    .endDate(sprint.getEndDate())
                    .projectId(projectId)
                    .tasks(taskDtos)
                    .totalStoryPoints(totalPoints)
                    .taskCount(count)
                    .build();
        }).collect(Collectors.toList());


        // 3. PHẦN B: PRODUCT BACKLOG (Tasks chưa được gán Sprint)
        Specification<Task> backlogSpec = TaskSpecification.filterTasks(
            projectId, null, true, keyword, assigneeId, priority, taskType, null, 
            false // *** QUAN TRỌNG: isArchived = false ***
        );

        // Định nghĩa Map sắp xếp cho Backlog Task
        Map<String, String> sortMapping = Map.of(
            "sortOrder", "sortOrder",
            "title", "title",
            "priority", "priority",
            "storyPoints", "storyPoints",
            "dueDate", "dueDate"
        );
        
        // Tạo Sort object, ưu tiên "sortOrder" mặc định ASC
        Sort sort = SortUtils.createSort(sortBy, sortDir, "sortOrder", sortMapping);
        if ("sortOrder".equals(sortBy) && (sortDir == null || sortDir.isEmpty())) {
             sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        }


        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> backlogPage = taskRepository.findAll(backlogSpec, pageable);

        List<TaskSummaryResponse> backlogTaskDtos = backlogPage.stream()
                .map(this::mapToTaskSummaryResponse)
                .collect(Collectors.toList());

        // 4. Đóng gói kết quả
        return ProjectBacklogResponse.builder()
                .activeSprints(sprintDtos)
                .backlogTasks(backlogTaskDtos)
                .backlogPageNumber(backlogPage.getNumber())
                .backlogPageSize(backlogPage.getSize())
                .backlogTotalElements(backlogPage.getTotalElements())
                .backlogTotalPages(backlogPage.getTotalPages())
                .build();
    }


    // ======================================================
    // LOGIC XEM BOARD (TỰ ĐỘNG TÌM ACTIVE SPRINT)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<BoardColumnResponse> getProjectBoard(
            Integer companyId, Integer workspaceId, Integer projectId,
            Integer sprintId, String keyword, Integer assigneeId,
            TaskPriority priority, TaskType taskType) {

        // 1. VALIDATE HỆ THỐNG PHÂN CẤP
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));

        if (!project.getWorkspace().getId().equals(workspaceId) ||
            !project.getWorkspace().getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project does not belong to this workspace or company.");
        }

        // 2. TỰ ĐỘNG GIẢI QUYẾT SPRINT ID
        Integer targetSprintId = sprintId;
        boolean isBacklog = false;

        if (targetSprintId == null) {
            // Nếu client không gửi ID -> Tìm Sprint đang chạy (IN_PROGRESS)
            List<Sprint> activeSprints = sprintRepository.findActiveSprintsByProjectId(
                projectId, Collections.singletonList(SprintStatus.IN_PROGRESS)
            );
            targetSprintId = activeSprints.isEmpty() ? -1 : activeSprints.get(0).getId();
        } else if (targetSprintId == 0) {
            // Client gửi 0 -> Backlog
            isBacklog = true;
            targetSprintId = null;
        }

        // 3. LẤY DANH SÁCH CỘT (STATUS)
        List<com.quanlyduan.project_manager_api.model.ProjectStatus> statuses =
                projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId);

        // 4. TẠO SPECIFICATION ĐỂ LỌC TASK
        Specification<Task> spec = TaskSpecification.filterTasks(
                projectId, targetSprintId, isBacklog, keyword, assigneeId, priority, taskType, null, 
                false // *** QUAN TRỌNG: isArchived = false ***
        );

        // 5. LẤY TASK TỪ DB (1 Query duy nhất, sort theo thứ tự trong cột)
        List<Task> tasks = taskRepository.findAll(spec, Sort.by("sortOrder").ascending());

        // 6. NHÓM TASK THEO STATUS ID (Grouping in Memory)
        Map<Integer, List<Task>> tasksByStatus = tasks.stream()
                .filter(t -> t.getStatus() != null)
                .collect(Collectors.groupingBy(t -> t.getStatus().getId()));

        // 7. BUILD RESPONSE
        List<BoardColumnResponse> board = new ArrayList<>();

        for (com.quanlyduan.project_manager_api.model.ProjectStatus status : statuses) {
            List<Task> tasksInColumn = tasksByStatus.getOrDefault(status.getId(), Collections.emptyList());

            // Sử dụng this.mapToTaskSummaryResponse để có cấu trúc JSON đầy đủ (Tags, Nested Objects)
            List<TaskSummaryResponse> taskResponses = tasksInColumn.stream()
                    .map(this::mapToTaskSummaryResponse)
                    .collect(Collectors.toList());

            // Tạo đối tượng cột
            board.add(BoardColumnResponse.builder()
                    .statusId(status.getId())
                    .statusName(status.getName())
                    .color(status.getColor())
                    .order(status.getSortOrder())
                    .isCompleted(status.getIsCompletedStatus() != null && status.getIsCompletedStatus())
                    .tasks(taskResponses) 
                    .build());
        }

        return board;
    }

    // ======================================================
    // US-S4-8-9-11: Lọc và Phân trang Task (List View) 
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<TaskSummaryResponse> getProjectTaskList(
            Integer companyId, Integer workspaceId, Integer projectId,
            Integer sprintId, String search, Integer assigneeId, TaskPriority priority,
            List<Integer> statusIds,
            int page, int size, String sortBy, String sortDir) {

        // 1. VALIDATE HỆ THỐNG PHÂN CẤP
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (!project.getWorkspace().getId().equals(workspaceId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project does not belong to the specified Workspace.");
        }
        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace does not belong to the specified Company.");
        }

        // 2. CHUẨN BỊ THAM SỐ CHO SPECIFICATION
        // Xử lý logic Backlog: Nếu client gửi sprintId = 0 thì coi là Backlog
        boolean isBacklog = (sprintId != null && sprintId == 0);

        // 3. GỌI FILTER SPECIFICATION
        Specification<Task> spec = TaskSpecification.filterTasks(
                projectId, sprintId, isBacklog, search, assigneeId, priority, null, statusIds, 
                false // *** QUAN TRỌNG: isArchived = false ***
        );

        // 4. XỬ LÝ SORT
        Map<String, String> sortMap = Map.of(
            "title", "title",
            "dueDate", "dueDate",
            "priority", "priority",
            "status", "status.name" // Sort theo tên status
        );
        Sort sort = SortUtils.createSort(sortBy, sortDir, "id", sortMap);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 5. QUERY DB & MAP RESPONSE
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);
        
        // *** CẬP NHẬT: Sử dụng mapToTaskSummaryResponse để lấy cấu trúc JSON mới ***
        Page<TaskSummaryResponse> responsePage = taskPage.map(this::mapToTaskSummaryResponse);

        return new PageResponseDTO<>(responsePage);
    }

    // ======================================================
    // US-S4-10: Nhóm Task (Grouping View) - 
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public Map<String, List<TaskSummaryResponse>> getTasksGroupedBy(
            Integer companyId, Integer workspaceId, Integer projectId,
            String groupBy, Integer sprintId, String search) {

        // 1. VALIDATE HỆ THỐNG PHÂN CẤP (Hierarchy Check)
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (!project.getWorkspace().getId().equals(workspaceId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project does not belong to the specified Workspace.");
        }
        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace does not belong to the specified Company.");
        }

        // 2. QUERY DATA
        // Xử lý logic Backlog (0 -> Backlog)
        boolean isBacklog = (sprintId != null && sprintId == 0);

        // Specification: Thêm tham số cuối cùng là FALSE (isArchived)
        Specification<Task> spec = TaskSpecification.filterTasks(
                projectId, sprintId, isBacklog, search, null, null, null, null, 
                false // *** QUAN TRỌNG: isArchived = false ***
        );

        List<Task> tasks = taskRepository.findAll(spec);

        // 3. GROUPING LOGIC (Java Streams)
        
        // Helper mapper để tái sử dụng
        java.util.function.Function<Task, TaskSummaryResponse> mapper = this::mapToTaskSummaryResponse;

        if ("assignee".equalsIgnoreCase(groupBy)) {
            // Nhóm theo Tên người được giao
            return tasks.stream().collect(Collectors.groupingBy(
                t -> t.getAssignee() != null ? t.getAssignee().getFullName() : "Unassigned",
                Collectors.mapping(mapper, Collectors.toList()) // Dùng mapper mới
            ));

        } else if ("priority".equalsIgnoreCase(groupBy)) {
             // Nhóm theo Độ ưu tiên
             return tasks.stream()
                 .filter(t -> t.getPriority() != null) // Lọc bỏ nếu priority null
                 .collect(Collectors.groupingBy(
                     t -> t.getPriority().name(), // Group theo tên Enum (HIGH, LOW...)
                     Collectors.mapping(mapper, Collectors.toList()) // Dùng mapper mới
                 ));

        } else if ("status".equalsIgnoreCase(groupBy)) {
             // Nhóm theo Trạng thái
             return tasks.stream()
                 .filter(t -> t.getStatus() != null)
                 .collect(Collectors.groupingBy(
                     t -> t.getStatus().getName(),
                     Collectors.mapping(mapper, Collectors.toList()) // Dùng mapper mới
                 ));
                 
        } else if ("sprint".equalsIgnoreCase(groupBy)) {
             // Nhóm theo Sprint
             return tasks.stream()
                 .collect(Collectors.groupingBy(
                     // Nếu có sprint -> lấy tên, nếu null -> gom vào "Backlog"
                     t -> t.getSprint() != null ? t.getSprint().getName() : "Backlog",
                     Collectors.mapping(mapper, Collectors.toList()) // Dùng mapper mới
                 ));
        }

        // Sửa thông báo sang tiếng Anh
        throw new BadRequestException("Invalid groupBy parameter. Use 'assignee', 'priority', 'status' or 'sprint'.");
    }

    
    // ======================================================
    // API XEM LỊCH (CALENDAR VIEW)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<TaskSummaryResponse> getTaskCalendar(
            Integer companyId, Integer workspaceId, Integer projectId,
            LocalDate from, LocalDate to,
            String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType) {

        // 1. VALIDATE HỆ THỐNG PHÂN CẤP (Hierarchy Validation)
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        // Kiểm tra Project có thuộc Workspace này không
        if (!project.getWorkspace().getId().equals(workspaceId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project does not belong to the specified Workspace.");
        }

        // Kiểm tra Workspace có thuộc Company này không
        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace does not belong to the specified Company.");
        }

        // 2. TẠO SPECIFICATION (Bộ lọc)
        // Gọi hàm filterTasksForCalendar trong TaskSpecification để xử lý logic giao thoa thời gian (Date Range Overlap)
        Specification<Task> spec = TaskSpecification.filterTasksForCalendar(
                projectId,
                from, to, // Khoảng thời gian View (Ví dụ: 01/10 - 31/10)
                keyword,
                assigneeId,
                priority,
                taskType
        );

        // 3. QUERY DATABASE
        // Lấy tất cả task thỏa mãn điều kiện, sắp xếp theo ngày bắt đầu tăng dần để hiển thị đẹp trên lịch
        // Lưu ý: Không phân trang (Pagination) ở đây vì Calendar thường load hết event trong khung nhìn
        List<Task> tasks = taskRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "startDate"));

        // 4. MAP SANG DTO & TRẢ VỀ
        // Sử dụng hàm helper mapToTaskSummaryResponse (đã nâng cấp Nested Object) để dữ liệu đồng nhất với Board/Backlog
        return tasks.stream()
                .map(this::mapToTaskSummaryResponse)
                .collect(Collectors.toList());
    }

    // ======================================================
    // LOGIC MỚI: XEM DANH SÁCH ĐÃ LƯU TRỮ (VIEW ARCHIVE)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<TaskSummaryResponse> getArchivedTasks(
            Integer projectId, 
            String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType,
            int page, int size) {
        
        // 1. Validate Project tồn tại
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }

        // 2. Gọi Specification để lọc
        // Lưu ý tham số cuối cùng là TRUE (isArchived = true)
        Specification<Task> spec = TaskSpecification.filterTasks(
            projectId, 
            null,   // sprintId (Archived thường ko quan tâm sprint, hoặc để null để lấy all)
            false,  // isBacklog (false vì archived ko phải backlog active)
            keyword, 
            assigneeId, 
            priority, 
            taskType, 
            null,   // statusIds
            true    // *** QUAN TRỌNG: isArchived = true ***
        );
        
        // 3. Phân trang, sắp xếp theo ngày cập nhật mới nhất (để thấy task vừa archive ở đầu)
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        
        Page<Task> tasks = taskRepository.findAll(spec, pageable);
        
        // 4. Map và trả về
        return new PageResponseDTO<>(tasks.map(this::mapToTaskSummaryResponse));
    }

    // ------------------------------------------------------------------------
    // NHÓM CHỨC NĂNG: QUẢN LÝ THÀNH VIÊN & LỜI MỜI
    // ------------------------------------------------------------------------

    // 1. LẤY DANH SÁCH THÀNH VIÊN (Cơ bản)
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectMemberResponse> getProjectMembers(
            Integer projectId, int page, int size, String sortBy, String sortDir) {

        // 1. Định nghĩa Map sắp xếp
        Map<String, String> sortMapping = Map.of(
            "joinedAt", "joinedAt",
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        // 2. Tạo Pageable
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", sortMapping);

        // 3. Query DB, Map và trả về
        Page<ProjectMember> membersPage = projectMemberRepository.findByProject_Id(projectId, pageable);
        Page<ProjectMemberResponse> dtoPage = membersPage.map(this::mapToProjectMemberResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // 2. TÌM KIẾM THÀNH VIÊN (Nâng cao)
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectMemberResponse> searchProjectMembers(
            Integer projectId,
            String searchName, String searchEmail, String searchRoleName, String searchPhone,
            int page, int size, String sortBy, String sortDir) {

        // 1. Định nghĩa Map sắp xếp
        Map<String, String> sortMapping = Map.of(
            "joinedAt", "joinedAt",
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        // 2. Tạo Pageable
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", sortMapping);

        // 3. Tạo Specification
        Specification<ProjectMember> spec = ProjectMemberSpecification.filterMembers(
            projectId, searchName, searchEmail, searchRoleName, searchPhone
        );

        // 4. Query DB, Map và trả về
        Page<ProjectMember> membersPage = projectMemberRepository.findAll(spec, pageable);
        Page<ProjectMemberResponse> dtoPage = membersPage.map(this::mapToProjectMemberResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    /**
     * Cập nhật vai trò của thành viên dự án.
     */
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "PROJECT", description = "Update Project Member Role")
    public ProjectMemberResponse updateProjectMemberRole(Integer projectId, Integer memberId, String newRoleCode) {
        // 1. Lấy thông tin thành viên
        ProjectMember member = projectMemberRepository.findById(memberId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project member not found with ID: " + memberId));

        // 2. Kiểm tra bảo mật (IDOR): Đảm bảo thành viên này thuộc đúng dự án
        if (!member.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("Member not found in this project.");
        }

        // 3. Kiểm tra nghiệp vụ: Không cho phép đổi vai trò của chính mình
        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("You cannot change your own role.");
        }

        // 4. Kiểm tra nếu vai trò mới trùng với vai trò hiện tại
        if (member.getRole().getRoleCode().equals(newRoleCode)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("New role is the same as the current role, no update needed.");
        }

        // 5. Tìm vai trò (Role) mới
        Role newRole = roleRepository.findFirstByRoleCode(newRoleCode)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with code: " + newRoleCode));

        // 6. Kiểm tra nghiệp vụ: Đảm bảo vai trò mới là CẤP DỰ ÁN
        if (newRole.getLevel() != RoleLevel.PROJECT) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Invalid role. Must be a PROJECT level role.");
        }

        // 7. Cập nhật vai trò
        member.setRole(newRole);
        ProjectMember updatedMember = projectMemberRepository.save(member);

        // 8. Trả về DTO đã cập nhật
        return mapToProjectMemberResponse(updatedMember);
    }

    /**
     * LOGIC: MỜI THÀNH VIÊN VÀO DỰ ÁN (Xử lý Nội bộ/Bên ngoài).
     */
    @Override
    @Transactional
    @LogActivity(action = "INVITE", entityType = "PROJECT_MEMBER", description = "Invite member to Project")
    public ProjectInvitation inviteMemberToProject(Integer projectId, InviteProjectMemberRequest request) {
        // 1. Tìm Project và lấy thông tin người mời/Công ty
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));

        User inviter = securityService.getCurrentAuthenticatedUser();
        Integer companyId = project.getWorkspace().getCompany().getId();

        // 2. Tìm Role và kiểm tra cấp độ
        Role role = roleRepository.findFirstByRoleCode(request.getRoleCode())
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleCode()));

        if (role.getLevel() != RoleLevel.PROJECT) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Invalid role. Must be a PROJECT level role.");
        }

        String email = request.getEmail();
        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        // 3. XỬ LÝ LOGIC: Phân biệt Nội bộ/Bên ngoài
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // 3.1. Nếu đã ở trong dự án -> Báo lỗi
            if (projectMemberRepository.findByProject_IdAndUser_Id(projectId, existingUser.getId()).isPresent()) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("This user is already a member of the project.");
            }

            // 3.2. Kiểm tra có phải thành viên công ty ACTIVE không
            boolean isCompanyMember = companyMemberRepository.existsByCompany_IdAndUser_IdAndStatus(
                    companyId, existingUser.getId(), MemberStatus.ACTIVE
            );

            if (isCompanyMember) {
                // ==> TRƯỜNG HỢP NỘI BỘ: THÊM THẲNG vào dự án
                ProjectMember newMember = ProjectMember.builder()
                        .project(project)
                        .user(existingUser)
                        .role(role)
                        .status(MemberStatus.ACTIVE)
                        .build();
                projectMemberRepository.save(newMember);

                // Gửi mail thông báo
                sendProjectNotificationEmail(inviter, existingUser, project, role);
                return null; // Kết thúc
            }
        }

        // ==> TRƯỜNG HỢP BÊN NGOÀI (Chưa có TK hoặc Không phải nhân viên Cty)
        // 4. Kiểm tra lời mời trùng đang chờ xử lý
        if (projectInvitationRepository.existsByProject_IdAndEmailAndStatus(projectId, email, InvitationStatus.PENDING)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("An invitation is already pending for this email.");
        }

        // 5. Tạo Token & Lưu DB
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7); // Hết hạn sau 7 ngày

        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .email(email)
                .role(role)
                .invitedBy(inviter)
                .token(token)
                .status(InvitationStatus.PENDING)
                .expiresAt(expiresAt)
                .build();

        ProjectInvitation projectInvitation = projectInvitationRepository.save(invitation);

        // 6. Gửi Email Mời
        sendProjectInvitationEmail(inviter, email, project, role, token);
        return projectInvitationRepository.save(projectInvitation);
    }

    /**
     * 1. Xem chi tiết lời mời (Public).
     */
    @Override
    @Transactional(readOnly = true)
    public ProjectInvitationDetailsResponse getProjectInvitationDetails(String token) {
        ProjectInvitation invitation = projectInvitationRepository.findByToken(token)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Invitation does not exist or token is invalid."));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This invitation is no longer valid.");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Invitation has expired.");
        }

        // Kiểm tra xem user có tồn tại chưa
        boolean accountExists = userRepository.existsByEmail(invitation.getEmail());

        return ProjectInvitationDetailsResponse.builder()
                .email(invitation.getEmail())
                .projectName(invitation.getProject().getName())
                .roleName(invitation.getRole().getRoleName())
                .accountExists(accountExists)
                .expiresAt(invitation.getExpiresAt())
                .build();
    }

    /**
     * 2. Chấp nhận lời mời (User đã login).
     */
    @Override
    @Transactional
    @LogActivity(action = "JOIN", entityType = "PROJECT_MEMBER", description = "Accept project invitation")
    public void acceptProjectInvitation(String token) {
        User currentUser = securityService.getCurrentAuthenticatedUser();

        ProjectInvitation invitation = projectInvitationRepository.findByToken(token)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation."));

        // Kiểm tra email khớp
        if (!invitation.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Account email does not match the invitation email.");
        }
        // Kiểm tra trạng thái
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Invitation is no longer valid.");
        }

        // Tạo Project Member
        ProjectMember newMember = ProjectMember.builder()
                .project(invitation.getProject())
                .user(currentUser)
                .role(invitation.getRole())
                .status(MemberStatus.ACTIVE)
                .build();
        projectMemberRepository.save(newMember);

        // Cập nhật trạng thái lời mời
        invitation.setStatus(InvitationStatus.ACCEPTED);
        projectInvitationRepository.save(invitation);

        String welcomeMsg = String.format("has joined the project <strong>%s</strong> 🎉", 
                invitation.getProject().getName());
        
        ActivityLogContext.setDetail(welcomeMsg);
    }

    // =================================================================================
    // ⏳ LOGIC LẤY DANH SÁCH LỜI MỜI DỰ ÁN (CÓ LỌC, TÌM KIẾM & TẠO LINK)
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectInvitationResponse> getProjectInvitations(
            Integer projectId,
            String keyword,
            String statusStr,
            int page, int size, String sortBy, String sortDir
    ) {
        // 1. Kiểm tra Project tồn tại
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        // 2. Cấu hình Map ánh xạ cho việc sắp xếp (Sort)
        // Lưu ý: Key là tên field FE gửi lên, Value là tên field trong Entity (JPA path)
        Map<String, String> sortMapping = Map.of(
            "createdAt", "createdAt",
            "email", "email",
            "role", "role.roleCode",
            "status", "status"
        );

        // 3. Tạo Pageable
        // (Giả sử bạn xử lý logic sort thủ công tại chỗ như code mẫu bạn gửi)
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("asc")
                ? org.springframework.data.domain.Sort.by(sortMapping.getOrDefault(sortBy, "createdAt")).ascending()
                : org.springframework.data.domain.Sort.by(sortMapping.getOrDefault(sortBy, "createdAt")).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);

        // 4. Xử lý bộ lọc
        String searchKeyword = (keyword != null) ? keyword.trim() : "";

        // Status: Mặc định là PENDING nếu không truyền
        InvitationStatus status = InvitationStatus.PENDING;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = InvitationStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Nếu sai format, giữ mặc định PENDING hoặc xử lý tùy ý
                status = InvitationStatus.PENDING;
            }
        }

        // 5. Gọi Repository (Đảm bảo Repository đã có hàm này)
        Page<ProjectInvitation> invitationPage = projectInvitationRepository
                .findByProject_IdAndStatusAndEmailContainingIgnoreCase(projectId, status, searchKeyword, pageable);

        // 6. Map sang DTO
        Page<ProjectInvitationResponse> dtoPage = invitationPage.map(this::mapToInvitationResponse);

        // 7. Trả về kết quả
        return new PageResponseDTO<>(dtoPage);
    }

    private ProjectInvitationResponse mapToInvitationResponse(ProjectInvitation invitation) {
        String fullLink = "";

        // Chỉ tạo link nếu trạng thái là PENDING
        if (invitation.getStatus() == InvitationStatus.PENDING) {
            // Cấu trúc: [FRONTEND_URL]/accept-project-invitation?token=[TOKEN]
            fullLink = frontendUrl + "/accept-project-invitation?token=" + invitation.getToken();
        }

        return ProjectInvitationResponse.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .roleCode(invitation.getRole().getRoleCode()) // Code của Role
                .status(invitation.getStatus().name())
                .invitedAt(invitation.getCreatedAt())
                .invitationLink(fullLink) // <--- Trường mới quan trọng
                
                // Mapping thông tin người mời (Kiểm tra null để tránh lỗi)
                .inviterName(invitation.getInvitedBy() != null ? invitation.getInvitedBy().getFullName() : "System")
                .inviterAvatar(invitation.getInvitedBy() != null ? invitation.getInvitedBy().getAvatarUrl() : null)
                .build();
    }


    // HỦY LỜI MỜI 
    @Override
    @Transactional
    public void cancelProjectInvitation(Integer projectId, Integer invitationId) {
        ProjectInvitation invitation = projectInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found."));

        if (!invitation.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Invitation does not belong to this project.");
        }

        // Xóa cứng (Hard Delete) hoặc chuyển trạng thái sang CANCELLED
        // Ở đây mình chọn xóa luôn cho sạch Database vì là lời mời chưa dùng
        projectInvitationRepository.delete(invitation);
    }

    // ------------------------------------------------------------------------
    // PRIVATE HELPER METHODS (MAPPERS & UTILS)
    // ------------------------------------------------------------------------

    /**
     * Helper: Tạo Pageable chung cho cả Project và ProjectMember.
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDir,
                                    String defaultSortField, Map<String, String> sortMapping) {

        Sort sort = SortUtils.createSort(sortBy, sortDir, defaultSortField, sortMapping);
        return PageRequest.of(page, size, sort);
    }


    /**
     * Helper: Map Project Entity sang ProjectResponse DTO.
     */
    private ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .workspaceId(p.getWorkspace() != null ? p.getWorkspace().getId() : null)
                .companyId(p.getWorkspace() != null && p.getWorkspace().getCompany() != null 
                           ? p.getWorkspace().getCompany().getId() : null)
                .name(p.getName())
                .projectCode(p.getProjectCode())
                .description(p.getDescription())
                .goal(p.getGoal())
                .coverImageUrl(p.getCoverImageUrl())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .priority(p.getPriority() != null ? p.getPriority().name() : null)
                .startDate(p.getStartDate())
                .dueDate(p.getDueDate())
                .completedAt(p.getCompletedAt())
                .progress(p.getProgress())
                .managerId(p.getManager() != null ? p.getManager().getId() : null)
                .managerName(p.getManager() != null ? p.getManager().getFullName() : null)
                .createdById(p.getCreatedBy() != null ? p.getCreatedBy().getId() : null)
                .createdByName(p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    /**
     * Helper: Map ProjectMember Entity sang ProjectMemberResponse DTO.
     */
    private ProjectMemberResponse mapToProjectMemberResponse(ProjectMember member) {
        return ProjectMemberResponse.builder()
                .memberId(member.getId())
                .userId(member.getUser().getId())
                .fullName(member.getUser().getFullName())
                .email(member.getUser().getEmail())
                .avatarUrl(member.getUser().getAvatarUrl())
                .phoneNumber(member.getUser().getPhoneNumber())
                .roleName(member.getRole().getRoleName())
                .joinedAt(member.getJoinedAt())
                .status(member.getStatus())
                .build();
    }

    /**
     * Helper: Map Task Entity sang TaskSummaryResponse DTO (Cấu trúc Nested).
     */
    public TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        User assignee = task.getAssignee();
        Epic epic = task.getEpic();
        com.quanlyduan.project_manager_api.model.ProjectStatus status = task.getStatus();

        // 1. Xử lý Subtask Summary
        int totalSubtasks = 0;
        int completedSubtasks = 0;
        if (task.getSubTasks() != null) {
            totalSubtasks = task.getSubTasks().size();
            completedSubtasks = (int) task.getSubTasks().stream()
                    .filter(st -> st.getStatus() == SubTaskStatus.DONE) // Giả sử trạng thái hoàn thành là DONE
                    .count();
        }

        // 2. Xử lý Tags
        List<TaskSummaryResponse.TagInfo> tagInfos = new ArrayList<>();
        if (task.getTags() != null) {
            tagInfos = task.getTags().stream()
                    .map(tag -> TaskSummaryResponse.TagInfo.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .color(tag.getColor())
                            .build())
                    .collect(Collectors.toList());
        }

        // 3. Build DTO
        return TaskSummaryResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .projectId(task.getProject().getId())
                .workspaceId(task.getProject().getWorkspace().getId())
                .companyId(task.getProject().getWorkspace().getCompany().getId())
                .taskType(task.getTaskType())
                .priority(task.getPriority())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .storyPoints(task.getStoryPoints())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .sortOrder(task.getSortOrder())

                // Mapping Status Object
                .status(status != null ? TaskSummaryResponse.StatusInfo.builder()
                        .id(status.getId())
                        .name(status.getName())
                        .color(status.getColor())
                        .build() : null)

                // Mapping Epic Object
                .epic(epic != null ? TaskSummaryResponse.EpicInfo.builder()
                        .id(epic.getId())
                        .name(epic.getName())
                        .color(epic.getColor())
                        .build() : null)

                // Mapping Assignee Object
                .assignee(assignee != null ? TaskSummaryResponse.UserInfo.builder()
                        .id(assignee.getId())
                        .name(assignee.getFullName())
                        .avatarUrl(assignee.getAvatarUrl())
                        .build() : null)

                // Mapping Tags List
                .tags(tagInfos)

                // Mapping Subtask Summary
                .subtaskSummary(TaskSummaryResponse.SubtaskSummary.builder()
                        .total(totalSubtasks)
                        .completed(completedSubtasks)
                        .build())
                .build();
    }

    // --- Helper: Gửi mail thông báo (Nội bộ - đã là member Cty) ---
    private void sendProjectNotificationEmail(User inviter, User user, Project project, Role role) {
        try {
            String projectUrl = frontendUrl + "/companies/" + project.getWorkspace().getCompany().getId() +
                                "/workspaces/" + project.getWorkspace().getId() +
                                "/projects/" + project.getId() + "/board";

            String subject = "You have been added to the project: " + project.getName();
            String body = String.format(
                "Hi %s,<br><br>" +
                "%s has included you into project <strong>%s</strong> with role <strong>%s</strong>.<br>" +
                "Please access project with this link: <a href=\"%s\">View Project</a>",
                user.getFullName(), inviter.getFullName(), project.getName(), role.getRoleName(), projectUrl
            );
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Error sending internal project notification email: " + e.getMessage());
        }
    }

    // --- Helper: Gửi mail mời (Bên ngoài - cần chấp nhận) ---
    private void sendProjectInvitationEmail(User inviter, String email, Project project, Role role, String token) {
        try {
            String acceptUrl = frontendUrl + "/accept-project-invitation?token=" + token;
            String subject = "Project Invitation: " + project.getName();
            String body = String.format(
                "Hi,<br><br>" +
                "%s has invited you into project<strong>%s</strong> with role <strong>%s</strong>.<br>" +
                "Please click the link below to accept the invitation:<br>" +
                "<a href=\"%s\">Accept Invitation</a><br><br>" +
                "This link will expire in 7 days.",
                inviter.getFullName(), project.getName(), role.getRoleName(), acceptUrl
            );
            emailService.sendEmail(email, subject, body);
        } catch (Exception e) {
            System.err.println("Error sending external project invitation email: " + e.getMessage());
        }
    }

    

    // ======================================================
    // PRIVATE HELPER: TẠO STATUS MẶC ĐỊNH
    // ======================================================
    private void initDefaultStatuses(Project project) {
        // Lưu ý: Sử dụng đường dẫn đầy đủ (Full Package Name) cho Entity ProjectStatus
        // Để tránh nhầm lẫn với Enum ProjectStatus đã import ở trên đầu file
        List<com.quanlyduan.project_manager_api.model.ProjectStatus> defaultStatuses = new ArrayList<>();

        // 1. TO DO (Cần làm)
        defaultStatuses.add(com.quanlyduan.project_manager_api.model.ProjectStatus.builder()
                .project(project)
                .name("To Do")
                .color("#95a5a6") // Gray
                .sortOrder(0)
                .isCompletedStatus(false)
                .build());

        // 2. IN PROGRESS (Đang làm)
        defaultStatuses.add(com.quanlyduan.project_manager_api.model.ProjectStatus.builder()
                .project(project)
                .name("In Progress")
                .color("#3498db") // Blue
                .sortOrder(1)
                .isCompletedStatus(false)
                .build());

        // 3. DONE (Hoàn thành)
        defaultStatuses.add(com.quanlyduan.project_manager_api.model.ProjectStatus.builder()
                .project(project)
                .name("Done")
                .color("#2ecc71") // Green
                .sortOrder(2)
                .isCompletedStatus(true)
                .build());

        // Lưu tất cả vào DB
        projectStatusRepository.saveAll(defaultStatuses);
    }

}