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

    public InvitationServiceImpl(CompanyInvitationRepository companyInvitationRepository, 
                                 CompanyMemberRepository companyMemberRepository) {
        this.companyInvitationRepository = companyInvitationRepository;
        this.companyMemberRepository = companyMemberRepository;
    }

    // LOGIC TẠO TOKEN LOI MOI
    @Override
    public CompanyInvitation validateInvitationToken(String token) { 
        CompanyInvitation invitation = companyInvitationRepository.findByToken(token) 
                .orElseThrow(() -> new ResourceNotFoundException("Mã lời mời không hợp lệ")); 

        if (invitation.getStatus() != InvitationStatus.PENDING) { 
            throw new BadRequestException("Lời mời này đã được xử lý hoặc đã bị hủy"); 
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) { 
            invitation.setStatus(InvitationStatus.EXPIRED); 
            companyInvitationRepository.save(invitation); 
            throw new BadRequestException("Lời mời này đã hết hạn"); 
        }
        return invitation; 
    }

    // LOGIC THEM THANH VIEN
    @Override
    public void addMemberToCompany(User user, Company company, Role role) { 
        CompanyMember membership = CompanyMember.builder() 
                .user(user) 
                .company(company) 
                .role(role)
                .status(MemberStatus.ACTIVE) 
                .build();
        companyMemberRepository.save(membership); 
    }
}