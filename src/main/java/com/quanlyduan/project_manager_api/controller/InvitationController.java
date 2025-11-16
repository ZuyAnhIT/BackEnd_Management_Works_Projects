// File: src/main/java/com/quanlyduan/project_manager_api/controller/InvitationController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.InvitationDetailsResponse;
import com.quanlyduan.project_manager_api.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InvitationController {

    private final CompanyService companyService; // Dùng lại logic trong CompanyService

    // API XAC THUC LOI MOI
    // API này CẦN xác thực, người dùng phải login để gọi
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Object>> acceptInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {
        
        companyService.acceptInvitation(request);
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận lời mời thành công.", null)); // Đã dịch
    }

    // API LAY THONG TIN LOI MOI (PUBLIC)
    // Dùng để kiểm tra token và quyết định luồng UI (Register/Login)
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<InvitationDetailsResponse>> getInvitationDetails(
            @RequestParam String token) {
        
        InvitationDetailsResponse details = companyService.getInvitationDetails(token);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết của lời mời thành công.", details)); // Đã dịch
    }
}