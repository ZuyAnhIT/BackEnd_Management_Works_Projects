// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/WorkspaceMemberSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.WorkspaceMember;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class WorkspaceMemberSpecification {

    /**
     * Tạo bộ lọc động cho thành viên phòng ban.
     */
    public static Specification<WorkspaceMember> filterMembers(
            Integer workspaceId,
            String searchName,      // Tìm theo Tên
            String searchEmail,     // Tìm theo Email
            String searchRoleName,  // Tìm theo Tên Role
            String searchPhone      // Tìm theo Số điện thoại
    ) {
        // 1. Điều kiện bắt buộc: Workspace ID
        // So sánh workspace.id với giá trị workspaceId
        Specification<WorkspaceMember> spec = (root, query, cb) -> 
                cb.equal(root.get("workspace").get("id"), workspaceId);

        // 2. Tìm theo Tên (JOIN bảng User -> fullName)
        if (searchName != null && !searchName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "fullName", searchName));
        }

        // 3. Tìm theo Email (JOIN bảng User -> email)
        if (searchEmail != null && !searchEmail.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "email", searchEmail));
        }

        // 4. Tìm theo Tên Role (JOIN bảng Role -> roleName)
        if (searchRoleName != null && !searchRoleName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("role", "roleName", searchRoleName));
        }
        
        // 5. Tìm theo Số điện thoại (JOIN bảng User -> phoneNumber)
        if (searchPhone != null && !searchPhone.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "phoneNumber", searchPhone));
        }

        return spec;
    }
}