// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/SprintSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SprintSpecification {

    public static Specification<Sprint> filterSprintsForRoadmap(
            Integer projectId,
            List<Integer> sprintIds,     // Lọc các Sprint cụ thể
            List<SprintStatus> statuses, // Lọc theo trạng thái
            String keyword,              // Tìm theo tên/mã
            LocalDate viewStart,
            LocalDate viewEnd
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Project ID
            predicates.add(cb.equal(root.get("project").get("id"), projectId));

            // 2. Filter IDs
            if (sprintIds != null && !sprintIds.isEmpty()) {
                predicates.add(root.get("id").in(sprintIds));
            }

            // 3. Filter Statuses
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            // 4. Keyword
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("sprintCode")), pattern)
                ));
            }

            // 5. Date Range Overlap
            if (viewStart != null && viewEnd != null) {
                Predicate startCondition = cb.lessThanOrEqualTo(
                    cb.coalesce(root.get("startDate"), root.get("createdAt").as(LocalDate.class)), 
                    viewEnd
                );
                Predicate endCondition = cb.or(
                    cb.isNull(root.get("endDate")),
                    cb.greaterThanOrEqualTo(root.get("endDate"), viewStart)
                );
                predicates.add(cb.and(startCondition, endCondition));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}