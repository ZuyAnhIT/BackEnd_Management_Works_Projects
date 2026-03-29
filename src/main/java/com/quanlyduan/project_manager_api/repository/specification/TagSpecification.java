package com.quanlyduan.project_manager_api.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.model.Tag;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Lop cung cap cac bo loc dong cho thuc the Nhan (Tag).
 * Ho tro tim kiem theo du an, tu khoa, nguoi tao va thoi gian.
 */
public class TagSpecification {

    // Khai bao cac hang so ten truong trong Entity de tranh hardcode
    private static final String FIELD_PROJECT = "project";
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_CREATED_BY = "createdBy";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String LIKE_PATTERN = "%%%s%%";

    /**
     * Constructor rieng tu de ngan viec khoi tao lop utility.
     */
    private TagSpecification() {
    }

    /**
     * Tao Specification loc Tag dong dua tren cac tham so yeu cau.
     */
    public static Specification<Tag> getFilterSpec(Integer projectId, TagFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Dieu kien bat buoc: Loc theo ID du an
            addProjectPredicate(predicates, root, cb, projectId);

            // 2. Ap dung cac dieu kien loc tuy chon (Stepdown Rule)
            if (filter != null) {
                addKeywordPredicate(predicates, root, cb, filter.getKeyword());
                addNamesPredicate(predicates, root, filter.getNames());
                addCreatorPredicate(predicates, root, cb, filter.getCreatedById());
                addDateRangePredicate(predicates, root, cb, filter);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ======================================================
    // CAC HAM HO TRO (STEPDOWN RULE)
    // ======================================================

    /**
     * Rang buoc Tag phai thuoc ve du an dang truy cap.
     */
    private static void addProjectPredicate(List<Predicate> predicates, Root<Tag> root, CriteriaBuilder cb, Integer projectId) {
        predicates.add(cb.equal(root.get(FIELD_PROJECT).get(FIELD_ID), projectId));
    }

    /**
     * Loc theo tu khoa tim kiem trong Ten hoac Mo ta.
     */
    private static void addKeywordPredicate(List<Predicate> predicates, Root<Tag> root, CriteriaBuilder cb, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = String.format(LIKE_PATTERN, keyword.trim().toLowerCase());
            
            Predicate hasName = cb.like(cb.lower(root.get(FIELD_NAME)), pattern);
            Predicate hasDesc = cb.like(cb.lower(root.get(FIELD_DESCRIPTION)), pattern);
            
            predicates.add(cb.or(hasName, hasDesc));
        }
    }

    /**
     * Loc theo danh sach cac ten nhan cu the (IN clause).
     */
    private static void addNamesPredicate(List<Predicate> predicates, Root<Tag> root, List<String> names) {
        if (names != null && !names.isEmpty()) {
            predicates.add(root.get(FIELD_NAME).in(names));
        }
    }

    /**
     * Loc theo dinh danh nguoi tao nhan.
     */
    private static void addCreatorPredicate(List<Predicate> predicates, Root<Tag> root, CriteriaBuilder cb, Integer createdById) {
        if (createdById != null) {
            predicates.add(cb.equal(root.get(FIELD_CREATED_BY).get(FIELD_ID), createdById));
        }
    }

    /**
     * Loc theo khoang thoi gian tao (Created From - Created To).
     */
    private static void addDateRangePredicate(List<Predicate> predicates, Root<Tag> root, CriteriaBuilder cb, TagFilterRequest filter) {
        // Loc tu ngay
        if (filter.getCreatedFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_CREATED_AT), filter.getCreatedFrom()));
        }
        // Loc den ngay
        if (filter.getCreatedTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_CREATED_AT), filter.getCreatedTo()));
        }
    }
}