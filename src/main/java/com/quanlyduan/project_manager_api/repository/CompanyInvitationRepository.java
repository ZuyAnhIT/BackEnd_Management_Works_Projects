// File: src/main/java/com/quanlyduan/project_manager_api/repository/CompanyInvitationRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CompanyInvitation; // Entity Lời mời Công ty
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository cho Entity CompanyInvitation (Quản lý lời mời tham gia công ty).
 */
public interface CompanyInvitationRepository extends JpaRepository<CompanyInvitation, Integer> {

    /**
     * Tìm kiếm Lời mời theo Token duy nhất.
     */
    Optional<CompanyInvitation> findByToken(String token);

    /**
     * Kiểm tra xem Lời mời có trạng thái PENDING cho một Email cụ thể trong Công ty đã tồn tại chưa.
     */
    boolean existsByCompany_IdAndEmailAndStatus(
        Integer companyId, String email, InvitationStatus status
    );

    /**
     * Lấy tất cả lời mời theo ID Công ty và Trạng thái (Không phân trang).
     */
    List<CompanyInvitation> findByCompany_IdAndStatus(
        Integer companyId, InvitationStatus status
    );

    /**
     * Đếm số lượng lời mời theo trạng thái (dùng để tính lại phân trang/thống kê).
     */
    long countByCompany_IdAndStatus(Integer companyId, InvitationStatus status);

    /**
     * Lấy danh sách lời mời theo trạng thái (Có phân trang).
     */
    Page<CompanyInvitation> findByCompany_IdAndStatus(Integer companyId, InvitationStatus status, Pageable pageable);
}