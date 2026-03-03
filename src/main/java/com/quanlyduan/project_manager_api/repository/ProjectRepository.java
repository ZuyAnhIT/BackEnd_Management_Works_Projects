// File: src/main/java/com/quanlyduan/project_manager_api/repository/ProjectRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Project; // Entity Dự án
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Entity Project.
 * Kế thừa JpaSpecificationExecutor để hỗ trợ tìm kiếm động (dynamic search).
 */
public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {
    /**
     * Kiểm tra xem Mã Dự án (Project Code) đã tồn tại trong Workspace chưa (Không phân biệt hoa thường).
     */
    boolean existsByWorkspace_IdAndProjectCodeIgnoreCase(Integer workspaceId, String projectCode);

    /**
     * Đếm tổng số lượng Dự án của toàn bộ Công ty (Xuyên qua tất cả các Workspace).
     * Dùng để kiểm tra giới hạn (Quota Guard) của gói cước SaaS.
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.workspace.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Integer companyId);

    /**
     * Lấy danh sách dự án thuộc về một Workspace (Có phân trang).
     * Spring Data JPA tự động xử lý LIMIT/OFFSET.
     */
    Page<Project> findByWorkspace_Id(Integer workspaceId, Pageable pageable);

    /**
     * Lấy danh sách tất cả các ID Dự án thuộc về một Workspace.
     * Dùng cho các truy vấn kiểm tra quyền/phạm vi.
     */
    @Query("SELECT p.id FROM Project p WHERE p.workspace.id = :workspaceId")
    List<Integer> findProjectIdsByWorkspaceId(@Param("workspaceId") Integer workspaceId);

    /**
     * Tìm dự án theo Workspace và Trạng thái (Enum).
     */
    Page<Project> findByWorkspace_IdAndStatus(Integer workspaceId, ProjectStatus status, Pageable pageable);

    /**
     * Tìm kiếm Project theo ID.
     */
    Optional<Project> findById(Integer id);
}