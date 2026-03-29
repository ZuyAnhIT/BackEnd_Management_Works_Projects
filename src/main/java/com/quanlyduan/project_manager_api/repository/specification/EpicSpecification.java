package com.quanlyduan.project_manager_api.repository.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Lop xay dung bo loc dong cho thuc the Epic.
 * Ho tro truy van danh sach co ban va cac bo loc nang cao cho bieu do Roadmap.
 */
public class EpicSpecification {

    // Khai bao cac hang so ten truong trong Entity
    private static final String FIELD_PROJECT = "project";
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_EPIC_CODE = "epicCode";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_DUE_DATE = "dueDate";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String LIKE_PATTERN = "%%%s%%";

    /**
     * Constructor rieng tu de ngan viec khoi tao lop utility.
     */
    private EpicSpecification() {
    }

    /**
     * Bo loc co ban theo dự án và từ khóa tên Epic.
     */
    public static Specification<Epic> filterEpics(Integer projectId, String keyword) {
        // Dieu kien bat buoc: Phai thuoc Project
        Specification<Epic> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get(FIELD_PROJECT).get(FIELD_ID), projectId);

        // Loc theo tu khoa ten Epic
        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains(FIELD_NAME, keyword));
        }

        return spec;
    }

    /**
     * Bo loc nang cao phuc vu cho bieu do Roadmap va Timeline.
     */
    public static Specification<Epic> filterEpicsForRoadmap(
            Integer projectId,
            List<Integer> epicIds,
            List<EpicStatus> statuses,
            String keyword,
            LocalDate viewStart,
            LocalDate viewEnd
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Dieu kien bat buoc: Phai thuoc Project
            predicates.add(cb.equal(root.get(FIELD_PROJECT).get(FIELD_ID), projectId));

            // 2. Ap dung cac bo loc tuy chon (Stepdown Rule)
            addIdFilter(predicates, root, epicIds);
            addStatusFilter(predicates, root, statuses);
            addKeywordFilter(predicates, root, cb, keyword);
            addTimelineFilter(predicates, root, cb, viewStart, viewEnd);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ======================================================
    // CAC HAM HO TRO (STEPDOWN RULE)
    // ======================================================

    private static void addIdFilter(List<Predicate> predicates, Root<Epic> root, List<Integer> epicIds) {
        if (epicIds != null && !epicIds.isEmpty()) {
            predicates.add(root.get(FIELD_ID).in(epicIds));
        }
    }

    private static void addStatusFilter(List<Predicate> predicates, Root<Epic> root, List<EpicStatus> statuses) {
        if (statuses != null && !statuses.isEmpty()) {
            predicates.add(root.get(FIELD_STATUS).in(statuses));
        }
    }

    private static void addKeywordFilter(List<Predicate> predicates, Root<Epic> root, CriteriaBuilder cb, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = String.format(LIKE_PATTERN, keyword.toLowerCase());
            predicates.add(cb.or(
                cb.like(cb.lower(root.get(FIELD_NAME)), pattern),
                cb.like(cb.lower(root.get(FIELD_EPIC_CODE)), pattern)
            ));
        }
    }

    /**
     * Xu ly logic giao thoa thoi gian (Timeline Overlap).
     * Cong thuc: (EpicStart <= ViewEnd) AND (EpicEnd >= ViewStart)
     */
    private static void addTimelineFilter(List<Predicate> predicates, Root<Epic> root, CriteriaBuilder cb, 
                                        LocalDate viewStart, LocalDate viewEnd) {
        if (viewStart != null && viewEnd != null) {
            
            // Dieu kien 1: Ngay bat dau cua Epic phai truoc hoac bang ngay ket thuc cua khung nhin
            // Su dung Coalesce de lay CreatedAt neu StartDate bi null
            Predicate startCondition = cb.lessThanOrEqualTo(
                cb.coalesce(root.get(FIELD_START_DATE), root.get(FIELD_CREATED_AT).as(LocalDate.class)), 
                viewEnd
            );

            // Dieu kien 2: Ngay ket thuc cua Epic phai sau hoac bang ngay bat dau cua khung nhin
            // Neu DueDate null, coi nhu Epic keo dai vo han
            Predicate endCondition = cb.or(
                cb.isNull(root.get(FIELD_DUE_DATE)),
                cb.greaterThanOrEqualTo(root.get(FIELD_DUE_DATE), viewStart)
            );
            
            predicates.add(cb.and(startCondition, endCondition));
        }
    }
}