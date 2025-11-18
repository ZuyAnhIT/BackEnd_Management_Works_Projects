// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/ProjectStatusServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.ReorderStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectStatusResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.ProjectStatus;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.ProjectStatusRepository;
import com.quanlyduan.project_manager_api.service.ProjectStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map; 
import java.util.function.Function; 

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

    // LOGIC TAO TRANG THAI MOI
    @Override
    @Transactional
    public ProjectStatusResponse createStatus(Integer projectId, CreateProjectStatusRequest request) {
        // 1. Tìm dự án
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId)); // Đã dịch

        // 2. Kiểm tra trùng tên (Trong cùng 1 dự án không được có 2 cột cùng tên)
        if (projectStatusRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
            throw new BadRequestException("Tên trạng thái này đã tồn tại trong dự án.");
        }

        // 3. Tính toán vị trí (sortOrder)
        // Lấy max hiện tại, cột mới sẽ là max + 1
        Integer maxSortOrder = projectStatusRepository.findMaxSortOrderByProjectId(projectId);
        int newSortOrder = maxSortOrder + 1;

        // 4. Tạo Entity
        ProjectStatus status = ProjectStatus.builder()
                .project(project)
                .name(request.getName())
                .color(request.getColor() != null ? request.getColor() : "#CCCCCC") // Mặc định màu xám nếu null
                .sortOrder(newSortOrder)
                .isCompletedStatus(request.getIsCompletedStatus() != null ? request.getIsCompletedStatus() : false)
                .build();

        ProjectStatus saved = projectStatusRepository.save(status);
        
        // 5. Map và trả về
        return mapToResponse(saved);
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

    // LOGIC SAP XEP LAI VI TRI COT
    @Override
    @Transactional
    public void reorderStatuses(Integer projectId, ReorderStatusRequest request) {
        // 1. Kiểm tra dự án tồn tại
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId); // Đã dịch
        }

        List<Integer> orderedIds = request.getOrderedStatusIds();
        
        // 2. Lấy tất cả status hiện tại của dự án
        List<ProjectStatus> currentStatuses = projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId);

        // 3. Validate: Số lượng ID gửi lên phải khớp với số lượng hiện có
        if (orderedIds.size() != currentStatuses.size()) {
            throw new BadRequestException("Danh sách ID sắp xếp không khớp với số lượng trạng thái hiện có của dự án."); // Đã dịch
        }

        // Tạo Map để tìm kiếm nhanh
        Map<Integer, ProjectStatus> statusMap = currentStatuses.stream()
                .collect(Collectors.toMap(ProjectStatus::getId, Function.identity()));

        // 4. Duyệt qua danh sách ID mới và cập nhật sortOrder
        for (int i = 0; i < orderedIds.size(); i++) {
            Integer statusId = orderedIds.get(i);
            ProjectStatus status = statusMap.get(statusId);

            if (status == null) {
                throw new BadRequestException("ID trạng thái không hợp lệ hoặc không thuộc dự án này: " + statusId); // Đã dịch
            }

            // Cập nhật vị trí mới (0, 1, 2...)
            status.setSortOrder(i);
            
            // (Lưu trong vòng lặp, hoặc dùng saveAll cuối cùng)
            projectStatusRepository.save(status);
        }
    }
}