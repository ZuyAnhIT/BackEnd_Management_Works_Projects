package com.quanlyduan.project_manager_api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.request.CreateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.ReorderStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectStatusResponse;
import com.quanlyduan.project_manager_api.service.ProjectStatusService;

/**
 * Controller xu ly cac nghiep vu lien quan den cau hinh trang thai (cac cot tren Board) cua Du an.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/statuses")
@CrossOrigin("*")
public class ProjectStatusController {

    // Khai bao cac thong bao tra ve (Response Messages)
    private static final String MSG_FETCH_LIST_SUCCESS = "Status list retrieved successfully.";
    private static final String MSG_CREATE_SUCCESS = "New status created successfully.";
    private static final String MSG_UPDATE_SUCCESS = "Status updated successfully.";
    private static final String MSG_REORDER_SUCCESS = "Status order updated successfully.";
    private static final String MSG_DELETE_SUCCESS = "Status deleted successfully.";

    private final ProjectStatusService projectStatusService;

    // Khoi tao thu cong de tiem phu thuoc (Dependency Injection)
    public ProjectStatusController(ProjectStatusService projectStatusService) {
        this.projectStatusService = projectStatusService;
    }

    /**
     * Lay danh sach tat ca cac trang thai (cot) trong mot du an cu the.
     * Yeu cau quyen xem du an.
     */
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<ProjectStatusResponse>>> getStatuses(
            @PathVariable Integer projectId) {

        List<ProjectStatusResponse> response = projectStatusService.getProjectStatuses(projectId);
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_LIST_SUCCESS, response));
    }

    /**
     * Tao moi mot trang thai cho du an.
     * Chi nhung nguoi co quyen chinh sua du an moi duoc thuc hien.
     */
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<ProjectStatusResponse>> createStatus(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateProjectStatusRequest request) {

        ProjectStatusResponse response = projectStatusService.createStatus(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MSG_CREATE_SUCCESS, response));
    }

    /**
     * Cap nhat ten hoac thong tin mo ta cua mot trang thai hien co.
     */
    @PutMapping("/{statusId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<ProjectStatusResponse>> updateStatus(
            @PathVariable Integer projectId,
            @PathVariable Integer statusId,
            @Valid @RequestBody UpdateStatusRequest request) {

        ProjectStatusResponse response = projectStatusService.updateStatus(projectId, statusId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_SUCCESS, response));
    }

    /**
     * Sap xep lai thu tu hien thi cua cac cot trang thai tren bang cong viec.
     */
    @PutMapping("/reorder")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<Object>> reorderStatuses(
            @PathVariable Integer projectId,
            @Valid @RequestBody ReorderStatusRequest request) {

        projectStatusService.reorderStatuses(projectId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_REORDER_SUCCESS, null));
    }

    /**
     * Xoa mot trang thai khoi du an. 
     * He thong se tu dong kiem tra cac rang buoc ve cong viec (Task) truoc khi cho phep xoa.
     */
    @DeleteMapping("/{statusId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<Object>> deleteStatus(
            @PathVariable Integer projectId,
            @PathVariable Integer statusId) {

        projectStatusService.deleteStatus(projectId, statusId);
        return ResponseEntity.ok(ApiResponse.success(MSG_DELETE_SUCCESS, null));
    }
}