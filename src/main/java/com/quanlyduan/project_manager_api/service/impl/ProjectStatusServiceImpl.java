// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/ProjectStatusServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.CreateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.ReorderStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectStatusResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.ProjectStatus;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.ProjectStatusRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
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
    private final TaskRepository taskRepository;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public ProjectStatusServiceImpl(ProjectStatusRepository projectStatusRepository,
                                     ProjectRepository projectRepository,
                                     TaskRepository taskRepository) {
        this.projectStatusRepository = projectStatusRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    // ======================================================
    // 1. LẤY DANH SÁCH TRẠNG THÁI (GET STATUSES)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<ProjectStatusResponse> getProjectStatuses(Integer projectId) {
        // 1. Kiểm tra dự án tồn tại
        if (!projectRepository.existsById(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }

        // 2. Lấy danh sách từ DB (đã sắp xếp theo SortOrder)
        List<ProjectStatus> statuses = projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId);

        // 3. Map sang DTO
        return statuses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ======================================================
    // 2. TẠO TRẠNG THÁI MỚI (CREATE STATUS / COLUMN)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "CREATE", entityType = "PROJECT", description = "Create new Project Column Status")
    public ProjectStatusResponse createStatus(Integer projectId, CreateProjectStatusRequest request) {
        // 1. Tìm dự án
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        // 2. Kiểm tra trùng tên (Trong cùng 1 dự án)
        if (projectStatusRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Status name already exists in this project.");
        }

        // 3. Tính toán vị trí (sortOrder): Cột mới sẽ ở vị trí cuối cùng
        Integer maxSortOrder = projectStatusRepository.findMaxSortOrderByProjectId(projectId);
        // Nếu không có cột nào, maxSortOrder sẽ là null, ta bắt đầu từ 0
        int newSortOrder = (maxSortOrder == null ? 0 : maxSortOrder + 1);

        // 4. Tạo Entity
        ProjectStatus status = ProjectStatus.builder()
                .project(project)
                .name(request.getName())
                // Mặc định màu xám nếu không có màu nào được cung cấp
                .color(request.getColor() != null ? request.getColor() : "#CCCCCC")
                .sortOrder(newSortOrder)
                // Mặc định cờ hoàn thành là false
                .isCompletedStatus(request.getIsCompletedStatus() != null ? request.getIsCompletedStatus() : false)
                .build();

        ProjectStatus saved = projectStatusRepository.save(status);

        // 5. Map và trả về
        return mapToResponse(saved);
    }

    // ======================================================
    // 3. CẬP NHẬT TRẠNG THÁI (UPDATE STATUS)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "PROJECT", description = "Update Project Column Status")
    public ProjectStatusResponse updateStatus(Integer projectId, Integer statusId, UpdateStatusRequest request) {
        // 1. Tìm Status
        ProjectStatus status = projectStatusRepository.findById(statusId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with ID: " + statusId));

        // 2. Validate: Status phải thuộc về Project này
        if (!status.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This status does not belong to the specified project.");
        }

        // 3. Cập nhật Tên (nếu có thay đổi)
        if (request.getName() != null && !request.getName().trim().isEmpty()
                && !request.getName().equalsIgnoreCase(status.getName())) {

            // Kiểm tra trùng tên trong project
            if (projectStatusRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Status name already exists in this project.");
            }
            status.setName(request.getName());
        }

        // 4. Cập nhật Màu (nếu có)
        if (request.getColor() != null) {
            status.setColor(request.getColor());
        }

        // 5. Cập nhật Cờ hoàn thành (nếu có)
        if (request.getIsCompletedStatus() != null) {
            status.setIsCompletedStatus(request.getIsCompletedStatus());
        }

        // 6. Lưu và trả về
        ProjectStatus updatedStatus = projectStatusRepository.save(status);
        return mapToResponse(updatedStatus);
    }


    // ======================================================
    // 4. SẮP XẾP LẠI VỊ TRÍ CỘT (REORDER STATUSES)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "PROJECT", description = "Rearrange Project Column Position")
    public void reorderStatuses(Integer projectId, ReorderStatusRequest request) {
        // 1. Kiểm tra dự án tồn tại
        if (!projectRepository.existsById(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }

        List<Integer> orderedIds = request.getOrderedStatusIds();

        // 2. Lấy tất cả status hiện tại của dự án
        List<ProjectStatus> currentStatuses = projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId);

        // 3. Validate: Số lượng ID gửi lên phải khớp với số lượng hiện có
        if (orderedIds.size() != currentStatuses.size()) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("The list of ordered IDs does not match the current number of statuses in the project.");
        }

        // Tạo Map để tìm kiếm nhanh
        Map<Integer, ProjectStatus> statusMap = currentStatuses.stream()
                .collect(Collectors.toMap(ProjectStatus::getId, Function.identity()));

        // 4. Duyệt qua danh sách ID mới và cập nhật sortOrder
        for (int i = 0; i < orderedIds.size(); i++) {
            Integer statusId = orderedIds.get(i);
            ProjectStatus status = statusMap.get(statusId);

            if (status == null) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Invalid status ID or does not belong to this project: " + statusId);
            }

            // Cập nhật vị trí mới (0, 1, 2...)
            status.setSortOrder(i);
            
            // Lưu đối tượng (có thể tối ưu bằng saveAll ở cuối, nhưng giữ nguyên logic save trong loop)
            projectStatusRepository.save(status);
        }
    }

    // ======================================================
    // 5. XÓA TRẠNG THÁI (DELETE STATUS / COLUMN)
    // ======================================================
    @Override
    @Transactional
    @LogActivity(action = "DELETE", entityType = "PROJECT", description = "Delte Project Column Status")
    public void deleteStatus(Integer projectId, Integer statusId) {
        // 1. Tìm Status
        ProjectStatus status = projectStatusRepository.findById(statusId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with ID: " + statusId));

        // 2. Validate: Status phải thuộc về Project này
        if (!status.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This status does not belong to the specified project.");
        }

        // 3. Validate: Không cho phép xóa nếu còn Task trong cột này
        if (taskRepository.existsByStatus_Id(statusId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Cannot delete this status because it contains tasks. Please move all tasks to another column first.");
        }

        // 4. Thực hiện xóa cứng
        projectStatusRepository.delete(status);
    }

    // ======================================================
    // ⚙️ PRIVATE HELPER: MAPPER
    // ======================================================

    /**
     * Helper: Map ProjectStatus Entity sang ProjectStatusResponse DTO.
     */
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