package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.WorkspaceMember;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.DashboardService;
import com.quanlyduan.project_manager_api.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final SecurityService securityService;

    public DashboardServiceImpl(WorkspaceMemberRepository workspaceMemberRepository,
                                SecurityService securityService) {
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.securityService = securityService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyWorkspaceResponse> getMyWorkspaces() {
        User currentUser = securityService.getCurrentAuthenticatedUser();

        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUser_Id(currentUser.getId());

        return memberships.stream()
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
}
