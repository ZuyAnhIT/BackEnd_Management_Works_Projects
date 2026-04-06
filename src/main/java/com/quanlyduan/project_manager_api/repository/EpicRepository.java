package com.quanlyduan.project_manager_api.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.Epic;

/**
 * Kho lưu trữ dữ liệu cho thực thể Epic (Các hạng mục công việc lớn trong Dự án).
 * Hỗ trợ các thao tác truy vấn theo dự án và tìm kiếm động thông qua JpaSpecificationExecutor.
 */
@Repository
public interface EpicRepository extends JpaRepository<Epic, Integer>, JpaSpecificationExecutor<Epic> {

    // ======================================================
    // 1. TRUY VẤN DỮ LIỆU (RETRIEVAL)
    // ======================================================

    /**
     * Lấy danh sách tất cả các Epic thuộc về một Dự án cụ thể.
     * @param projectId ID của dự án cần truy vấn.
     * @return Danh sách các Epic tìm thấy.
     */
    List<Epic> findByProject_Id(Integer projectId);

    // ======================================================
    // 2. TIỆN ÍCH HỖ TRỢ (UTILITIES)
    // ======================================================

    /**
     * Đếm tổng số lượng Epic trong một dự án.
     * Thường được sử dụng để sinh mã định danh Epic Code (Ví dụ: PROJ-E-01).
     * @param projectId ID của dự án.
     * @return Tổng số lượng Epic hiện có.
     */
    long countByProject_Id(Integer projectId);

    // ======================================================
    // 3. KIỂM TRA RÀNG BUỘC (VALIDATION)
    // ======================================================

    /**
     * Kiểm tra sự tồn tại của Epic theo tên trong cùng một dự án (không phân biệt hoa thường).
     * Dùng để tránh trùng lặp tên khi tạo mới Epic.
     */
    boolean existsByProject_IdAndNameIgnoreCase(Integer projectId, String name);

    /**
     * Kiểm tra sự tồn tại của Epic khác có cùng tên trong cùng một dự án.
     * Dùng để kiểm tra ràng buộc tên khi cập nhật thông tin Epic.
     */
    boolean existsByProject_IdAndNameIgnoreCaseAndIdNot(Integer projectId, String name, Integer id);

    String SEARCH_GLOBAL_EPICS = "SELECT e FROM Epic e JOIN FETCH e.project p JOIN FETCH p.workspace w " + 
            "WHERE p.id IN (SELECT pm.project.id FROM ProjectMember pm WHERE pm.user.id = :userId) " +
            "AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.epicCode) LIKE LOWER(CONCAT('%', :keyword, '%')))";

    /**
     * Search epics by keyword ensuring the user is a member of the underlying project.
     */
    @Query(SEARCH_GLOBAL_EPICS)
    List<Epic> searchEpicsGlobal(@Param("userId") Integer userId, @Param("keyword") String keyword, Pageable pageable);
}