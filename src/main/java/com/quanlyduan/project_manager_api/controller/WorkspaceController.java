package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.model.KhongGian;
import com.quanlyduan.project_manager_api.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse; 

@RestController
@RequestMapping("/api/companies/{congTyId}/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @PreAuthorize("@securityService.isCompanyAdmin(#congTyId)")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace( // Sửa kiểu trả về
            @PathVariable Integer congTyId,
            @Valid @RequestBody CreateWorkspaceRequest request) {
        
        // Nhận về DTO thay vì Entity
        WorkspaceResponse newWorkspace = workspaceService.createWorkspace(congTyId, request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo không gian làm việc thành công", newWorkspace));
    }
}