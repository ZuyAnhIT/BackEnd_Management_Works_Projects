// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/ProjectServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.BoardColumnResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectBacklogResponse;
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.security.SecurityService; 

import java.math.BigDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

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
import com.quanlyduan.project_manager_api.repository.EpicRepository;
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

    // *** CONSTRUCTOR THỦ CÔNG ĐÃ CẬP NHẬT (12 tham số) ***
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
                              FileStorageService fileStorageService
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
    }

    private boolean isProvided(String value) {
        return value != null && !value.isBlank() && !"string".equalsIgnoreCase(value.trim());
    }
    /**
     * US7: Tạo Project mới trong Workspace (TICH HOP UPLOAD).
     */
    @Override
    @Transactional
    public ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request, Integer creatorId, MultipartFile coverImageFile) {
        // (1) Kiểm tra workspace tồn tại
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc")); // Đã dịch

        // (1b) Xác nhận workspace thuộc đúng companyId
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Không gian làm việc không thuộc về công ty được chỉ định"); // Đã dịch
        }

        // (2) Kiểm tra unique projectCode
        if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, request.getProjectCode())) {
            throw new BadRequestException("Mã dự án đã tồn tại trong không gian làm việc này"); // Đã dịch
        }

        // (3) Lấy createdBy
        User createdBy = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng")); // Đã dịch

        // (4) Khởi tạo Project
        Project project = new Project();
        project.setWorkspace(workspace);
        project.setCreatedBy(createdBy);

        // Scalar từ request
        project.setName(request.getName());
        project.setProjectCode(request.getProjectCode());
        project.setDescription(request.getDescription());
        project.setGoal(request.getGoal());
        
        // *** XỬ LÝ UPLOAD ẢNH BÌA (MỚI) ***
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            // Lưu vào thư mục "project-covers"
            String coverPath = fileStorageService.storeFile(coverImageFile, "project-covers");
            project.setCoverImageUrl(coverPath);
        } else if (request.getCoverImageUrl() != null) {
            // Nếu người dùng gửi link ảnh (URL string)
            project.setCoverImageUrl(request.getCoverImageUrl());
        }

        // boardConfig
        if (request.getBoardConfig() != null) {
            try {
                project.setBoardConfig(objectMapper.writeValueAsString(request.getBoardConfig()));
            } catch (JsonProcessingException e) {
                throw new BadRequestException("JSON boardConfig không hợp lệ"); // Đã dịch
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
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng quản lý")); // Đã dịch
            project.setManager(manager);
        }

        // Project Type
        if (request.getProjectTypeId() != null && request.getProjectTypeId() > 0) {
            ProjectType type = projectTypeRepository.findById(request.getProjectTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại dự án")); // Đã dịch
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
                        "Không tìm thấy vai trò: " + RoleCode.PROJECT_ADMIN.name() + ". Vui lòng cấu hình trong cơ sở dữ liệu." // Đã dịch
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
     * (Giữ nguyên comment)
     */
    @Override
    @Transactional
    public void deleteProject(Integer companyId, Integer workspaceId, Integer projectId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc"));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Không gian làm việc không thuộc về công ty được chỉ định");
        }

       Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án"));
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Dự án không thuộc về không gian làm việc được chỉ định");
        }
        project.setStatus(ProjectStatus.CANCELLED);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectStatusRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc"));
            if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
                throw new BadRequestException("Không gian làm việc không thuộc về công ty được chỉ định");
            }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án"));
            if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
                throw new BadRequestException("Dự án không thuộc về không gian làm việc được chỉ định");
            }

        ProjectStatus newStatus = request.getNewStatus();
            if (newStatus == null) {
                throw new BadRequestException("Trạng thái mới không được để trống");
            }
            if (newStatus == ProjectStatus.CANCELLED) {
                throw new BadRequestException("Không thể cập nhật trạng thái thành CANCELLED. Vui lòng sử dụng API xóa thay thế.");
            }

            if (project.getStatus() == newStatus) {
                throw new BadRequestException("Dự án đã ở trạng thái được yêu cầu.");
            }

        project.setStatus(newStatus);
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    // LOGIC CAP NHAT DU AN (TICH HOP UPLOAD ANH)
    @Override
    @Transactional
    public ProjectResponse updateProject(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectRequest request, MultipartFile coverImageFile) {
        
        // 1. Tìm và Kiểm tra Workspace
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc")); // Đã dịch
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Không gian làm việc không thuộc về công ty được chỉ định"); // Đã dịch
        }

        // 2. Tìm Project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án")); // Đã dịch
        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Dự án không thuộc về không gian làm việc được chỉ định"); // Đã dịch
        }

        // 3. Cập nhật các trường thông tin (Scalar)
        if (isProvided(request.getName())) {
            project.setName(request.getName());
        }

        if (isProvided(request.getProjectCode())) {
            String newCode = request.getProjectCode();
            String currentCode = project.getProjectCode();
            if (!newCode.equalsIgnoreCase(currentCode)) {
                if (projectRepository.existsByWorkspace_IdAndProjectCodeIgnoreCase(workspaceId, newCode)) {
                    throw new BadRequestException("Mã dự án đã tồn tại trong không gian làm việc này"); // Đã dịch
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

        if (request.getManagerId() != null) {
            Integer managerId = request.getManagerId();
            if (managerId != 0) {
                User manager = userRepository.findById(managerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quản lý")); // Đã dịch
                project.setManager(manager);
            }
        }

        if (request.getProjectTypeId() != null) {
            Integer projectTypeId = request.getProjectTypeId();
            if (projectTypeId == 0) {
                project.setProjectType(null);
            } else {
                ProjectType type = projectTypeRepository.findById(projectTypeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại dự án")); // Đã dịch
                project.setProjectType(type);
            }
        }

        if (isProvided(request.getBoardConfig())) {
            try {
                objectMapper.readTree(request.getBoardConfig());
            } catch (Exception e) {
                throw new BadRequestException("boardConfig không phải là JSON hợp lệ"); // Đã dịch
            }
            project.setBoardConfig(request.getBoardConfig());
        }

        // 4. XỬ LÝ UPLOAD ẢNH BÌA (MỚI)
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            // Lưu vào thư mục "project-covers"
            String coverPath = fileStorageService.storeFile(coverImageFile, "project-covers");
            project.setCoverImageUrl(coverPath);
        }
        // Nếu gửi link ảnh trực tiếp (String) và không gửi file
        else if (isProvided(request.getCoverImageUrl())) {
            project.setCoverImageUrl(request.getCoverImageUrl());
        }

        // 5. Lưu và trả về
        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }
    /**
     * Helper map Entity -> DTO (tối thiểu, không viết mapping phức tạp; chỉ rút gọn trường cần thiết).
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
    @Override
    public ProjectResponse getProjectDetails(Integer companyId, Integer workspaceId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án"));

        // Kiểm tra project có thuộc workspace và company tương ứng không
        if (!project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Dự án không thuộc về không gian làm việc đã chỉ định");
        }

        if (!project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Không gian làm việc không thuộc công ty được chỉ định");
        }

        // Dùng mapper chung để đảm bảo đầy đủ field như khi tạo/list
        return toResponse(project);
    }

    // LOGIC LAY DU LIEU MAN HINH BACKLOG (ACTIVE SPRINTS + PAGINATED BACKLOG)
    @Override
    @Transactional(readOnly = true)
    public ProjectBacklogResponse getProjectBacklog(
            Integer companyId, Integer workspaceId, Integer projectId,
            String keyword, Integer assigneeId, 
            TaskPriority priority, TaskType taskType, // *** THAM SỐ MỚI ***
            int page, int size, String sortBy, String sortDir) {
        
        // 1. Validate Project (Giữ nguyên)
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án"));
        if (!project.getWorkspace().getId().equals(workspaceId) || 
            !project.getWorkspace().getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Dự án không thuộc về không gian hoặc công ty này");
        }

        // 2. PHẦN A: ACTIVE SPRINTS
        List<Sprint> activeSprints = sprintRepository.findActiveSprintsByProjectId(
            projectId, Arrays.asList(SprintStatus.NOT_STARTED, SprintStatus.IN_PROGRESS)
        );

        List<SprintDetailsResponse> sprintDtos = activeSprints.stream().map(sprint -> {
            // *** CẬP NHẬT: Truyền priority và taskType vào bộ lọc Sprint ***
            Specification<Task> sprintTaskSpec = TaskSpecification.filterBacklog(
                projectId, sprint.getId(), false, keyword, assigneeId, priority, taskType
            );
            
            List<Task> tasks = taskRepository.findAll(sprintTaskSpec, Sort.by("sortOrder").ascending());
            
            return SprintDetailsResponse.builder()
                    .id(sprint.getId())
                    .name(sprint.getName())
                    .goal(sprint.getGoal())
                    .status(sprint.getStatus())
                    .startDate(sprint.getStartDate())
                    .endDate(sprint.getEndDate())
                    .projectId(projectId)
                    .tasks(tasks.stream().map(this::mapToTaskSummaryResponse).collect(Collectors.toList()))
                    .build();
        }).collect(Collectors.toList());


        // 3. PHẦN B: PRODUCT BACKLOG
        // *** CẬP NHẬT: Truyền priority và taskType vào bộ lọc Backlog ***
        Specification<Task> backlogSpec = TaskSpecification.filterBacklog(
            projectId, null, true, keyword, assigneeId, priority, taskType
        );
        
        // Map sắp xếp
        Map<String, String> sortMapping = Map.of(
            "sortOrder", "sortOrder",
            "title", "title",
            "priority", "priority",
            "storyPoints", "storyPoints",
            "dueDate", "dueDate"
        );
        Sort sort = SortUtils.createSort(sortBy, sortDir, "sortOrder", sortMapping);
        
        // Logic giữ nguyên thứ tự ưu tiên nếu sort mặc định
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
     * Hàm helper để map Task (Entity) sang TaskSummaryResponse (DTO)
     */
    private TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        User assignee = task.getAssignee();
        Epic epic = task.getEpic();
        
        // *** SỬA LỖI: Lấy đối tượng ProjectStatus ***
        com.quanlyduan.project_manager_api.model.ProjectStatus status = task.getStatus(); 

        return TaskSummaryResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .taskType(task.getTaskType())
                
                // *** SỬA LỖI: Đọc từ đối tượng status ***
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


  // ========================================================================
    // NHÓM CHỨC NĂNG QUẢN LÝ DỰ ÁN (PROJECTS)
    // ========================================================================

    // 1. LẤY DANH SÁCH DỰ ÁN (Cơ bản)
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectResponse> listProjectsByWorkspace(
            Integer companyId, Integer workspaceId, ProjectStatus status, 
            int page, int size, String sortBy, String sortDir) {
        
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc"));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Không gian làm việc không thuộc về công ty được chỉ định");
        }

        // Định nghĩa Map sắp xếp cho PROJECT
        Map<String, String> sortMapping = Map.of(
            "createdAt", "createdAt",
            "name", "name",
            "code", "projectCode",
            "status", "status",
            "dueDate", "dueDate",
            "manager", "manager.fullName"
        );

        // Gọi Helper chung
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "createdAt", sortMapping);

        Page<Project> projectPage;
        if (status != null) {
            projectPage = projectRepository.findByWorkspace_IdAndStatus(workspaceId, status, pageable);
        } else {
            projectPage = projectRepository.findByWorkspace_Id(workspaceId, pageable);
        }

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
        
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc"));
        if (workspace.getCompany() == null || !workspace.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Không gian làm việc không thuộc về công ty được chỉ định");
        }

        // Định nghĩa Map sắp xếp cho PROJECT
        Map<String, String> sortMapping = Map.of(
            "createdAt", "createdAt",
            "name", "name",
            "code", "projectCode",
            "status", "status",
            "dueDate", "dueDate",
            "manager", "manager.fullName"
        );

        // Gọi Helper chung
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "createdAt", sortMapping);

        Specification<Project> spec = ProjectSpecification.filterProjects(
            workspaceId, searchName, searchCode, searchManager, searchStatus
        );

        Page<Project> projectPage = projectRepository.findAll(spec, pageable);
        Page<ProjectResponse> dtoPage = projectPage.map(this::toResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // ========================================================================
    // NHÓM CHỨC NĂNG QUẢN LÝ THÀNH VIÊN DỰ ÁN (MEMBERS)
    // ========================================================================

    // 1. LẤY DANH SÁCH THÀNH VIEN (Cơ bản)
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectMemberResponse> getProjectMembers(
            Integer projectId, int page, int size, String sortBy, String sortDir) {
        
        // Định nghĩa Map sắp xếp cho PROJECT MEMBER
        Map<String, String> sortMapping = Map.of(
            "joinedAt", "joinedAt",
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        // Gọi Helper chung
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", sortMapping);

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
        
        // Định nghĩa Map sắp xếp cho PROJECT MEMBER
        Map<String, String> sortMapping = Map.of(
            "joinedAt", "joinedAt",
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "phone", "user.phoneNumber"
        );

        // Gọi Helper chung
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", sortMapping);

        Specification<ProjectMember> spec = ProjectMemberSpecification.filterMembers(
            projectId, searchName, searchEmail, searchRoleName, searchPhone
        );

        Page<ProjectMember> membersPage = projectMemberRepository.findAll(spec, pageable);
        Page<ProjectMemberResponse> dtoPage = membersPage.map(this::mapToProjectMemberResponse);
        return new PageResponseDTO<>(dtoPage);
    }

    // ========================================================================
    // PRIVATE HELPER METHODS 
    // ========================================================================

    /**
     * Helper tạo Pageable chung cho cả Project và ProjectMember.
     * @param defaultSortField Trường sort mặc định (ví dụ: "createdAt" cho Project, "joinedAt" cho Member)
     * @param sortMapping Map ánh xạ tên trường
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDir, 
                                    String defaultSortField, Map<String, String> sortMapping) {
        
        Sort sort = SortUtils.createSort(sortBy, sortDir, defaultSortField, sortMapping);
        return PageRequest.of(page, size, sort);
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

    @Override
    @Transactional
    public ProjectMemberResponse updateProjectMemberRole(Integer projectId, Integer memberId, String newRoleCode) {
        // 1. Lấy thông tin thành viên
        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên dự án với ID: " + memberId));

        // 2. Kiểm tra bảo mật (IDOR): Đảm bảo thành viên này thuộc đúng dự án
        if (!member.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Không tìm thấy thành viên này trong dự án");
        }

        // 3. Kiểm tra nghiệp vụ: Không cho phép đổi vai trò của chính mình
        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            throw new BadRequestException("Bạn không thể thay đổi vai trò của chính mình.");
        }

        // 4. Kiểm tra nếu vai trò mới trùng với vai trò hiện tại
        if (member.getRole().getRoleCode().equals(newRoleCode)) {
            throw new BadRequestException("Vai trò mới trùng với vai trò hiện tại, không cần cập nhật.");
        }

        // 5. Tìm vai trò (Role) mới
        Role newRole = roleRepository.findFirstByRoleCode(newRoleCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với mã: " + newRoleCode));

        // 6. Kiểm tra nghiệp vụ: Đảm bảo vai trò mới là CẤP DỰ ÁN
        if (newRole.getLevel() != RoleLevel.PROJECT) {
            throw new BadRequestException("Vai trò không hợp lệ (Không phải vai trò cấp DỰ ÁN)");
        }

        // 7. Cập nhật vai trò
        member.setRole(newRole);
        ProjectMember updatedMember = projectMemberRepository.save(member);

        // 8. Trả về DTO đã cập nhật
        return mapToProjectMemberResponse(updatedMember);
    }
      // --- US-S4-2 (Xem Board) & US-S4-4 (Lọc Board) ---
    @Override
    @Transactional(readOnly = true)
    public List<BoardColumnResponse> getProjectBoard(Integer projectId, Integer sprintId, String search, Integer assigneeId, String priority) {
        
        // 1. Lấy danh sách Cột (Status) của dự án
        List<com.quanlyduan.project_manager_api.model.ProjectStatus> statuses = projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId);

        // 2. Tạo Specification để lọc Task (US 4)
        // Lưu ý: Board thường hiện tất cả Status nên statusNames = null
        Specification<Task> spec = TaskSpecification.filterTasks(projectId, sprintId, search, assigneeId, priority, null);
        
        // 3. Lấy danh sách Task đã lọc
        List<Task> tasks = taskRepository.findAll(spec);

        // 4. Nhóm Task theo Status ID (Grouping in Memory)
        // Map<StatusID, List<Task>>
        Map<Integer, List<Task>> tasksByStatus = tasks.stream()
                .filter(t -> t.getStatus() != null) // Bỏ qua task lỗi không có status
                .collect(Collectors.groupingBy(t -> t.getStatus().getId()));

        // 5. Build Response (Ghép Cột + Task)
        List<BoardColumnResponse> board = new ArrayList<>();
        
        for (com.quanlyduan.project_manager_api.model.ProjectStatus status : statuses) {
            // Lấy task thuộc cột này (hoặc List rỗng nếu không có task nào)
            List<Task> tasksInColumn = tasksByStatus.getOrDefault(status.getId(), Collections.emptyList());
            
            // Convert sang DTO
            List<TaskResponse> taskResponses = tasksInColumn.stream()
                    .map(taskService::mapToTaskResponse)
                    .collect(Collectors.toList());

            board.add(BoardColumnResponse.builder()
                    .statusId(status.getId())
                    .statusName(status.getName())
                    .color(status.getColor())
                    .order(status.getSortOrder())
                    .tasks(taskResponses)
                    .build());
        }
        
        return board;
    }

}