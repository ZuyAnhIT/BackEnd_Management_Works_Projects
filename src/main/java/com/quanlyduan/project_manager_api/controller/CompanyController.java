package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
import com.quanlyduan.project_manager_api.model.CongTy;
import com.quanlyduan.project_manager_api.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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


    // API TAO CONG TY
    @PostMapping
    public ResponseEntity<ApiResponse<CongTy>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request) {
        
        CongTy newCompany = companyService.createCompany(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED) // Dùng 201 Created cho việc tạo mới
                .body(ApiResponse.success("Tạo công ty thành công", newCompany));
    }
    
    // (Thêm các API khác cho Company tại đây: GET, PUT, DELETE, ...)


    // API MOI THANH VIEN VAO CONG TY
    @PostMapping("/{congTyId}/invitations")
    public ResponseEntity<ApiResponse<Object>> inviteMember(
            @PathVariable Integer congTyId,
            @Valid @RequestBody InviteMemberRequest request) {
        
        companyService.inviteMember(congTyId, request);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi lời mời thành công", null));
    }


    // API HIEN THI DANH SACH THANH VIEN CONG TY 
    @GetMapping("/{congTyId}/members")
    public ResponseEntity<ApiResponse<List<CompanyMemberResponse>>> getCompanyMembers(
            @PathVariable Integer congTyId) {
        
        List<CompanyMemberResponse> members = companyService.getCompanyMembers(congTyId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành viên thành công", members));
    }

    // API HIEN THI THONG TIN CHI TIET CONG TY
    @GetMapping("/{congTyId}")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> getCompanyDetails(
            @PathVariable Integer congTyId) {
        
        CompanyDetailsResponse companyDetails = companyService.getCompanyDetails(congTyId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin công ty thành công", companyDetails));
    }
    
}