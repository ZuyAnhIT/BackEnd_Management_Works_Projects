// File: src/main/java/com/quanlyduan/project_manager_api/repository/ProjectStatusRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Integer> {

    /**
     * Tìm trạng thái (cột) đầu tiên (mặc định) của một dự án,
     * dựa trên thứ tự sắp xếp (sort_order).
     */
    Optional<ProjectStatus> findFirstByProject_IdOrderBySortOrderAsc(Integer projectId);
}