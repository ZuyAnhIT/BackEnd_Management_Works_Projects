package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.SubTask;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus; // Import Enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Integer> {

    // [FIX] Sửa tên hàm để khớp với Service (thêm CreatedAtAsc để sắp xếp ổn định)
    List<SubTask> findByParentTask_IdOrderBySortOrderAscCreatedAtAsc(Integer taskId);

    // Dùng để đếm số lượng khi tạo mới (tính sortOrder)
    Integer countByParentTask_Id(Integer taskId);

    // --- Phần của Dev 4 (Tiến độ) ---

    // 1. Đếm tổng (Hàm này thực ra giống countByParentTask_Id, nhưng dùng @Query cũng được)
    @Query("SELECT COUNT(s) FROM SubTask s WHERE s.parentTask.id = :taskId")
    long countTotalByTaskId(@Param("taskId") Integer taskId);

    // 2. Đếm đã xong (DONE)
    // [FIX] Dùng tham số :status để an toàn hơn là hardcode chuỗi 'DONE'
    @Query("SELECT COUNT(s) FROM SubTask s WHERE s.parentTask.id = :taskId AND s.status = :status")
    long countByTaskIdAndStatus(@Param("taskId") Integer taskId, @Param("status") SubTaskStatus status);
}