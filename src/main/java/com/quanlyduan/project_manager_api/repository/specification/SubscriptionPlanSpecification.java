package com.quanlyduan.project_manager_api.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.model.SubscriptionPlan;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Lop cung cap cac bo loc dong cho thuc the Goi cuoc (Subscription Plan).
 * Ho tro tim kiem linh hoat cho ca quan tri vien va nguoi dung thong thuong.
 */
public class SubscriptionPlanSpecification {

    // Khai bao cac hang so ten truong trong Entity de tranh hardcode
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PLAN_CODE = "planCode";
    private static final String FIELD_IS_ACTIVE = "isActive";
    private static final String LIKE_PATTERN = "%%%s%%";

    /**
     * Constructor rieng tu de ngan viec khoi tao lop utility.
     */
    private SubscriptionPlanSpecification() {
    }

    /**
     * Tao bo loc tong quat cho Admin voi day du cac tieu chi quan ly.
     */
    public static Specification<SubscriptionPlan> filterPlans(
            String searchName, 
            String searchPlanCode, 
            Boolean searchStatus) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addNamePredicate(predicates, root, criteriaBuilder, searchName);
            addPlanCodePredicate(predicates, root, criteriaBuilder, searchPlanCode);
            addStatusPredicate(predicates, root, criteriaBuilder, searchStatus);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Tao bo loc cho khach hang (chi lay cac goi dang mo ban).
     */
    public static Specification<SubscriptionPlan> filterPublicPlans(String searchName) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Mac dinh chi lay nhung goi dang o trang thai hoat dong
            predicates.add(criteriaBuilder.equal(root.get(FIELD_IS_ACTIVE), true));

            addNamePredicate(predicates, root, criteriaBuilder, searchName);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ======================================================
    // CAC HAM HO TRO (STEPDOWN RULE)
    // ======================================================

    /**
     * Loc theo ten goi cuoc (khong phan biet hoa thuong).
     */
    private static void addNamePredicate(List<Predicate> predicates, Root<SubscriptionPlan> root, CriteriaBuilder cb, String name) {
        if (name != null && !name.trim().isEmpty()) {
            predicates.add(cb.like(
                    cb.lower(root.get(FIELD_NAME)), 
                    String.format(LIKE_PATTERN, name.toLowerCase())
            ));
        }
    }

    /**
     * Loc theo ma dinh danh cua goi cuoc.
     */
    private static void addPlanCodePredicate(List<Predicate> predicates, Root<SubscriptionPlan> root, CriteriaBuilder cb, String planCode) {
        if (planCode != null && !planCode.trim().isEmpty()) {
            predicates.add(cb.like(
                    cb.lower(root.get(FIELD_PLAN_CODE)), 
                    String.format(LIKE_PATTERN, planCode.toLowerCase())
            ));
        }
    }

    /**
     * Loc theo trang thai hoat dong (Active/Inactive).
     */
    private static void addStatusPredicate(List<Predicate> predicates, Root<SubscriptionPlan> root, CriteriaBuilder cb, Boolean status) {
        if (status != null) {
            predicates.add(cb.equal(root.get(FIELD_IS_ACTIVE), status));
        }
    }
}