// File: src/main/java/com/quanlyduan/project_manager_api/repository/SubTaskRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Integer> {
    
    /**
     * Đếm số lượng SubTask của một Task
     */
    Integer countByParentTask_Id(Integer taskId);
    
    /**
     * Tìm tất cả SubTask có sortOrder lớn hơn một giá trị, 
     * sắp xếp theo sortOrder tăng dần
     */
    List<SubTask> findByParentTask_IdAndSortOrderGreaterThanOrderBySortOrderAsc(
        Integer taskId, 
        Integer sortOrder
    );
    List<SubTask> findByParentTask_IdOrderBySortOrderAsc(Integer taskId);

}