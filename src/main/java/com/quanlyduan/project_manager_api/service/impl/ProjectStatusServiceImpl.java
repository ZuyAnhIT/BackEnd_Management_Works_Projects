// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/ProjectStatusServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.ProjectStatusResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.ProjectStatus;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.ProjectStatusRepository;
import com.quanlyduan.project_manager_api.service.ProjectStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectStatusServiceImpl implements ProjectStatusService {

    private final ProjectStatusRepository projectStatusRepository;
    private final ProjectRepository projectRepository;

    // *** CONSTRUCTOR THỦ CÔNG ***
    public ProjectStatusServiceImpl(ProjectStatusRepository projectStatusRepository,
                                    ProjectRepository projectRepository) {
        this.projectStatusRepository = projectStatusRepository;
        this.projectRepository = projectRepository;
    }

    // LOGIC LẤY DANH SÁCH TRẠNG THÁI
    @Override
    @Transactional(readOnly = true)
    public List<ProjectStatusResponse> getProjectStatuses(Integer projectId) {
        // 1. Kiểm tra dự án tồn tại
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId);
        }

        // 2. Lấy danh sách từ DB (đã sắp xếp)
        List<ProjectStatus> statuses = projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId);

        // 3. Map sang DTO
        return statuses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper mapping
    private ProjectStatusResponse mapToResponse(ProjectStatus s) {
        return ProjectStatusResponse.builder()
                .id(s.getId())
                .projectId(s.getProject().getId())
                .name(s.getName())
                .color(s.getColor())
                .sortOrder(s.getSortOrder())
                .isCompletedStatus(s.getIsCompletedStatus())
                .build();
    }
}