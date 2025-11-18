// File: src/main/java/com/quanlyduan/project_manager_api/repository/ProjectStatusRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    // *** HÀM CHO CHỨC NĂNG NÀY ***
    // Lấy tất cả status của project, sắp xếp theo sortOrder
    List<ProjectStatus> findByProject_IdOrderBySortOrderAsc(Integer projectId);
    
    // Tìm giá trị sort_order lớn nhất hiện tại (dùng cho chức năng tạo sau này)
    @Query("SELECT MAX(s.sortOrder) FROM ProjectStatus s WHERE s.project.id = :projectId")
    Integer findMaxSortOrderByProjectId(Integer projectId);
}