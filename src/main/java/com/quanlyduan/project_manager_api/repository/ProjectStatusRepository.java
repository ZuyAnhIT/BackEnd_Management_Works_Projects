// File: src/main/java/com/quanlyduan/project_manager_api/repository/ProjectStatusRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Integer> {

    /**
     * Tìm trạng thái (cột) đầu tiên (mặc định) của một dự án,
     * dựa trên thứ tự sắp xếp (sort_order).
     */
    Optional<ProjectStatus> findFirstByProject_IdOrderBySortOrderAsc(Integer projectId);

    // Lấy tất cả status của project, sắp xếp theo sortOrder
    List<ProjectStatus> findByProject_IdOrderBySortOrderAsc(Integer projectId);

    // Kiểm tra trùng tên trong cùng 1 project (không phân biệt hoa thường)
    boolean existsByProject_IdAndNameIgnoreCase(Integer projectId, String name);

    // 2. Tìm giá trị sort_order lớn nhất hiện tại (để thêm vào cuối)
    // COALESCE để trả về 0 nếu chưa có cột nào
    @Query("SELECT COALESCE(MAX(s.sortOrder), -1) FROM ProjectStatus s WHERE s.project.id = :projectId")
    Integer findMaxSortOrderByProjectId(@Param("projectId") Integer projectId);
}