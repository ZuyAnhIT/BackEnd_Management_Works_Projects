package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    // Spring Data JPA tự động cung cấp 'findById(Integer taskId)'.
    // Hàm này là đủ để SecurityService tìm Task và lấy 'projectId' từ nó.

    // 'existsById(Integer taskId)' cũng được cung cấp,
    // dùng để TaskCommentServiceImpl kiểm tra Task có tồn tại không.

    @Query("SELECT t FROM Task t " +
            "JOIN FETCH t.project p " +
            "JOIN FETCH p.workspace w " +
            "WHERE t.assignee.id = :assigneeId " +
            "AND t.status NOT IN :excludedStatuses")
    List<Task> findByAssignee_IdAndStatusNotInWithDetails(
            @Param("assigneeId") Integer assigneeId,
            @Param("excludedStatuses") Collection<String> excludedStatuses
    );

}