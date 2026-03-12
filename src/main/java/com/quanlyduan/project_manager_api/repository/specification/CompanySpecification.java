package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Company;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CompanySpecification {

    public static Specification<Company> filterCompaniesForAdmin(
            String searchName, 
            String searchCode, 
            String searchEmail, 
            String searchStatus, 
            String searchPlanCode) {
            
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Tìm theo Tên công ty
            if (searchName != null && !searchName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), 
                        "%" + searchName.toLowerCase() + "%"
                ));
            }

            // 2. Tìm theo Mã công ty
            if (searchCode != null && !searchCode.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("companyCode")), 
                        "%" + searchCode.toLowerCase() + "%"
                ));
            }

            // 3. Tìm theo Email
            if (searchEmail != null && !searchEmail.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), 
                        "%" + searchEmail.toLowerCase() + "%"
                ));
            }

            // 4. Tìm theo Trạng thái công ty (ACTIVE, SUSPENDED...)
            if (searchStatus != null && !searchStatus.trim().isEmpty()) {
                // Ép kiểu String về Enum tương ứng trong Entity của bạn
                // Ví dụ: predicates.add(criteriaBuilder.equal(root.get("status"), CompanyStatus.valueOf(searchStatus)));
                // Tạm thời dùng toString để so sánh nếu Entity định nghĩa là String/Enum
                predicates.add(criteriaBuilder.equal(root.get("status").as(String.class), searchStatus));
            }

            // 5. TÌM THEO GÓI CƯỚC (JOIN VÀO BẢNG SUBSCRIPTIONS VÀ PLANS)
            if (searchPlanCode != null && !searchPlanCode.trim().isEmpty()) {
                // LƯU Ý: Tên trường "subscription" và "plan" phải KHỚP với tên biến khai báo trong Entity Company.java và CompanySubscription.java
                predicates.add(criteriaBuilder.equal(
                        root.join("subscription", JoinType.LEFT)
                            .join("plan", JoinType.LEFT)
                            .get("planCode"), 
                        searchPlanCode
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}