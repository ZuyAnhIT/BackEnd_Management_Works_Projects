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
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
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

    // Khai bao cac hang so de loai bo hardcode
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_INVITE = "INVITE";
    public static final String ACTION_JOIN = "JOIN";

    public static final String ENTITY_PROJECT = "PROJECT";
    public static final String ENTITY_PROJECT_MEMBER = "PROJECT_MEMBER";

    public static final String DESC_CREATE_PROJECT = "Create new Project";
    public static final String DESC_DELETE_PROJECT = "Delete Project";
    public static final String DESC_UPDATE_PROJECT_STATUS = "Update Project Status";
    public static final String DESC_UPDATE_PROJECT = "Update project information";
    public static final String DESC_UPDATE_MEMBER_ROLE = "Update Project Member Role";
    public static final String DESC_INVITE_MEMBER = "Invite member to Project";
    public static final String DESC_ACCEPT_INVITATION = "Accept project invitation";

    public static final String ERROR_WORKSPACE_NOT_FOUND = "Workspace not found.";
    public static final String ERROR_WORKSPACE_WRONG_COMPANY = "Workspace does not belong to the specified company.";
    public static final String ERROR_PROJECT_CODE_EXISTS = "Project code already exists in this workspace.";
    public static final String ERROR_USER_NOT_FOUND = "User not found.";
    public static final String ERROR_INVALID_BOARD_CONFIG = "Invalid JSON boardConfig.";
    public static final String ERROR_MANAGER_NOT_FOUND = "Manager user not found.";
    public static final String ERROR_PROJECT_TYPE_NOT_FOUND = "Project type not found.";
    public static final String ERROR_ROLE_NOT_FOUND = "Role not found: ";
    public static final String ERROR_PROJECT_NOT_FOUND = "Project not found.";
    public static final String ERROR_PROJECT_NOT_FOUND_ID = "Project not found with ID: ";
    public static final String ERROR_PROJECT_WRONG_WORKSPACE = "Project does not belong to the specified workspace.";
    public static final String ERROR_PROJECT_WRONG_HIERARCHY = "Project does not belong to this workspace or company.";
    public static final String ERROR_EMPTY_STATUS = "New status cannot be empty.";
    public static final String ERROR_CANNOT_UPDATE_TO_CANCELLED = "Cannot update status to CANCELLED. Please use the delete API instead.";
    public static final String ERROR_SAME_STATUS = "Project is already in the requested status.";
    public static final String ERROR_INVALID_GROUP_BY = "Invalid groupBy parameter. Use 'assignee', 'priority', 'status' or 'sprint'.";
    public static final String ERROR_MEMBER_NOT_FOUND_ID = "Project member not found with ID: ";
    public static final String ERROR_MEMBER_NOT_IN_PROJECT = "Member not found in this project.";
    public static final String ERROR_CHANGE_OWN_ROLE = "You cannot change your own role.";
    public static final String ERROR_SAME_ROLE = "New role is the same as the current role, no update needed.";
    public static final String ERROR_INVALID_ROLE_LEVEL = "Invalid role. Must be a PROJECT level role.";
    public static final String ERROR_ALREADY_MEMBER = "This user is already a member of the project.";
    public static final String ERROR_INVITE_GUEST_ONLY = "Security Policy: Outsiders can only be invited with the Guest role (GUEST_PROJECT). To assign higher roles like Admin or Member, they must be invited to the Company first.";
    public static final String ERROR_PENDING_INVITATION = "An invitation is already pending for this email.";
    public static final String ERROR_INVALID_INVITATION = "Invitation does not exist or token is invalid.";
    public static final String ERROR_INVITATION_PROCESSED = "This invitation is no longer valid or has been processed.";
    public static final String ERROR_INVITATION_EXPIRED = "Invitation has expired.";
    public static final String ERROR_EMAIL_MISMATCH = "Account email does not match the invitation email.";
    public static final String ERROR_INVITATION_WRONG_PROJECT = "Invitation does not belong to this project.";

    public static final String LOG_RENAMED = "renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"";
    public static final String LOG_CODE_CHANGED = "changed code from <strong>%s</strong> to <strong>%s</strong>";
    public static final String LOG_DESC_UPDATED = "updated description";
    public static final String LOG_GOAL_UPDATED = "updated goal";
    public static final String LOG_PRIORITY_CHANGED = "changed priority to <strong>%s</strong>";
    public static final String LOG_START_DATE_CHANGED = "changed start date";
    public static final String LOG_DUE_DATE_CHANGED = "changed due date";
    public static final String LOG_MANAGER_REMOVED = "removed manager";
    public static final String LOG_MANAGER_CHANGED = "changed manager to <strong>%s</strong>";
    public static final String LOG_COVER_UPDATED = "updated cover image";
    public static final String LOG_JOINED_PROJECT = "has joined the project <strong>%s</strong>";

    public static final String GROUP_BY_ASSIGNEE = "assignee";
    public static final String GROUP_BY_PRIORITY = "priority";
    public static final String GROUP_BY_STATUS = "status";
    public static final String GROUP_BY_SPRINT = "sprint";

    public static final String LABEL_UNASSIGNED = "Unassigned";
    public static final String LABEL_BACKLOG = "Backlog";
    public static final String LABEL_SYSTEM = "System";

    public static final String ROLE_GUEST_PROJECT = "GUEST_PROJECT";
    public static final String ROLE_PROJECT_ADMIN = "PROJECT_ADMIN";

    public static final String SORT_FIELD_CREATED_AT = "createdAt";
    public static final String SORT_FIELD_SORT_ORDER = "sortOrder";
    public static final String SORT_FIELD_JOINED_AT = "joinedAt";
    public static final String SORT_DIR_ASC = "asc";

    public static final String FOLDER_PROJECT_COVERS = "project-covers";

    public static final String DEFAULT_STATUS_TODO = "To Do";
    public static final String DEFAULT_STATUS_IN_PROGRESS = "In Progress";
    public static final String DEFAULT_STATUS_DONE = "Done";
    public static final String DEFAULT_COLOR_TODO = "#95a5a6";
    public static final String DEFAULT_COLOR_IN_PROGRESS = "#3498db";
    public static final String DEFAULT_COLOR_DONE = "#2ecc71";

    public static final String EMAIL_SUBJECT_ADDED = "You have been added to the project: ";
    public static final String EMAIL_BODY_ADDED = "Hi %s,<br><br>%s has included you into project <strong>%s</strong> with role <strong>%s</strong>.<br>Please access project with this link: <a href=\"%s\">View Project</a>";
    public static final String EMAIL_SUBJECT_INVITED = "Project Invitation: ";
    public static final String EMAIL_BODY_INVITED = "Hi,<br><br>%s has invited you into project <strong>%s</strong> with role <strong>%s</strong>.<br>Please click the link below to accept the invitation:<br><a href=\"%s\">Accept Invitation</a><br><br>This link will expire in 7 days.";

    // Khai bao cac bien phu thuoc va cau hinh
    @Value("${app.frontend.url}")
    private String frontendUrl;

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

    // Constructor khoi tao thu cong
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
                              QuotaValidationServiceImpl quotaValidationService) {
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

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional
    @LogActivity(action = ACTION_CREATE, entityType = ENTITY_PROJECT, description = DESC_CREATE_PROJECT)
    public ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request, Integer creatorId, MultipartFile coverImageFile) {
        // Kiem tra han muc truoc khi tao du an moi
        quotaValidationService.validateProjectCreationQuota(companyId);
        
        // Kiem tra khong gian lam viec
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND));

        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_WORKSPACE_WRONG_COMPANY);
        }

        // Kiem tra ma du an da ton tai hay chua
        if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, request.getProjectCode())) {
            throw new BadRequestException(ERROR_PROJECT_CODE_EXISTS);
        }

        // Lay thong tin nguoi tao du an
        User createdBy = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND));

        // Khoi tao doi tuong du an
        Project project = new Project();
        project.setWorkspace(workspace);
        project.setCreatedBy(createdBy);
        project.setName(request.getName());
        project.setProjectCode(request.getProjectCode());
        project.setDescription(request.getDescription());
        project.setGoal(request.getGoal());

        // Xu ly upload anh bia
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String coverPath = fileStorageService.storeFile(coverImageFile, FOLDER_PROJECT_COVERS);
            project.setCoverImageUrl(coverPath);
        } else if (request.getCoverImageUrl() != null) {
            project.setCoverImageUrl(request.getCoverImageUrl());
        }

        // Cau hinh bang cong viec
        if (request.getBoardConfig() != null) {
            try {
                project.setBoardConfig(objectMapper.writeValueAsString(request.getBoardConfig()));
            } catch (JsonProcessingException e) {
                throw new BadRequestException(ERROR_INVALID_BOARD_CONFIG);
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
                    .orElseThrow(() -> new ResourceNotFoundException(ERROR_MANAGER_NOT_FOUND));
            project.setManager(manager);
        }

        if (request.getProjectTypeId() != null && request.getProjectTypeId() > 0) {
            ProjectType type = projectTypeRepository.findById(request.getProjectTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_TYPE_NOT_FOUND));
            project.setProjectType(type);
        }

        if (project.getProgress() == null) {
            project.setProgress(BigDecimal.ZERO);
        }

        // Luu du an moi vao co so du lieu
        Project saved = projectRepository.save(project);

        // Gan quyen quan tri du an cho nguoi tao
        Role projectAdminRole = roleRepository.findFirstByRoleCode(ROLE_PROJECT_ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_NOT_FOUND + ROLE_PROJECT_ADMIN));

        ProjectMember projectMember = ProjectMember.builder()
                .project(saved)
                .user(createdBy)
                .role(projectAdminRole)
                .status(MemberStatus.ACTIVE)
                .build();

        projectMemberRepository.save(projectMember);

        // Khoi tao cac trang thai mac dinh cho du an
        initDefaultStatuses(saved); 

        return toResponse(saved);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_DELETE, entityType = ENTITY_PROJECT, description = DESC_DELETE_PROJECT)
    public void deleteProject(Integer companyId, Integer workspaceId, Integer projectId) {
        // Kiem tra phan cap cua khong gian lam viec
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND));
                
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_WORKSPACE_WRONG_COMPANY);
        }

        // Kiem tra phan cap cua du an
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND));
                
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException(ERROR_PROJECT_WRONG_WORKSPACE);
        }
        
        // Thuc hien xoa mem bang cach chuyen trang thai sang huy bo
        project.setStatus(ProjectStatus.CANCELLED);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_PROJECT, description = DESC_UPDATE_PROJECT_STATUS)
    public ProjectResponse updateProjectStatus(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectStatusRequest request) {
        // Kiem tra phan cap cua khong gian lam viec
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND));
                
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_WORKSPACE_WRONG_COMPANY);
        }

        // Kiem tra phan cap cua du an
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND));
                
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException(ERROR_PROJECT_WRONG_WORKSPACE);
        }

        // Kiem tra tinh hop le cua trang thai moi
        ProjectStatus newStatus = request.getNewStatus();
        if (newStatus == null) {
            throw new BadRequestException(ERROR_EMPTY_STATUS);
        }
        
        if (newStatus == ProjectStatus.CANCELLED) {
            throw new BadRequestException(ERROR_CANNOT_UPDATE_TO_CANCELLED);
        }
        
        if (project.getStatus() == newStatus) {
            throw new BadRequestException(ERROR_SAME_STATUS);
        }

        // Cap nhat trang thai va luu
        project.setStatus(newStatus);
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_PROJECT, description = DESC_UPDATE_PROJECT)
    public ProjectResponse updateProject(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectRequest request, MultipartFile coverImageFile) {
        // Kiem tra phan cap
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND));
                
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_WORKSPACE_WRONG_COMPANY);
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND));
                
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException(ERROR_PROJECT_WRONG_WORKSPACE);
        }

        StringBuilder changes = new StringBuilder();

        // Cap nhat ten va ma du an
        if (isProvided(request.getName()) && !request.getName().equals(project.getName())) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(String.format(LOG_RENAMED, project.getName(), request.getName()));
            project.setName(request.getName());
        }

        if (isProvided(request.getProjectCode()) && !request.getProjectCode().equals(project.getProjectCode())) {
            if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, request.getProjectCode())) {
                throw new BadRequestException(ERROR_PROJECT_CODE_EXISTS);
            }
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(String.format(LOG_CODE_CHANGED, project.getProjectCode(), request.getProjectCode()));
            project.setProjectCode(request.getProjectCode());
        }

        // Cap nhat mo ta va muc tieu
        if (isProvided(request.getDescription()) && !request.getDescription().equals(project.getDescription())) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(LOG_DESC_UPDATED);
            project.setDescription(request.getDescription());
        }
        
        if (isProvided(request.getGoal()) && !request.getGoal().equals(project.getGoal())) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(LOG_GOAL_UPDATED);
            project.setGoal(request.getGoal());
        }

        // Cap nhat muc do uu tien
        if (request.getPriority() != null && request.getPriority() != project.getPriority()) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(String.format(LOG_PRIORITY_CHANGED, request.getPriority()));
            project.setPriority(request.getPriority());
        }

        // Cap nhat thoi gian
        if (request.getStartDate() != null && !request.getStartDate().equals(project.getStartDate())) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(LOG_START_DATE_CHANGED);
            project.setStartDate(request.getStartDate());
        }
        
        if (request.getDueDate() != null && !request.getDueDate().equals(project.getDueDate())) {
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(LOG_DUE_DATE_CHANGED);
            project.setDueDate(request.getDueDate());
        }
        
        if (request.getCompletedAt() != null && !request.getCompletedAt().equals(project.getCompletedAt())) {
             project.setCompletedAt(request.getCompletedAt());
        }

        // Cap nhat nguoi quan ly
        if (request.getManagerId() != null) {
            Integer oldManagerId = project.getManager() != null ? project.getManager().getId() : 0;
            if (!request.getManagerId().equals(oldManagerId)) {
                if (request.getManagerId() == 0) {
                     if (changes.length() > 0) {
                         changes.append(", ");
                     }
                     changes.append(LOG_MANAGER_REMOVED);
                     project.setManager(null);
                } else {
                    User manager = userRepository.findById(request.getManagerId())
                            .orElseThrow(() -> new ResourceNotFoundException(ERROR_MANAGER_NOT_FOUND));
                    
                    if (changes.length() > 0) {
                        changes.append(", ");
                    }
                    changes.append(String.format(LOG_MANAGER_CHANGED, manager.getFullName()));
                    project.setManager(manager);
                }
            }
        }

        // Cap nhat anh bia
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String coverPath = fileStorageService.storeFile(coverImageFile, FOLDER_PROJECT_COVERS);
            if (changes.length() > 0) {
                changes.append(", ");
            }
            changes.append(LOG_COVER_UPDATED);
            project.setCoverImageUrl(coverPath);
        } else if (request.getCoverImageUrl() != null && !request.getCoverImageUrl().equals(project.getCoverImageUrl())) {
             if (changes.length() > 0) {
                 changes.append(", ");
             }
             changes.append(LOG_COVER_UPDATED);
             project.setCoverImageUrl(request.getCoverImageUrl().isBlank() ? null : request.getCoverImageUrl());
        }

        // Ghi log vao context
        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        }

        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    @Override
    public ProjectResponse getProjectDetails(Integer companyId, Integer workspaceId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND));

        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException(ERROR_PROJECT_WRONG_WORKSPACE);
        }

        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_WORKSPACE_WRONG_COMPANY);
        }

        return toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectResponse> listProjectsByWorkspace(
            Integer companyId, Integer workspaceId, ProjectStatus status,
            int page, int size, String sortBy, String sortDir) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND));
                
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_WORKSPACE_WRONG_COMPANY);
        }

        Map<String, String> sortMapping = Map.of(
            SORT_FIELD_CREATED_AT, SORT_FIELD_CREATED_AT,
            "name", "name",
            "code", "projectCode",
            "status", "status",
            "dueDate", "dueDate",
            "manager", "manager.fullName"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, SORT_FIELD_CREATED_AT, sortMapping);

        Page<Project> projectPage;
        if (status != null) {
            projectPage = projectRepository.findByWorkspace_IdAndStatus(workspaceId, status, pageable);
        } else {
            projectPage = projectRepository.findByWorkspace_Id(workspaceId, pageable);
        }

        Page<ProjectResponse> dtoPage = projectPage.map(this::toResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectResponse> searchProjects(
            Integer companyId, Integer workspaceId,
            String searchName, String searchCode, String searchManager, ProjectStatus searchStatus,
            int page, int size, String sortBy, String sortDir) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_WORKSPACE_NOT_FOUND));
                
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_WORKSPACE_WRONG_COMPANY);
        }

        Map<String, String> sortMapping = Map.of(
            SORT_FIELD_CREATED_AT, SORT_FIELD_CREATED_AT,
            "name", "name",
            "code", "projectCode",
            "status", "status",
            "dueDate", "dueDate",
            "manager", "manager.fullName"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, SORT_FIELD_CREATED_AT, sortMapping);

        Specification<Project> spec = ProjectSpecification.filterProjects(
            workspaceId, searchName, searchCode, searchManager, searchStatus
        );

        Page<Project> projectPage = projectRepository.findAll(spec, pageable);
        Page<ProjectResponse> dtoPage = projectPage.map(this::toResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectBacklogResponse getProjectBacklog(
            Integer companyId, Integer workspaceId, Integer projectId,
            String keyword, Integer assigneeId,
            TaskPriority priority, TaskType taskType,
            int page, int size, String sortBy, String sortDir) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND));
                
        if (!project.getWorkspace().getId().equals(workspaceId) ||
            !project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_PROJECT_WRONG_HIERARCHY);
        }

        // Tinh toan cho cac sprint dang hoat dong
        List<Sprint> activeSprints = sprintRepository.findActiveSprintsByProjectId(
            projectId, Arrays.asList(SprintStatus.NOT_STARTED, SprintStatus.IN_PROGRESS)
        );

        List<SprintDetailsResponse> sprintDtos = activeSprints.stream().map(sprint -> {
            Specification<Task> sprintTaskSpec = TaskSpecification.filterTasks(
                projectId, sprint.getId(), false, keyword, assigneeId, priority, taskType, null, false
            );

            List<Task> tasks = taskRepository.findAll(sprintTaskSpec, Sort.by(SORT_FIELD_SORT_ORDER).ascending());

            long totalPoints = tasks.stream()
                    .mapToLong(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                    .sum();

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
                    .taskCount(tasks.size())
                    .build();
        }).collect(Collectors.toList());

        // Tinh toan cho danh sach cho (Backlog)
        Specification<Task> backlogSpec = TaskSpecification.filterTasks(
            projectId, null, true, keyword, assigneeId, priority, taskType, null, false
        );

        Map<String, String> sortMapping = Map.of(
            SORT_FIELD_SORT_ORDER, SORT_FIELD_SORT_ORDER,
            "title", "title",
            "priority", "priority",
            "storyPoints", "storyPoints",
            "dueDate", "dueDate"
        );
        
        Sort sort = SortUtils.createSort(sortBy, sortDir, SORT_FIELD_SORT_ORDER, sortMapping);
        if (SORT_FIELD_SORT_ORDER.equals(sortBy) && (sortDir == null || sortDir.isEmpty())) {
             sort = Sort.by(Sort.Direction.ASC, SORT_FIELD_SORT_ORDER);
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Task> backlogPage = taskRepository.findAll(backlogSpec, pageable);

        List<TaskSummaryResponse> backlogTaskDtos = backlogPage.stream()
                .map(this::mapToTaskSummaryResponse)
                .collect(Collectors.toList());

        return ProjectBacklogResponse.builder()
                .activeSprints(sprintDtos)
                .backlogTasks(backlogTaskDtos)
                .backlogPageNumber(backlogPage.getNumber())
                .backlogPageSize(backlogPage.getSize())
                .backlogTotalElements(backlogPage.getTotalElements())
                .backlogTotalPages(backlogPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardColumnResponse> getProjectBoard(
            Integer companyId, Integer workspaceId, Integer projectId,
            Integer sprintId, String keyword, Integer assigneeId,
            TaskPriority priority, TaskType taskType) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND));

        if (!project.getWorkspace().getId().equals(workspaceId) ||
            !project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_PROJECT_WRONG_HIERARCHY);
        }

        Integer targetSprintId = sprintId;
        boolean isBacklog = false;

        if (targetSprintId == null) {
            List<Sprint> activeSprints = sprintRepository.findActiveSprintsByProjectId(
                projectId, Collections.singletonList(SprintStatus.IN_PROGRESS)
            );
            targetSprintId = activeSprints.isEmpty() ? -1 : activeSprints.get(0).getId();
        } else if (targetSprintId == 0) {
            isBacklog = true;
            targetSprintId = null;
        }

        List<com.quanlyduan.project_manager_api.model.ProjectStatus> statuses =
                projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId);

        Specification<Task> spec = TaskSpecification.filterTasks(
                projectId, targetSprintId, isBacklog, keyword, assigneeId, priority, taskType, null, false
        );

        List<Task> tasks = taskRepository.findAll(spec, Sort.by(SORT_FIELD_SORT_ORDER).ascending());

        Map<Integer, List<Task>> tasksByStatus = tasks.stream()
                .filter(t -> t.getStatus() != null)
                .collect(Collectors.groupingBy(t -> t.getStatus().getId()));

        List<BoardColumnResponse> board = new ArrayList<>();

        for (com.quanlyduan.project_manager_api.model.ProjectStatus status : statuses) {
            List<Task> tasksInColumn = tasksByStatus.getOrDefault(status.getId(), Collections.emptyList());

            List<TaskSummaryResponse> taskResponses = tasksInColumn.stream()
                    .map(this::mapToTaskSummaryResponse)
                    .collect(Collectors.toList());

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

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<TaskSummaryResponse> getProjectTaskList(
            Integer companyId, Integer workspaceId, Integer projectId,
            Integer sprintId, String search, Integer assigneeId, TaskPriority priority,
            List<Integer> statusIds,
            int page, int size, String sortBy, String sortDir) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND_ID + projectId));

        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException(ERROR_PROJECT_WRONG_WORKSPACE);
        }
        
        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_WORKSPACE_WRONG_COMPANY);
        }

        boolean isBacklog = (sprintId != null && sprintId == 0);

        Specification<Task> spec = TaskSpecification.filterTasks(
                projectId, sprintId, isBacklog, search, assigneeId, priority, null, statusIds, false
        );

        Map<String, String> sortMap = Map.of(
            "title", "title",
            "dueDate", "dueDate",
            "priority", "priority",
            "status", "status.name"
        );
        
        Sort sort = SortUtils.createSort(sortBy, sortDir, "id", sortMap);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage = taskRepository.findAll(spec, pageable);
        Page<TaskSummaryResponse> responsePage = taskPage.map(this::mapToTaskSummaryResponse);

        return new PageResponseDTO<>(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<TaskSummaryResponse>> getTasksGroupedBy(
            Integer companyId, Integer workspaceId, Integer projectId,
            String groupBy, Integer sprintId, String search) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND_ID + projectId));

        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException(ERROR_PROJECT_WRONG_WORKSPACE);
        }
        
        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_WORKSPACE_WRONG_COMPANY);
        }

        boolean isBacklog = (sprintId != null && sprintId == 0);

        Specification<Task> spec = TaskSpecification.filterTasks(
                projectId, sprintId, isBacklog, search, null, null, null, null, false
        );

        List<Task> tasks = taskRepository.findAll(spec);
        java.util.function.Function<Task, TaskSummaryResponse> mapper = this::mapToTaskSummaryResponse;

        if (GROUP_BY_ASSIGNEE.equalsIgnoreCase(groupBy)) {
            return tasks.stream().collect(Collectors.groupingBy(
                t -> t.getAssignee() != null ? t.getAssignee().getFullName() : LABEL_UNASSIGNED,
                Collectors.mapping(mapper, Collectors.toList())
            ));
        } else if (GROUP_BY_PRIORITY.equalsIgnoreCase(groupBy)) {
             return tasks.stream()
                 .filter(t -> t.getPriority() != null)
                 .collect(Collectors.groupingBy(
                     t -> t.getPriority().name(),
                     Collectors.mapping(mapper, Collectors.toList())
                 ));
        } else if (GROUP_BY_STATUS.equalsIgnoreCase(groupBy)) {
             return tasks.stream()
                 .filter(t -> t.getStatus() != null)
                 .collect(Collectors.groupingBy(
                     t -> t.getStatus().getName(),
                     Collectors.mapping(mapper, Collectors.toList())
                 ));
        } else if (GROUP_BY_SPRINT.equalsIgnoreCase(groupBy)) {
             return tasks.stream()
                 .collect(Collectors.groupingBy(
                     t -> t.getSprint() != null ? t.getSprint().getName() : LABEL_BACKLOG,
                     Collectors.mapping(mapper, Collectors.toList())
                 ));
        }

        throw new BadRequestException(ERROR_INVALID_GROUP_BY);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskSummaryResponse> getTaskCalendar(
            Integer companyId, Integer workspaceId, Integer projectId,
            LocalDate from, LocalDate to,
            String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND_ID + projectId));

        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException(ERROR_PROJECT_WRONG_WORKSPACE);
        }

        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_WORKSPACE_WRONG_COMPANY);
        }

        Specification<Task> spec = TaskSpecification.filterTasksForCalendar(
                projectId, from, to, keyword, assigneeId, priority, taskType
        );

        List<Task> tasks = taskRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "startDate"));

        return tasks.stream()
                .map(this::mapToTaskSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<TaskSummaryResponse> getArchivedTasks(
            Integer projectId, 
            String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType,
            int page, int size) {
        
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND_ID + projectId);
        }

        Specification<Task> spec = TaskSpecification.filterTasks(
            projectId, null, false, keyword, assigneeId, priority, taskType, null, true
        );
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Task> tasks = taskRepository.findAll(spec, pageable);
        
        return new PageResponseDTO<>(tasks.map(this::mapToTaskSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectMemberResponse> getProjectMembers(
            Integer projectId, int page, int size, String sortBy, String sortDir) {

        Map<String, String> sortMapping = Map.of(
            SORT_FIELD_JOINED_AT, SORT_FIELD_JOINED_AT,
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, SORT_FIELD_JOINED_AT, sortMapping);

        Page<ProjectMember> membersPage = projectMemberRepository.findByProject_Id(projectId, pageable);
        Page<ProjectMemberResponse> dtoPage = membersPage.map(this::mapToProjectMemberResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectMemberResponse> searchProjectMembers(
            Integer projectId,
            String searchName, String searchEmail, String searchRoleName, String searchPhone,
            int page, int size, String sortBy, String sortDir) {

        Map<String, String> sortMapping = Map.of(
            SORT_FIELD_JOINED_AT, SORT_FIELD_JOINED_AT,
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, SORT_FIELD_JOINED_AT, sortMapping);

        Specification<ProjectMember> spec = ProjectMemberSpecification.filterMembers(
            projectId, searchName, searchEmail, searchRoleName, searchPhone
        );

        Page<ProjectMember> membersPage = projectMemberRepository.findAll(spec, pageable);
        Page<ProjectMemberResponse> dtoPage = membersPage.map(this::mapToProjectMemberResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_PROJECT, description = DESC_UPDATE_MEMBER_ROLE)
    public ProjectMemberResponse updateProjectMemberRole(Integer projectId, Integer memberId, String newRoleCode) {
        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_MEMBER_NOT_FOUND_ID + memberId));

        if (!member.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException(ERROR_MEMBER_NOT_IN_PROJECT);
        }

        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            throw new BadRequestException(ERROR_CHANGE_OWN_ROLE);
        }

        if (member.getRole().getRoleCode().equals(newRoleCode)) {
            throw new BadRequestException(ERROR_SAME_ROLE);
        }

        Role newRole = roleRepository.findFirstByRoleCode(newRoleCode)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_NOT_FOUND + newRoleCode));

        if (newRole.getLevel() != RoleLevel.PROJECT) {
            throw new BadRequestException(ERROR_INVALID_ROLE_LEVEL);
        }

        member.setRole(newRole);
        ProjectMember updatedMember = projectMemberRepository.save(member);

        return mapToProjectMemberResponse(updatedMember);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_INVITE, entityType = ENTITY_PROJECT_MEMBER, description = DESC_INVITE_MEMBER)
    public ProjectInvitation inviteMemberToProject(Integer projectId, InviteProjectMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND));

        User inviter = securityService.getCurrentAuthenticatedUser();
        Integer companyId = project.getWorkspace().getCompany().getId();

        Role role = roleRepository.findFirstByRoleCode(request.getRoleCode())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_NOT_FOUND + request.getRoleCode()));

        if (role.getLevel() != RoleLevel.PROJECT) {
            throw new BadRequestException(ERROR_INVALID_ROLE_LEVEL);
        }

        String email = request.getEmail();
        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            if (projectMemberRepository.findByProject_IdAndUser_Id(projectId, existingUser.getId()).isPresent()) {
                throw new BadRequestException(ERROR_ALREADY_MEMBER);
            }

            boolean isCompanyMember = companyMemberRepository.existsByCompany_IdAndUser_IdAndStatus(
                    companyId, existingUser.getId(), MemberStatus.ACTIVE
            );

            if (isCompanyMember) {
                ProjectMember newMember = ProjectMember.builder()
                        .project(project)
                        .user(existingUser)
                        .role(role)
                        .status(MemberStatus.ACTIVE)
                        .build();
                projectMemberRepository.save(newMember);

                sendProjectNotificationEmail(inviter, existingUser, project, role);
                return null; 
            }
        }
        
        if (!role.getRoleCode().equalsIgnoreCase(ROLE_GUEST_PROJECT)) {
            throw new BadRequestException(ERROR_INVITE_GUEST_ONLY);
        }

        if (projectInvitationRepository.existsByProject_IdAndEmailAndStatus(projectId, email, InvitationStatus.PENDING)) {
            throw new BadRequestException(ERROR_PENDING_INVITATION);
        }

        String token = UUID.randomUUID().toString();
        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .email(email)
                .role(role)
                .invitedBy(inviter)
                .token(token)
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        ProjectInvitation projectInvitation = projectInvitationRepository.save(invitation);

        sendProjectInvitationEmail(inviter, email, project, role, token);
        return projectInvitation;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectInvitationDetailsResponse getProjectInvitationDetails(String token) {
        ProjectInvitation invitation = projectInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVALID_INVITATION));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException(ERROR_INVITATION_PROCESSED);
        }
        
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ERROR_INVITATION_EXPIRED);
        }

        boolean accountExists = userRepository.existsByEmail(invitation.getEmail());

        return ProjectInvitationDetailsResponse.builder()
                .email(invitation.getEmail())
                .projectName(invitation.getProject().getName())
                .roleName(invitation.getRole().getRoleName())
                .accountExists(accountExists)
                .expiresAt(invitation.getExpiresAt())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_JOIN, entityType = ENTITY_PROJECT_MEMBER, description = DESC_ACCEPT_INVITATION)
    public void acceptProjectInvitation(String token) {
        User currentUser = securityService.getCurrentAuthenticatedUser();

        ProjectInvitation invitation = projectInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVALID_INVITATION));

        if (!invitation.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new BadRequestException(ERROR_EMAIL_MISMATCH);
        }
        
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException(ERROR_INVITATION_PROCESSED);
        }

        ProjectMember newMember = ProjectMember.builder()
                .project(invitation.getProject())
                .user(currentUser)
                .role(invitation.getRole())
                .status(MemberStatus.ACTIVE)
                .build();
        projectMemberRepository.save(newMember);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        projectInvitationRepository.save(invitation);

        String welcomeMsg = String.format(LOG_JOINED_PROJECT, invitation.getProject().getName());
        ActivityLogContext.setDetail(welcomeMsg);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectInvitationResponse> getProjectInvitations(
            Integer projectId,
            String keyword,
            String statusStr,
            int page, int size, String sortBy, String sortDir
    ) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND_ID + projectId);
        }

        Map<String, String> sortMapping = Map.of(
            SORT_FIELD_CREATED_AT, SORT_FIELD_CREATED_AT,
            "email", "email",
            "role", "role.roleCode",
            "status", "status"
        );

        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(SORT_DIR_ASC)
                ? org.springframework.data.domain.Sort.by(sortMapping.getOrDefault(sortBy, SORT_FIELD_CREATED_AT)).ascending()
                : org.springframework.data.domain.Sort.by(sortMapping.getOrDefault(sortBy, SORT_FIELD_CREATED_AT)).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);

        String searchKeyword = (keyword != null) ? keyword.trim() : "";

        InvitationStatus status = InvitationStatus.PENDING;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = InvitationStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                status = InvitationStatus.PENDING;
            }
        }

        Page<ProjectInvitation> invitationPage = projectInvitationRepository
                .findByProject_IdAndStatusAndEmailContainingIgnoreCase(projectId, status, searchKeyword, pageable);

        Page<ProjectInvitationResponse> dtoPage = invitationPage.map(this::mapToInvitationResponse);

        return new PageResponseDTO<>(dtoPage);
    }

    @Override
    @Transactional
    public void cancelProjectInvitation(Integer projectId, Integer invitationId) {
        ProjectInvitation invitation = projectInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVALID_INVITATION));

        if (!invitation.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_INVITATION_WRONG_PROJECT);
        }

        projectInvitationRepository.delete(invitation);
    }

    // --- CAC HAM PRIVATE HO TRO NGHIEP VU ---

    private boolean isProvided(String value) {
        return value != null && !value.isBlank() && !"string".equalsIgnoreCase(value.trim());
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir,
                                    String defaultSortField, Map<String, String> sortMapping) {
        Sort sort = SortUtils.createSort(sortBy, sortDir, defaultSortField, sortMapping);
        return PageRequest.of(page, size, sort);
    }

    private void sendProjectNotificationEmail(User inviter, User user, Project project, Role role) {
        try {
            String projectUrl = frontendUrl + "/companies/" + project.getWorkspace().getCompany().getId() +
                                "/workspaces/" + project.getWorkspace().getId() +
                                "/projects/" + project.getId() + "/board";

            String subject = EMAIL_SUBJECT_ADDED + project.getName();
            String body = String.format(
                EMAIL_BODY_ADDED,
                user.getFullName(), inviter.getFullName(), project.getName(), role.getRoleName(), projectUrl
            );
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            // Bo qua loi gui mail de khong lam gian doan luong xu ly chinh
        }
    }

    private void sendProjectInvitationEmail(User inviter, String email, Project project, Role role, String token) {
        try {
            String acceptUrl = frontendUrl + "/accept-project-invitation?token=" + token;
            String subject = EMAIL_SUBJECT_INVITED + project.getName();
            String body = String.format(
                EMAIL_BODY_INVITED,
                inviter.getFullName(), project.getName(), role.getRoleName(), acceptUrl
            );
            emailService.sendEmail(email, subject, body);
        } catch (Exception e) {
            // Bo qua loi gui mail de khong lam gian doan luong xu ly chinh
        }
    }

    private void initDefaultStatuses(Project project) {
        List<com.quanlyduan.project_manager_api.model.ProjectStatus> defaultStatuses = new ArrayList<>();

        defaultStatuses.add(com.quanlyduan.project_manager_api.model.ProjectStatus.builder()
                .project(project)
                .name(DEFAULT_STATUS_TODO)
                .color(DEFAULT_COLOR_TODO) 
                .sortOrder(0)
                .isCompletedStatus(false)
                .build());

        defaultStatuses.add(com.quanlyduan.project_manager_api.model.ProjectStatus.builder()
                .project(project)
                .name(DEFAULT_STATUS_IN_PROGRESS)
                .color(DEFAULT_COLOR_IN_PROGRESS) 
                .sortOrder(1)
                .isCompletedStatus(false)
                .build());

        defaultStatuses.add(com.quanlyduan.project_manager_api.model.ProjectStatus.builder()
                .project(project)
                .name(DEFAULT_STATUS_DONE)
                .color(DEFAULT_COLOR_DONE) 
                .sortOrder(2)
                .isCompletedStatus(true)
                .build());

        projectStatusRepository.saveAll(defaultStatuses);
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

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

    public TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        User assignee = task.getAssignee();
        Epic epic = task.getEpic();
        com.quanlyduan.project_manager_api.model.ProjectStatus status = task.getStatus();

        int totalSubtasks = 0;
        int completedSubtasks = 0;
        if (task.getSubTasks() != null) {
            totalSubtasks = task.getSubTasks().size();
            completedSubtasks = (int) task.getSubTasks().stream()
                    .filter(st -> st.getStatus() == SubTaskStatus.DONE)
                    .count();
        }

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
                .status(status != null ? TaskSummaryResponse.StatusInfo.builder()
                        .id(status.getId())
                        .name(status.getName())
                        .color(status.getColor())
                        .build() : null)
                .epic(epic != null ? TaskSummaryResponse.EpicInfo.builder()
                        .id(epic.getId())
                        .name(epic.getName())
                        .color(epic.getColor())
                        .build() : null)
                .assignee(assignee != null ? TaskSummaryResponse.UserInfo.builder()
                        .id(assignee.getId())
                        .name(assignee.getFullName())
                        .avatarUrl(assignee.getAvatarUrl())
                        .build() : null)
                .tags(tagInfos)
                .subtaskSummary(TaskSummaryResponse.SubtaskSummary.builder()
                        .total(totalSubtasks)
                        .completed(completedSubtasks)
                        .build())
                .build();
    }

    private ProjectInvitationResponse mapToInvitationResponse(ProjectInvitation invitation) {
        String fullLink = "";

        if (invitation.getStatus() == InvitationStatus.PENDING) {
            fullLink = frontendUrl + "/accept-project-invitation?token=" + invitation.getToken();
        }

        return ProjectInvitationResponse.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .roleCode(invitation.getRole().getRoleCode())
                .status(invitation.getStatus().name())
                .invitedAt(invitation.getCreatedAt())
                .invitationLink(fullLink)
                .inviterName(invitation.getInvitedBy() != null ? invitation.getInvitedBy().getFullName() : LABEL_SYSTEM)
                .inviterAvatar(invitation.getInvitedBy() != null ? invitation.getInvitedBy().getAvatarUrl() : null)
                .build();
    }
}