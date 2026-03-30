package com.quanlyduan.project_manager_api.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.service.InvitationService;

@Service
public class InvitationServiceImpl implements InvitationService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ERROR_INVALID_TOKEN = "Invalid invitation token.";
    public static final String ERROR_INVITATION_PROCESSED = "This invitation has already been processed or cancelled.";
    public static final String ERROR_INVITATION_EXPIRED = "This invitation has expired.";

    // Khai bao cac bien phu thuoc
    private final CompanyInvitationRepository companyInvitationRepository;
    private final CompanyMemberRepository companyMemberRepository;

    // Constructor khoi tao thu cong
    public InvitationServiceImpl(CompanyInvitationRepository companyInvitationRepository,
                                 CompanyMemberRepository companyMemberRepository) {
        this.companyInvitationRepository = companyInvitationRepository;
        this.companyMemberRepository = companyMemberRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    public CompanyInvitation validateInvitationToken(String token) {
        // Tim kiem loi moi dua tren token truyen vao
        CompanyInvitation invitation = companyInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVALID_TOKEN));

        // Kiem tra trang thai cua loi moi phai la dang cho xu ly
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException(ERROR_INVITATION_PROCESSED);
        }

        // Kiem tra thoi han hieu luc cua loi moi
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Cap nhat trang thai thanh het han neu da qua thoi diem hieu luc
            invitation.setStatus(InvitationStatus.EXPIRED);
            companyInvitationRepository.save(invitation);
            
            throw new BadRequestException(ERROR_INVITATION_EXPIRED);
        }
        
        // Tra ve doi tuong loi moi neu tat ca cac dieu kien deu hop le
        return invitation;
    }

    @Override
    public void addMemberToCompany(User user, Company company, Role role) {
        // Khoi tao doi tuong thanh vien moi voi trang thai hoat dong mac dinh
        CompanyMember membership = CompanyMember.builder()
                .user(user)
                .company(company)
                .role(role)
                .status(MemberStatus.ACTIVE)
                .build();

        // Luu thong tin thanh vien vao he thong
        companyMemberRepository.save(membership);
    }
}