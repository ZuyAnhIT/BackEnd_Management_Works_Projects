// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/EpicSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EpicSpecification {

    /**
     * [CŨ] Tạo bộ lọc động (Specification) cho Entity Epic.
     * Dùng để lấy danh sách Epic theo Dự án và từ khóa tìm kiếm.
     */
    public static Specification<Epic> filterEpics(Integer projectId, String keyword) {
        
        // 1. Điều kiện bắt buộc: Epic phải thuộc Project này
        Specification<Epic> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("project").get("id"), projectId);

        // 2. Lọc theo từ khóa (Tên Epic)
        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("name", keyword));
        }

        return spec;
    }

    /**
     * [MỚI] Bộ lọc nâng cao cho Epic trên Roadmap/Timeline.
     * Hỗ trợ lọc đa tiêu chí: IDs, Statuses, Keyword (Tên/Mã), và Khoảng thời gian.
     */
    public static Specification<Epic> filterEpicsForRoadmap(
            Integer projectId,
            List<Integer> epicIds,      // Lọc các Epic cụ thể (nếu user chọn)
            List<EpicStatus> statuses,  // Lọc theo trạng thái (Open, Done...)
            String keyword,             // Tìm theo tên hoặc mã Epic
            LocalDate viewStart,        // Lọc theo thời gian (Start View)
            LocalDate viewEnd           // Lọc theo thời gian (End View)
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Bắt buộc: Phải thuộc Project
            predicates.add(cb.equal(root.get("project").get("id"), projectId));

            // 2. Lọc theo danh sách ID (nếu user chọn cụ thể vài Epic để xem)
            if (epicIds != null && !epicIds.isEmpty()) {
                predicates.add(root.get("id").in(epicIds));
            }

            // 3. Lọc theo danh sách Trạng thái (ví dụ: ẩn các Epic đã đóng)
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            // 4. Tìm kiếm từ khóa (Tìm trong Tên hoặc Mã Epic Code)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("epicCode")), pattern)
                ));
            }

            // 5. Lọc theo Thời gian (Date Range Overlap Logic)
            // Logic: Epic hiển thị nếu khoảng thời gian của nó GIAO với [viewStart, viewEnd]
            // Công thức giao thoa: (StartA <= EndB) AND (EndA >= StartB)
            if (viewStart != null && viewEnd != null) {
                
                // Điều kiện A: Epic Start <= View End
                // Nếu Epic chưa có startDate, dùng createdAt để thay thế (xử lý null an toàn)
                Predicate startCondition = cb.lessThanOrEqualTo(
                    cb.coalesce(root.get("startDate"), root.get("createdAt").as(LocalDate.class)), 
                    viewEnd
                );

                // Điều kiện B: Epic End >= View Start
                // Nếu Epic chưa có endDate (dueDate), coi như vô hạn -> luôn thỏa mãn
                Predicate endCondition = cb.or(
                    cb.isNull(root.get("dueDate")),
                    cb.greaterThanOrEqualTo(root.get("dueDate"), viewStart)
                );
                
                predicates.add(cb.and(startCondition, endCondition));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}