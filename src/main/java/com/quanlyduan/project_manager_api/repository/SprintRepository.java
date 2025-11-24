// File: src/main/java/com/quanlyduan/project_manager_api/repository/SprintRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    
    // Lấy tất cả sprint của 1 project
    List<Sprint> findByProject_IdOrderByStartDateDesc(Integer projectId);
    List<Sprint> findByProject_IdAndStatusOrderByStartDateDesc(Integer projectId, SprintStatus status);
    // Kiểm tra xem có sprint nào đang chạy trong project không
    boolean existsByProject_IdAndStatus(Integer projectId, SprintStatus status);

    Optional<Sprint> findByIdAndProject_Id(Integer sprintId, Integer projectId);
    
    /**
     * Lấy danh sách Sprint KHÔNG bao gồm COMPLETED hoặc CANCELLED.
     * (Chỉ lấy NOT_STARTED và IN_PROGRESS)
     */
    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId AND s.status IN :statuses ORDER BY s.startDate ASC")
    List<Sprint> findActiveSprintsByProjectId(
        @Param("projectId") Integer projectId, 
        @Param("statuses") List<SprintStatus> statuses
    );
}
