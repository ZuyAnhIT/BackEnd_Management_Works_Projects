// File: src/main/java/com/quanlyduan/project_manager_api/repository/ProjectStatusRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectStatus; // Entity Trạng thái Dự án
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository cho Entity ProjectStatus (Quản lý các cột trạng thái/Board của Dự án).
 */
public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Integer> {

    /**
     * Tìm trạng thái (cột) đầu tiên (mặc định) của một dự án,
     * dựa trên thứ tự sắp xếp (sort_order) thấp nhất.
     */
    Optional<ProjectStatus> findFirstByProject_IdOrderBySortOrderAsc(Integer projectId);

    /**
     * Lấy tất cả trạng thái (cột) của dự án, sắp xếp theo thứ tự hiển thị (sortOrder) tăng dần.
     */
    List<ProjectStatus> findByProject_IdOrderBySortOrderAsc(Integer projectId);

    /**
     * Kiểm tra trùng tên cột (không phân biệt hoa thường) trong cùng 1 dự án.
     */
    boolean existsByProject_IdAndNameIgnoreCase(Integer projectId, String name);

    /**
     * Tìm giá trị sort_order lớn nhất hiện tại trong dự án.
     * COALESCE(MAX(s.sortOrder), -1) để trả về -1 nếu không có cột nào tồn tại,
     * giúp logic nghiệp vụ dễ dàng cộng 1 để bắt đầu từ 0.
     */
    @Query("SELECT COALESCE(MAX(s.sortOrder), -1) FROM ProjectStatus s WHERE s.project.id = :projectId")
    Integer findMaxSortOrderByProjectId(@Param("projectId") Integer projectId);
}