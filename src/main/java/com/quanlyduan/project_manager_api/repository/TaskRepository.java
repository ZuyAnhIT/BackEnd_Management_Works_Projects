// File: src/main/java/com/quanlyduan/project_manager_api/repository/TaskRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Sprint; // Entity Sprint
import com.quanlyduan.project_manager_api.model.Task; // Entity Task

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Collection;

@Repository
/**
 * Repository cho Entity Task (Quản lý các công việc trong Dự án).
 * Kế thừa JpaSpecificationExecutor để hỗ trợ tìm kiếm động.
 */
public interface TaskRepository extends JpaRepository<Task, Integer>, JpaSpecificationExecutor<Task> {

    // --- PHƯƠNG THỨC TỰ ĐỘNG TỪ SPRING DATA JPA ---

    // Spring Data JPA tự động cung cấp 'findById(Integer taskId)'.
    // 'existsById(Integer taskId)' cũng được cung cấp.


    /**
     * Lấy Task được gán cho User, ngoại trừ các trạng thái đã hoàn thành (Dùng Enum/String).
     */
    List<Task> findByAssignee_IdAndStatusNotIn(Integer assigneeId, List<String> excludedStatuses);

    /**
     * Lấy backlog (task chưa có sprint) của một project, sắp xếp theo thứ tự.
     */
    List<Task> findByProject_IdAndSprint_IdIsNullOrderBySortOrderAsc(Integer projectId);

    /**
     * Lấy task trong một sprint, sắp xếp theo thứ tự.
     */
    List<Task> findBySprint_IdOrderBySortOrderAsc(Integer sprintId);

    // --- TRUY VẤN DÙNG @MODIFIYING ---

    /**
     * Gán nhiều Task (theo danh sách IDs) vào một Sprint cụ thể.
     */
    @Modifying
    @Query("UPDATE Task t SET t.sprint = :sprint WHERE t.id IN :taskIds")
    void updateSprintForTasks(@Param("sprint") Sprint sprint, @Param("taskIds") List<Integer> taskIds);

    /**
     * Di chuyển tất cả Task thuộc một Sprint về Backlog (sprint = NULL).
     * Dùng khi hủy hoặc hoàn thành Sprint.
     */
    @Modifying
    @Query("UPDATE Task t SET t.sprint = NULL WHERE t.sprint.id = :sprintId")
    void moveTasksToBacklogBySprintId(@Param("sprintId") Integer sprintId);


    // --- TRUY VẤN CHI TIẾT (DÙNG FETCH JOIN) ---

    /**
     * Lấy task được gán cho user, ngoại trừ các status đã hoàn thành, kèm theo chi tiết Project và Workspace.
     */
    @Query("SELECT t FROM Task t " +
            "JOIN FETCH t.project p " +
            "JOIN FETCH p.workspace w " +
            "WHERE t.assignee.id = :assigneeId " +
            "AND t.status NOT IN :excludedStatuses")
    List<Task> findByAssignee_IdAndStatusNotInWithDetails(
            @Param("assigneeId") Integer assigneeId,
            @Param("excludedStatuses") Collection<String> excludedStatuses
    );

    /**
     * Lấy tất cả Task của Project, sắp xếp theo Sprint (Backlog lên đầu) và SortOrder, kèm theo chi tiết.
     */
    @Query("SELECT t FROM Task t " +
            "LEFT JOIN FETCH t.assignee " +
            "LEFT JOIN FETCH t.epic " +
            "LEFT JOIN FETCH t.status " +
            "WHERE t.project.id = :projectId " +
            "ORDER BY t.sprint.id ASC NULLS FIRST, t.sortOrder ASC")
    List<Task> findByProjectIdWithDetails(Integer projectId);

