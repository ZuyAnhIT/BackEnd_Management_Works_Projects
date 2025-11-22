// File: src/main/java/com/quanlyduan/project_manager_api/controller/WorkspaceController.java
package com.quanlyduan.project_manager_api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.RoleUpdateRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMemberResponse;
import com.quanlyduan.project_manager_api.service.WorkspaceService;

import io.swagger.v3.oas.annotations.media.Schema;

import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceStatusRequest;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map; 
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.quanlyduan.project_manager_api.exception.BadRequestException; 
import org.springframework.http.MediaType; 
import io.swagger.v3.oas.annotations.Parameter; 

import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus; 

@RestController
@RequestMapping("/api/companies/{companyId}/workspaces") 
@CrossOrigin("*")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final ObjectMapper objectMapper;

    public WorkspaceController(WorkspaceService workspaceService, ObjectMapper objectMapper) {
        this.workspaceService = workspaceService;
        this.objectMapper = objectMapper;
    }

    // API TAO KHONG GIAN CONG TY (TICH HOP UPLOAD)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Thêm consumes
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:create')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @PathVariable Integer companyId,
            
            // Nhận JSON String
            @Parameter(schema = @Schema(implementation = CreateWorkspaceRequest.class))
            @RequestPart("data") String dataString,
            
            // Nhận file ảnh (Optional)
            @RequestPart(value = "file", required = false) MultipartFile file) {
        
        // Convert String -> DTO
        CreateWorkspaceRequest request;
        try {
            request = objectMapper.readValue(dataString, CreateWorkspaceRequest.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Dữ liệu JSON không hợp lệ: " + e.getMessage()); // Đã dịch
        }

        // Gọi Service
        WorkspaceResponse newWorkspace = workspaceService.createWorkspace(companyId, request, file);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo không gian làm việc thành công.", newWorkspace)); // Đã dịch
    }

    // API 1: LẤY DANH SÁCH (Cơ bản)
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceResponse>>> getWorkspaces(
            @PathVariable Integer companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponseDTO<WorkspaceResponse> workspaces = workspaceService.getWorkspacesByCompany(companyId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách không gian làm việc thành công.", workspaces));
    }

    // API 2: TÌM KIẾM (Nâng cao - MỚI)
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceResponse>>> searchWorkspaces(
            @PathVariable Integer companyId,
            
            // Các tham số tìm kiếm
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) WorkspaceStatus status,

            // Các tham số phân trang
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        
        PageResponseDTO<WorkspaceResponse> results = workspaceService.searchWorkspaces(
            companyId, name, code, description, status, 
            page, size, sortBy, sortDir
        );
        
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm không gian làm việc thành công.", results)); // Đã dịch
    }


    // API XEM CHI TIET KHONG GIAN CONG TY
    @GetMapping("/{workspaceId}")
    // Bảo vệ endpoint: Yêu cầu là thành viên của không gian này
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {
        
        WorkspaceResponse workspaceDetails = workspaceService.getWorkspaceDetails(workspaceId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết không gian làm việc thành công.", workspaceDetails));
    }


    // API THEM THANH VIEN VAO KHONG 
    @PostMapping("/{workspaceId}/invite-members") // Giữ nguyên tên API của bạn
    // Bảo vệ: Chỉ người có quyền "mời"
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:invite_member')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<Object>> inviteMemberToWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody InviteWorkspaceMemberRequest request) {
        
        workspaceService.inviteMemberToWorkspace(companyId, workspaceId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Thêm thành viên vào không gian làm việc thành công.", null));
    }

    // API CAP NHAT KHONG GIAN (TICH HOP UPLOAD)
    @PutMapping(value = "/{workspaceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:edit')")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            
            // Nhận JSON String
            @Parameter(schema = @Schema(implementation = UpdateWorkspaceRequest.class))
            @RequestPart("data") String dataString,
            
            // Nhận file ảnh
            @RequestPart(value = "file", required = false) MultipartFile file) {

        // Convert String -> DTO
        UpdateWorkspaceRequest request;
        try {
            request = objectMapper.readValue(dataString, UpdateWorkspaceRequest.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Dữ liệu JSON không hợp lệ: " + e.getMessage()); // Đã dịch
        }

        WorkspaceResponse updatedWorkspace = workspaceService.updateWorkspace(workspaceId, request, file);

        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật không gian làm việc thành công.", // Đã dịch
                updatedWorkspace
        ));
    }

    // API XOA MEM
    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("@securityService.hasPermission('company', #companyId, 'workspace:delete')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<Object>> deleteWorkspace(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId) {

        workspaceService.deleteWorkspace(workspaceId);

        return ResponseEntity.ok(ApiResponse.success(
                "Xóa không gian làm việc thành công.",
                null
        ));
    }

   // API 1: LẤY DANH SÁCH (Sửa defaultValue)
    @GetMapping("/{workspaceId}/members")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceMemberResponse>>> getWorkspaceMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            
            // *** SỬA LỖI Ở ĐÂY: Đổi "createdAt" thành "joinedAt" ***
            @RequestParam(defaultValue = "joinedAt") String sortBy, 
            
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponseDTO<WorkspaceMemberResponse> members = workspaceService.getWorkspaceMembers(workspaceId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành viên không gian thành công.", members));
    }

    // API 2: TÌM KIẾM (Sửa defaultValue)
    @GetMapping("/{workspaceId}/members/search")
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<WorkspaceMemberResponse>>> searchWorkspaceMembers(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,

            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String phone,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            
            // *** SỬA LỖI Ở ĐÂY: Đổi "createdAt" thành "joinedAt" ***
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponseDTO<WorkspaceMemberResponse> members = workspaceService.searchWorkspaceMembers(
            workspaceId, name, email, role, phone, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm thành viên không gian thành công.", members));
    }


    // API XEM CHI TIET THANH VIEN TRONG KHONG GIAN
    @GetMapping("/{workspaceId}/members/{memberId}")
    // Bảo vệ: Chỉ thành viên của không gian (isWorkspaceMember) mới được xem
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:view')") // Sửa: Dùng @securityService
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> getWorkspaceMemberDetails(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId) {
        
        WorkspaceMemberResponse memberDetails = workspaceService.getWorkspaceMemberDetails(workspaceId, memberId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết thành viên của không gian làm việc thành công.", memberDetails));
    }

    // API CAP NHAT TRANG THAI THANH VIEN KHONG GIAN (ACTIVE/SUSPENDED)
    // *** SỬA QUYỀN: Quyền đúng phải là 'workspace:remove_member' (quản lý thành viên) ***
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:remove_member')") // Sửa: Dùng @securityService
    @PutMapping("/{workspaceId}/members/{memberId}/status")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> updateWorkspaceMemberStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId,
            @Valid @RequestBody UpdateMemberStatusRequest request) {
        
        WorkspaceMemberResponse updatedMember = workspaceService.updateWorkspaceMemberStatus(companyId, workspaceId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành viên của không gian làm việc thành công.", updatedMember));
    }

    // API CAP NHAT TRANG THAI KHONG GIAN (ACTIVE/ARCHIVED/DELETED)
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:edit')")// Tái sử dụng quyền // Sửa: Dùng @securityService
    @PutMapping("/{workspaceId}/status")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspaceStatus(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @Valid @RequestBody UpdateWorkspaceStatusRequest request) {
        
        WorkspaceResponse updatedWorkspace = workspaceService.updateWorkspaceStatus(companyId, workspaceId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái của không gian làm việc thành công.", updatedWorkspace));
    }
    
    // API CAP NHAT VAI TRO THANH VIEN KHONG GIAN
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:manage_roles')") // Giả định quyền là 'workspace:manage_roles'
    @PutMapping("/{workspaceId}/members/{memberId}/role")
    public ResponseEntity<ApiResponse<Object>> updateWorkspaceMemberRole(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId,
            @Valid @RequestBody RoleUpdateRequest request) { // Tái sử dụng DTO

        // 1. Gọi service
        WorkspaceMemberResponse updatedMember = workspaceService.updateWorkspaceMemberRole(companyId, workspaceId, memberId, request.getRoleCode());

        // 2. Tạo message động
        String message = String.format("Cập nhật vai trò cho người dùng '%s' (ID: %d) thành '%s' thành công.",
            updatedMember.getFullName(),
            updatedMember.getUserId(),
            updatedMember.getRoleName()
        );

        // 3. Tạo data trả về
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", updatedMember.getUserId());
        responseData.put("fullName", updatedMember.getFullName());
        responseData.put("newRoleCode", request.getRoleCode()); // Trả về role code
        responseData.put("newRoleName", updatedMember.getRoleName());

        return ResponseEntity.ok(ApiResponse.success(message, responseData));
    }
    // API XOA THANH VIEN KHOI WORKSPACE (Soft Delete)
    // SỬA Ở ĐÂY: Bỏ "{companyId}/workspaces/" đi vì class đã định nghĩa rồi
    @DeleteMapping("/{workspaceId}/members/{memberId}") 
    // Bảo vệ: Kiểm tra quyền 'workspace:remove_member'
    @PreAuthorize("@securityService.hasPermission('workspace', #workspaceId, 'workspace:remove_member')")
    public ResponseEntity<ApiResponse<Object>> removeWorkspaceMember(
            @PathVariable Integer companyId, // Vẫn lấy được từ đường dẫn cha (Class level)
            @PathVariable Integer workspaceId,
            @PathVariable Integer memberId) {
        
        workspaceService.removeMemberFromWorkspace(companyId, workspaceId, memberId);
        
        return ResponseEntity.ok(ApiResponse.success("Xóa thành viên khỏi không gian làm việc thành công.", null));
    }
}