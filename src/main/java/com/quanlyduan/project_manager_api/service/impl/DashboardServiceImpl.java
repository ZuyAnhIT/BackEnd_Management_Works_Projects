package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SecurityService securityService;
    private final CompanyMemberRepository companyMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public DashboardServiceImpl(
            SecurityService securityService,
            CompanyMemberRepository companyMemberRepository,
            ProjectMemberRepository projectMemberRepository) {

        this.securityService = securityService;
        this.companyMemberRepository = companyMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    // ==========================
    // 1. Lấy danh sách công ty của tôi
    // ==========================
    @Override
    public List<MyCompanyResponse> getMyCompanies() {
        Integer userId = securityService.getCurrentUserId();

        List<CompanyStatus> allowedStatuses = List.of(
                CompanyStatus.ACTIVE,
                CompanyStatus.SUSPENDED
        );

        return companyMemberRepository.findByUserIdAndCompanyStatuses(userId, allowedStatuses)
                .stream()
                .map(this::mapToMyCompanyResponse)
                .collect(Collectors.toList());
    }

    private MyCompanyResponse mapToMyCompanyResponse(CompanyMember companyMember) {
        Company company = companyMember.getCompany();

        return MyCompanyResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .companyCode(company.getCompanyCode())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .roleCode(companyMember.getRole().getRoleCode())
                .memberStatus(companyMember.getStatus().name())
                .jobTitle(companyMember.getJobTitle())
                .department(companyMember.getDepartment())
                .joinedAt(companyMember.getJoinedAt())
                .build();
    }

    // ==========================
    // 2. Lấy danh sách project tôi tham gia
    // ==========================
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
}
