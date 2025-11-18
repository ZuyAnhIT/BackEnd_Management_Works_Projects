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
    
    // *** THÊM CONSTRUCTOR THỦ CÔNG (Theo yêu cầu) ***
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
    // 1. Lấy danh sách WORKSPACE mà user đang tham gia
    // ======================================================
    @Override
    public List<MyWorkspaceResponse> getMyWorkspaces() {
        User currentUser = securityService.getCurrentAuthenticatedUser();

        List<WorkspaceMember> memberships =
                workspaceMemberRepository.findByUser_Id(currentUser.getId());

        return memberships.stream()
                .filter(member -> member.getWorkspace() != null
                        && member.getWorkspace().getStatus() != WorkspaceStatus.DELETED)
                .map(this::mapToMyWorkspaceResponse)
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

    // ======================================================
    // 2. Lấy danh sách COMPANY mà user là thành viên
    // ======================================================
    @Override
    public List<MyCompanyResponse> getMyCompanies() {
        Integer userId = securityService.getCurrentUserId();

        List<CompanyStatus> allowedStatuses =
                List.of(CompanyStatus.ACTIVE, CompanyStatus.SUSPENDED);
        
        // (Giả sử bạn đã thêm hàm findByUserIdAndCompanyStatuses vào Repository)
        List<CompanyMember> members = companyMemberRepository.findByUser_IdAndCompany_StatusIn(userId, allowedStatuses);

        return members.stream()
                .map(this::mapToMyCompanyResponse)
                .collect(Collectors.toList());
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

    // ======================================================
    // 3. Lấy danh sách PROJECT mà user đang tham gia
    // ======================================================
    @Override
    public List<MyProjectResponse> getMyProjects() {
        Integer currentUserId = securityService.getCurrentUserId();

        if (currentUserId == null) {
            return List.of();
        }

        List<ProjectMember> memberships =
                projectMemberRepository.findByUser_Id(currentUserId);

        return memberships.stream()
                .map(this::mapToMyProjectResponse)
                .collect(Collectors.toList());
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

    // ======================================================
    // 4. US4-sprint3: Logic lấy tất cả Task được giao cho tôi 
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<MyTaskResponse> getMyTasks() {
        // 1. Lấy user ID từ SecurityService (Bean "securityService")
        Integer currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            return List.of();
        }
        
        List<Task> tasks = taskRepository.findByAssignee_IdWithDetails(
                currentUserId
        );

        // 4. Map sang DTO VÀ LỌC
        return tasks.stream()
            // *** SỬA: Lọc các task chưa hoàn thành (isCompletedStatus = false) ***
            .filter(task -> task.getStatus() != null && !task.getStatus().getIsCompletedStatus())
            .map(this::mapToMyTaskResponse) // Sử dụng helper đã sửa
            .collect(Collectors.toList());
    }

    // Hàm helper để map từ Task Entity sang MyTaskResponse DTO (ĐÃ SỬA)
    private MyTaskResponse mapToMyTaskResponse(Task task) {
        // *** SỬA: Lấy đối tượng ProjectStatus ***
        ProjectStatus status = task.getStatus();
        
        return MyTaskResponse.builder()
                .taskId(task.getId())
                .taskCode(task.getTaskCode())
                .taskTitle(task.getTitle())
                
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