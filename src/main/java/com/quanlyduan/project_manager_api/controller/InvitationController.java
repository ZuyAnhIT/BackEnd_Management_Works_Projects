// File: src/main/java/com/quanlyduan/project_manager_api/controller/InvitationController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.InvitationDetailsResponse;
import com.quanlyduan.project_manager_api.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final CompanyService companyService; // Dùng lại logic trong CompanyService

    // API XAC THUC LOI MOI
    // API này CẦN xác thực, người dùng phải login để gọi
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Object>> acceptInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {
        
        companyService.acceptInvitation(request);
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted successfully", null)); // Đã dịch
    }

    // API LAY THONG TIN LOI MOI (PUBLIC)
    // Dùng để kiểm tra token và quyết định luồng UI (Register/Login)
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<InvitationDetailsResponse>> getInvitationDetails(
            @RequestParam String token) {
        
        InvitationDetailsResponse details = companyService.getInvitationDetails(token);
        return ResponseEntity.ok(ApiResponse.success("Invitation details fetched successfully", details)); // Đã dịch
    }
}