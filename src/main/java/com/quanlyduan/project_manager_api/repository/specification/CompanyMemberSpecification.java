// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/CompanyMemberSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class CompanyMemberSpecification {

    /**
     * Tạo bộ lọc động cho thành viên công ty.
     * Tham số nào null sẽ bị bỏ qua.
     */
    public static Specification<CompanyMember> filterMembers(
            Integer companyId,
            String searchName,      // Tìm theo Tên
            String searchEmail,     // Tìm theo Email
            String searchJobTitle,  // Tìm theo Chức vụ
            String searchRoleName,  // Tìm theo Tên Role
            MemberStatus searchStatus // Tìm theo Trạng thái
    ) {
        Specification<CompanyMember> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("company").get("id"), companyId);

        // 2. Tìm theo Tên (JOIN bảng User -> fullName)
        if (searchName != null && !searchName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "fullName", searchName));
        }

        // 3. Tìm theo Email (JOIN bảng User -> email)
        if (searchEmail != null && !searchEmail.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "email", searchEmail));
        }
        
        // 4. Tìm theo Chức vụ (Trường trực tiếp: jobTitle)
        if (searchJobTitle != null && !searchJobTitle.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("jobTitle", searchJobTitle));
        }

        // 5. Tìm theo Tên Role (JOIN bảng Role -> roleName)
        if (searchRoleName != null && !searchRoleName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("role", "roleName", searchRoleName));
        }

        // 6. Tìm theo Trạng thái (Enum)
        if (searchStatus != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("status", searchStatus));
        }

        return spec;
    }
}