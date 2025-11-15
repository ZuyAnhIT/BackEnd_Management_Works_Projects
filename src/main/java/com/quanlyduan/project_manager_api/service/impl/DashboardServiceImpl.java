package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;
import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.WorkspaceMember;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.service.SecurityService;
import com.quanlyduan.project_manager_api.service.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SecurityService securityService;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public DashboardServiceImpl(SecurityService securityService,
                                WorkspaceMemberRepository workspaceMemberRepository) {
        this.securityService = securityService;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @Override
    public List<MyWorkspaceResponse> getMyWorkspaces() {
        Integer userId = securityService.getCurrentUserId();

        return workspaceMemberRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapToMyWorkspaceResponse)
                .collect(Collectors.toList());
    }

    private MyWorkspaceResponse mapToMyWorkspaceResponse(WorkspaceMember member) {
        Workspace workspace = member.getWorkspace();

        return MyWorkspaceResponse.builder()
                .workspaceId(workspace.getId())
                .companyId(workspace.getCompany().getId())
                .companyName(workspace.getCompany().getName())
                .workspaceName(workspace.getName())
                .workspaceCode(workspace.getWorkspaceCode())
                .description(workspace.getDescription())
                .coverImageUrl(workspace.getCoverImageUrl())
                .color(workspace.getColor())
                .status(workspace.getStatus().name())
                .roleCode(member.getRole().getRoleCode())
                .memberStatus(member.getStatus().name())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
