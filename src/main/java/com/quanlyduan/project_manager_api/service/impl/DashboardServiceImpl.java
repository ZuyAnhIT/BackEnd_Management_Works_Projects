package com.quanlyduan.project_manager_api.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.MyTaskResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;
import com.quanlyduan.project_manager_api.dto.response.PersonalDashboardResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.model.ProjectStatus;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.WorkspaceMember;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String DEFAULT_STATUS_NAME = "Unknown";
    public static final String DEFAULT_STATUS_COLOR = "#CCCCCC";
    public static final String ERROR_UNAUTHENTICATED = "Authenticated user information not found.";
    public static final int UPCOMING_DAYS_LIMIT = 7;

    // Khai bao cac bien phu thuoc
    private final SecurityService securityService;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    // Constructor thay the cho annotation autowired
    public DashboardServiceImpl(SecurityService securityService,
                                WorkspaceMemberRepository workspaceMemberRepository,
                                CompanyMemberRepository companyMemberRepository,
                                ProjectMemberRepository projectMemberRepository,
                                TaskRepository taskRepository) {
        this.securityService = securityService;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    public PersonalDashboardResponse getMyTaskDashboard() {
        Integer currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException(ERROR_UNAUTHENTICATED);
        }

        // Truy xuat toan bo cong viec dang duoc giao cho nguoi dung hien tai
        List<Task> allMyTasks = taskRepository.findByAssignee_IdAndIsArchivedFalseOrderByDueDateAsc(currentUserId);

        // Khoi tao cac moc thoi gian can thiet de phan loai
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime upcomingDeadline = todayEnd.plusDays(UPCOMING_DAYS_LIMIT);

        // Khoi tao cac danh sach tuong ung voi cac nhom cong viec
        List<Task> overdue = new ArrayList<>();
        List<Task> today = new ArrayList<>();
        List<Task> upcoming = new ArrayList<>();
        List<Task> noDueDate = new ArrayList<>();
        List<Task> other = new ArrayList<>();

        // Phan loai cong viec dua tren han chot (chi xet cong viec chua hoan thanh)
        for (Task task : allMyTasks) {
            if (task.getStatus() != null && Boolean.TRUE.equals(task.getStatus().getIsCompletedStatus())) {
                continue;
            }

            LocalDateTime dueDate = task.getDueDate();

            if (dueDate == null) {
                noDueDate.add(task);
            } else if (dueDate.isBefore(now)) {
                overdue.add(task);
            } else if (dueDate.isBefore(todayEnd) || dueDate.isEqual(todayEnd)) {
                today.add(task);
            } else if (dueDate.isBefore(upcomingDeadline)) {
                upcoming.add(task);
            } else {
                other.add(task);
            }
        }

        // Dong goi du lieu va tra ve thong qua DTO
        return PersonalDashboardResponse.builder()
                .overdue(mapToMyTaskResponseList(overdue))
                .today(mapToMyTaskResponseList(today))
                .upcoming(mapToMyTaskResponseList(upcoming))
                .noDueDate(mapToMyTaskResponseList(noDueDate))
                .other(mapToMyTaskResponseList(other))
                .build();
    }

    @Override
    public List<MyWorkspaceResponse> getMyWorkspaces() {
        User currentUser = securityService.getCurrentAuthenticatedUser();
        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUser_Id(currentUser.getId());

        return memberships.stream()
                .filter(member -> member.getWorkspace() != null
                        && member.getWorkspace().getStatus() != WorkspaceStatus.DELETED)
                .map(this::mapToMyWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MyCompanyResponse> getMyCompanies() {
        Integer userId = securityService.getCurrentUserId();
        List<CompanyStatus> allowedStatuses = List.of(CompanyStatus.ACTIVE, CompanyStatus.SUSPENDED);
        List<CompanyMember> members = companyMemberRepository.findByUser_IdAndCompany_StatusIn(userId, allowedStatuses);

        return members.stream()
                .map(this::mapToMyCompanyResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MyProjectResponse> getMyProjects() {
        Integer currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            return List.of();
        }

        List<ProjectMember> memberships = projectMemberRepository.findByUser_Id(currentUserId);

        return memberships.stream()
                .map(this::mapToMyProjectResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MyTaskResponse> getMyTasks() {
        Integer currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            return List.of();
        }

        List<Task> tasks = taskRepository.findByAssignee_IdWithDetails(currentUserId);

        return tasks.stream()
                .filter(task -> task.getStatus() != null && !task.getStatus().getIsCompletedStatus())
                .map(this::mapToMyTaskResponse)
                .collect(Collectors.toList());
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private List<MyTaskResponse> mapToMyTaskResponseList(List<Task> tasks) {
        return tasks.stream()
                .map(this::mapToMyTaskResponse)
                .collect(Collectors.toList());
    }

    private MyWorkspaceResponse mapToMyWorkspaceResponse(WorkspaceMember membership) {
        Workspace workspace = membership.getWorkspace();
        Company company = workspace.getCompany();
        Role role = membership.getRole();

        return MyWorkspaceResponse.builder()
                .workspaceId(workspace.getId())
                .workspaceName(workspace.getName())
                .workspaceCode(workspace.getWorkspaceCode())
                .workspaceDescription(workspace.getDescription())
                .workspaceCoverImage(workspace.getCoverImageUrl())
                .workspaceColor(workspace.getColor())
                .workspaceStatus(workspace.getStatus().name())
                .companyId(company.getId())
                .companyName(company.getName())
                .companyLogoUrl(company.getLogoUrl())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .membershipStatus(membership.getStatus())
                .joinedAt(membership.getJoinedAt())
                .build();
    }

    private MyCompanyResponse mapToMyCompanyResponse(CompanyMember member) {
        Company company = member.getCompany();

        return MyCompanyResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .companyCode(company.getCompanyCode())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .roleCode(member.getRole().getRoleCode())
                .memberStatus(member.getStatus().name())
                .jobTitle(member.getJobTitle())
                .department(member.getDepartment())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    private MyProjectResponse mapToMyProjectResponse(ProjectMember member) {
        Project project = member.getProject();
        Workspace workspace = project.getWorkspace();
        Company company = workspace.getCompany();

        return MyProjectResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .description(project.getDescription())
                .coverImage(project.getCoverImageUrl())
                .workspaceId(workspace.getId())
                .workspaceName(workspace.getName())
                .companyId(company.getId())
                .companyName(company.getName())
                .myRoleName(member.getRole().getRoleName())
                .build();
    }

    private MyTaskResponse mapToMyTaskResponse(Task task) {
        ProjectStatus status = task.getStatus();

        Integer statusId = status != null ? status.getId() : null;
        String statusName = status != null ? status.getName() : DEFAULT_STATUS_NAME;
        String statusColor = status != null ? status.getColor() : DEFAULT_STATUS_COLOR;

        return MyTaskResponse.builder()
                .taskId(task.getId())
                .taskCode(task.getTaskCode())
                .taskTitle(task.getTitle())
                .taskStatusId(statusId)
                .taskStatusName(statusName)
                .taskStatusColor(statusColor)
                .taskPriority(task.getPriority())
                .taskDueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .workspaceId(task.getProject().getWorkspace().getId())
                .workspaceName(task.getProject().getWorkspace().getName())
                .build();
    }
}