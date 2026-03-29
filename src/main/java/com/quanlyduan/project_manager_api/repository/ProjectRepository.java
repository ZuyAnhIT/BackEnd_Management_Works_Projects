package com.quanlyduan.project_manager_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;

/**
 * Kho lưu trữ dữ liệu quản lý thực thể Dự án (Project).
 * Hỗ trợ các thao tác kiểm tra mã dự án, quản lý hạn mức và tìm kiếm động.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {

    // Khai báo các câu truy vấn tĩnh để tránh hardcode
    String COUNT_BY_COMPANY_QUERY = "SELECT COUNT(p) FROM Project p WHERE p.workspace.company.id = :companyId";
    String FIND_IDS_BY_WORKSPACE_QUERY = "SELECT p.id FROM Project p WHERE p.workspace.id = :workspaceId";

    // ======================================================
    // 1. KIỂM TRA RÀNG BUỘC (VALIDATION)
    // ======================================================

    /**
     * Kiểm tra tính duy nhất của Mã Dự án trong phạm vi một Không gian làm việc.
     * @param workspaceId ID của không gian làm việc.
     * @param projectCode Mã dự án cần kiểm tra (không phân biệt hoa thường).
     */
    boolean existsByWorkspace_IdAndProjectCodeIgnoreCase(Integer workspaceId, String projectCode);

    // ======================================================
    // 2. QUẢN LÝ HẠN MỨC DỮ LIỆU (QUOTA & COUNT)
    // ======================================================

    /**
     * Đếm tổng số lượng dự án hiện có của một Công ty xuyên suốt các Workspace.
     * Thường được dùng cho việc kiểm tra giới hạn gói cước SaaS.
     */
    @Query(COUNT_BY_COMPANY_QUERY)
    long countByCompanyId(@Param("companyId") Integer companyId);

    /**
     * Đếm tổng số lượng dự án của một công ty và loại trừ trạng thái cụ thể.
     * Ví dụ: Đếm tất cả dự án trừ các dự án đã hủy bỏ.
     */
    long countByWorkspace_Company_IdAndStatusNot(Integer companyId, ProjectStatus status);

    // ======================================================
    // 3. TRUY VẤN DỮ LIỆU (RETRIEVAL)
    // ======================================================

    /**
     * Tìm kiếm thông tin dự án dựa trên ID.
     */
    Optional<Project> findById(Integer id);

    /**
     * Lấy danh sách ID của tất cả dự án thuộc về một Không gian làm việc.
     * Phục vụ cho các logic kiểm tra quyền hạn hoặc lọc phạm vi dữ liệu.
     */
    @Query(FIND_IDS_BY_WORKSPACE_QUERY)
    List<Integer> findProjectIdsByWorkspaceId(@Param("workspaceId") Integer workspaceId);

    /**
     * Lấy danh sách dự án thuộc về một Không gian làm việc hỗ trợ phân trang.
     */
    Page<Project> findByWorkspace_Id(Integer workspaceId, Pageable pageable);

    /**
     * Lọc danh sách dự án theo trạng thái trong một Không gian làm việc.
     */
    Page<Project> findByWorkspace_IdAndStatus(Integer workspaceId, ProjectStatus status, Pageable pageable);
}