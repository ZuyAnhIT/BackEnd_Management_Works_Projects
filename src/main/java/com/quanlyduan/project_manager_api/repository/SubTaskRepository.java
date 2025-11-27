package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Integer> {
    List<SubTask> findByParentTask_IdOrderBySortOrderAsc(Integer taskId);
    Integer countByParentTask_Id(Integer taskId);

    // Phần của Dev 4: Đếm để tính tiến độ
    @Query("SELECT COUNT(s) FROM SubTask s WHERE s.parentTask.id = :taskId")
    long countTotalByTaskId(@Param("taskId") Integer taskId);

    @Query("SELECT COUNT(s) FROM SubTask s WHERE s.parentTask.id = :taskId AND s.status = 'DONE'")
    long countDoneByTaskId(@Param("taskId") Integer taskId);
}
