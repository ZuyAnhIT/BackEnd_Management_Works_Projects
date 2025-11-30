// File: src/main/java/com/quanlyduan/project_manager_api/controller/EpicController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateEpicRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateEpicRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import com.quanlyduan.project_manager_api.service.EpicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/epics")
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến Epic (nhóm công việc lớn) trong Dự án.
 */
public class EpicController {

    private final EpicService epicService;

    // Constructor thủ công
    public EpicController(EpicService epicService) {
        this.epicService = epicService;
    }

    // ======================================================
    // 1. LẤY DANH SÁCH EPIC (LIST EPICS)
    // ======================================================
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<EpicResponse>>> getEpics(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String keyword) { // Thêm keyword tìm kiếm

        List<EpicResponse> epics = epicService.getEpicsByProject(projectId, keyword);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Epic list retrieved successfully.", epics));
    }

    // ======================================================
    // 2. TẠO EPIC MỚI (CREATE EPIC)
    // ======================================================
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<EpicResponse>> createEpic(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateEpicRequest request) {

        EpicResponse epic = epicService.createEpic(projectId, request);
        // Sửa thông báo trả về sang tiếng Anh (201 Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Epic created successfully.", epic));
    }

    // ======================================================
    // 3. CẬP NHẬT EPIC (UPDATE EPIC)
    // ======================================================
    @PutMapping("/{epicId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<EpicResponse>> updateEpic(
            @PathVariable Integer projectId,
            @PathVariable Integer epicId,
            @Valid @RequestBody UpdateEpicRequest request) {

        EpicResponse epic = epicService.updateEpic(projectId, epicId, request);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Epic updated successfully.", epic));
    }


    // ======================================================
    // 4. XÓA EPIC (DELETE EPIC)
    // ======================================================
    @DeleteMapping("/{epicId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteEpic(
            @PathVariable Integer projectId,
            @PathVariable Integer epicId) {

        // Logic service sẽ kiểm tra ràng buộc (Task bên trong) và xóa
        epicService.deleteEpic(projectId, epicId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Epic deleted successfully.", null));
    }
}