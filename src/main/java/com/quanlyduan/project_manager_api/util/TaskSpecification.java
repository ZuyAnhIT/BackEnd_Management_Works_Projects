// File: src/main/java/com/quanlyduan/project_manager_api/util/TaskSpecification.java
package com.quanlyduan.project_manager_api.util;

import com.quanlyduan.project_manager_api.dto.request.TaskFilterRequest;
import com.quanlyduan.project_manager_api.model.Task;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng câu truy vấn động cho Task dựa trên các tiêu chí lọc từ TaskFilterRequest.
 */
@AllArgsConstructor
public class TaskSpecification implements Specification<Task> {

    private final TaskFilterRequest filter;

    @Override
    public Predicate toPredicate(Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // --- GHI CHÚ QUAN TRỌNG ---
        // Giả định rằng Task.java sử dụng enum "Priority" (đã cung cấp)
        // thay vì "TaskPriority" (được import trong Task.java).
        // Nếu Task.java BẮT BUỘC phải dùng "TaskPriority",
        // thì "TaskFilterRequest" cũng phải dùng "TaskPriority".

        // Lọc theo project_id (rất quan trọng)
        if (filter.getProjectId() != null) {
            predicates.add(cb.equal(root.get("project").get("id"), filter.getProjectId()));
        }

        // Lọc theo epic_id
        if (filter.getEpicId() != null) {
            predicates.add(cb.equal(root.get("epic").get("id"), filter.getEpicId()));
        }

        // Lọc theo sprint_id
        if (filter.getSprintId() != null) {
            predicates.add(cb.equal(root.get("sprint").get("id"), filter.getSprintId()));
        }

        // Tìm kiếm 'like' theo title
        if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getTitle().toLowerCase() + "%"));
        }

        // Lọc theo status
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }

        // Lọc theo assignee_id
        if (filter.getAssigneeId() != null) {
            predicates.add(cb.equal(root.get("assignee").get("id"), filter.getAssigneeId()));
        }

        // Lọc theo reviewer_id
        if (filter.getReviewerId() != null) {
            predicates.add(cb.equal(root.get("reviewer").get("id"), filter.getReviewerId()));
        }

        // Lọc theo danh sách taskTypes (sử dụng 'IN')
        if (filter.getTaskTypes() != null && !filter.getTaskTypes().isEmpty()) {
            predicates.add(root.get("taskType").in(filter.getTaskTypes()));
        }

        // Lọc theo danh sách priorities (sử dụng 'IN')
        if (filter.getPriorities() != null && !filter.getPriorities().isEmpty()) {
            predicates.add(root.get("priority").in(filter.getPriorities()));
        }

        // Lọc theo khoảng ngày due_date
        if (filter.getDueDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
        }
        if (filter.getDueDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
        }
        
        // Lọc theo khoảng ngày start_date
        if (filter.getStartDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), filter.getStartDateFrom()));
        }
        if (filter.getStartDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), filter.getStartDateTo()));
        }

        // Kết hợp tất cả các điều kiện lọc bằng 'AND'
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}