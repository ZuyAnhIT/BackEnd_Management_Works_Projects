// File: src/main/java/com/quanlyduan/project_manager_api/repository/SprintRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Sprint; // Entity Sprint
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository cho Entity Sprint (Quản lý các chu kỳ phát triển Scrum/Agile).
 */
public interface SprintRepository extends JpaRepository<Sprint, Integer>, JpaSpecificationExecutor<Sprint> {

    /**
     * Lấy tất cả sprint của một dự án, sắp xếp theo Ngày bắt đầu giảm dần (mới nhất lên đầu).
     */
    List<Sprint> findByProject_IdOrderByStartDateDesc(Integer projectId);

    /**
     * Lấy danh sách sprint theo Project ID và Trạng thái cụ thể.
     */
    List<Sprint> findByProject_IdAndStatusOrderByStartDateDesc(Integer projectId, SprintStatus status);

    /**
     * Kiểm tra xem có sprint nào đang ở trạng thái cụ thể (ví dụ: IN_PROGRESS) trong Project không.
     */
    boolean existsByProject_IdAndStatus(Integer projectId, SprintStatus status);

    /**
     * Tìm kiếm Sprint theo ID và đảm bảo nó thuộc Project ID được cung cấp.
     */
    Optional<Sprint> findByIdAndProject_Id(Integer sprintId, Integer projectId);

    /**
     * Lấy danh sách Sprint đang HOẠT ĐỘNG (NOT_STARTED và IN_PROGRESS).
     * Dùng cho Backlog/Board để hiển thị các sprint sắp tới hoặc đang chạy.
     */
    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId AND s.status IN :statuses ORDER BY s.startDate ASC")
    List<Sprint> findActiveSprintsByProjectId(
        @Param("projectId") Integer projectId,
        @Param("statuses") List<SprintStatus> statuses
    );

    /**
     * Đếm số lượng Sprint trong dự án để tự sinh tên (Ví dụ: Sprint 1, Sprint 2...).
     */
    long countByProject_Id(Integer projectId);

    // Lấy 5 Sprint gần nhất đã hoàn thành để tính Velocity
    List<Sprint> findTop5ByProject_IdAndStatusOrderByEndDateDesc(Integer projectId, SprintStatus status);
}