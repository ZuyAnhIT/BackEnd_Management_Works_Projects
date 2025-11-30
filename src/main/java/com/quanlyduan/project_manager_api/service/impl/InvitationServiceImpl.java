// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/InvitationServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.service.InvitationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final CompanyInvitationRepository companyInvitationRepository;
    private final CompanyMemberRepository companyMemberRepository;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public InvitationServiceImpl(CompanyInvitationRepository companyInvitationRepository,
                                 CompanyMemberRepository companyMemberRepository) {
        this.companyInvitationRepository = companyInvitationRepository;
        this.companyMemberRepository = companyMemberRepository;
    }

    // ======================================================
    // 1. XÁC THỰC TOKEN LỜI MỜI (VALIDATE INVITATION TOKEN)
    // ======================================================
    @Override
    public CompanyInvitation validateInvitationToken(String token) {
        // 1. Tìm lời mời theo Token
        CompanyInvitation invitation = companyInvitationRepository.findByToken(token)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token."));

        // 2. Kiểm tra trạng thái: Phải là PENDING
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This invitation has already been processed or cancelled.");
        }

        // 3. Kiểm tra ngày hết hạn
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Cập nhật trạng thái lời mời thành EXPIRED
            invitation.setStatus(InvitationStatus.EXPIRED);
            companyInvitationRepository.save(invitation);
            
            // Ném lỗi
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This invitation has expired.");
        }
        
        // 4. Hợp lệ, trả về đối tượng lời mời
        return invitation;
    }

    // ======================================================
    // 2. THÊM THÀNH VIÊN VÀO CÔNG TY (ADD MEMBER TO COMPANY)
    // ======================================================
    @Override
    public void addMemberToCompany(User user, Company company, Role role) {
        // Tạo đối tượng CompanyMember
        CompanyMember membership = CompanyMember.builder()
                .user(user)
                .company(company)
                .role(role)
                .status(MemberStatus.ACTIVE) // Mặc định thành viên mới tham gia là ACTIVE
                .build();

        // Lưu thông tin thành viên
        companyMemberRepository.save(membership);
    }
}