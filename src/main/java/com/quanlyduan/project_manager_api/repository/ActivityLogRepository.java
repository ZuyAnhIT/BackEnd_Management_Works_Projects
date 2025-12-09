package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {

    // 1. PROJECT LEVEL: Log trong phạm vi dự án
    List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Integer projectId, Pageable pageable);

    // 2. WORKSPACE LEVEL: Log của tất cả project trong workspace + log của chính workspace
    List<ActivityLog> findByWorkspaceIdOrderByCreatedAtDesc(Integer workspaceId, Pageable pageable);

    // 3. COMPANY LEVEL: Log toàn công ty
    List<ActivityLog> findByCompanyIdOrderByCreatedAtDesc(Integer companyId, Pageable pageable);

    // 4. USER LEVEL (Personal): Log liên quan đến user (ví dụ: họ được giao task, họ comment)
    // Hoặc đơn giản là "Nhật ký hoạt động CỦA user đó"
    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);
}