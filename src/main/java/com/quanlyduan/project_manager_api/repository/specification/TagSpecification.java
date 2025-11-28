package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.model.Tag;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class TagSpecification {

    /**
     * Tạo Specification lọc Tag động (Dynamic Query).
     * @param projectId (Bắt buộc) ID của dự án.
     * @param filter (Tùy chọn) Object chứa các điều kiện lọc (keyword, dates, creators...).
     */
    public static Specification<Tag> getFilterSpec(Integer projectId, TagFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. BẮT BUỘC: Lọc theo Project ID
            // Đảm bảo Tag thuộc về đúng dự án đang truy cập
            predicates.add(cb.equal(root.get("project").get("id"), projectId));

            // 2. Xử lý các điều kiện lọc TÙY CHỌN (nếu filter != null)
            if (filter != null) {
                
                // --- A. Lọc theo Keyword (Name OR Description) ---
                // Logic giống filterTasks: check null và empty trước khi query
                String keyword = filter.getKeyword();
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String pattern = "%" + keyword.trim().toLowerCase() + "%";
                    
                    // Tìm trong Name
                    Predicate hasName = cb.like(cb.lower(root.get("name")), pattern);
                    // Tìm trong Description
                    Predicate hasDesc = cb.like(cb.lower(root.get("description")), pattern);
                    
                    // Kết hợp bằng OR: (name LIKE %key% OR description LIKE %key%)
                    predicates.add(cb.or(hasName, hasDesc));
                }

                // --- B. Lọc theo danh sách tên (Multi-select) ---
                if (filter.getNames() != null && !filter.getNames().isEmpty()) {
                    predicates.add(root.get("name").in(filter.getNames()));
                }

                // --- C. Lọc theo người tạo ---
                if (filter.getCreatedById() != null) {
                    predicates.add(cb.equal(root.get("createdBy").get("id"), filter.getCreatedById()));
                }

                // --- D. Lọc theo ngày tạo (From - To) ---
                if (filter.getCreatedFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
                }
                if (filter.getCreatedTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
                }
            }

            // Kết hợp tất cả điều kiện bằng AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}