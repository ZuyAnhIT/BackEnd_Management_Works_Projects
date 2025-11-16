package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.DashboardService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SecurityService securityService;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public DashboardServiceImpl(SecurityService securityService,
                                WorkspaceMemberRepository workspaceMemberRepository,
                                CompanyMemberRepository companyMemberRepository,
                                ProjectMemberRepository projectMemberRepository) {

        this.securityService = securityService;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
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

        return companyMemberRepository.findByUserIdAndCompanyStatuses(userId, allowedStatuses)
                .stream()
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

        if (currentUserId == null)
