package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.model.Workspace;
// import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
// SỬA: Import lớp triển khai (implementation)
import com.quanlyduan.project_manager_api.security.SecurityServiceImpl;
import com.quanlyduan.project_manager_api.service.DashboardService;
// import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired; // Có thể cần
import org.springframework.beans.factory.annotation.Qualifier; // Có thể cần
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ProjectMemberRepository projectMemberRepository;

    // SỬA: Đổi tên biến thành "securityService"
    private final SecurityServiceImpl securityService;

    /**
     * SỬA: Constructor tiêm SecurityServiceImpl (lớp)
     * * @param projectMemberRepository
     * @param securityService - Spring sẽ tìm bean tên "securityService"
     */
    @Autowired // Thêm @Autowired để rõ ràng
    public DashboardServiceImpl(ProjectMemberRepository projectMemberRepository,
                                @Qualifier("securityService") SecurityServiceImpl securityService) {
        this.projectMemberRepository = projectMemberRepository;
        this.securityService = securityService; // Sửa tên biến
    }

    /**
     * US-S3-3: Lấy danh sách các project mà người dùng hiện tại đang tham gia.
     */
    @Override
    public List<MyProjectResponse> getMyProjects() {
        // 1. Lấy ID người dùng hiện tại
        // SỬA: Gọi từ biến "securityService"
        Integer currentUserId = securityService.getCurrentUserId();

        if (currentUserId == null) {
            return List.of();
        }

        // 2. Lấy danh sách "tư cách thành viên" (memberships)
        List<ProjectMember> memberships = projectMemberRepository.findByUser_Id(currentUserId);

        // 3. Map danh sách ProjectMember sang MyProjectResponse DTO
        return memberships.stream()
                .map(this::mapToMyProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Hàm helper để chuyển đổi (map) ProjectMember sang MyProjectResponse.
     */
    private MyProjectResponse mapToMyProjectResponse(ProjectMember membership) {
        Project project = membership.getProject();
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
                .myRoleName(membership.getRole().getRoleName())
                .build();
    }
}