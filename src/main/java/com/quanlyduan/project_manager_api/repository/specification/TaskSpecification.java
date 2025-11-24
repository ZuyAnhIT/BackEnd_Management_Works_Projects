// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/TaskSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecification {

    /**
     * Bộ lọc cho màn hình Backlog.
     * @param projectId Bắt buộc.
     * @param isBacklog Nếu true -> Tìm task chưa có Sprint. Nếu false -> Tìm task trong sprintId cụ thể.
     */
    public static Specification<Task> filterBacklog(
            Integer projectId,
            Integer sprintId, // Nếu isBacklog=true thì cái này bị bỏ qua
            boolean isBacklog,
            String keyword,
            Integer assigneeId
    ) {
        // 1. Bắt buộc thuộc Project
        Specification<Task> spec = (root, query, cb) -> 
                cb.equal(root.get("project").get("id"), projectId);

        // 2. Lọc theo Sprint hoặc Backlog
        if (isBacklog) {
            // Lấy task nằm trong Backlog (sprint_id IS NULL)
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("sprint")));
        } else if (sprintId != null) {
            // Lấy task trong một Sprint cụ thể
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sprint").get("id"), sprintId));
        }

        // 3. Tìm kiếm từ khóa (Tên, Code)
        if (keyword != null && !keyword.isEmpty()) {
             Specification<Task> titleSpec = JpaSpecificationUtil.attributeContains("title", keyword);
             Specification<Task> codeSpec = JpaSpecificationUtil.attributeContains("taskCode", keyword);
             spec = spec.and(titleSpec.or(codeSpec));
        }

        // 4. Lọc theo người làm
        if (assigneeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId));
        }

        return spec;
    }
}