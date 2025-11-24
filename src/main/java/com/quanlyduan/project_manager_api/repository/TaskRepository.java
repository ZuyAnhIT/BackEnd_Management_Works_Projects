package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer>, JpaSpecificationExecutor<Task> {

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

    // US4-sprnt3: Lấy task được gán cho user, ngoại trừ các status đã hoàn thành.
    @Query("SELECT t FROM Task t " +
            "JOIN FETCH t.project p " +
            "JOIN FETCH p.workspace w " +
            "WHERE t.assignee.id = :assigneeId " +
            "AND t.status NOT IN :excludedStatuses")
    List<Task> findByAssignee_IdAndStatusNotInWithDetails(
            @Param("assigneeId") Integer assigneeId,
            @Param("excludedStatuses") Collection<String> excludedStatuses
    );

    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.assignee " + 
           "LEFT JOIN FETCH t.epic " +
           "LEFT JOIN FETCH t.status " + 
           "WHERE t.project.id = :projectId " +
           "ORDER BY t.sprint.id ASC NULLS FIRST, t.sortOrder ASC")
    List<Task> findByProjectIdWithDetails(Integer projectId);
    
    long countByProjectId(Integer projectId);

    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.assignee " +
           "LEFT JOIN FETCH t.epic " +
           "LEFT JOIN FETCH t.status " + 
           "WHERE t.sprint.id = :sprintId " +
           "ORDER BY t.sortOrder ASC")
    List<Task> findBySprintIdWithDetails(Integer sprintId);

    /**
     * Lấy tất cả task được gán cho một user,
     * kèm theo chi tiết (project, workspace, status)
     */
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.project p " +
           "JOIN FETCH p.workspace w " +
           "LEFT JOIN FETCH t.status s " +
           "WHERE t.assignee.id = :assigneeId " +
           "ORDER BY t.dueDate ASC")
    List<Task> findByAssignee_IdWithDetails(@Param("assigneeId") Integer assigneeId);

    /**
     * Kiểm tra xem có bất kỳ task nào đang ở trạng thái này không.
     * Dùng để chặn việc xóa Status đang có dữ liệu.
     */
    boolean existsByStatus_Id(Integer statusId);

    /**
     * Lấy danh sách Task thuộc Backlog (sprint_id IS NULL) của dự án.
     * Sắp xếp theo sortOrder.
     */
    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.assignee " + 
           "LEFT JOIN FETCH t.epic " +
           "LEFT JOIN FETCH t.status " +
           "WHERE t.project.id = :projectId AND t.sprint IS NULL " +
           "ORDER BY t.sortOrder ASC")
    List<Task> findBacklogTasksByProjectId(Integer projectId);


    // CÁC HÀM HỖ TRỢ KÉO THẢ (SORT ORDER)
    // 1. Tìm vị trí lớn nhất trong Sprint (để thêm vào cuối)
    @Query("SELECT COALESCE(MAX(t.sortOrder), 0) FROM Task t WHERE t.sprint.id = :sprintId")
    Integer findMaxSortOrderBySprintId(@Param("sprintId") Integer sprintId);

    // 2. Tìm vị trí lớn nhất trong Backlog (project nhưng sprint null)
    @Query("SELECT COALESCE(MAX(t.sortOrder), 0) FROM Task t WHERE t.project.id = :projectId AND t.sprint IS NULL")
    Integer findMaxSortOrderByProjectIdAndSprintIsNull(@Param("projectId") Integer projectId);

    // 3. Đẩy các task phía sau xuống 1 bậc (Trong Sprint)
    @Modifying
    @Query("UPDATE Task t SET t.sortOrder = t.sortOrder + 1 WHERE t.sprint.id = :sprintId AND t.sortOrder >= :newSortOrder")
    void shiftSortOrderInSprint(@Param("sprintId") Integer sprintId, @Param("newSortOrder") Integer newSortOrder);

    // 4. Đẩy các task phía sau xuống 1 bậc (Trong Backlog)
    @Modifying
    @Query("UPDATE Task t SET t.sortOrder = t.sortOrder + 1 WHERE t.project.id = :projectId AND t.sprint IS NULL AND t.sortOrder >= :newSortOrder")
    void shiftSortOrderInBacklog(@Param("projectId") Integer projectId, @Param("newSortOrder") Integer newSortOrder);

    
    /**
     * Đếm số task trong một sprint.
     */
    long countBySprint_Id(Integer sprintId);

    // Để di chuyển nhanh task về Backlog
    @Modifying
    @Query("UPDATE Task t SET t.sprint = NULL WHERE t.sprint.id = :sprintId")
    void moveTasksToBacklogBySprintId(@Param("sprintId") Integer sprintId);

    /**
     * Tìm các Task trong Sprint mà chưa hoàn thành (isCompletedStatus = false hoặc null).
     */
    @Query("SELECT t FROM Task t " +
           "WHERE t.sprint.id = :sprintId " +
           "AND (t.status IS NULL OR t.status.isCompletedStatus = false)")
    List<Task> findIncompleteTasksBySprintId(@Param("sprintId") Integer sprintId);
}