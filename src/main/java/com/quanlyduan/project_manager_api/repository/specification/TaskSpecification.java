// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/TaskSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

public class TaskSpecification {

    // ========================================================================
    // 1. BỘ LỌC TỔNG QUÁT (MASTER FILTER)
    // Dùng cho: Board, Backlog, List View, Search
    // ========================================================================
    /**
     * @param projectId  (Bắt buộc) ID dự án
     * @param sprintId   (Tùy chọn) ID Sprint cụ thể.
     * @param isBacklog  (Tùy chọn) Nếu true, Task phải có Sprint IS NULL.
     * @param keyword    (Tùy chọn) Tìm kiếm chung (Title hoặc Code).
     * @param assigneeId (Tùy chọn) Tìm theo người được giao.
     * @param priority   (Tùy chọn) Tìm theo độ ưu tiên (Enum).
     * @param taskType   (Tùy chọn) Tìm theo loại task (Enum).
     * @param statusIds  (Tùy chọn) Danh sách ID trạng thái để lọc (Dùng cho List
     *                   View).
     */
    public static Specification<Task> filterTasks(
            Integer projectId,
            Integer sprintId,
            boolean isBacklog,
            String keyword,
            Integer assigneeId,
            TaskPriority priority,
            TaskType taskType,
            List<Integer> statusIds) {
        // 1. ĐIỀU KIỆN BẮT BUỘC: Thuộc Project
        Specification<Task> spec = (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);

        // 2. LỌC THEO SPRINT HOẶC BACKLOG
        if (isBacklog) {
            // Lấy task nằm trong Backlog (sprint_id IS NULL)
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("sprint")));
        } else if (sprintId != null && sprintId > 0) {
            // Lấy task trong Sprint cụ thể
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sprint").get("id"), sprintId));
        }

        // 3. TÌM KIẾM TỪ KHÓA (Title OR Code)
        if (keyword != null && !keyword.trim().isEmpty()) {
            Specification<Task> titleSpec = JpaSpecificationUtil.attributeContains("title", keyword);
            Specification<Task> codeSpec = JpaSpecificationUtil.attributeContains("taskCode", keyword);
            // Kết hợp bằng OR
            spec = spec.and(titleSpec.or(codeSpec));
        }

        // 4. LỌC THEO NGƯỜI LÀM (Assignee ID)
        if (assigneeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId));
        }

        // 5. LỌC THEO PRIORITY (Enum)
        if (priority != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("priority", priority));
        }

        // 6. LỌC THEO TASK TYPE (Enum)
        if (taskType != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("taskType", taskType));
        }

        // 7. LỌC THEO DANH SÁCH STATUS ID (IN Clause)
        if (statusIds != null && !statusIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").get("id").in(statusIds));
        }

        return spec;
    }

    /**
     * (Wrapper) Hàm tương thích ngược/tiện ích cho logic Backlog.
     * Hàm này chỉ gọi hàm tổng quát 'filterTasks' và gán statusIds = null.
     */
    public static Specification<Task> filterBacklog(
            Integer projectId,
            Integer sprintId,
            boolean isBacklog,
            String keyword,
            Integer assigneeId,
            TaskPriority priority,
            TaskType taskType) {
        return filterTasks(projectId, sprintId, isBacklog, keyword, assigneeId, priority, taskType, null);
    }

    // ========================================================================
    // 2. BỘ LỌC LỊCH (CALENDAR FILTER) - MỚI
    // Dùng cho: Calendar View (Tìm task trong khoảng thời gian)
    // ========================================================================
    /**
     * @param projectId ID dự án
     * @param viewStart Ngày bắt đầu của view lịch (VD: 01/10)
     * @param viewEnd   Ngày kết thúc của view lịch (VD: 31/10)
     * @param keyword,  assigneeId... Các filter phụ
     */
    public static Specification<Task> filterTasksForCalendar(
            Integer projectId,
            LocalDate viewStart, LocalDate viewEnd,
            String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter theo Project (Bắt buộc)
            predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));

            // 2. Filter theo THỜI GIAN (QUAN TRỌNG)
            // Logic: Task hiển thị nếu khoảng thời gian của nó GIAO với [viewStart,
            // viewEnd]
            // Công thức Range Overlap: (TaskStart <= ViewEnd) AND (TaskEnd >= ViewStart)

            // Ở đây ta dùng 'startDate' và 'dueDate' của Task
            // Nếu startDate null, có thể thay thế bằng createdAt hoặc bỏ qua
            // Nếu dueDate null, task đó có thể hiển thị ở ngày start hoặc không hiển thị

            if (viewStart != null && viewEnd != null) {
                // Điều kiện 1: Task bắt đầu trước khi view kết thúc (startDate <= viewEnd)
                // Dùng coalesce để xử lý null: nếu startDate null thì dùng createdAt
                Predicate startCondition = criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.coalesce(root.get("startDate"), root.get("createdAt").as(LocalDate.class)),
                        viewEnd);

                // Điều kiện 2: Task kết thúc sau khi view bắt đầu (dueDate >= viewStart)
                // Nếu dueDate null (task vô hạn), ta coi như nó thỏa mãn (luôn >= viewStart)
                // Hoặc tùy logic, ở đây giả sử dueDate null thì chỉ check startDate
                Predicate endCondition = criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("dueDate")),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), viewStart));

                predicates.add(criteriaBuilder.and(startCondition, endCondition));
            }

            // 3. Các filter phụ (Copy logic từ filterTasks)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("taskCode")), likePattern)));
            }
            if (assigneeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId));
            }
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }
            if (taskType != null) {
                predicates.add(criteriaBuilder.equal(root.get("taskType"), taskType));
            }

            // 4. (Tùy chọn) Loại bỏ các task đã xóa mềm hoặc Archived nếu cần
            // predicates.add(criteriaBuilder.notEqual(root.get("status").get("name"),
            // "Archived"));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ======================================================
    // 3. CÁC BỘ LỌC THỐNG KÊ (STATISTICS FILTERS) - MỚI
    // ======================================================

    public static Specification<Task> filterBase(
            Integer projectId, Integer assigneeId,
            String keyword, TaskPriority priority, TaskType taskType, List<Integer> statusIds
    ) {
        return filterTasks(projectId, null, false, keyword, assigneeId, priority, taskType, statusIds);
    }

    /**
     * Lọc theo ngày TẠO (Created At) trong khoảng thời gian chính xác.
     */
    public static Specification<Task> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null || to == null) return null;
            return cb.between(root.get("createdAt"), from, to);
        };
    }

    /**
     * Lọc theo ngày HOÀN THÀNH (Completed At).
     */
    public static Specification<Task> completedBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null || to == null) return null;
            Predicate time = cb.between(root.get("completedAt"), from, to);
            Predicate isDone = cb.isNotNull(root.get("completedAt"));
            return cb.and(time, isDone);
        };
    }

    /**
     * Lọc theo ngày CẬP NHẬT (Updated At).
     */
    public static Specification<Task> updatedBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null || to == null) return null;
            return cb.between(root.get("updatedAt"), from, to);
        };
    }

    /**
     * Lọc Task SẮP ĐẾN HẠN (Due Date) và CHƯA HOÀN THÀNH.
     */
    public static Specification<Task> dueBetweenAndNotDone(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null || to == null) return null;

            // 1. Hạn chót trong khoảng (dueDate là LocalDateTime trong DB)
            Predicate time = cb.between(root.get("dueDate"), from, to);

            // 2. Chưa hoàn thành: Status null HOẶC isCompletedStatus = false
            Predicate statusNull = cb.isNull(root.get("status"));
            
            // Lưu ý: Cần join bảng status để check flag isCompletedStatus
            // Nếu dùng root.get("status").get(...) có thể gây lỗi nếu status null
            // Cách an toàn nhất là dùng OR
            
            // Cách đơn giản: Nếu status != null thì check flag
            Predicate notCompleted = cb.or(
                statusNull,
                cb.equal(root.get("status").get("isCompletedStatus"), false)
            );

            return cb.and(time, notCompleted);
        };
    }
}