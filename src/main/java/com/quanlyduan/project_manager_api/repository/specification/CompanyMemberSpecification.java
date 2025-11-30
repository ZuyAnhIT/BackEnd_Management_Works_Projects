// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/CompanyMemberSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class CompanyMemberSpecification {

    /**
     * Tạo bộ lọc động cho thành viên công ty.
     * Sử dụng JpaSpecificationExecutor để xây dựng truy vấn phức tạp (JOIN và LIKE) dựa trên các tham số tìm kiếm.
     *
     * @param companyId ID công ty hiện tại (Điều kiện BẮT BUỘC)
     * @param searchName Tìm theo Tên (JOIN User)
     * @param searchEmail Tìm theo Email (JOIN User)
     * @param searchJobTitle Tìm theo Chức vụ (Trường trực tiếp)
     * @param searchRoleName Tìm theo Tên Role (JOIN Role)
     * @param searchStatus Tìm theo Trạng thái (Enum)
     * @param searchPhone Tìm theo Số điện thoại (JOIN User)
     * @return Specification đã ghép nối các Predicate (Điều kiện lọc)
     */
    public static Specification<CompanyMember> filterMembers(
            Integer companyId,
            String searchName,      
            String searchEmail,     
            String searchJobTitle,  
            String searchRoleName,  
            MemberStatus searchStatus, 
            String searchPhone      
    ) {
        // 1. Điều kiện bắt buộc: Company ID
        // Tránh lỗi so sánh Object vs Integer bằng cách trỏ cụ thể vào ID: root.get("company").get("id")
        Specification<CompanyMember> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("company").get("id"), companyId);

        // 2. Tìm theo Tên (Sử dụng JOIN)
        if (searchName != null && !searchName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "fullName", searchName));
        }

        // 3. Tìm theo Email (Sử dụng JOIN)
        if (searchEmail != null && !searchEmail.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "email", searchEmail));
        }
        
        // 4. Tìm theo Chức vụ (Trường trực tiếp)
        if (searchJobTitle != null && !searchJobTitle.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("jobTitle", searchJobTitle));
        }

        // 5. Tìm theo Tên Role (Sử dụng JOIN)
        if (searchRoleName != null && !searchRoleName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("role", "roleName", searchRoleName));
        }

        // 6. Tìm theo Trạng thái (Enum)
        if (searchStatus != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("status", searchStatus));
        }

        // 7. Tìm theo Số điện thoại (Sử dụng JOIN)
        if (searchPhone != null && !searchPhone.isEmpty()) {
            // Lưu ý: JpaSpecificationUtil sử dụng LEFT JOIN để đảm bảo không mất thành viên có số điện thoại null
            spec = spec.and(JpaSpecificationUtil.attributeContainsJoin("user", "phoneNumber", searchPhone));
        }

        return spec;
    }
}