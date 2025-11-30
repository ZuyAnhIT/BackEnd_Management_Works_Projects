// File: src/main/java/com/quanlyduan/project_manager_api/repository/SubTaskRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.SubTask; // Entity SubTask
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repository cho Entity SubTask (Quản lý các công việc con).
 */
public interface SubTaskRepository extends JpaRepository<SubTask, Integer> {

    /**
     * Đếm tổng số lượng SubTask thuộc về một Task cha.
     * Dùng để xác định 'sort_order' cho SubTask mới.
     */
    Integer countByParentTask_Id(Integer taskId);

    /**
     * Tìm tất cả SubTask của một Task cha, sắp xếp theo 'sort_order' tăng dần.
     * Dùng để lấy danh sách SubTask cho chi tiết Task.
     */
    List<SubTask> findByParentTask_IdOrderBySortOrderAsc(Integer taskId);

    /**
     * Tìm tất cả SubTask của một Task cha có 'sort_order' lớn hơn một giá trị cụ thể,
     * sắp xếp theo 'sort_order' tăng dần.
     * Dùng cho logic cập nhật lại vị trí (shifting) khi kéo thả.
     */
    List<SubTask> findByParentTask_IdAndSortOrderGreaterThanOrderBySortOrderAsc(
        Integer taskId,
        Integer sortOrder
    );

}