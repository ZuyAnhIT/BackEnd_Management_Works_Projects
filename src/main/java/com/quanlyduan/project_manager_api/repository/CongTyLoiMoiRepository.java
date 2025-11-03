package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CongTyLoiMoi;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CongTyLoiMoiRepository extends JpaRepository<CongTyLoiMoi, Integer> {
    
    Optional<CongTyLoiMoi> findByToken(String token);
    
    // Kiểm tra lời mời PENDING đã tồn tại chưa
    boolean existsByCongTy_IdCongTyAndEmailAndTrangThai(
        Integer congTyId, String email, InvitationStatus trangThai
    );

    // Lấy tất cả lời mời PENDING của công ty
    List<CongTyLoiMoi> findByCongTy_IdCongTyAndTrangThai(
        Integer congTyId, InvitationStatus trangThai
    );
}