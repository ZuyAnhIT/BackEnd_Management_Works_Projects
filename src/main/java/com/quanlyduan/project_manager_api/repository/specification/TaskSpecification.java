// File: src/main/java/com.quanlyduan.project_manager_api/repository/specification/TaskSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority; 
import com.quanlyduan.project_manager_api.model.common.enums.TaskType; 
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

public class TaskSpecification {

    /**
     * Bộ lọc TỔNG QUÁT (MASTER FILTER) cho Task.
     * Hàm này được sử dụng cho cả màn hình Backlog, Board và các API tìm kiếm Task.
     * * @param projectId (Bắt buộc) ID dự án
     * @param sprintId (Tùy chọn) ID Sprint cụ thể.
     * @param isBacklog (Tùy chọn) Nếu true, Task phải có Sprint IS NULL.
     * @param keyword (Tùy chọn) Tìm kiếm chung (Title hoặc Code).
     * @param assigneeId (Tùy chọn) Tìm theo người được giao.
     * @param priority (Tùy chọn) Tìm theo độ ưu tiên (Enum).
     * @param taskType (Tùy chọn) Tìm theo loại task (Enum).
     * @param statusIds (Tùy chọn) Danh sách ID trạng thái để lọc (Dùng cho List View).
     */
    public static Specification<Task> filterTasks(
            Integer projectId,
            Integer sprintId,
            boolean isBacklog,
            String keyword,
            Integer assigneeId,
            TaskPriority priority,
            TaskType taskType,
            List<Integer> statusIds 
    ) {
        // 1. ĐIỀU KIỆN BẮT BUỘC: Thuộc Project
        Specification<Task> spec = (root, query, cb) -> 
                cb.equal(root.get("project").get("id"), projectId);

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
        // Áp dụng khi người dùng chọn lọc nhiều trạng thái cùng lúc (ví dụ: Board List View)
        if (statusIds != null && !statusIds.isEmpty()) {
            // Lọc: Task.status.id IN (statusIds)
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
            TaskType taskType
    ) {
        // Gọi hàm tổng quát filterTasks (8 tham số) với statusIds = null
        return filterTasks(projectId, sprintId, isBacklog, keyword, assigneeId, priority, taskType, null);
    }
}