// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/TaskSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority; 
import com.quanlyduan.project_manager_api.model.common.enums.TaskType; 
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

public class TaskSpecification {

    /**
     * Bộ lọc nâng cấp cho Backlog (Hỗ trợ tìm kiếm theo trường tùy chọn).
     */
    public static Specification<Task> filterBacklog(
            Integer projectId,
            Integer sprintId,
            boolean isBacklog,
            String keyword,      // Tìm chung (Title hoặc Code)
            Integer assigneeId,  // Tìm theo người làm
            TaskPriority priority, // Tìm theo độ ưu tiên 
            TaskType taskType      // Tìm theo loại task 
    ) {
        // 1. Bắt buộc thuộc Project
        Specification<Task> spec = (root, query, cb) -> 
                cb.equal(root.get("project").get("id"), projectId);

        // 2. Lọc theo Sprint hoặc Backlog
        if (isBacklog) {
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("sprint")));
        } else if (sprintId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sprint").get("id"), sprintId));
        }

        // 3. Tìm kiếm từ khóa chung (Vẫn giữ để UX tiện lợi)
        if (keyword != null && !keyword.isEmpty()) {
             Specification<Task> titleSpec = JpaSpecificationUtil.attributeContains("title", keyword);
             Specification<Task> codeSpec = JpaSpecificationUtil.attributeContains("taskCode", keyword);
             spec = spec.and(titleSpec.or(codeSpec));
        }

        // 4. Lọc theo người làm
        if (assigneeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId));
        }

        // 5. Lọc theo Priority 
        if (priority != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("priority", priority));
        }

        // 6. Lọc theo TaskType 
        if (taskType != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("taskType", taskType));
        }

        return spec;
    }
        public static Specification<Task> filterTasks(
            Integer projectId,
            Integer sprintId, // null=all, 0=backlog, >0=specific sprint
            String searchTitle,
            Integer assigneeId,
            String priority,
            List<String> statusNames // (US 11 - Lọc theo nhiều trạng thái)
    ) {
        // 1. Bắt buộc: Task phải thuộc Project này
        Specification<Task> spec = (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);

        // 2. Lọc theo Sprint
        if (sprintId != null) {
            if (sprintId == 0) { 
                // Sprint ID = 0 nghĩa là Backlog (chưa vào sprint nào)
                spec = spec.and((root, query, cb) -> cb.isNull(root.get("sprint")));
            } else {
                // Tìm trong Sprint cụ thể
                spec = spec.and((root, query, cb) -> cb.equal(root.get("sprint").get("id"), sprintId));
            }
        }

        // 3. Tìm kiếm theo Tiêu đề (Title) - US 8
        if (searchTitle != null && !searchTitle.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("title", searchTitle));
        }

        // 4. Lọc theo Người được giao (Assignee) - US 4, 8
        if (assigneeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId));
        }

        // 5. Lọc theo Độ ưu tiên (Priority) - US 4, 8
        if (priority != null && !priority.isEmpty()) {
            try {
                Priority priorityEnum = Priority.valueOf(priority.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priorityEnum));
            } catch (IllegalArgumentException e) {
                // Bỏ qua nếu priority string không hợp lệ
            }
        }
        
        // 6. Lọc theo Danh sách Trạng thái (Status Names) - US 11
        // (Ví dụ: Lọc lấy các task đang "TO_DO" hoặc "IN_PROGRESS")
        if (statusNames != null && !statusNames.isEmpty()) {
             spec = spec.and((root, query, cb) -> root.get("status").get("name").in(statusNames));
        }

        return spec;
    }

}