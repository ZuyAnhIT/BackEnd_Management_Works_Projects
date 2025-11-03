package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final CompanyService companyService; // Dùng lại logic trong CompanyService

    // API này CẦN xác thực, người dùng phải login để gọi
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Object>> acceptInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {
        
        companyService.acceptInvitation(request);
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận lời mời thành công", null));
    }
}
