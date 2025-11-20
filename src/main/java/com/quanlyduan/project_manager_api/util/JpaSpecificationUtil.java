// File: src/main/java/com/quanlyduan/project_manager_api/util/JpaSpecificationUtil.java
package com.quanlyduan.project_manager_api.util;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import java.util.Collection;

public class JpaSpecificationUtil {

    /**
     * Tìm kiếm LIKE (Chứa) - Dùng cho Tên dự án, Tên Task, Code...
     */
    public static <T> Specification<T> attributeContains(String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        String pattern = "%" + value.toLowerCase() + "%";
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), pattern);
    }
    
    /**
     * Tìm kiếm chính xác (EQUAL) - Dùng cho ID, Boolean...
     */
    public static <T> Specification<T> attributeEquals(String field, Object value) {
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
    }

    /**
     * [NÂNG CẤP] Tìm kiếm trong danh sách (IN)
     * Ví dụ: Tìm Task có độ ưu tiên là HIGH hoặc URGENT
     */
    public static <T> Specification<T> attributeIn(String field, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) -> root.get(field).in(values);
    }
    
    /**
     * [NÂNG CẤP] Tìm kiếm LIKE trên bảng JOIN (Dùng LEFT JOIN để không mất dữ liệu)
     * Ví dụ: Tìm Task theo tên người được giao (assignee.fullName)
     */
    public static <T, R> Specification<T> attributeContainsJoin(String joinAttribute, String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        String pattern = "%" + value.toLowerCase() + "%";
        
        return (root, query, criteriaBuilder) -> {
            // Dùng LEFT JOIN thay vì INNER để tránh mất task nếu assignee = null
            Join<T, R> join = root.join(joinAttribute, JoinType.LEFT); 
            return criteriaBuilder.like(criteriaBuilder.lower(join.get(field)), pattern);
        };
    }

    /**
     * [MỚI] Tìm kiếm khoảng thời gian hoặc số (BETWEEN)
     * Ví dụ: Tìm dự án tạo từ ngày A đến ngày B
     */
    public static <T, Y extends Comparable<? super Y>> Specification<T> attributeBetween(String field, Y min, Y max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) return criteriaBuilder.conjunction();
            
            Path<Y> path = root.get(field);
            if (min != null && max != null) {
                return criteriaBuilder.between(path, min, max);
            } else if (min != null) {
                return criteriaBuilder.greaterThanOrEqualTo(path, min);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(path, max);
            }
        };
    }
}