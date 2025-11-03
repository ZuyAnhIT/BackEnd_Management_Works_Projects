package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.model.CongTy;
import com.quanlyduan.project_manager_api.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CongTy>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request) {
        
        CongTy newCompany = companyService.createCompany(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED) // Dùng 201 Created cho việc tạo mới
                .body(ApiResponse.success("Tạo công ty thành công", newCompany));
    }
    
    // (Thêm các API khác cho Company tại đây: GET, PUT, DELETE, ...)

    @PostMapping("/{congTyId}/invitations")
    public ResponseEntity<ApiResponse<Object>> inviteMember(
            @PathVariable Integer congTyId,
            @Valid @RequestBody InviteMemberRequest request) {
        
        companyService.inviteMember(congTyId, request);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi lời mời thành công", null));
    }

    
}