// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/ProjectServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.InviteProjectMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.BoardColumnResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectBacklogResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectInvitationDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.ProjectTypeRepository;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;
import com.quanlyduan.project_manager_api.repository.specification.ProjectMemberSpecification;
import com.quanlyduan.project_manager_api.repository.specification.ProjectSpecification;
import com.quanlyduan.project_manager_api.repository.specification.TaskSpecification;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.FileStorageService;
import com.quanlyduan.project_manager_api.service.ProjectService;
import com.quanlyduan.project_manager_api.service.TaskService;
import com.quanlyduan.project_manager_api.util.SortUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.security.SecurityService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.repository.SprintRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.EpicRepository;
import com.quanlyduan.project_manager_api.repository.ProjectInvitationRepository;
import com.quanlyduan.project_manager_api.repository.ProjectStatusRepository;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
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

    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // ========================================================================
    // CONSTRUCTOR (Dependency Injection)
    // ========================================================================
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              WorkspaceRepository workspaceRepository,
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
                              ProjectInvitationRepository projectInvitationRepository
                              ) {
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
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

    /**
     * US7: Tạo Project mới trong Workspace (TICH HOP UPLOAD).
     */
    @Override
    @Transactional
    public ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request, Integer creatorId, MultipartFile coverImageFile) {
        // (1) Kiểm tra workspace tồn tại và thuộc đúng companyId
        Workspace workspace = workspaceRepository.findById(workspaceId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));

        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace does not belong to the specified company.");
        }

        // (2) Kiểm tra unique projectCode
        if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, request.getProjectCode())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project code already exists in this workspace.");
        }

        // (3) Lấy createdBy
        User createdBy = userRepository.findById(creatorId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // (4) Khởi tạo Project
        Project project = new Project();
        project.setWorkspace(workspace);
        project.setCreatedBy(createdBy);

        // Map các trường Scalar từ request
        project.setName(request.getName());
        project.setProjectCode(request.getProjectCode());
        project.setDescription(request.getDescription());
        project.setGoal(request.getGoal());

        // XỬ LÝ UPLOAD ẢNH BÌA
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            // Lưu vào thư mục "project-covers"
            String coverPath = fileStorageService.storeFile(coverImageFile, "project-covers");
            project.setCoverImageUrl(coverPath);
        } else if (request.getCoverImageUrl() != null) {
            // Nếu người dùng gửi link ảnh (URL string)
            project.setCoverImageUrl(request.getCoverImageUrl());
        }

        // boardConfig (Chuyển đối tượng JSON thành String)
        if (request.getBoardConfig() != null) {
            try {
                project.setBoardConfig(objectMapper.writeValueAsString(request.getBoardConfig()));
            } catch (JsonProcessingException e) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Invalid JSON boardConfig.");
            }
        }
        project.setStartDate(request.getStartDate());
        project.setDueDate(request.getDueDate());

        // Priority
        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        } else {
            project.setPriority(ProjectPriority.MEDIUM);
        }

        // Manager
        if (request.getManagerId() != null && request.getManagerId() > 0) {
            User manager = userRepository.findById(request.getManagerId())
                    // Sửa thông báo sang tiếng Anh
                    .orElseThrow(() -> new ResourceNotFoundException("Manager user not found."));
            project.setManager(manager);
        }

        // Project Type
        if (request.getProjectTypeId() != null && request.getProjectTypeId() > 0) {
            ProjectType type = projectTypeRepository.findById(request.getProjectTypeId())
                    // Sửa thông báo sang tiếng Anh
                    .orElseThrow(() -> new ResourceNotFoundException("Project type not found."));
            project.setProjectType(type);
        }

        // Mặc định progress là 0
        if (project.getProgress() == null) {
            project.setProgress(BigDecimal.ZERO);
        }

        // (5) Lưu Project
        Project saved = projectRepository.save(project);

        // (6) Gán người tạo làm Project Admin
        Role projectAdminRole = roleRepository.findFirstByRoleCode(RoleCode.PROJECT_ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                        // Sửa thông báo sang tiếng Anh
                        "Role not found: " + RoleCode.PROJECT_ADMIN.name() + ". Please configure in the database."
                ));

        ProjectMember projectMember = ProjectMember.builder()
                .project(saved)
                .user(createdBy)
                .role(projectAdminRole)
                .status(MemberStatus.ACTIVE)
                .build();

        projectMemberRepository.save(projectMember);

        // (7) Trả response
        return toResponse(saved);
    }

    /**
     * US9: Xóa dự án (soft delete) bằng cách chuyển trạng thái sang CANCELLED.
     */
    @Override
    @Transactional
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
    @Override
    @Transactional
    public ProjectResponse updateProject(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectRequest request, MultipartFile coverImageFile) {

        // 1. Tìm và Kiểm tra Workspace Hierarchy
        Workspace workspace = workspaceRepository.findById(workspaceId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Workspace does not belong to the specified company.");
        }

        // 2. Tìm Project Hierarchy
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Project does not belong to the specified workspace.");
        }

        // 3. Cập nhật các trường thông tin (Scalar)
        if (isProvided(request.getName())) {
            project.setName(request.getName());
        }

        // Cập nhật Project Code (kiểm tra trùng lặp nếu code thay đổi)
        if (isProvided(request.getProjectCode())) {
            String newCode = request.getProjectCode();
            String currentCode = project.getProjectCode();
            if (!newCode.equalsIgnoreCase(currentCode)) {
                if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, newCode)) {
                    // Sửa thông báo sang tiếng Anh
                    throw new BadRequestException("Project code already exists in this workspace.");
                }
                project.setProjectCode(newCode);
            }
        }

        if (isProvided(request.getDescription())) {
            project.setDescription(request.getDescription());
        }
        if (isProvided(request.getGoal())) {
            project.setGoal(request.getGoal());
        }

        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            project.setDueDate(request.getDueDate());
        }
        if (request.getCompletedAt() != null) {
            project.setCompletedAt(request.getCompletedAt());
        }

        // Cập nhật Manager
        if (request.getManagerId() != null) {
            Integer managerId = request.getManagerId();
            if (managerId == 0) { // Set null
                project.setManager(null);
            } else {
                User manager = userRepository.findById(managerId)
                        // Sửa thông báo sang tiếng Anh
                        .orElseThrow(() -> new ResourceNotFoundException("Manager not found."));
                project.setManager(manager);
            }
        }

        // Cập nhật Project Type
        if (request.getProjectTypeId() != null) {
            Integer projectTypeId = request.getProjectTypeId();
            if (projectTypeId == 0) { // Set null
                project.setProjectType(null);
            } else {
                ProjectType type = projectTypeRepository.findById(projectTypeId)
                        // Sửa thông báo sang tiếng Anh
                        .orElseThrow(() -> new ResourceNotFoundException("Project type not found."));
                project.setProjectType(type);
            }
        }

        // Cập nhật Board Config (kiểm tra JSON hợp lệ)
        if (isProvided(request.getBoardConfig())) {
            try {
                objectMapper.readTree(request.getBoardConfig());
            } catch (Exception e) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("boardConfig is not valid JSON.");
            }
            project.setBoardConfig(request.getBoardConfig());
        }

        // 4. XỬ LÝ UPLOAD ẢNH BÌA
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            // Lưu file mới và cập nhật đường dẫn
            String coverPath = fileStorageService.storeFile(coverImageFile, "project-covers");
            project.setCoverImageUrl(coverPath);
        }
        // Nếu gửi link ảnh trực tiếp (String) hoặc muốn xóa ảnh bằng cách gửi chuỗi rỗng
        else if (request.getCoverImageUrl() != null) {
            // Cập nhật URL (bao gồm null hoặc rỗng nếu người dùng muốn xóa ảnh)
            project.setCoverImageUrl(request.getCoverImageUrl().isBlank() ? null : request.getCoverImageUrl());
        }

        // 5. Lưu và trả về
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
            Specification<Task> sprintTaskSpec = TaskSpecification.filterBacklog(
                projectId, sprint.getId(), false, keyword, assigneeId, priority, taskType
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
        Specification<Task> backlogSpec = TaskSpecification.filterBacklog(
            projectId, null, true, keyword, assigneeId, priority, taskType
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


    /**
     * LOGIC XEM BOARD (TỰ ĐỘNG TÌM ACTIVE SPRINT).
     */
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

        // 2. TỰ ĐỘNG GIẢI QUYẾT SPRINT ID (Xác định targetSprintId hoặc Backlog)
        Integer targetSprintId = sprintId;
        boolean isBacklog = false;

        if (targetSprintId == null) {
            // Nếu client không gửi ID nào, tự tìm Sprint đang chạy (IN_PROGRESS)
            List<Sprint> activeSprints = sprintRepository.findActiveSprintsByProjectId(
                projectId, Collections.singletonList(SprintStatus.IN_PROGRESS)
            );
            // Gán ID của sprint đầu tiên tìm được, hoặc -1 nếu không có sprint nào đang chạy
            targetSprintId = activeSprints.isEmpty() ? -1 : activeSprints.get(0).getId();
        } else if (targetSprintId == 0) {
            // Client gửi 0 -> Backlog
            isBacklog = true;
            targetSprintId = null; // Set null để Specification query đúng
        }

        // 3. LẤY DANH SÁCH CỘT (STATUS) CỦA DỰ ÁN
        List<com.quanlyduan.project_manager_api.model.ProjectStatus> statuses =
                projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId);

        // 4. TẠO SPECIFICATION ĐỂ LỌC TASK
        Specification<Task> spec = TaskSpecification.filterTasks(
                projectId,
                targetSprintId,
                isBacklog,
                keyword,
                assigneeId,
                priority,
                taskType,
                null // statusIds = null để lấy tất cả task (Board)
        );

        // 5. LẤY TASK TỪ DB (Lấy tất cả task của sprint/backlog đang xét)
        List<Task> tasks = taskRepository.findAll(spec, Sort.by("sortOrder").ascending());

        // 6. NHÓM TASK THEO STATUS ID (Grouping in Memory)
        Map<Integer, List<Task>> tasksByStatus = tasks.stream()
                .filter(t -> t.getStatus() != null)
                .collect(Collectors.groupingBy(t -> t.getStatus().getId()));

        // 7. BUILD RESPONSE: Lặp qua từng cột status để điền tasks vào
        List<BoardColumnResponse> board = new ArrayList<>();

        for (com.quanlyduan.project_manager_api.model.ProjectStatus status : statuses) {
            List<Task> tasksInColumn = tasksByStatus.getOrDefault(status.getId(), Collections.emptyList());

            List<TaskResponse> taskResponses = tasksInColumn.stream()
                    .map(taskService::mapToTaskResponse)
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

    /**
     * US-S4-8-9-11: Lọc và Phân trang Task (List View).
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<TaskResponse> getProjectTaskList(
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
                projectId,      // 1. projectId
                sprintId,       // 2. sprintId (0 hoặc ID)
                isBacklog,      // 3. isBacklog
                search,         // 4. keyword
                assigneeId,     // 5. assigneeId
                priority,       // 6. priority
                null,           // 7. taskType (null, không lọc)
                statusIds       // 8. statusIds (List trạng thái để lọc)
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
        Page<TaskResponse> responsePage = taskPage.map(taskService::mapToTaskResponse);

        return new PageResponseDTO<>(responsePage);
    }

    /**
     * US-S4-10: Nhóm Task (Grouping View).
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, List<TaskResponse>> getTasksGroupedBy(
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

        // Gọi Specification theo chuẩn mới (8 tham số)
        Specification<Task> spec = TaskSpecification.filterTasks(
                projectId,
                sprintId,       // Filter theo sprintId
                isBacklog,      // Filter boolean isBacklog
                search,         // Search keyword
                null,           // assigneeId (null để lấy hết)
                null,           // priority (null để lấy hết)
                null,           // taskType (null)
                null            // statusIds (null)
        );

        List<Task> tasks = taskRepository.findAll(spec);

        // 3. GROUPING LOGIC (Java Streams)
        if ("assignee".equalsIgnoreCase(groupBy)) {
            // Nhóm theo Tên người được giao
            return tasks.stream().collect(Collectors.groupingBy(
                t -> t.getAssignee() != null ? t.getAssignee().getFullName() : "Unassigned",
                Collectors.mapping(taskService::mapToTaskResponse, Collectors.toList())
            ));

        } else if ("priority".equalsIgnoreCase(groupBy)) {
             // Nhóm theo Độ ưu tiên
             return tasks.stream()
                 .filter(t -> t.getPriority() != null) // Lọc bỏ nếu priority null
                 .collect(Collectors.groupingBy(
                     t -> t.getPriority().name(), // Group theo tên Enum (HIGH, LOW...)
                     Collectors.mapping(taskService::mapToTaskResponse, Collectors.toList())
                 ));

        } else if ("status".equalsIgnoreCase(groupBy)) {
             // Nhóm theo Trạng thái
             return tasks.stream()
                 .filter(t -> t.getStatus() != null)
                 .collect(Collectors.groupingBy(
                     t -> t.getStatus().getName(),
                     Collectors.mapping(taskService::mapToTaskResponse, Collectors.toList())
                 ));
        } else if ("sprint".equalsIgnoreCase(groupBy)) {
             // Nhóm theo Sprint
             return tasks.stream()
                 .collect(Collectors.groupingBy(
                     // Nếu có sprint -> lấy tên, nếu null -> gom vào "Backlog"
                     t -> t.getSprint() != null ? t.getSprint().getName() : "Backlog",
                     Collectors.mapping(taskService::mapToTaskResponse, Collectors.toList())
                 ));
        }

        // Sửa thông báo sang tiếng Anh
        throw new BadRequestException("Invalid groupBy parameter. Use 'assignee', 'priority', 'status' or 'sprint'.");
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
    public void inviteMemberToProject(Integer projectId, InviteProjectMemberRequest request) {
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
                return; // Kết thúc
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

        projectInvitationRepository.save(invitation);

        // 6. Gửi Email Mời
        sendProjectInvitationEmail(inviter, email, project, role, token);
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
     * Helper: Map Task Entity sang TaskSummaryResponse DTO.
     */
    private TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        User assignee = task.getAssignee();
        Epic epic = task.getEpic();
        com.quanlyduan.project_manager_api.model.ProjectStatus status = task.getStatus();

        return TaskSummaryResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .taskType(task.getTaskType())

                .statusId(status != null ? status.getId() : null)
                .statusName(status != null ? status.getName() : "N/A")
                .statusColor(status != null ? status.getColor() : "#FFFFFF")

                .priority(task.getPriority())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .assigneeId(assignee != null ? assignee.getId() : null)
                .assigneeName(assignee != null ? assignee.getFullName() : null)
                .assigneeAvatarUrl(assignee != null ? assignee.getAvatarUrl() : null)
                .epicId(epic != null ? epic.getId() : null)
                .epicName(epic != null ? epic.getName() : null)
                .epicColor(epic != null ? epic.getColor() : null)
                .storyPoints(task.getStoryPoints())
                .dueDate(task.getDueDate())
                .sortOrder(task.getSortOrder())
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
                "Xin chào %s,<br><br>" +
                "%s đã thêm bạn vào dự án <strong>%s</strong> với vai trò <strong>%s</strong>.<br>" +
                "Truy cập dự án tại đây: <a href=\"%s\">View Project</a>",
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
                "Xin chào,<br><br>" +
                "%s đã mời bạn tham gia dự án <strong>%s</strong> với vai trò <strong>%s</strong>.<br>" +
                "Vui lòng nhấp vào liên kết dưới đây để chấp nhận lời mời:<br>" +
                "<a href=\"%s\">Accept Invitation</a><br><br>" +
                "Liên kết này sẽ hết hạn sau 7 ngày.",
                inviter.getFullName(), project.getName(), role.getRoleName(), acceptUrl
            );
            emailService.sendEmail(email, subject, body);
        } catch (Exception e) {
            System.err.println("Error sending external project invitation email: " + e.getMessage());
        }
    }
}