    /**
     * Lấy tất cả task được gán cho user (cho Dashboard), kèm theo chi tiết.
     */
    @Query("SELECT t FROM Task t " +
            "JOIN FETCH t.project p " +
            "JOIN FETCH p.workspace w " +
            "LEFT JOIN FETCH t.status s " +
            "WHERE t.assignee.id = :assigneeId " +
            "ORDER BY t.dueDate ASC")
    List<Task> findByAssignee_IdWithDetails(@Param("assigneeId") Integer assigneeId);

    /**
     * Lấy tất cả Task trong Sprint, kèm theo chi tiết.
     */
    @Query("SELECT t FROM Task t " +
            "LEFT JOIN FETCH t.assignee " +
            "LEFT JOIN FETCH t.epic " +
            "LEFT JOIN FETCH t.status " +
            "WHERE t.sprint.id = :sprintId " +
            "ORDER BY t.sortOrder ASC")
    List<Task> findBySprintIdWithDetails(Integer sprintId);

    /**
     * Lấy danh sách Task thuộc Backlog (sprint_id IS NULL) của dự án, kèm theo chi tiết.
     */
    @Query("SELECT t FROM Task t " +
            "LEFT JOIN FETCH t.assignee " +
            "LEFT JOIN FETCH t.epic " +
            "LEFT JOIN FETCH t.status " +
            "WHERE t.project.id = :projectId AND t.sprint IS NULL " +
            "ORDER BY t.sortOrder ASC")
    List<Task> findBacklogTasksByProjectId(Integer projectId);

    /**
     * Tìm các Task trong Sprint mà chưa hoàn thành (status chưa có cờ completed=true).
     */
    @Query("SELECT t FROM Task t " +
            "WHERE t.sprint.id = :sprintId " +
            "AND (t.status IS NULL OR t.status.isCompletedStatus = false)")
    List<Task> findIncompleteTasksBySprintId(@Param("sprintId") Integer sprintId);





    // --- HÀM HỖ TRỢ AUDIT/VALIDATION ---

    /**
     * Đếm tổng số Task trong một Project.
     * Dùng để sinh mã Task Code (Ví dụ: PROJ-1).
     */
    long countByProjectId(Integer projectId);

    /**
     * Kiểm tra xem có bất kỳ Task nào đang ở Status này không (Chặn xóa Status).
     */
    boolean existsByStatus_Id(Integer statusId);

    /**
     * Kiểm tra xem có Task nào thuộc Epic này không (Chặn xóa Epic).
     */
    boolean existsByEpic_Id(Integer epicId);

    /**
     * Lấy danh sách Task thuộc Epic cụ thể.
     */
    List<Task> findByEpicId(Integer epicId);

    /**
     * Đếm số Task trong một Sprint.
     */
    long countBySprint_Id(Integer sprintId);


    // --- HÀM HỖ TRỢ KÉO THẢ (SORT ORDER) ---

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

    // 5. Tìm vị trí lớn nhất trong một Status của một Project (để thêm vào cuối)
    @Query("SELECT COALESCE(MAX(t.sortOrder), 0) FROM Task t WHERE t.project.id = :projectId AND t.status.id = :statusId")
    Integer findMaxSortOrderByStatusId(@Param("projectId") Integer projectId, @Param("statusId") Integer statusId);

    // 6. Đẩy các task phía sau xuống 1 bậc (Khi chèn vào giữa) trong cùng 1 cột
    @Modifying
    @Query("UPDATE Task t SET t.sortOrder = t.sortOrder + 1 WHERE t.project.id = :projectId AND t.status.id = :statusId AND t.sortOrder >= :newSortOrder")
    void shiftSortOrderInStatus(@Param("projectId") Integer projectId, @Param("statusId") Integer statusId, @Param("newSortOrder") Integer newSortOrder);


    // --- HÀM HỖ TRỢ TÍNH TOÁN (CALENDAR) ---

