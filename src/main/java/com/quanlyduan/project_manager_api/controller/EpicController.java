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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.request.CreateEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateEpicRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import com.quanlyduan.project_manager_api.service.EpicService;

/**
 * Controller xử lý các nghiệp vụ liên quan đến Epic (nhóm công việc lớn) trong Dự án.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/epics")
@CrossOrigin("*")
public class EpicController {

    // Khai báo các câu thông báo trả về
    private static final String MSG_GET_EPICS_SUCCESS = "Epic list retrieved successfully.";
    private static final String MSG_GET_EPIC_DETAILS_SUCCESS = "Epic details retrieved successfully.";
    private static final String MSG_CREATE_EPIC_SUCCESS = "Epic created successfully.";
    private static final String MSG_UPDATE_EPIC_SUCCESS = "Epic updated successfully.";
    private static final String MSG_DELETE_EPIC_SUCCESS = "Epic deleted successfully.";

    private final EpicService epicService;

    // Khởi tạo thủ công để tiêm (inject) phụ thuộc
    public EpicController(EpicService epicService) {
        this.epicService = epicService;
    }

    /**
     * Lấy danh sách các Epic trong dự án (hỗ trợ tìm kiếm theo từ khóa).
     */
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<EpicResponse>>> getEpics(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String keyword) {

        List<EpicResponse> epics = epicService.getEpicsByProject(projectId, keyword);
        return ResponseEntity.ok(ApiResponse.success(MSG_GET_EPICS_SUCCESS, epics));
    }

    /**
     * Xem chi tiết thông tin của một Epic cụ thể.
     */
    @GetMapping("/{epicId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<EpicResponse>> getEpicDetails(
            @PathVariable Integer projectId,
            @PathVariable Integer epicId) {
        
        EpicResponse epic = epicService.getEpicDetails(projectId, epicId);
        return ResponseEntity.ok(ApiResponse.success(MSG_GET_EPIC_DETAILS_SUCCESS, epic));
    }

    /**
     * Tạo mới một Epic trong dự án.
     */
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<EpicResponse>> createEpic(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateEpicRequest request) {

        EpicResponse epic = epicService.createEpic(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(MSG_CREATE_EPIC_SUCCESS, epic));
    }

    /**
     * Cập nhật thông tin của một Epic hiện có.
     */
    @PutMapping("/{epicId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<EpicResponse>> updateEpic(
            @PathVariable Integer projectId,
            @PathVariable Integer epicId,
            @Valid @RequestBody UpdateEpicRequest request) {

        EpicResponse epic = epicService.updateEpic(projectId, epicId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_EPIC_SUCCESS, epic));
    }

    /**
     * Xóa một Epic khỏi dự án.
     * Service sẽ tự động kiểm tra các ràng buộc liên quan (như Task bên trong) trước khi xóa.
     */
    @DeleteMapping("/{epicId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteEpic(
            @PathVariable Integer projectId,
            @PathVariable Integer epicId) {

        epicService.deleteEpic(projectId, epicId);
        return ResponseEntity.ok(ApiResponse.success(MSG_DELETE_EPIC_SUCCESS, null));
    }
}