// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/ProjectMemberSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class ProjectMemberSpecification {

    /**
     * Tạo bộ lọc động cho thành viên dự án.
     */
    public static Specification<ProjectMember> filterMembers(
            Integer projectId,
            String searchName,      // Tìm theo Tên
            String searchEmail,     // Tìm theo Email
            String searchRoleName,  // Tìm theo Tên Role
            String searchPhone      // Tìm theo Số điện thoại
    ) {
        // 1. Điều kiện bắt buộc: Project ID
        Specification<ProjectMember> spec = (root, query, cb) -> 
                cb.equal(root.get("project").get("id"), projectId);

        // 2. Tìm theo Tên (JOIN User -> fullName)
        if (searchName != null && !searchName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "fullName", searchName));
        }

        // 3. Tìm theo Email (JOIN User -> email)
        if (searchEmail != null && !searchEmail.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "email", searchEmail));
        }

        // 4. Tìm theo Tên Role (JOIN Role -> roleName)
        if (searchRoleName != null && !searchRoleName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("role", "roleName", searchRoleName));
        }
        
        // 5. Tìm theo Số điện thoại (JOIN User -> phoneNumber)
        if (searchPhone != null && !searchPhone.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "phoneNumber", searchPhone));
        }

        return spec;
    }
}