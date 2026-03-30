package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class ProjectStatusServiceImpl implements ProjectStatusService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    public static final String ENTITY_PROJECT = "PROJECT";

    public static final String DESC_CREATE_STATUS = "Create new Project Column Status";
    public static final String DESC_UPDATE_STATUS = "Update Project Column Status";
    public static final String DESC_REORDER_STATUS = "Rearrange Project Column Position";
    public static final String DESC_DELETE_STATUS = "Delete Project Column Status";

    public static final String ERROR_PROJECT_NOT_FOUND = "Project not found with ID: ";
    public static final String ERROR_STATUS_NAME_EXISTS = "Status name already exists in this project.";
    public static final String ERROR_STATUS_NOT_FOUND = "Status not found with ID: ";
    public static final String ERROR_WRONG_PROJECT = "This status does not belong to the specified project.";
    public static final String ERROR_REORDER_MISMATCH = "The list of ordered IDs does not match the current number of statuses in the project.";
    public static final String ERROR_INVALID_STATUS_ID = "Invalid status ID or does not belong to this project: ";
    public static final String ERROR_DELETE_CONTAINS_TASKS = "Cannot delete this status because it contains tasks. Please move all tasks to another column first.";

    public static final String DEFAULT_COLOR = "#CCCCCC";

    // Khai bao cac bien phu thuoc
    private final ProjectStatusRepository projectStatusRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    // Constructor khoi tao thu cong
    public ProjectStatusServiceImpl(ProjectStatusRepository projectStatusRepository,
                                    ProjectRepository projectRepository,
                                    TaskRepository taskRepository) {
        this.projectStatusRepository = projectStatusRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional(readOnly = true)
    public List<ProjectStatusResponse> getProjectStatuses(Integer projectId) {
        // Kiem tra du an ton tai
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND + projectId);
        }

        // Lay danh sach trang thai da duoc sap xep tu database
        List<ProjectStatus> statuses = projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId);

        // Chuyen doi sang DTO
        return statuses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_CREATE, entityType = ENTITY_PROJECT, description = DESC_CREATE_STATUS)
    public ProjectStatusResponse createStatus(Integer projectId, CreateProjectStatusRequest request) {
        // Tim kiem du an
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND + projectId));

        // Kiem tra trung ten trang thai trong cung mot du an
        if (projectStatusRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
            throw new BadRequestException(ERROR_STATUS_NAME_EXISTS);
        }

        // Tinh toan vi tri sap xep moi nhat cho cot
        Integer maxSortOrder = projectStatusRepository.findMaxSortOrderByProjectId(projectId);
        int newSortOrder = (maxSortOrder == null ? 0 : maxSortOrder + 1);

        // Khoi tao Entity trang thai
        ProjectStatus status = ProjectStatus.builder()
                .project(project)
                .name(request.getName())
                .color(request.getColor() != null ? request.getColor() : DEFAULT_COLOR)
                .sortOrder(newSortOrder)
                .isCompletedStatus(request.getIsCompletedStatus() != null ? request.getIsCompletedStatus() : false)
                .build();

        ProjectStatus saved = projectStatusRepository.save(status);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_PROJECT, description = DESC_UPDATE_STATUS)
    public ProjectStatusResponse updateStatus(Integer projectId, Integer statusId, UpdateStatusRequest request) {
        // Tim kiem trang thai
        ProjectStatus status = projectStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_STATUS_NOT_FOUND + statusId));

        // Xac thuc quyen so huu cua du an
        if (!status.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_WRONG_PROJECT);
        }

        // Cap nhat ten neu co su thay doi va khong bi trung lap
        if (request.getName() != null && !request.getName().trim().isEmpty()
                && !request.getName().equalsIgnoreCase(status.getName())) {

            if (projectStatusRepository.existsByProject_IdAndNameIgnoreCase(projectId, request.getName())) {
                throw new BadRequestException(ERROR_STATUS_NAME_EXISTS);
            }
            status.setName(request.getName());
        }

        // Cap nhat mau sac
        if (request.getColor() != null) {
            status.setColor(request.getColor());
        }

        // Cap nhat co hoan thanh
        if (request.getIsCompletedStatus() != null) {
            status.setIsCompletedStatus(request.getIsCompletedStatus());
        }

        ProjectStatus updatedStatus = projectStatusRepository.save(status);
        
        return mapToResponse(updatedStatus);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_PROJECT, description = DESC_REORDER_STATUS)
    public void reorderStatuses(Integer projectId, ReorderStatusRequest request) {
        // Kiem tra du an ton tai
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND + projectId);
        }

        List<Integer> orderedIds = request.getOrderedStatusIds();

        // Lay tat ca trang thai hien tai cua du an
        List<ProjectStatus> currentStatuses = projectStatusRepository.findByProject_IdOrderBySortOrderAsc(projectId);

        // Xac thuc so luong ID gui len phai khop voi thuc te
        if (orderedIds.size() != currentStatuses.size()) {
            throw new BadRequestException(ERROR_REORDER_MISMATCH);
        }

        // Tao danh ba de truy xuat nhanh
        Map<Integer, ProjectStatus> statusMap = currentStatuses.stream()
                .collect(Collectors.toMap(ProjectStatus::getId, Function.identity()));

        // Duyet qua tung ID de cap nhat vi tri
        for (int i = 0; i < orderedIds.size(); i++) {
            Integer statusId = orderedIds.get(i);
            ProjectStatus status = statusMap.get(statusId);

            if (status == null) {
                throw new BadRequestException(ERROR_INVALID_STATUS_ID + statusId);
            }

            status.setSortOrder(i);
            projectStatusRepository.save(status);
        }
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_DELETE, entityType = ENTITY_PROJECT, description = DESC_DELETE_STATUS)
    public void deleteStatus(Integer projectId, Integer statusId) {
        // Tim kiem trang thai
        ProjectStatus status = projectStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_STATUS_NOT_FOUND + statusId));

        // Xac thuc quyen so huu cua du an
        if (!status.getProject().getId().equals(projectId)) {
            throw new BadRequestException(ERROR_WRONG_PROJECT);
        }

        // Kiem tra rang buoc neu con cong viec nam trong cot
        if (taskRepository.existsByStatus_Id(statusId)) {
            throw new BadRequestException(ERROR_DELETE_CONTAINS_TASKS);
        }

        projectStatusRepository.delete(status);
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private ProjectStatusResponse mapToResponse(ProjectStatus s) {
        return ProjectStatusResponse.builder()
                .id(s.getId())
                .projectId(s.getProject().getId())
                .workspaceId(s.getProject().getWorkspace().getId())
                .companyId(s.getProject().getWorkspace().getCompany().getId())
                .name(s.getName())
                .color(s.getColor())
                .sortOrder(s.getSortOrder())
                .isCompletedStatus(s.getIsCompletedStatus())
                .build();
    }
}