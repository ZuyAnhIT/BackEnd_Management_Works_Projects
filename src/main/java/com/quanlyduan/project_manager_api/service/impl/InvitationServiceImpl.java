package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.repository.CongTyLoiMoiRepository;
import com.quanlyduan.project_manager_api.repository.CongTyThanhVienRepository;
import com.quanlyduan.project_manager_api.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final CongTyLoiMoiRepository congTyLoiMoiRepository;
    private final CongTyThanhVienRepository congTyThanhVienRepository;

    @Override
    public CongTyLoiMoi validateInvitationToken(String token) {
        CongTyLoiMoi loiMoi = congTyLoiMoiRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token lời mời không hợp lệ"));

        if (loiMoi.getTrangThai() != InvitationStatus.PENDING) {
            throw new BadRequestException("Lời mời đã được xử lý hoặc đã hủy");
        }

        if (loiMoi.getNgayHetHan().isBefore(LocalDateTime.now())) {
            loiMoi.setTrangThai(InvitationStatus.EXPIRED);
            congTyLoiMoiRepository.save(loiMoi);
            throw new BadRequestException("Lời mời đã hết hạn");
        }
        return loiMoi;
    }

    @Override
    public void addMemberToCompany(NguoiDung user, CongTy congTy, Role role) {
        CongTyThanhVien membership = CongTyThanhVien.builder()
                .nguoiDung(user)
                .congTy(congTy)
                .role(role)
                .trangThai(MemberStatus.HOAT_DONG)
                .build();
        congTyThanhVienRepository.save(membership);
    }
}