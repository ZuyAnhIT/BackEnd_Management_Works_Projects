// File: src/main/java/com/quanlyduan/project_manager_api/controller/EpicController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateEpicRequest; 
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
public class EpicController {

    private final EpicService epicService;

    // Constructor thủ công
    public EpicController(EpicService epicService) {
        this.epicService = epicService;
    }

    // 1. Lấy danh sách Epic (Để hiển thị Panel trái)
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<EpicResponse>>> getEpics(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String keyword) { // Thêm keyword tìm kiếm
            
        List<EpicResponse> epics = epicService.getEpicsByProject(projectId, keyword);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách Epic thành công.", epics));
    }

    // 2. Tạo Epic mới
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<EpicResponse>> createEpic(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateEpicRequest request) { // *** ĐÃ SỬA: Dùng CreateEpicRequest ***

        EpicResponse epic = epicService.createEpic(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo Epic thành công.", epic));
    }

}