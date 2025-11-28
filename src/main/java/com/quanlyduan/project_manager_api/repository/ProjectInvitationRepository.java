package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectInvitation;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Integer> {
    
    Optional<ProjectInvitation> findByToken(String token);
    
    // Kiểm tra xem đã mời chưa để tránh spam
    boolean existsByProject_IdAndEmailAndStatus(Integer projectId, String email, InvitationStatus status);
}