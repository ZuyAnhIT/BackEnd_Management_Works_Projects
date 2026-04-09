package com.quanlyduan.project_manager_api.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.Task;

/**
 * Kho lưu trữ dữ liệu quản lý công việc (Task).
 * Hỗ trợ các truy vấn phức tạp cho bảng Board, Backlog, lịch trình và phân tích AI.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Integer>, JpaSpecificationExecutor<Task> {

    // ======================================================
    // KHAI BÁO HẰNG SỐ TRUY VẤN (QUERY CONSTANTS)
    // ======================================================

    String UPDATE_SPRINT_FOR_TASKS = "UPDATE Task t SET t.sprint = :sprint WHERE t.id IN :taskIds";
    
    String MOVE_TASKS_TO_BACKLOG = "UPDATE Task t SET t.sprint = NULL WHERE t.sprint.id = :sprintId";

    String FIND_BY_ASSIGNEE_WITH_DETAILS = "SELECT t FROM Task t JOIN FETCH t.project p JOIN FETCH p.workspace w " +
                                           "WHERE t.assignee.id = :assigneeId AND t.status NOT IN :excludedStatuses";

    String FIND_PROJECT_TASKS_ORDERED = "SELECT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.epic " +
                                        "LEFT JOIN FETCH t.status WHERE t.project.id = :projectId " +
                                        "ORDER BY t.sprint.id ASC NULLS FIRST, t.sortOrder ASC";

    String FIND_DASHBOARD_TASKS = "SELECT t FROM Task t JOIN FETCH t.project p JOIN FETCH p.workspace w " +
                                  "LEFT JOIN FETCH t.status s WHERE t.assignee.id = :assigneeId ORDER BY t.dueDate ASC";

    String FIND_SPRINT_TASKS_WITH_DETAILS = "SELECT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.epic " +
                                            "LEFT JOIN FETCH t.status WHERE t.sprint.id = :sprintId ORDER BY t.sortOrder ASC";

    String FIND_BACKLOG_TASKS = "SELECT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.epic " +
                                "LEFT JOIN FETCH t.status WHERE t.project.id = :projectId AND t.sprint IS NULL " +
                                "ORDER BY t.sortOrder ASC";

    String FIND_INCOMPLETE_SPRINT_TASKS = "SELECT t FROM Task t WHERE t.sprint.id = :sprintId " +
                                          "AND (t.status IS NULL OR t.status.isCompletedStatus = false)";

    String MAX_SORT_ORDER_SPRINT = "SELECT COALESCE(MAX(t.sortOrder), 0) FROM Task t WHERE t.sprint.id = :sprintId";

    String MAX_SORT_ORDER_BACKLOG = "SELECT COALESCE(MAX(t.sortOrder), 0) FROM Task t WHERE t.project.id = :projectId AND t.sprint IS NULL";

    String SHIFT_SORT_ORDER_SPRINT = "UPDATE Task t SET t.sortOrder = t.sortOrder + 1 " +
                                     "WHERE t.sprint.id = :sprintId AND t.sortOrder >= :newSortOrder";

    String SHIFT_SORT_ORDER_BACKLOG = "UPDATE Task t SET t.sortOrder = t.sortOrder + 1 " +
                                      "WHERE t.project.id = :projectId AND t.sprint IS NULL AND t.sortOrder >= :newSortOrder";

    String MAX_SORT_ORDER_STATUS = "SELECT COALESCE(MAX(t.sortOrder), 0) FROM Task t WHERE t.project.id = :projectId AND t.status.id = :statusId";

    String SHIFT_SORT_ORDER_STATUS = "UPDATE Task t SET t.sortOrder = t.sortOrder + 1 " +
                                     "WHERE t.project.id = :projectId AND t.status.id = :statusId AND t.sortOrder >= :newSortOrder";

    String COUNT_CREATED_TASKS = "SELECT COUNT(t) FROM Task t WHERE (:projectId IS NULL OR t.project.id = :projectId) " +
                                 "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND t.createdAt BETWEEN :startDate AND :endDate";

    String COUNT_COMPLETED_TASKS = "SELECT COUNT(t) FROM Task t WHERE (:projectId IS NULL OR t.project.id = :projectId) " +
                                   "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND t.completedAt BETWEEN :startDate AND :endDate";

    String COUNT_UPDATED_TASKS = "SELECT COUNT(t) FROM Task t WHERE (:projectId IS NULL OR t.project.id = :projectId) " +
                                 "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND t.updatedAt BETWEEN :startDate AND :endDate";

    String FIND_TASKS_DUE_SOON = "SELECT t FROM Task t JOIN FETCH t.project p LEFT JOIN FETCH t.status s " +
                                 "WHERE (:projectId IS NULL OR t.project.id = :projectId) AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
                                 "AND t.dueDate BETWEEN :now AND :futureDate AND (t.status.isCompletedStatus = false OR t.status IS NULL) " +
                                 "ORDER BY t.dueDate ASC";

    String FIND_CREATED_TASKS_LIST = "SELECT t FROM Task t JOIN FETCH t.project p LEFT JOIN FETCH t.status s LEFT JOIN FETCH t.assignee a " +
                                     "WHERE (:projectId IS NULL OR t.project.id = :projectId) AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
                                     "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC";

    String FIND_COMPLETED_TASKS_LIST = "SELECT t FROM Task t JOIN FETCH t.project p LEFT JOIN FETCH t.status s LEFT JOIN FETCH t.assignee a " +
                                       "WHERE (:projectId IS NULL OR t.project.id = :projectId) AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
                                       "AND t.completedAt BETWEEN :startDate AND :endDate ORDER BY t.completedAt DESC";

    String FIND_UPDATED_TASKS_LIST = "SELECT t FROM Task t JOIN FETCH t.project p LEFT JOIN FETCH t.status s LEFT JOIN FETCH t.assignee a " +
                                     "WHERE (:projectId IS NULL OR t.project.id = :projectId) AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
                                     "AND t.updatedAt BETWEEN :startDate AND :endDate ORDER BY t.updatedAt DESC";

    String COUNT_BY_STATUS_GROUP = "SELECT t.status, COUNT(t) FROM Task t WHERE (:projectId IS NULL OR t.project.id = :projectId) " +
                                   "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) GROUP BY t.status";

    String COUNT_BY_PRIORITY_GROUP = "SELECT t.priority, COUNT(t) FROM Task t WHERE (:projectId IS NULL OR t.project.id = :projectId) " +
                                     "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) GROUP BY t.priority";

    String COUNT_BY_TYPE_GROUP = "SELECT t.taskType, COUNT(t) FROM Task t WHERE (:projectId IS NULL OR t.project.id = :projectId) " +
                                 "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) GROUP BY t.taskType";

    String COUNT_COMPLETED_BY_KEYWORD = "SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId AND t.project.id = :projectId " +
                                        "AND t.status.isCompletedStatus = true AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                                        "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))";

    String CURRENT_SPRINT_WORKLOAD = "SELECT COALESCE(SUM(t.storyPoints), 0) FROM Task t WHERE t.assignee.id = :userId " +
                                     "AND t.project.id = :projectId AND t.sprint.status = 'IN_PROGRESS' " +
                                     "AND (t.status.isCompletedStatus = false OR t.status.isCompletedStatus IS NULL)";

    String SUM_COMPLETED_POINTS_SPRINT = "SELECT COALESCE(SUM(t.storyPoints), 0) FROM Task t WHERE t.sprint.id = :sprintId AND t.status.isCompletedStatus = true";

    String SUM_REMAINING_POINTS_PROJECT = "SELECT COALESCE(SUM(t.storyPoints), 0) FROM Task t WHERE t.project.id = :projectId " +
                                          "AND (t.status.isCompletedStatus = false OR t.status.isCompletedStatus IS NULL)";

    // ======================================================
    // 1. TRUY VẤN CHI TIẾT (FETCH JOIN / DETAILS)
    // ======================================================

    /**
     * Lấy danh sách công việc của người được gán kèm chi tiết Dự án và Không gian làm việc.
     */
    @Query(FIND_BY_ASSIGNEE_WITH_DETAILS)
    List<Task> findByAssignee_IdAndStatusNotInWithDetails(@Param("assigneeId") Integer assigneeId, @Param("excludedStatuses") Collection<String> excludedStatuses);

    /**
     * Lấy toàn bộ công việc trong dự án, ưu tiên Backlog lên đầu và sắp xếp theo thứ tự hiển thị.
     */
    @Query(FIND_PROJECT_TASKS_ORDERED)
    List<Task> findByProjectIdWithDetails(Integer projectId);

    /**
     * Lấy danh sách công việc cho Dashboard người dùng, sắp xếp theo hạn chót.
     */
    @Query(FIND_DASHBOARD_TASKS)
    List<Task> findByAssignee_IdWithDetails(@Param("assigneeId") Integer assigneeId);

    /**
     * Lấy danh sách công việc thuộc về một Sprint cụ thể kèm thông tin chi tiết.
     */
    @Query(FIND_SPRINT_TASKS_WITH_DETAILS)
    List<Task> findBySprintIdWithDetails(Integer sprintId);

    /**
     * Lấy danh sách công việc chưa hoàn thành trong một Sprint.
     */
    @Query(FIND_INCOMPLETE_SPRINT_TASKS)
    List<Task> findIncompleteTasksBySprintId(@Param("sprintId") Integer sprintId);

    /**
     * Lấy chi tiết công việc dựa trên ID và ID dự án.
     */
    Optional<Task> findByIdAndProject_Id(Integer taskId, Integer projectId);

    // ======================================================
    // 2. QUẢN LÝ SPRINT & BACKLOG (SPRINT LOGIC)
    // ======================================================

    /**
     * Lấy danh sách công việc trong Backlog của dự án (chưa gán Sprint).
     */
    @Query(FIND_BACKLOG_TASKS)
    List<Task> findBacklogTasksByProjectId(Integer projectId);

    /**
     * Cập nhật Sprint cho danh sách công việc.
     */
    @Modifying
    @Query(UPDATE_SPRINT_FOR_TASKS)
    void updateSprintForTasks(@Param("sprint") Sprint sprint, @Param("taskIds") List<Integer> taskIds);

    /**
     * Di chuyển toàn bộ công việc từ Sprint về Backlog.
     */
    @Modifying
    @Query(MOVE_TASKS_TO_BACKLOG)
    void moveTasksToBacklogBySprintId(@Param("sprintId") Integer sprintId);

    List<Task> findByProject_IdAndSprint_IdIsNullOrderBySortOrderAsc(Integer projectId);

    List<Task> findBySprint_IdOrderBySortOrderAsc(Integer sprintId);

    List<Task> findBySprint_Id(Integer sprintId);

    List<Task> findByAssignee_IdAndIsArchivedFalseOrderByDueDateAsc(Integer assigneeId);

    

    // ======================================================
    // 3. LOGIC KÉO THẢ & THỨ TỰ (SORT ORDER)
    // ======================================================

    @Query(MAX_SORT_ORDER_SPRINT)
    Integer findMaxSortOrderBySprintId(@Param("sprintId") Integer sprintId);

    @Query(MAX_SORT_ORDER_BACKLOG)
    Integer findMaxSortOrderByProjectIdAndSprintIsNull(@Param("projectId") Integer projectId);

    @Modifying
    @Query(SHIFT_SORT_ORDER_SPRINT)
    void shiftSortOrderInSprint(@Param("sprintId") Integer sprintId, @Param("newSortOrder") Integer newSortOrder);

    @Modifying
    @Query(SHIFT_SORT_ORDER_BACKLOG)
    void shiftSortOrderInBacklog(@Param("projectId") Integer projectId, @Param("newSortOrder") Integer newSortOrder);

    @Query(MAX_SORT_ORDER_STATUS)
    Integer findMaxSortOrderByStatusId(@Param("projectId") Integer projectId, @Param("statusId") Integer statusId);

    @Modifying
    @Query(SHIFT_SORT_ORDER_STATUS)
    void shiftSortOrderInStatus(@Param("projectId") Integer projectId, @Param("statusId") Integer statusId, @Param("newSortOrder") Integer newSortOrder);

    // ======================================================
    // 4. THỐNG KÊ & LỊCH TRÌNH (CALENDAR & STATS)
    // ======================================================

    @Query(COUNT_CREATED_TASKS)
    long countCreatedTasks(@Param("projectId") Integer projectId, @Param("assigneeId") Integer assigneeId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(COUNT_COMPLETED_TASKS)
    long countCompletedTasks(@Param("projectId") Integer projectId, @Param("assigneeId") Integer assigneeId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(COUNT_UPDATED_TASKS)
    long countUpdatedTasks(@Param("projectId") Integer projectId, @Param("assigneeId") Integer assigneeId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(FIND_TASKS_DUE_SOON)
    List<Task> findTasksDueSoon(@Param("projectId") Integer projectId, @Param("assigneeId") Integer assigneeId, @Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);

    @Query(FIND_CREATED_TASKS_LIST)
    List<Task> findCreatedTasks(@Param("projectId") Integer projectId, @Param("assigneeId") Integer assigneeId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query(FIND_COMPLETED_TASKS_LIST)
    List<Task> findCompletedTasks(@Param("projectId") Integer projectId, @Param("assigneeId") Integer assigneeId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query(FIND_UPDATED_TASKS_LIST)
    List<Task> findUpdatedTasks(@Param("projectId") Integer projectId, @Param("assigneeId") Integer assigneeId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query(COUNT_BY_STATUS_GROUP)
    List<Object[]> countTasksByStatusGroup(@Param("projectId") Integer projectId, @Param("assigneeId") Integer assigneeId);

    @Query(COUNT_BY_PRIORITY_GROUP)
    List<Object[]> countTasksByPriorityGroup(@Param("projectId") Integer projectId, @Param("assigneeId") Integer assigneeId);

    @Query(COUNT_BY_TYPE_GROUP)
    List<Object[]> countTasksByTypeGroup(@Param("projectId") Integer projectId, @Param("assigneeId") Integer assigneeId);

    // ======================================================
    // 5. PHÂN TÍCH AI & STORY POINTS (AI ANALYTICS)
    // ======================================================

    @Query(COUNT_COMPLETED_BY_KEYWORD)
    int countCompletedTasksByKeyword(@Param("projectId") Integer projectId, @Param("userId") Integer userId, @Param("keyword") String keyword);

    @Query(CURRENT_SPRINT_WORKLOAD)
    int getCurrentSprintWorkload(@Param("projectId") Integer projectId, @Param("userId") Integer userId);

    @Query(SUM_COMPLETED_POINTS_SPRINT)
    Integer sumCompletedPointsBySprintId(@Param("sprintId") Integer sprintId);

    @Query(SUM_REMAINING_POINTS_PROJECT)
    Integer sumRemainingPoints(@Param("projectId") Integer projectId);

    // ======================================================
    // 6. KIỂM TRA & KIỂM TOÁN (VALIDATION & AUDIT)
    // ======================================================

    boolean existsByProject_IdAndTaskCode(Integer projectId, String taskCode);

    long countByProjectId(Integer projectId);

    boolean existsByStatus_Id(Integer statusId);

    boolean existsByEpic_Id(Integer epicId);

    List<Task> findByEpicId(Integer epicId);

    long countBySprint_Id(Integer sprintId);

    List<Task> findByAssignee_IdAndStatusNotIn(Integer assigneeId, List<String> excludedStatuses);

    String SEARCH_GLOBAL_TASKS = "SELECT t FROM Task t JOIN FETCH t.project p JOIN FETCH p.workspace w LEFT JOIN FETCH t.status s " + 
            "WHERE p.id IN (SELECT pm.project.id FROM ProjectMember pm WHERE pm.user.id = :userId) " +
            "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.taskCode) LIKE LOWER(CONCAT('%', :keyword, '%')))";

    /**
     * Search tasks by keyword ensuring the user is a member of the underlying project.
     */
    @Query(SEARCH_GLOBAL_TASKS)
    List<Task> searchTasksGlobal(@Param("userId") Integer userId, @Param("keyword") String keyword, Pageable pageable);

    String COUNT_NEW_TASKS_BY_DATE_RANGE = 
        "SELECT COUNT(t) FROM Task t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate";

    /**
     * Count the number of tasks created across the system within a specific time frame.
     */
    @Query(COUNT_NEW_TASKS_BY_DATE_RANGE)
    long countNewTasksByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                  @Param("endDate") java.time.LocalDateTime endDate);


    // ======================================================
    // 7. HỖ TRỢ SINH MÃ TỰ ĐỘNG (AUTO-GENERATE CODE)
    // ======================================================

    String FIND_MAX_TASK_SEQUENCE = 
        "SELECT MAX(CAST(SUBSTRING_INDEX(task_code, '-', -1) AS UNSIGNED)) " +
        "FROM tasks WHERE project_id = :projectId";

    /**
     * Tìm số thứ tự lớn nhất của Task Code trong một dự án.
     * Khắc phục triệt để lỗi trùng lặp khi dùng COUNT() do có Task bị xóa.
     */
    @Query(value = FIND_MAX_TASK_SEQUENCE, nativeQuery = true)
    Integer findMaxTaskSequenceByProjectId(@Param("projectId") Integer projectId);
}