// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/EpicSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class EpicSpecification {

    /**
     * Tạo bộ lọc động (Specification) cho Entity Epic.
     * Dùng để lấy danh sách Epic theo Dự án và từ khóa tìm kiếm.
     */
    public static Specification<Epic> filterEpics(Integer projectId, String keyword) {
        
        // 1. Điều kiện bắt buộc: Epic phải thuộc Project này
        // Sử dụng lambda expression để trỏ cụ thể vào ID của Project, tránh lỗi Type Mismatch (Object vs Integer)
        Specification<Epic> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("project").get("id"), projectId);

        // 2. Lọc theo từ khóa (Tên Epic)
        // Nếu keyword tồn tại, ghép thêm điều kiện tìm kiếm gần đúng (LIKE) vào tên Epic.
        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("name", keyword));
        }

        return spec;
    }
}