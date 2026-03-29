package com.quanlyduan.project_manager_api.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.model.Company;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Lop cung cap cac bo loc dong cho thuc the Cong ty (Company).
 * Ho tro quan tri vien tim kiem theo nhieu tieu chi khac nhau.
 */
public class CompanySpecification {

    // Khai bao cac hang so ten truong trong Entity
    private static final String FIELD_NAME = "name";
    private static final String FIELD_COMPANY_CODE = "companyCode";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_STATUS = "status";
    private static final String JOIN_SUBSCRIPTION = "subscription";
    private static final String JOIN_PLAN = "plan";
    private static final String FIELD_PLAN_CODE = "planCode";
    private static final String LIKE_PATTERN = "%%%s%%";

    /**
     * Constructor rieng tu de ngan viec khoi tao lop utility.
     */
    private CompanySpecification() {
    }

    /**
     * Tao bo loc danh sach cong ty cho Admin voi cac tham so tuy chon.
     */
    public static Specification<Company> filterCompaniesForAdmin(
            String searchName, 
            String searchCode, 
            String searchEmail, 
            String searchStatus, 
            String searchPlanCode) {
            
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Ap dung cac dieu kien loc
            addNamePredicate(predicates, root, criteriaBuilder, searchName);
            addCodePredicate(predicates, root, criteriaBuilder, searchCode);
            addEmailPredicate(predicates, root, criteriaBuilder, searchEmail);
            addStatusPredicate(predicates, root, criteriaBuilder, searchStatus);
            addPlanPredicate(predicates, root, criteriaBuilder, searchPlanCode);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ======================================================
    // CAC HAM HO TRO (STEPDOWN RULE)
    // ======================================================

    /**
     * Loc theo ten cong ty (khong phan biet hoa thuong).
     */
    private static void addNamePredicate(List<Predicate> predicates, Root<Company> root, CriteriaBuilder cb, String name) {
        if (name != null && !name.trim().isEmpty()) {
            predicates.add(cb.like(
                    cb.lower(root.get(FIELD_NAME)), 
                    String.format(LIKE_PATTERN, name.toLowerCase())
            ));
        }
    }

    /**
     * Loc theo ma dinh danh cong ty.
     */
    private static void addCodePredicate(List<Predicate> predicates, Root<Company> root, CriteriaBuilder cb, String code) {
        if (code != null && !code.trim().isEmpty()) {
            predicates.add(cb.like(
                    cb.lower(root.get(FIELD_COMPANY_CODE)), 
                    String.format(LIKE_PATTERN, code.toLowerCase())
            ));
        }
    }

    /**
     * Loc theo dia chi email lien he.
     */
    private static void addEmailPredicate(List<Predicate> predicates, Root<Company> root, CriteriaBuilder cb, String email) {
        if (email != null && !email.trim().isEmpty()) {
            predicates.add(cb.like(
                    cb.lower(root.get(FIELD_EMAIL)), 
                    String.format(LIKE_PATTERN, email.toLowerCase())
            ));
        }
    }

    /**
     * Loc theo trang thai hoat dong (ACTIVE, SUSPENDED...).
     */
    private static void addStatusPredicate(List<Predicate> predicates, Root<Company> root, CriteriaBuilder cb, String status) {
        if (status != null && !status.trim().isEmpty()) {
            predicates.add(cb.equal(root.get(FIELD_STATUS).as(String.class), status));
        }
    }

    /**
     * Loc theo ma goi cuoc (Yeu cau JOIN qua Subscription va Plan).
     */
    private static void addPlanPredicate(List<Predicate> predicates, Root<Company> root, CriteriaBuilder cb, String planCode) {
        if (planCode != null && !planCode.trim().isEmpty()) {
            // Thuc hien LEFT JOIN tu Company -> Subscription -> Plan
            predicates.add(cb.equal(
                    root.join(JOIN_SUBSCRIPTION, JoinType.LEFT)
                        .join(JOIN_PLAN, JoinType.LEFT)
                        .get(FIELD_PLAN_CODE), 
                    planCode
            ));
        }
    }
}