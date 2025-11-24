// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/TaskSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority; 
import com.quanlyduan.project_manager_api.model.common.enums.TaskType; 
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
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

        // 5. Lọc theo Priority ***
        if (priority != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("priority", priority));
        }

        // 6. Lọc theo TaskType ***
        if (taskType != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("taskType", taskType));
        }

        return spec;
    }
}