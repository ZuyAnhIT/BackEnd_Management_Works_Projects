// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/ProjectSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class ProjectSpecification {

    /**
     * Tạo bộ lọc động cho Dự án.
     */
    public static Specification<Project> filterProjects(
            Integer workspaceId,
            String searchName,      // Tìm theo Tên
            String searchCode,      // Tìm theo Mã
            String searchManager,   // Tìm theo Tên Quản lý
            ProjectStatus searchStatus // Tìm theo Trạng thái
    ) {
        // 1. Điều kiện bắt buộc: Workspace ID
        // (So sánh root.get("workspace").get("id") để tránh lỗi so sánh Object với Integer)
        Specification<Project> spec = (root, query, cb) -> 
                cb.equal(root.get("workspace").get("id"), workspaceId);

        // 2. Tìm theo Tên Dự án
        if (searchName != null && !searchName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("name", searchName));
        }

        // 3. Tìm theo Mã Dự án
        if (searchCode != null && !searchCode.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("projectCode", searchCode));
        }

        // 4. Tìm theo Tên Quản lý (JOIN bảng User -> fullName)
        if (searchManager != null && !searchManager.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("manager", "fullName", searchManager));
        }

        // 5. Tìm theo Trạng thái
        if (searchStatus != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("status", searchStatus));
        }

        return spec;
    }
}