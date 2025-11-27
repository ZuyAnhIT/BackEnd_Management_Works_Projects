// File: src/main/java/com.quanlyduan.project_manager_api/repository/specification/EpicSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class EpicSpecification {

    /**
     * Lọc Epic theo Project ID và từ khóa (tên Epic).
     */
    public static Specification<Epic> filterEpics(Integer projectId, String keyword) {
        
        // 1. Bắt buộc: Thuộc Project này (ĐÃ SỬA LỖI TẠI ĐÂY)
        // Cần so sánh Project.id với projectId
        Specification<Epic> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("project").get("id"), projectId); // FIX: Trỏ vào .get("id")

        // 2. Lọc theo từ khóa (Tên Epic)
        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("name", keyword));
        }

        return spec;
    }
}