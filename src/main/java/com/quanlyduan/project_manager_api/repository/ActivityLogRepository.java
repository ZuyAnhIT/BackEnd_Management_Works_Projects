package com.quanlyduan.project_manager_api.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.ActivityLog;

/**
 * Kho luu tru du lieu nhat ky hoat dong (Activity Logs).
 * Ho tro truy van lich su o cac cap do: Du an, Khong gian lam viec, Cong ty va Ca nhan.
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {

    /**
     * Lay danh sach hoat dong trong pham vi mot Du an.
     * @param projectId ID cua du an.
     * @param pageable Thong tin phan trang.
     */
    List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(Integer projectId, Pageable pageable);

    /**
     * Lay danh sach hoat dong trong pham vi mot Khong gian lam viec (Workspace).
     * @param workspaceId ID cua khong gian lam viec.
     * @param pageable Thong tin phan trang.
     */
    List<ActivityLog> findByWorkspaceIdOrderByCreatedAtDesc(Integer workspaceId, Pageable pageable);

    /**
     * Lay danh sach hoat dong trong pham vi toan bo Cong ty.
     * @param companyId ID cua cong ty.
     * @param pageable Thong tin phan trang.
     */
    List<ActivityLog> findByCompanyIdOrderByCreatedAtDesc(Integer companyId, Pageable pageable);

    /**
     * Lay nhat ky hoat dong ca nhan cua mot Nguoi dung.
     * @param userId ID cua nguoi dung.
     * @param pageable Thong tin phan trang.
     */
    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);
}