// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/ProjectSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class ProjectSpecification {

    /**
     * Tạo bộ lọc động (Specification) cho Entity Project.
     * Dùng để tìm kiếm Dự án theo nhiều tiêu chí khác nhau trong một Workspace cụ thể.
     */
    public static Specification<Project> filterProjects(
            Integer workspaceId,
            String searchName,      // Tìm theo Tên Dự án
            String searchCode,      // Tìm theo Mã Dự án
            String searchManager,   // Tìm theo Tên Quản lý
            ProjectStatus searchStatus // Tìm theo Trạng thái (Enum)
    ) {
        // 1. Điều kiện bắt buộc: Project phải thuộc Workspace ID
        // Sử dụng lambda expression để trỏ cụ thể vào ID của Workspace, tránh lỗi Type Mismatch (Object vs Integer)
        Specification<Project> spec = (root, query, cb) -> 
                cb.equal(root.get("workspace").get("id"), workspaceId);

        // 2. Tìm theo Tên Dự án (LIKE)
        if (searchName != null && !searchName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("name", searchName));
        }

        // 3. Tìm theo Mã Dự án (LIKE)
        if (searchCode != null && !searchCode.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("projectCode", searchCode));
        }

        // 4. Tìm theo Tên Quản lý (JOIN bảng Manager/User)
        // Dùng attributeContainsJoin để thực hiện tìm kiếm LIKE trên bảng liên quan
        if (searchManager != null && !searchManager.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("manager", "fullName", searchManager));
        }

        // 5. Tìm theo Trạng thái (EQUAL - Enum)
        if (searchStatus != null) {
            // Dùng attributeEquals, Spring JPA tự động xử lý so sánh Enum
            spec = spec.and(JpaSpecificationUtil.attributeEquals("status", searchStatus));
        }

        return spec;
    }
}