// File: src/main/java/com/quanlyduan/project_manager_api/repository/ProjectInvitationRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectInvitation; // Entity Lời mời Dự án
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/**
 * Repository cho Entity ProjectInvitation (Quản lý lời mời tham gia Dự án).
 */
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Integer> {

    /**
     * Tìm kiếm Lời mời theo Token duy nhất.
     */
    Optional<ProjectInvitation> findByToken(String token);

    /**
     * Kiểm tra xem Lời mời có trạng thái PENDING cho một Email cụ thể trong Dự án đã tồn tại chưa (tránh spam).
     */
    boolean existsByProject_IdAndEmailAndStatus(Integer projectId, String email, InvitationStatus status);
}