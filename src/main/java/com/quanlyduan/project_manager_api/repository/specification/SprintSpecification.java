package com.quanlyduan.project_manager_api.repository.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Lop xay dung bo loc dong cho thuc the Sprint.
 * Ho tro truy van nang cao phuc vu cho giao dien Roadmap va Timeline.
 */
public class SprintSpecification {

    // Khai bao cac hang so ten truong trong Entity de tranh hardcode
    private static final String FIELD_PROJECT = "project";
    private static final String FIELD_ID = "id";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SPRINT_CODE = "sprintCode";
    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_END_DATE = "endDate";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String LIKE_PATTERN = "%%%s%%";

    /**
     * Constructor rieng tu de ngan viec khoi tao lop utility.
     */
    private SprintSpecification() {
    }

    /**
     * Tao bo loc nang cao cho Sprint tren Roadmap/Timeline.
     * Ho tro loc da tieu chi: IDs, Statuses, Keyword, va Khoang thoi gian.
     */
    public static Specification<Sprint> filterSprintsForRoadmap(
            Integer projectId,
            List<Integer> sprintIds,
            List<SprintStatus> statuses,
            String keyword,
            LocalDate viewStart,
            LocalDate viewEnd
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Ap dung cac dieu kien loc theo thu tu (Stepdown Rule)
            addProjectFilter(predicates, root, cb, projectId);
            addIdFilter(predicates, root, sprintIds);
            addStatusFilter(predicates, root, statuses);
            addKeywordFilter(predicates, root, cb, keyword);
            addTimelineFilter(predicates, root, cb, viewStart, viewEnd);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ======================================================
    // CAC HAM HO TRO (STEPDOWN RULE)
    // ======================================================

    /**
     * Loc theo ID du an (Dieu kien bat buoc).
     */
    private static void addProjectFilter(List<Predicate> predicates, Root<Sprint> root, CriteriaBuilder cb, Integer projectId) {
        predicates.add(cb.equal(root.get(FIELD_PROJECT).get(FIELD_ID), projectId));
    }

    /**
     * Loc theo danh sach cac ID Sprint cu the.
     */
    private static void addIdFilter(List<Predicate> predicates, Root<Sprint> root, List<Integer> sprintIds) {
        if (sprintIds != null && !sprintIds.isEmpty()) {
            predicates.add(root.get(FIELD_ID).in(sprintIds));
        }
    }

    /**
     * Loc theo danh sach cac trang thai cua Sprint.
     */
    private static void addStatusFilter(List<Predicate> predicates, Root<Sprint> root, List<SprintStatus> statuses) {
        if (statuses != null && !statuses.isEmpty()) {
            predicates.add(root.get(FIELD_STATUS).in(statuses));
        }
    }

    /**
     * Loc theo tu khoa tim kiem (Tim trong Ten hoac Ma Sprint).
     */
    private static void addKeywordFilter(List<Predicate> predicates, Root<Sprint> root, CriteriaBuilder cb, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = String.format(LIKE_PATTERN, keyword.toLowerCase());
            predicates.add(cb.or(
                cb.like(cb.lower(root.get(FIELD_NAME)), pattern),
                cb.like(cb.lower(root.get(FIELD_SPRINT_CODE)), pattern)
            ));
        }
    }

    /**
     * Xu ly logic giao thoa thoi gian (Timeline Overlap).
     * Cong thuc: (SprintStart <= ViewEnd) AND (SprintEnd >= ViewStart)
     */
    private static void addTimelineFilter(List<Predicate> predicates, Root<Sprint> root, CriteriaBuilder cb, 
                                        LocalDate viewStart, LocalDate viewEnd) {
        if (viewStart != null && viewEnd != null) {
            
            // Dieu kien 1: Ngay bat dau cua Sprint phai truoc hoac bang ngay ket thuc khung nhin
            // NeuStartDate bi null, su dung CreatedAt de thay the
            Predicate startCondition = cb.lessThanOrEqualTo(
                cb.coalesce(root.get(FIELD_START_DATE), root.get(FIELD_CREATED_AT).as(LocalDate.class)), 
                viewEnd
            );

            // Dieu kien 2: Ngay ket thuc cua Sprint phai sau hoac bang ngay bat dau khung nhin
            // NeuEndDate bi null, coi nhu Sprint keo dai vo han
            Predicate endCondition = cb.or(
                cb.isNull(root.get(FIELD_END_DATE)),
                cb.greaterThanOrEqualTo(root.get(FIELD_END_DATE), viewStart)
            );
            
            predicates.add(cb.and(startCondition, endCondition));
        }
    }
}