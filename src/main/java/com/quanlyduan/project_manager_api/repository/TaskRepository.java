package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    // Spring Data JPA tự động cung cấp 'findById(Integer taskId)'.
    // Hàm này là đủ để SecurityService tìm Task và lấy 'projectId' từ nó.

    // 'existsById(Integer taskId)' cũng được cung cấp,
    // dùng để TaskCommentServiceImpl kiểm tra Task có tồn tại không.

    
     // US-S3-4: Lấy task được gán cho user
    // Lấy các task chưa hoàn thành (COMPLETED/CANCELLED)
    List<Task> findByAssignee_IdAndStatusNotIn(Integer assigneeId, List<String> excludedStatuses);

    // US-S3-5: Lấy backlog (task chưa có sprint) của 1 project
    List<Task> findByProject_IdAndSprint_IdIsNullOrderBySortOrderAsc(Integer projectId);
    
    // US-S3-9: Lấy task trong 1 sprint
    List<Task> findBySprint_IdOrderBySortOrderAsc(Integer sprintId);

    // Dùng cho US-S3-6: gán nhiều task vào sprint
    @Modifying
    @Query("UPDATE Task t SET t.sprint = :sprint WHERE t.id IN :taskIds")
    void updateSprintForTasks(@Param("sprint") Sprint sprint, @Param("taskIds") List<Integer> taskIds);


}