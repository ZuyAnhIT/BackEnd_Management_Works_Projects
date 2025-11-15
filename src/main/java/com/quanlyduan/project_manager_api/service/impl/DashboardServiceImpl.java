package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.quanlyduan.project_manager_api.dto.response.MyTaskResponse;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SecurityService securityService;
    private final CompanyMemberRepository companyMemberRepository;
    private final TaskRepository taskRepository;

    public DashboardServiceImpl(SecurityService securityService,
                                CompanyMemberRepository companyMemberRepository,
                                TaskRepository taskRepository) {
        this.securityService = securityService;
        this.companyMemberRepository = companyMemberRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public List<MyCompanyResponse> getMyCompanies() {
        Integer userId = securityService.getCurrentUserId();

        List<CompanyStatus> allowedStatuses = List.of(CompanyStatus.ACTIVE, CompanyStatus.SUSPENDED);

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

    /**
     * US4-sprint3: Logic lấy tất cả Task được giao cho tôi
     */
    @Override
    @Transactional(readOnly = true)
    public List<MyTaskResponse> getMyTasks() {
        // 1. Lấy user ID từ SecurityService (mà bạn đã cung cấp)
        Integer currentUserId = securityService.getCurrentUserId();

        // 2. Định nghĩa các status đã hoàn thành (bạn có thể thay đổi)
        Set<String> completedStatuses = Set.of("DONE", "COMPLETED", "CANCELLED");

        // 3. Gọi repository (hàm mới bạn tạo ở trên)
        List<Task> tasks = taskRepository.findByAssignee_IdAndStatusNotInWithDetails(
                currentUserId,
                completedStatuses
        );

        // 4. Map sang DTO
        return tasks.stream()
                .map(this::mapToMyTaskResponse)
                .collect(Collectors.toList());
    }

    private MyTaskResponse mapToMyTaskResponse(Task task) {
        return MyTaskResponse.builder()
                .taskId(task.getId())
                .taskCode(task.getTaskCode())
                .taskTitle(task.getTitle())
                .taskStatus(task.getStatus())
                .taskPriority(task.getPriority())
                .taskDueDate(task.getDueDate() != null ? task.getDueDate().toLocalDate() : null)
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .workspaceId(task.getProject().getWorkspace().getId())
                .workspaceName(task.getProject().getWorkspace().getName())
                .build();
    }

}
