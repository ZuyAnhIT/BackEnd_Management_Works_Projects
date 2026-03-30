package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.MyTaskResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;
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
    public List<MyWorkspaceResponse> getMyWorkspaces() {
        // Lay thong tin nguoi dung dang dang nhap
        User currentUser = securityService.getCurrentAuthenticatedUser();

        // Tim tat ca cac khong gian lam viec ma nguoi dung la thanh vien
        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUser_Id(currentUser.getId());

        // Loc cac khong gian lam viec ton tai va khong bi xoa, sau do map sang DTO
        return memberships.stream()
                .filter(member -> member.getWorkspace() != null
                        && member.getWorkspace().getStatus() != WorkspaceStatus.DELETED)
                .map(this::mapToMyWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MyCompanyResponse> getMyCompanies() {
        Integer userId = securityService.getCurrentUserId();

        // Dinh nghia cac trang thai cong ty cho phep hien thi
        List<CompanyStatus> allowedStatuses = List.of(CompanyStatus.ACTIVE, CompanyStatus.SUSPENDED);

        // Tim tat ca thanh vien thuoc cac cong ty co trang thai hop le
        List<CompanyMember> members = companyMemberRepository.findByUser_IdAndCompany_StatusIn(userId, allowedStatuses);

        // Chuyen doi danh sach Entity sang DTO
        return members.stream()
                .map(this::mapToMyCompanyResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MyProjectResponse> getMyProjects() {
        Integer currentUserId = securityService.getCurrentUserId();

        // Kiem tra bao mat, neu khong co user id thi tra ve danh sach rong
        if (currentUserId == null) {
            return List.of();
        }

        // Tim tat ca cac du an ma nguoi dung la thanh vien
        List<ProjectMember> memberships = projectMemberRepository.findByUser_Id(currentUserId);

        // Chuyen doi danh sach Entity sang DTO
        return memberships.stream()
                .map(this::mapToMyProjectResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MyTaskResponse> getMyTasks() {
        Integer currentUserId = securityService.getCurrentUserId();
        
        // Kiem tra bao mat
        if (currentUserId == null) {
            return List.of();
        }

        // Lay danh sach cong viec duoc giao kem theo chi tiet de toi uu truy van
        List<Task> tasks = taskRepository.findByAssignee_IdWithDetails(currentUserId);

        // Loc bo nhung cong viec da hoan thanh va map sang DTO
        return tasks.stream()
                .filter(task -> task.getStatus() != null && !task.getStatus().getIsCompletedStatus())
                .map(this::mapToMyTaskResponse)
                .collect(Collectors.toList());
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

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

        // Kiem tra an toan khi truy xuat trang thai cua cong viec
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