    // 1. Đếm Task được TẠO trong khoảng thời gian
    // Nếu projectId null -> đếm toàn bộ (cho ngữ cảnh cá nhân user)
    // Nếu assigneeId null -> đếm toàn bộ team
    @Query("SELECT COUNT(t) FROM Task t WHERE " +
           "(:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
           "t.createdAt BETWEEN :startDate AND :endDate")
    long countCreatedTasks(@Param("projectId") Integer projectId, 
                           @Param("assigneeId") Integer assigneeId,
                           @Param("startDate") LocalDateTime startDate, 
                           @Param("endDate") LocalDateTime endDate);

    // 2. Đếm Task HOÀN THÀNH (Dựa vào completedAt)
    @Query("SELECT COUNT(t) FROM Task t WHERE " +
           "(:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
           "t.completedAt BETWEEN :startDate AND :endDate")
    long countCompletedTasks(@Param("projectId") Integer projectId, 
                             @Param("assigneeId") Integer assigneeId,
                             @Param("startDate") LocalDateTime startDate, 
                             @Param("endDate") LocalDateTime endDate);

    // 3. Đếm Task CẬP NHẬT (updatedAt trong khoảng, và created != updated để tránh trùng lúc tạo)
    @Query("SELECT COUNT(t) FROM Task t WHERE " +
           "(:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
           "t.updatedAt BETWEEN :startDate AND :endDate")
    long countUpdatedTasks(@Param("projectId") Integer projectId, 
                           @Param("assigneeId") Integer assigneeId,
                           @Param("startDate") LocalDateTime startDate, 
                           @Param("endDate") LocalDateTime endDate);

    // 4. Tìm Task SẮP ĐẾN HẠN (DueDate trong tương lai gần & Chưa xong)
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.project p " + // Fetch để hiển thị chi tiết
           "LEFT JOIN FETCH t.status s " +
           "WHERE (:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
           "t.dueDate BETWEEN :now AND :futureDate AND " +
           "(t.status.isCompletedStatus = false OR t.status IS NULL) " +
           "ORDER BY t.dueDate ASC")
    List<Task> findTasksDueSoon(@Param("projectId") Integer projectId, 
                                @Param("assigneeId") Integer assigneeId,
                                @Param("now") LocalDateTime now, 
                                @Param("futureDate") LocalDateTime futureDate);


   // --- CÁC HÀM MỚI THỐNG KÊ ---

    // 1. Lấy danh sách Task TẠO trong khoảng thời gian
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.project p " +
           "LEFT JOIN FETCH t.status s " +
           "LEFT JOIN FETCH t.assignee a " +
           "WHERE (:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
           "t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC") // Mới nhất lên đầu
    List<Task> findCreatedTasks(@Param("projectId") Integer projectId,
                                @Param("assigneeId") Integer assigneeId,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable); // Dùng Pageable để giới hạn số lượng (Limit)

    // 2. Lấy danh sách Task HOÀN THÀNH
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.project p " +
           "LEFT JOIN FETCH t.status s " +
           "LEFT JOIN FETCH t.assignee a " +
           "WHERE (:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
           "t.completedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.completedAt DESC")
    List<Task> findCompletedTasks(@Param("projectId") Integer projectId,
                                  @Param("assigneeId") Integer assigneeId,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);

    // 3. Lấy danh sách Task CẬP NHẬT
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.project p " +
           "LEFT JOIN FETCH t.status s " +
           "LEFT JOIN FETCH t.assignee a " +
           "WHERE (:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
           "t.updatedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.updatedAt DESC")
    List<Task> findUpdatedTasks(@Param("projectId") Integer projectId,
                                @Param("assigneeId") Integer assigneeId,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable);


    /**
     * Thống kê số lượng Task theo Trạng thái (GROUP BY Status).
     * Kết quả trả về List<Object[]>: [ProjectStatus entity, Long count]
     */
    @Query("SELECT t.status, COUNT(t) " +
           "FROM Task t " +
           "WHERE (:projectId IS NULL OR t.project.id = :projectId) " +
           "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
           "GROUP BY t.status")
    List<Object[]> countTasksByStatusGroup(@Param("projectId") Integer projectId, 
                                           @Param("assigneeId") Integer assigneeId);
}