// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/TagSpecification.java
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
     * Hàm này xây dựng bộ lọc SQL dựa trên các tham số tùy chọn được gửi trong TagFilterRequest.
     *
     * @param projectId ID của dự án (BẮT BUỘC)
     * @param filter Object chứa các điều kiện lọc (keyword, dates, creators...)
     * @return Specification đã ghép nối các Predicate
     */
    public static Specification<Tag> getFilterSpec(Integer projectId, TagFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ========================================================================
            // 1. ĐIỀU KIỆN BẮT BUỘC: Lọc theo Project ID
            // ========================================================================
            // Đảm bảo Tag thuộc về đúng dự án đang truy cập
            predicates.add(cb.equal(root.get("project").get("id"), projectId));

            // 2. Xử lý các điều kiện lọc TÙY CHỌN
            if (filter != null) {
                
                // --- A. Lọc theo Keyword (Name OR Description - Tìm kiếm chung) ---
                String keyword = filter.getKeyword();
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String pattern = "%" + keyword.trim().toLowerCase() + "%";
                    
                    // Lọc 1: Tìm kiếm gần đúng trong Name
                    Predicate hasName = cb.like(cb.lower(root.get("name")), pattern);
                    // Lọc 2: Tìm kiếm gần đúng trong Description
                    Predicate hasDesc = cb.like(cb.lower(root.get("description")), pattern);
                    
                    // Kết hợp bằng OR
                    predicates.add(cb.or(hasName, hasDesc));
                }

                // --- B. Lọc theo danh sách tên (Multi-select / IN clause) ---
                // Chỉ tìm kiếm các Tag có tên nằm trong danh sách được cung cấp
                if (filter.getNames() != null && !filter.getNames().isEmpty()) {
                    predicates.add(root.get("name").in(filter.getNames()));
                }

                // --- C. Lọc theo người tạo (Chính xác theo ID) ---
                if (filter.getCreatedById() != null) {
                    predicates.add(cb.equal(root.get("createdBy").get("id"), filter.getCreatedById()));
                }

                // --- D. Lọc theo ngày tạo (Date Range - BETWEEN) ---
                // Lọc theo mốc BẮT ĐẦU (createdFrom)
                if (filter.getCreatedFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
                }
                // Lọc theo mốc KẾT THÚC (createdTo)
                if (filter.getCreatedTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
                }
            }

            // 3. Kết hợp tất cả điều kiện còn lại bằng toán tử AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}