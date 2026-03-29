package com.quanlyduan.project_manager_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.ProjectStatus;

/**
 * Kho lưu trữ dữ liệu quản lý các trạng thái dự án (Kanban Columns).
 * Điều khiển thứ tự hiển thị và các ràng buộc về tên cột trên bảng công việc.
 */
@Repository
public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Integer> {

    // Khai báo câu truy vấn tìm giá trị thứ tự sắp xếp lớn nhất
    String FIND_MAX_SORT_ORDER_QUERY = "SELECT COALESCE(MAX(s.sortOrder), -1) " +
                                       "FROM ProjectStatus s " +
                                       "WHERE s.project.id = :projectId";

    // ======================================================
    // 1. TRUY VẤN DỮ LIỆU (RETRIEVAL)
    // ======================================================

    /**
     * Lấy toàn bộ danh sách trạng thái của dự án theo thứ tự hiển thị tăng dần.
     * @param projectId ID của dự án.
     */
    List<ProjectStatus> findByProject_IdOrderBySortOrderAsc(Integer projectId);

    /**
     * Tìm trạng thái mặc định (cột đầu tiên) của dự án dựa trên thứ tự thấp nhất.
     */
    Optional<ProjectStatus> findFirstByProject_IdOrderBySortOrderAsc(Integer projectId);

    // ======================================================
    // 2. TIỆN ÍCH LOGIC (UTILITIES)
    // ======================================================

    /**
     * Tìm giá trị sortOrder lớn nhất hiện tại trong dự án.
     * Trả về -1 nếu dự án chưa có cột nào (giúp logic Service dễ dàng cộng 1 để bắt đầu từ 0).
     */
    @Query(FIND_MAX_SORT_ORDER_QUERY)
    Integer findMaxSortOrderByProjectId(@Param("projectId") Integer projectId);

    // ======================================================
    // 3. KIỂM TRA RÀNG BUỘC (VALIDATION)
    // ======================================================

    /**
     * Kiểm tra sự tồn tại của tên cột trong cùng một dự án (không phân biệt hoa thường).
     * Dùng để tránh việc tạo hai cột trùng tên trên cùng một Board.
     */
    boolean existsByProject_IdAndNameIgnoreCase(Integer projectId, String name);
}