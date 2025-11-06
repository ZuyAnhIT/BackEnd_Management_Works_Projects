// File: src/main/java/com/quanlyduan/project_manager_api/repository/CompanyInvitationRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CompanyInvitation; // Đã dịch
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyInvitationRepository extends JpaRepository<CompanyInvitation, Integer> { // Đã dịch
    
    Optional<CompanyInvitation> findByToken(String token); // Đã dịch
    
    // Kiểm tra lời mời PENDING đã tồn tại chưa
    boolean existsByCompany_IdAndEmailAndStatus( // Đã dịch
        Integer companyId, String email, InvitationStatus status // Đã dịch
    );

    // Lấy tất cả lời mời PENDING của công ty
    List<CompanyInvitation> findByCompany_IdAndStatus( // Đã dịch
        Integer companyId, InvitationStatus status // Đã dịch
    );
}