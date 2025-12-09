package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {
    
    // Lấy tất cả log mới nhất
    List<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Query nâng cao: Lấy log của những User thuộc Project cụ thể
    @Query("SELECT a FROM ActivityLog a " +
           "WHERE a.userId IN (SELECT pm.user.id FROM ProjectMember pm WHERE pm.project.id = :projectId) " +
           "ORDER BY a.createdAt DESC")
    List<ActivityLog> findByProjectMembers(@Param("projectId") Integer projectId, Pageable pageable);
}
