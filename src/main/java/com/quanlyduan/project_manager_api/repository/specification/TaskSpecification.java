package com.quanlyduan.project_manager_api.repository.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Lop xay dung bo loc dong cho thuc the Cong viec (Task).
 * Ho tro cac truy van phuc tap cho Board, Backlog, Lich va Thong ke.
 */
public class TaskSpecification {

    // Khai bao cac hang so ten truong Entity de tranh hardcode
    private static final String FIELD_PROJECT = "project";
    private static final String FIELD_SPRINT = "sprint";
    private static final String FIELD_ASSIGNEE = "assignee";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_ID = "id";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_TASK_CODE = "taskCode";
    private static final String FIELD_PRIORITY = "priority";
    private static final String FIELD_TASK_TYPE = "taskType";
    private static final String FIELD_IS_ARCHIVED = "isArchived";
    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_DUE_DATE = "dueDate";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_COMPLETED_AT = "completedAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";
    private static final String ATTR_IS_COMPLETED = "isCompletedStatus";

    /**
     * Constructor rieng tu de ngan viec khoi tao lop utility.
     */
    private TaskSpecification() {
    }

    // ======================================================
    // 1. BO LOC TONG QUAT (MASTER FILTER)
    // ======================================================

    /**
     * Bo loc chinh dung cho Board, Backlog va tim kiem danh sach.
     */
    public static Specification<Task> filterTasks(
            Integer projectId,
            Integer sprintId,
            boolean isBacklog,
            String keyword,
            Integer assigneeId,
            TaskPriority priority,
            TaskType taskType,
            List<Integer> statusIds,
            Boolean isArchived
    ) {
        // 1. Dieu kien bat buoc: Thuoc Project
        Specification<Task> spec = (root, query, cb) -> cb.equal(root.get(FIELD_PROJECT).get(FIELD_ID), projectId);

        // 2. Ap dung cac logic loc chi tiet (Stepdown Rule)
        spec = spec.and(applyArchiveFilter(isArchived));
        spec = spec.and(applySprintOrBacklogFilter(sprintId, isBacklog));
        spec = spec.and(applyKeywordFilter(keyword));
        spec = spec.and(applyAssigneeFilter(assigneeId));
        spec = spec.and(applyEnumFilters(priority, taskType));
        spec = spec.and(applyStatusFilter(statusIds));

        return spec;
    }

    /**
     * Ham tien ich cho logic Backlog (Mac dinh khong lay task da luu tru).
     */
    public static Specification<Task> filterBacklog(
            Integer projectId,
            Integer sprintId,
            boolean isBacklog,
            String keyword,
            Integer assigneeId,
            TaskPriority priority,
            TaskType taskType) {
        return filterTasks(projectId, sprintId, isBacklog, keyword, assigneeId, priority, taskType, null, false);
    }

    /**
     * Ham overload tuong thich cho cac doan code cu goi 8 tham so.
     */
    public static Specification<Task> filterTasks(
            Integer projectId,
            Integer sprintId,
            boolean isBacklog,
            String keyword,
            Integer assigneeId,
            TaskPriority priority,
            TaskType taskType,
            List<Integer> statusIds
    ) {
        return filterTasks(projectId, sprintId, isBacklog, keyword, assigneeId, priority, taskType, statusIds, false);
    }

    // ======================================================
    // 2. BO LOC LICH (CALENDAR FILTER)
    // ======================================================

    /**
     * Tim kiem cong viec trong khoang thoi gian de hien thi tren Calendar.
     */
    public static Specification<Task> filterTasksForCalendar(
            Integer projectId,
            LocalDate viewStart, 
            LocalDate viewEnd,
            String keyword, 
            Integer assigneeId, 
            TaskPriority priority, 
            TaskType taskType) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Dieu kien co ban: Dung du an va chua luu tru
            predicates.add(cb.equal(root.get(FIELD_PROJECT).get(FIELD_ID), projectId));
            predicates.add(cb.equal(root.get(FIELD_IS_ARCHIVED), false));

            // Logic hien thi theo thoi gian (Overlap)
            if (viewStart != null && viewEnd != null) {
                addTimeOverlapPredicates(predicates, root, cb, viewStart, viewEnd);
            }

            // Cac bo loc phu tro
            addAdditionalPredicates(predicates, root, cb, keyword, assigneeId, priority, taskType);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ======================================================
    // 3. BO LOC THONG KE (STATISTICS FILTERS)
    // ======================================================

    public static Specification<Task> filterBase(
            Integer projectId, 
            Integer assigneeId,
            String keyword, 
            TaskPriority priority, 
            TaskType taskType, 
            List<Integer> statusIds
    ) {
        return filterTasks(projectId, null, false, keyword, assigneeId, priority, taskType, statusIds, false);
    }

