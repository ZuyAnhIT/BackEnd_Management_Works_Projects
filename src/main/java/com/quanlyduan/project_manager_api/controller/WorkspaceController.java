package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.model.KhongGian;
import com.quanlyduan.project_manager_api.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

    // API TAO KHONG GIAN CONG TY
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

    // API XEM KHONG GIAN TRONG CONG TY
    @GetMapping
    // Bảo vệ endpoint: Chỉ thành viên công ty (isCompanyMember) mới được xem
    @PreAuthorize("@securityService.isCompanyMember(#congTyId)")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getWorkspaces(
            @PathVariable Integer congTyId) {
        
        List<WorkspaceResponse> workspaces = workspaceService.getWorkspacesByCompany(congTyId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách không gian làm việc thành công", workspaces));
    }
}