// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/DashboardServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.MyTaskResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.DashboardService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SecurityService securityService;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    // ======================================================
    // CONSTRUCTOR (DI)
    // ======================================================
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

    // ======================================================
    // 1. LẤY DANH SÁCH WORKSPACE THAM GIA (GET MY WORKSPACES)
    // ======================================================
    @Override
    public List<MyWorkspaceResponse> getMyWorkspaces() {
        // Lấy thông tin người dùng đang đăng nhập
        User currentUser = securityService.getCurrentAuthenticatedUser();

        // Tìm tất cả các Workspace mà người dùng là thành viên
        List<WorkspaceMember> memberships =
                workspaceMemberRepository.findByUser_Id(currentUser.getId());

        // Lọc các Workspace không bị xóa và Map sang DTO
        return memberships.stream()
                // Điều kiện lọc: Đảm bảo Workspace tồn tại và không bị trạng thái DELETED
                .filter(member -> member.getWorkspace() != null
                        && member.getWorkspace().getStatus() != WorkspaceStatus.DELETED)
                .map(this::mapToMyWorkspaceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper: Map WorkspaceMember Entity sang MyWorkspaceResponse DTO.
     */
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

    // ======================================================
    // 2. LẤY DANH SÁCH CÔNG TY THAM GIA (GET MY COMPANIES)
    // ======================================================
    @Override
    public List<MyCompanyResponse> getMyCompanies() {
        Integer userId = securityService.getCurrentUserId();

        // Định nghĩa các trạng thái Công ty cho phép hiển thị
        List<CompanyStatus> allowedStatuses =
                List.of(CompanyStatus.ACTIVE, CompanyStatus.SUSPENDED);

        // Tìm tất cả các CompanyMember có trạng thái công ty nằm trong danh sách cho phép
        List<CompanyMember> members = companyMemberRepository.findByUser_IdAndCompany_StatusIn(userId, allowedStatuses);

        // Map sang DTO và trả về
        return members.stream()
                .map(this::mapToMyCompanyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper: Map CompanyMember Entity sang MyCompanyResponse DTO.
     */
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

    // ======================================================
    // 3. LẤY DANH SÁCH PROJECT THAM GIA (GET MY PROJECTS)
    // ======================================================
    @Override
    public List<MyProjectResponse> getMyProjects() {
        Integer currentUserId = securityService.getCurrentUserId();

        // Kiểm tra bảo mật
        if (currentUserId == null) {
            return List.of();
        }

        // Tìm tất cả các Project mà người dùng là thành viên
        List<ProjectMember> memberships =
                projectMemberRepository.findByUser_Id(currentUserId);

        // Map sang DTO và trả về
        return memberships.stream()
                .map(this::mapToMyProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper: Map ProjectMember Entity sang MyProjectResponse DTO.
     */
    private MyProjectResponse mapToMyProjectResponse(ProjectMember member) {
        Project project = member.getProject();
        Workspace workspace = project.getWorkspace();
        Company company = workspace.getCompany(); // Lấy thông tin công ty qua Workspace

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

    // ======================================================
    // 4. LẤY DANH SÁCH TASK ĐƯỢC GIAO CHO TÔI (GET MY TASKS)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<MyTaskResponse> getMyTasks() {
        // 1. Lấy user ID
        Integer currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            return List.of();
        }

        // 2. Lấy danh sách Task được giao (Fetch Join để tối ưu)
        List<Task> tasks = taskRepository.findByAssignee_IdWithDetails(
                currentUserId
        );

        // 3. Lọc và Map sang DTO
        return tasks.stream()
            // Lọc: Chỉ giữ lại các task **chưa hoàn thành** (isCompletedStatus = false)
            .filter(task -> task.getStatus() != null && !task.getStatus().getIsCompletedStatus())
            .map(this::mapToMyTaskResponse)
            .collect(Collectors.toList());
    }

    /**
     * Helper: Map Task Entity sang MyTaskResponse DTO.
     */
    private MyTaskResponse mapToMyTaskResponse(Task task) {
        // Lấy đối tượng ProjectStatus
        ProjectStatus status = task.getStatus();

        return MyTaskResponse.builder()
                .taskId(task.getId())
                .taskCode(task.getTaskCode())
                .taskTitle(task.getTitle())

                // Xử lý an toàn (Null check) cho trạng thái Task
                .taskStatusId(status != null ? status.getId() : null)
                .taskStatusName(status != null ? status.getName() : "Không xác định")
                .taskStatusColor(status != null ? status.getColor() : "#CCCCCC")

                .taskPriority(task.getPriority())
                .taskDueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .workspaceId(task.getProject().getWorkspace().getId())
                .workspaceName(task.getProject().getWorkspace().getName())
                .build();
    }
}