    public static Specification<Task> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> (from == null || to == null) ? null : cb.between(root.get(FIELD_CREATED_AT), from, to);
    }

    public static Specification<Task> completedBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null || to == null) return null;
            Predicate time = cb.between(root.get(FIELD_COMPLETED_AT), from, to);
            Predicate isDone = cb.isNotNull(root.get(FIELD_COMPLETED_AT));
            return cb.and(time, isDone);
        };
    }

    public static Specification<Task> updatedBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> (from == null || to == null) ? null : cb.between(root.get(FIELD_UPDATED_AT), from, to);
    }

    /**
     * Loc cong viec sap den han nhung chua hoan thanh.
     */
    public static Specification<Task> dueBetweenAndNotDone(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null || to == null) return null;

            Predicate timeRange = cb.between(root.get(FIELD_DUE_DATE), from, to);
            
            // Trang thai chua hoan thanh (Status bi null hoac isCompletedStatus = false)
            Predicate statusNull = cb.isNull(root.get(FIELD_STATUS));
            Predicate statusNotDone = cb.equal(root.get(FIELD_STATUS).get(ATTR_IS_COMPLETED), false);
            Predicate incomplete = cb.or(statusNull, statusNotDone);

            return cb.and(timeRange, incomplete);
        };
    }

    // ======================================================
    // CAC HAM PRIVATE HO TRO (STEPDOWN RULE)
    // ======================================================

    private static Specification<Task> applyArchiveFilter(Boolean isArchived) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_IS_ARCHIVED), isArchived != null ? isArchived : false);
    }

    private static Specification<Task> applySprintOrBacklogFilter(Integer sprintId, boolean isBacklog) {
        return (root, query, cb) -> {
            if (isBacklog) return cb.isNull(root.get(FIELD_SPRINT));
            if (sprintId != null && sprintId > 0) return cb.equal(root.get(FIELD_SPRINT).get(FIELD_ID), sprintId);
            return null;
        };
    }

    private static Specification<Task> applyKeywordFilter(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return null;
        Specification<Task> titleSpec = JpaSpecificationUtil.attributeContains(FIELD_TITLE, keyword);
        Specification<Task> codeSpec = JpaSpecificationUtil.attributeContains(FIELD_TASK_CODE, keyword);
        return titleSpec.or(codeSpec);
    }

    private static Specification<Task> applyAssigneeFilter(Integer assigneeId) {
        return (root, query, cb) -> assigneeId == null ? null : cb.equal(root.get(FIELD_ASSIGNEE).get(FIELD_ID), assigneeId);
    }

    private static Specification<Task> applyEnumFilters(TaskPriority priority, TaskType taskType) {
        Specification<Task> spec = Specification.where(null);
        if (priority != null) spec = spec.and(JpaSpecificationUtil.attributeEquals(FIELD_PRIORITY, priority));
        if (taskType != null) spec = spec.and(JpaSpecificationUtil.attributeEquals(FIELD_TASK_TYPE, taskType));
        return spec;
    }

    private static Specification<Task> applyStatusFilter(List<Integer> statusIds) {
        return (root, query, cb) -> (statusIds == null || statusIds.isEmpty()) ? null : root.get(FIELD_STATUS).get(FIELD_ID).in(statusIds);
    }

    private static void addTimeOverlapPredicates(List<Predicate> predicates, Root<Task> root, CriteriaBuilder cb, LocalDate start, LocalDate end) {
        // Cong viec bat dau truoc khi View ket thuc
        Predicate startCondition = cb.lessThanOrEqualTo(
                cb.coalesce(root.get(FIELD_START_DATE), root.get(FIELD_CREATED_AT).as(LocalDate.class)), end);

        // Cong viec ket thuc sau khi View bat dau (hoac chua co han chot)
        Predicate endCondition = cb.or(
                cb.isNull(root.get(FIELD_DUE_DATE)),
                cb.greaterThanOrEqualTo(root.get(FIELD_DUE_DATE), start));

        predicates.add(cb.and(startCondition, endCondition));
    }

    private static void addAdditionalPredicates(List<Predicate> predicates, Root<Task> root, CriteriaBuilder cb, 
                                              String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get(FIELD_TITLE)), pattern),
                    cb.like(cb.lower(root.get(FIELD_TASK_CODE)), pattern)));
        }
        if (assigneeId != null) predicates.add(cb.equal(root.get(FIELD_ASSIGNEE).get(FIELD_ID), assigneeId));
        if (priority != null) predicates.add(cb.equal(root.get(FIELD_PRIORITY), priority));
        if (taskType != null) predicates.add(cb.equal(root.get(FIELD_TASK_TYPE), taskType));
    }
}