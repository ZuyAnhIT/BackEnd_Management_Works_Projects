// File: src/main/java/com/quanlyduan.project_manager_api/repository/specification/TaskSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority; 
import com.quanlyduan.project_manager_api.model.common.enums.TaskType; 
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

public class TaskSpecification {

    /**
     * Bộ lọc TỔNG QUÁT cho Task (Dùng cho cả Backlog và Board).
     * @param projectId (Bắt buộc) ID dự án
     * @param sprintId  (Tùy chọn) ID Sprint cụ thể.
     * @param isBacklog (Tùy chọn) Nếu true -> Task phải có Sprint IS NULL.
     * @param keyword   (Tùy chọn) Tìm kiếm theo Title hoặc Code.
     * @param assigneeId (Tùy chọn) Tìm theo người được giao.
     * @param priority  (Tùy chọn) Tìm theo độ ưu tiên (Enum).
     * @param taskType  (Tùy chọn) Tìm theo loại task (Enum).
     * @param statusIds (Tùy chọn) Danh sách ID trạng thái (dùng cho lọc Board).
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
        // 1. Bắt buộc thuộc Project
        Specification<Task> spec = (root, query, cb) -> 
                cb.equal(root.get("project").get("id"), projectId);

        // 2. Lọc theo Sprint hoặc Backlog
        if (isBacklog) {
            // Lấy task nằm trong Backlog (sprint_id IS NULL)
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("sprint")));
        } else if (sprintId != null && sprintId > 0) {
            // Lấy task trong Sprint cụ thể
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sprint").get("id"), sprintId));
        }

        // 3. Tìm kiếm từ khóa (Tên hoặc Mã Task)
        if (keyword != null && !keyword.trim().isEmpty()) {
             Specification<Task> titleSpec = JpaSpecificationUtil.attributeContains("title", keyword);
             Specification<Task> codeSpec = JpaSpecificationUtil.attributeContains("taskCode", keyword);
             spec = spec.and(titleSpec.or(codeSpec));
        }

        // 4. Lọc theo người làm
        if (assigneeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId));
        }

        // 5. Lọc theo Priority (Enum)
        if (priority != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("priority", priority));
        }

        // 6. Lọc theo TaskType (Enum)
        if (taskType != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("taskType", taskType));
        }
        
        // 7. Lọc theo Danh sách Status ID (Cho Board)
        if (statusIds != null && !statusIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").get("id").in(statusIds));
        }

        return spec;
    }
    
    /**
     * (Wrapper) Hàm tương thích ngược cho logic Backlog cũ.
     * Chỉ cần gọi hàm này, nó sẽ tự chuyển tiếp sang hàm filterTasks tổng quát (cung cấp 8 tham số).
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
        // Gọi hàm tổng quát với statusIds = null
        return filterTasks(projectId, sprintId, isBacklog, keyword, assigneeId, priority, taskType, null);
    }
}