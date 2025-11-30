// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/WorkspaceSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class WorkspaceSpecification {

    /**
     * Tạo bộ lọc động (Specification) cho Entity Workspace.
     * Dùng để tìm kiếm Không gian làm việc theo nhiều tiêu chí khác nhau trong một Công ty cụ thể.
     *
     * @param companyId ID công ty hiện tại (Điều kiện BẮT BUỘC)
     * @param searchName Tìm theo Tên Workspace
     * @param searchCode Tìm theo Mã Workspace
     * @param searchDescription Tìm theo Mô tả
     * @param searchStatus Tìm theo Trạng thái (Enum)
     * @return Specification đã ghép nối các Predicate (Điều kiện lọc)
     */
    public static Specification<Workspace> filterWorkspaces(
            Integer companyId,
            String searchName,      
            String searchCode,      
            String searchDescription,
            WorkspaceStatus searchStatus 
    ) {
        // 1. Điều kiện bắt buộc: Workspace phải thuộc Company ID
        // Trỏ cụ thể vào ID của Company để tránh lỗi Type Mismatch (Object vs Integer)
        Specification<Workspace> spec = (root, query, cb) -> 
                cb.equal(root.get("company").get("id"), companyId);

        // 2. Lọc theo Tên (LIKE)
        if (searchName != null && !searchName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("name", searchName));
        }

        // 3. Lọc theo Mã (LIKE)
        if (searchCode != null && !searchCode.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("workspaceCode", searchCode));
        }

        // 4. Lọc theo Mô tả (LIKE)
        if (searchDescription != null && !searchDescription.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("description", searchDescription));
        }

        // 5. Lọc theo Trạng thái (EQUAL - Enum)
        if (searchStatus != null) {
            // Dùng attributeEquals, Spring JPA tự động xử lý so sánh Enum
            spec = spec.and(JpaSpecificationUtil.attributeEquals("status", searchStatus));
        }

        return spec;
    }
}