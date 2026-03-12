package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.SubscriptionPlan;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionPlanSpecification {

    /**
     * Tạo bộ lọc động cho Gói cước.
     */
    public static Specification<SubscriptionPlan> filterPlans(
            String searchName, 
            String searchPlanCode, 
            Boolean searchStatus) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Tìm theo Tên gói (Gần giống LIKE %search%)
            if (searchName != null && !searchName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), 
                        "%" + searchName.toLowerCase() + "%"
                ));
            }

            // 2. Tìm theo Mã gói
            if (searchPlanCode != null && !searchPlanCode.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("planCode")), 
                        "%" + searchPlanCode.toLowerCase() + "%"
                ));
            }

            // 3. Tìm theo Trạng thái (isActive)
            if (searchStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), searchStatus));
            }

            // Ghép nối tất cả các điều kiện bằng phép AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<SubscriptionPlan> filterPublicPlans(String searchName) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));

            // Tìm theo Tên gói (Nếu khách hàng có gõ tìm kiếm)
            if (searchName != null && !searchName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), 
                        "%" + searchName.toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}