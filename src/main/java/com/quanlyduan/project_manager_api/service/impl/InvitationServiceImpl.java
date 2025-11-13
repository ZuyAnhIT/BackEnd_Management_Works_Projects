// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/InvitationServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository; // Đã dịch
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository; // Đã dịch
import com.quanlyduan.project_manager_api.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final CompanyInvitationRepository companyInvitationRepository; // Đã dịch
    private final CompanyMemberRepository companyMemberRepository; // Đã dịch

    // LOGIC TẠO TOKEN LOI MOI
    @Override
    public CompanyInvitation validateInvitationToken(String token) { // Đã dịch
        CompanyInvitation invitation = companyInvitationRepository.findByToken(token) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Mã lời mời không hợp lệ")); // Đã dịch

        if (invitation.getStatus() != InvitationStatus.PENDING) { // Đã dịch
            throw new BadRequestException("Lời mời này đã được xử lý hoặc đã bị hủy"); // Đã dịch
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) { // Đã dịch
            invitation.setStatus(InvitationStatus.EXPIRED); // Đã dịch
            companyInvitationRepository.save(invitation); // Đã dịch
            throw new BadRequestException("Lời mời này đã hết hạn"); // Đã dịch
        }
        return invitation; // Đã dịch
    }

    // LOGIC THEM THANH VIEN
    @Override
    public void addMemberToCompany(User user, Company company, Role role) { // Đã dịch
        CompanyMember membership = CompanyMember.builder() // Đã dịch
                .user(user) // Đã dịch
                .company(company) // Đã dịch
                .role(role)
                .status(MemberStatus.ACTIVE) // Đã dịch
                .build();
        companyMemberRepository.save(membership); // Đã dịch
    }
}