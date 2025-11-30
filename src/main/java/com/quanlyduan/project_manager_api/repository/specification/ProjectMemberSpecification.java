// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/ProjectMemberSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class ProjectMemberSpecification {

    /**
     * Tạo bộ lọc động (Specification) cho Entity ProjectMember.
     * Dùng để tìm kiếm thành viên dự án theo nhiều tiêu chí khác nhau.
     *
     * @param projectId ID dự án hiện tại (Điều kiện BẮT BUỘC)
     * @param searchName Tìm theo Tên (JOIN User)
     * @param searchEmail Tìm theo Email (JOIN User)
     * @param searchRoleName Tìm theo Tên Role (JOIN Role)
     * @param searchPhone Tìm theo Số điện thoại (JOIN User)
     * @return Specification đã ghép nối các Predicate (Điều kiện lọc)
     */
    public static Specification<ProjectMember> filterMembers(
            Integer projectId,
            String searchName,      
            String searchEmail,     
            String searchRoleName,  
            String searchPhone      
    ) {
        // 1. Điều kiện bắt buộc: Project ID
        // Sử dụng lambda expression để trỏ cụ thể vào ID của Project, tránh lỗi Type Mismatch (Object vs Integer)
        Specification<ProjectMember> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("project").get("id"), projectId);

        // 2. Tìm theo Tên (Sử dụng LEFT JOIN tới bảng User)
        if (searchName != null && !searchName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "fullName", searchName));
        }

        // 3. Tìm theo Email (Sử dụng LEFT JOIN tới bảng User)
        if (searchEmail != null && !searchEmail.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "email", searchEmail));
        }

        // 4. Tìm theo Tên Role (Sử dụng LEFT JOIN tới bảng Role)
        if (searchRoleName != null && !searchRoleName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("role", "roleName", searchRoleName));
        }
        
        // 5. Tìm theo Số điện thoại (Sử dụng LEFT JOIN tới bảng User)
        if (searchPhone != null && !searchPhone.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "phoneNumber", searchPhone));
        }

        return spec;
    }
}