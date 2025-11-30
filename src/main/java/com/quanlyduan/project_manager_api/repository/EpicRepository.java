// File: src/main/java/com/quanlyduan/project_manager_api/repository/EpicRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Epic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
/**
 * Repository cho Entity Epic (Quản lý các Epic/công việc lớn trong Dự án).
 * Kế thừa JpaSpecificationExecutor để hỗ trợ tìm kiếm động.
 */
public interface EpicRepository extends JpaRepository<Epic, Integer>, JpaSpecificationExecutor<Epic> {

    /**
     * Lấy danh sách tất cả Epic thuộc về một Dự án.
     */
    List<Epic> findByProject_Id(Integer projectId);

    /**
     * Đếm tổng số Epic thuộc về một Dự án.
     * Dùng để sinh mã Epic Code (Ví dụ: PROJ-E-1).
     */
    long countByProject_Id(Integer projectId);

    // 1. Kiểm tra khi Tạo mới
    /**
     * Kiểm tra xem đã tồn tại Epic với Tên (không phân biệt hoa thường) trong Dự án chưa.
     */
    boolean existsByProject_IdAndNameIgnoreCase(Integer projectId, String name);

    // 2. Kiểm tra khi Cập nhật
    /**
     * Kiểm tra xem có tồn tại Epic khác (khác ID) có Tên trùng trong cùng Dự án không.
     */
    boolean existsByProject_IdAndNameIgnoreCaseAndIdNot(Integer projectId, String name, Integer id);
}