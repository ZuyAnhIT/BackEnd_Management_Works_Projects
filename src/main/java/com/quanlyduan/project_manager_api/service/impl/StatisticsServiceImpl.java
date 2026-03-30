package com.quanlyduan.project_manager_api.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.dto.response.CalendarEventResponse;
import com.quanlyduan.project_manager_api.dto.response.EpicProgressResponse;
import com.quanlyduan.project_manager_api.dto.response.PriorityDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.RoadmapItemResponse;
import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;
import com.quanlyduan.project_manager_api.dto.response.StatusDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskTypeDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkloadResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.ProjectStatus;
import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.repository.EpicRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.SprintRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.specification.EpicSpecification;
import com.quanlyduan.project_manager_api.repository.specification.SprintSpecification;
import com.quanlyduan.project_manager_api.repository.specification.TaskSpecification;
import com.quanlyduan.project_manager_api.service.StatisticsService;
import com.quanlyduan.project_manager_api.util.ExcelHelper;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String SORT_CREATED_AT = "createdAt";
    public static final String SORT_COMPLETED_AT = "completedAt";
    public static final String SORT_UPDATED_AT = "updatedAt";
    public static final String SORT_DUE_DATE = "dueDate";

    public static final int LIMIT_TOP_RECENT = 5;
    public static final int LIMIT_TOP_DUE_SOON = 10;
    public static final int DUE_SOON_DAYS_OFFSET = 3;
    public static final int DEFAULT_ROADMAP_DAYS = 14;

    public static final double PERCENTAGE_MULTIPLIER = 100.0;
    public static final double PERCENTAGE_ROUNDING_FACTOR = 10000.0;

    public static final String LABEL_UNASSIGNED = "Unassigned";
    public static final String LABEL_UNKNOWN = "Unknown";
    public static final String LABEL_UNKNOWN_CODE = "UNKNOWN";
    public static final String LABEL_NONE = "NONE";
    public static final String LABEL_ALL = "ALL";
    public static final String LABEL_EPIC = "EPIC";
    public static final String LABEL_SPRINT = "SPRINT";
    public static final String LABEL_TASK = "TASK";
    public static final String LABEL_HOURS = "HOURS";
    public static final String LABEL_PRIORITY = "PRIORITY";

    public static final String PREFIX_EPIC_ID = "epic-";
    public static final String PREFIX_SPRINT_ID = "sprint-";
    public static final String PREFIX_TASK_ID = "task-";

    public static final String COLOR_URGENT = "#e74c3c";
    public static final String COLOR_HIGH = "#e67e22";
    public static final String COLOR_MEDIUM = "#3498db";
    public static final String COLOR_LOW = "#2ecc71";
    public static final String COLOR_DEFAULT_GRAY = "#95a5a6";
    public static final String COLOR_LIGHT_GRAY = "#bdc3c7";
    public static final String COLOR_PURPLE = "#9b59b6";
    public static final String COLOR_DARK_GRAY_BLUE = "#34495e";
    public static final String COLOR_BLUE = "#3b82f6";
    public static final String COLOR_WHITE = "#ffffff";
    public static final String COLOR_DARK_GRAY = "#333333";
    public static final String COLOR_LIGHT_PURPLE = "#f3e8ff";
    public static final String COLOR_BORDER_PURPLE = "#9333ea";
    public static final String COLOR_LIGHT_SLATE = "#f1f5f9";
    public static final String COLOR_BORDER_SLATE = "#94a3b8";

    // Khai bao cac hang so cho bao cao Excel
    public static final String EXCEL_SHEET_WORKLOAD = "Phân bổ công việc";
    public static final String EXCEL_SHEET_EPIC_PROGRESS = "Tiến độ Epic";
    public static final String EXCEL_TITLE_WORKLOAD = "BÁO CÁO PHÂN BỔ CÔNG VIỆC (WORKLOAD)";
    public static final String EXCEL_TITLE_EPIC_PROGRESS = "BÁO CÁO TIẾN ĐỘ EPIC (EPIC PROGRESS)";
    public static final String EXCEL_LABEL_DATE_EXPORT = "Ngày xuất: ";
    public static final String EXCEL_LABEL_DATE_EXPORT_EPIC = "Ngày xuất báo cáo: ";
    public static final String EXCEL_LABEL_UNIT = "Đơn vị tính: ";
    public static final String EXCEL_UNIT_HOURS = "Giờ làm việc (Hours)";
    public static final String EXCEL_UNIT_POINTS = "Điểm (Story Points)";
    public static final String EXCEL_LABEL_GROUP_BY = "Gom nhóm theo: ";
    public static final String EXCEL_GROUP_PRIORITY = "Độ ưu tiên";
    public static final String EXCEL_GROUP_STATUS = "Trạng thái";
    public static final String EXCEL_LABEL_TIME_RANGE = "Khoảng thời gian: ";
    public static final String EXCEL_LABEL_SPRINT_FILTER = "Sprint ID: ";
    public static final String EXCEL_LABEL_SPRINT_ALL = "Sprint: Tất cả";
    public static final String EXCEL_LABEL_PROJECT_ID = "Dự án ID: ";
    public static final String EXCEL_LABEL_SPRINT_FILTER_EPIC = "Lọc theo Sprint ID: ";
    public static final String EXCEL_LABEL_TOTAL_EPICS = "Tổng số Epic tìm thấy: ";
    public static final String EXCEL_NA = "N/A";
    public static final String EXCEL_TEXT_TO = " đến ";
    public static final String EXCEL_UNIT_SUFFIX_HOURS = "Hours";
    public static final String EXCEL_UNIT_SUFFIX_POINTS = "Points";
    public static final String EXCEL_HEADER_EMP_ID = "ID NV";
    public static final String EXCEL_HEADER_EMP_NAME = "Thành viên";
    public static final String EXCEL_HEADER_TOTAL = "Tổng (";
    public static final String EXCEL_PERCENT_FORMAT = "0.00%";
    public static final String ERROR_EXPORT_RUNTIME = "Export Error: ";
    public static final String ERROR_EXPORT_EPIC_RUNTIME = "Export Epic Error: ";

    // Khai bao cac bien phu thuoc
    private final TaskRepository taskRepository;
    private final ProjectServiceImpl projectService;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    // Constructor khoi tao thu cong
    public StatisticsServiceImpl(TaskRepository taskRepository,
                                 ProjectServiceImpl projectService,
                                 EpicRepository epicRepository,
                                 SprintRepository sprintRepository,
                                 ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.epicRepository = epicRepository;
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional(readOnly = true)
    public StatisticsResponse getOverviewStatistics(
            Integer projectId, Integer assigneeId,
            LocalDate from, LocalDate to,
            String keyword, Integer filterAssigneeId,
            TaskPriority priority, TaskType taskType, List<Integer> statusIds
    ) {
        Integer finalAssigneeId = (assigneeId != null) ? assigneeId : filterAssigneeId;

        // Chuyen doi thoi gian sang dinh dang phu hop cho truy van
        LocalDateTime startDateTime = from.atStartOfDay();
        LocalDateTime endDateTime = to.atTime(LocalTime.MAX);

        // Tao dieu kien loc chung
        Specification<Task> baseSpec = TaskSpecification.filterBase(
                projectId, finalAssigneeId, keyword, priority, taskType, statusIds
        );

        // Tinh toan thong ke cong viec moi tao
        Specification<Task> createdSpec = baseSpec.and(TaskSpecification.createdBetween(startDateTime, endDateTime));
        long createdCount = taskRepository.count(createdSpec);
        Pageable top5Newest = PageRequest.of(0, LIMIT_TOP_RECENT, Sort.by(SORT_CREATED_AT).descending());
        List<Task> createdTasks = taskRepository.findAll(createdSpec, top5Newest).getContent();

        // Tinh toan thong ke cong viec da hoan thanh
        Specification<Task> completedSpec = baseSpec.and(TaskSpecification.completedBetween(startDateTime, endDateTime));
        long completedCount = taskRepository.count(completedSpec);
        Pageable top5Completed = PageRequest.of(0, LIMIT_TOP_RECENT, Sort.by(SORT_COMPLETED_AT).descending());
        List<Task> completedTasksList = taskRepository.findAll(completedSpec, top5Completed).getContent();

        // Tinh toan thong ke cong viec vua cap nhat
        Specification<Task> updatedSpec = baseSpec.and(TaskSpecification.updatedBetween(startDateTime, endDateTime));
        long updatedCount = taskRepository.count(updatedSpec);
        Pageable top5Updated = PageRequest.of(0, LIMIT_TOP_RECENT, Sort.by(SORT_UPDATED_AT).descending());
        List<Task> updatedTasks = taskRepository.findAll(updatedSpec, top5Updated).getContent();

        // Tinh toan thong ke cong viec sap den han
        LocalDateTime dueFrom = LocalDateTime.now();
        LocalDateTime dueTo = dueFrom.plusDays(DUE_SOON_DAYS_OFFSET);
        
        Specification<Task> dueSoonSpec = baseSpec.and(TaskSpecification.dueBetweenAndNotDone(dueFrom, dueTo));
        long dueSoonCount = taskRepository.count(dueSoonSpec);
        Pageable top10Due = PageRequest.of(0, LIMIT_TOP_DUE_SOON, Sort.by(SORT_DUE_DATE).ascending());
        List<Task> dueSoonTasks = taskRepository.findAll(dueSoonSpec, top10Due).getContent();

        // Dong goi va tra ve ket qua tong hop
        return StatisticsResponse.builder()
                .fromDate(from.toString())
                .toDate(to.toString())
                .createdCount(createdCount)
                .createdTasks(mapList(createdTasks))
                .completedCount(completedCount)
                .completedTasks(mapList(completedTasksList))
                .updatedCount(updatedCount)
                .updatedTasks(mapList(updatedTasks))
                .dueSoonCount(dueSoonCount)
                .dueSoonTasks(mapList(dueSoonTasks))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusDistributionResponse> getTaskStatusDistribution(Integer projectId, Integer assigneeId) {
        // Lay du lieu thong ke tho tu co so du lieu
        List<Object[]> results = taskRepository.countTasksByStatusGroup(projectId, assigneeId);

        // Tinh tong so luong de phuc vu viec tinh phan tram
        long totalTasks = results.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        List<StatusDistributionResponse> responseList = new ArrayList<>();

        for (Object[] row : results) {
            ProjectStatus status = (ProjectStatus) row[0];
            Long count = (Long) row[1];

            if (status == null) {
                responseList.add(StatusDistributionResponse.builder()
                        .statusId(null)
                        .statusName(LABEL_UNASSIGNED)
                        .color(COLOR_DEFAULT_GRAY)
                        .taskCount(count)
                        .percentage(calculatePercentage(count, totalTasks))
                        .build());
                continue;
            }

            responseList.add(StatusDistributionResponse.builder()
                    .statusId(status.getId())
                    .statusName(status.getName())
                    .color(status.getColor())
                    .taskCount(count)
                    .percentage(calculatePercentage(count, totalTasks))
                    .build());
        }

        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriorityDistributionResponse> getTaskPriorityDistribution(Integer projectId, Integer assigneeId) {
        // Lay du lieu thong ke tho tu co so du lieu
        List<Object[]> results = taskRepository.countTasksByPriorityGroup(projectId, assigneeId);

        // Tinh tong so luong de phuc vu viec tinh phan tram
        long totalTasks = results.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        List<PriorityDistributionResponse> responseList = new ArrayList<>();

        for (Object[] row : results) {
            TaskPriority priority = (TaskPriority) row[0];
            Long count = (Long) row[1];

            if (priority == null) {
                responseList.add(PriorityDistributionResponse.builder()
                        .priorityName(LABEL_UNKNOWN)
                        .priorityCode(LABEL_UNKNOWN_CODE)
                        .color(COLOR_LIGHT_GRAY)
                        .taskCount(count)
                        .percentage(calculatePercentage(count, totalTasks))
                        .build());
                continue;
            }

            responseList.add(PriorityDistributionResponse.builder()
                    .priorityName(priority.name())
                    .priorityCode(priority.name())
                    .color(getPriorityColor(priority))
                    .taskCount(count)
                    .percentage(calculatePercentage(count, totalTasks))
                    .build());
        }

        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskTypeDistributionResponse> getTaskTypeDistribution(Integer projectId, Integer assigneeId) {
        // Lay du lieu thong ke tho tu co so du lieu
        List<Object[]> results = taskRepository.countTasksByTypeGroup(projectId, assigneeId);

        // Tinh tong so luong de phuc vu viec tinh phan tram
        long totalTasks = results.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        List<TaskTypeDistributionResponse> responseList = new ArrayList<>();

        for (Object[] row : results) {
            TaskType type = (TaskType) row[0];
            Long count = (Long) row[1];

            if (type == null) {
                continue;
            }

            responseList.add(TaskTypeDistributionResponse.builder()
                    .typeName(type.name())
                    .typeCode(type.name())
                    .color(getTypeColor(type))
                    .taskCount(count)
                    .percentage(calculatePercentage(count, totalTasks))
                    .build());
        }

        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkloadResponse> getWorkloadDistribution(
            Integer projectId, String viewType, String groupBy,
            Integer sprintId, LocalDate from, LocalDate to, List<Integer> statusIds) {

        // Tao dieu kien loc chung
        Specification<Task> spec = TaskSpecification.filterTasksForCalendar(
                projectId, from, to, null, null, null, null
        );

        if (sprintId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sprint").get("id"), sprintId));
        }
        if (statusIds != null && !statusIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").get("id").in(statusIds));
        }

        // Lay du lieu cong viec
        List<Task> tasks = taskRepository.findAll(spec);

        // Gom nhom theo tung thanh vien thuc hien
        Map<User, List<Task>> tasksByUser = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getAssignee() != null ? t.getAssignee() : createUnassignedUser()));

        List<WorkloadResponse> response = new ArrayList<>();

        // Xu ly khoi luong cong viec cho tung thanh vien
        for (Map.Entry<User, List<Task>> entry : tasksByUser.entrySet()) {
            User user = entry.getKey();
            List<Task> userTasks = entry.getValue();

            Map<String, List<Task>> tasksByStackKey;

            // Gom nhom chi tiet dua theo yeu cau cua nguoi dung
            if (LABEL_PRIORITY.equalsIgnoreCase(groupBy)) {
                tasksByStackKey = userTasks.stream()
                        .collect(Collectors.groupingBy(t -> t.getPriority() != null ? t.getPriority().name() : LABEL_NONE));
            } else {
                tasksByStackKey = userTasks.stream()
                        .collect(Collectors.groupingBy(t -> t.getStatus() != null ? t.getStatus().getName() : LABEL_UNKNOWN));
            }

            List<WorkloadResponse.WorkloadBreakdown> breakdowns = new ArrayList<>();
            double totalLoad = 0;

            for (Map.Entry<String, List<Task>> stackEntry : tasksByStackKey.entrySet()) {
                String stackName = stackEntry.getKey();
                List<Task> stackTasks = stackEntry.getValue();

                double value;
                if (LABEL_HOURS.equalsIgnoreCase(viewType)) {
                    value = stackTasks.stream()
                            .map(t -> t.getEstimatedHours() != null ? t.getEstimatedHours() : BigDecimal.ZERO)
                            .mapToDouble(BigDecimal::doubleValue).sum();
                } else {
                    value = stackTasks.stream()
                            .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                            .sum();
                }

                String color = getStackColor(stackTasks.get(0), groupBy);

                breakdowns.add(WorkloadResponse.WorkloadBreakdown.builder()
                        .stackName(stackName)
                        .value(value)
                        .taskCount(stackTasks.size())
                        .color(color)
                        .build());

                totalLoad += value;
            }

            response.add(WorkloadResponse.builder()
                    .userId(user.getId())
                    .userName(user.getFullName())
                    .avatarUrl(user.getAvatarUrl())
                    .totalLoad(totalLoad)
                    .breakdowns(breakdowns)
                    .build());
        }

        // Sap xep thanh vien co khoi luong cong viec cao nhat len dau
        response.sort((a, b) -> Double.compare(b.getTotalLoad(), a.getTotalLoad()));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpicProgressResponse> getEpicProgress(
            Integer projectId, 
            Integer sprintId, 
            LocalDate from, LocalDate to,
            List<Integer> statusIds
    ) {
        // Khoi tao bo loc thoi gian va du an
        Specification<Task> spec = TaskSpecification.filterTasksForCalendar(
                projectId, from, to, null, null, null, null
        );

        if (sprintId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sprint").get("id"), sprintId));
        }
        if (statusIds != null && !statusIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").get("id").in(statusIds));
        }

        // Chi lay nhung cong viec thuoc cac Epic
        spec = spec.and((root, query, cb) -> cb.isNotNull(root.get("epic")));

        List<Task> tasks = taskRepository.findAll(spec);

        // Gom nhom theo tung Epic rieng biet
        Map<Epic, List<Task>> tasksByEpic = tasks.stream()
                .collect(Collectors.groupingBy(Task::getEpic));

        List<EpicProgressResponse> response = new ArrayList<>();

        // Tinh toan tien do cho tung Epic
        for (Map.Entry<Epic, List<Task>> entry : tasksByEpic.entrySet()) {
            Epic epic = entry.getKey();
            List<Task> epicTasks = entry.getValue();

            long totalTasks = epicTasks.size();
            long totalPoints = epicTasks.stream().mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0).sum();

            long completedTasks = epicTasks.stream()
                    .filter(t -> t.getStatus() != null && Boolean.TRUE.equals(t.getStatus().getIsCompletedStatus()))
                    .count();
            
            long completedPoints = epicTasks.stream()
                    .filter(t -> t.getStatus() != null && Boolean.TRUE.equals(t.getStatus().getIsCompletedStatus()))
                    .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                    .sum();

            double taskPercent = (totalTasks == 0) ? 0 : (double) completedTasks / totalTasks * PERCENTAGE_MULTIPLIER;
            double pointPercent = (totalPoints == 0) ? 0 : (double) completedPoints / totalPoints * PERCENTAGE_MULTIPLIER;

            response.add(EpicProgressResponse.builder()
                    .epicId(epic.getId())
                    .epicName(epic.getName())
                    .epicCode(epic.getEpicCode())
                    .color(epic.getColor())
                    .totalTasks(totalTasks)
                    .completedTasks(completedTasks)
                    .taskProgressPercent(Math.round(taskPercent * PERCENTAGE_MULTIPLIER) / PERCENTAGE_MULTIPLIER)
                    .totalPoints(totalPoints)
                    .completedPoints(completedPoints)
                    .pointProgressPercent(Math.round(pointPercent * PERCENTAGE_MULTIPLIER) / PERCENTAGE_MULTIPLIER)
                    .build());
        }
        
        // Sap xep uu tien nhung Epic co tien do tot nhat len dau
        response.sort((a, b) -> Double.compare(b.getTaskProgressPercent(), a.getTaskProgressPercent()));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadmapItemResponse> getProjectRoadmap(
            Integer projectId,
            String viewType,
            List<Integer> epicIds,
            List<EpicStatus> epicStatuses,
            List<Integer> sprintIds,
            List<SprintStatus> sprintStatuses,
            String keyword,
            LocalDate from,
            LocalDate to
    ) {
        List<RoadmapItemResponse> roadmapItems = new ArrayList<>();

        // Xu ly the hien lo trinh cho cac Epic
        if (LABEL_EPIC.equalsIgnoreCase(viewType) || LABEL_ALL.equalsIgnoreCase(viewType)) {
            Specification<Epic> spec = EpicSpecification.filterEpicsForRoadmap(
                    projectId, epicIds, epicStatuses, keyword, from, to
            );
            List<Epic> epics = epicRepository.findAll(spec);

            for (Epic epic : epics) {
                List<Task> tasks = taskRepository.findByEpicId(epic.getId());
                long total = tasks.size();
                long completed = tasks.stream()
                        .filter(t -> t.getStatus() != null && Boolean.TRUE.equals(t.getStatus().getIsCompletedStatus()))
                        .count();
                double progress = (total == 0) ? 0 : (double) completed / total * PERCENTAGE_MULTIPLIER;

                LocalDateTime start = epic.getStartDate() != null ? epic.getStartDate().atStartOfDay() : epic.getCreatedAt();
                LocalDateTime end = epic.getDueDate() != null ? epic.getDueDate().atStartOfDay() : start.plusDays(DEFAULT_ROADMAP_DAYS);

                roadmapItems.add(RoadmapItemResponse.builder()
                        .id(PREFIX_EPIC_ID + epic.getId())
                        .originalId(epic.getId())
                        .title(epic.getName())
                        .type(LABEL_EPIC)
                        .startDate(start)
                        .endDate(end)
                        .progress(Math.round(progress * PERCENTAGE_MULTIPLIER) / PERCENTAGE_MULTIPLIER)
                        .status(epic.getStatus().name())
                        .color(epic.getColor() != null ? epic.getColor() : COLOR_PURPLE)
                        .totalTasks(total)
                        .completedTasks(completed)
                        .build());
            }
        }

        // Xu ly the hien lo trinh cho cac Sprint
        if (LABEL_SPRINT.equalsIgnoreCase(viewType) || LABEL_ALL.equalsIgnoreCase(viewType)) {
            Specification<Sprint> spec = SprintSpecification.filterSprintsForRoadmap(
                    projectId, sprintIds, sprintStatuses, keyword, from, to
            );
            List<Sprint> sprints = sprintRepository.findAll(spec);

            for (Sprint sprint : sprints) {
                long total = taskRepository.countBySprint_Id(sprint.getId());
                long incompleteCount = taskRepository.findIncompleteTasksBySprintId(sprint.getId()).size();
                long completed = total - incompleteCount;
                double progress = (total == 0) ? 0 : (double) completed / total * PERCENTAGE_MULTIPLIER;

                String color;
                if (sprint.getStatus() == SprintStatus.IN_PROGRESS) {
                    color = COLOR_LOW; 
                } else if (sprint.getStatus() == SprintStatus.COMPLETED) {
                    color = COLOR_DEFAULT_GRAY; 
                } else {
                    color = COLOR_MEDIUM; 
                }

                LocalDateTime start = sprint.getStartDate() != null ? sprint.getStartDate() : LocalDateTime.now();
                LocalDateTime end = sprint.getEndDate() != null ? sprint.getEndDate() : start.plusDays(DEFAULT_ROADMAP_DAYS);

                roadmapItems.add(RoadmapItemResponse.builder()
                        .id(PREFIX_SPRINT_ID + sprint.getId())
                        .originalId(sprint.getId())
                        .title(sprint.getName())
                        .type(LABEL_SPRINT)
                        .startDate(start)
                        .endDate(end)
                        .progress(Math.round(progress * PERCENTAGE_MULTIPLIER) / PERCENTAGE_MULTIPLIER)
                        .status(sprint.getStatus().name())
                        .color(color)
                        .totalTasks(total)
                        .completedTasks(completed)
                        .build());
            }
        }

        return roadmapItems;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getProjectCalendar(
            Integer projectId,
            LocalDate from, LocalDate to,
            String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType,
            boolean showSprints
    ) {
        // Xac thuc du an co ton tai hay khong
        if (!projectRepository.existsById(projectId)) {
             throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }

        List<CalendarEventResponse> events = new ArrayList<>();

        // Xu ly bieu dien cac cong viec tren lich
        Specification<Task> taskSpec = TaskSpecification.filterTasksForCalendar(
                projectId, from, to, keyword, assigneeId, priority, taskType
        );
        
        List<Task> tasks = taskRepository.findAll(taskSpec);

        for (Task t : tasks) {
            String color = (t.getStatus() != null && t.getStatus().getColor() != null) 
                            ? t.getStatus().getColor() 
                            : COLOR_BLUE; 

            LocalDateTime start = t.getStartDate() != null ? t.getStartDate() : (t.getDueDate() != null ? t.getDueDate() : t.getCreatedAt());
            LocalDateTime end = t.getDueDate() != null ? t.getDueDate() : start.plusHours(1);

            events.add(CalendarEventResponse.builder()
                    .id(PREFIX_TASK_ID + t.getId())
                    .originalId(t.getId())
                    .title(t.getTaskCode() + " - " + t.getTitle())
                    .start(start)
                    .end(end)
                    .allDay(false)
                    .type(LABEL_TASK)
                    .backgroundColor(color)
                    .borderColor(color)
                    .textColor(COLOR_WHITE)
                    .statusName(t.getStatus() != null ? t.getStatus().getName() : LABEL_UNKNOWN)
                    .priority(t.getPriority() != null ? t.getPriority().name() : "")
                    .assigneeName(t.getAssignee() != null ? t.getAssignee().getFullName() : LABEL_UNASSIGNED)
                    .assigneeAvatar(t.getAssignee() != null ? t.getAssignee().getAvatarUrl() : null)
                    .build());
        }

        // Xu ly bieu dien cac Sprint tren lich neu duoc yeu cau
        if (showSprints) {
            Specification<Sprint> sprintSpec = SprintSpecification.filterSprintsForRoadmap(
                    projectId, null, null, keyword, from, to
            );
            
            List<Sprint> sprints = sprintRepository.findAll(sprintSpec);

            for (Sprint s : sprints) {
                String bgColor = COLOR_LIGHT_PURPLE; 
                String bdColor = COLOR_BORDER_PURPLE; 
                
                if (s.getStatus() == SprintStatus.COMPLETED) {
                    bgColor = COLOR_LIGHT_SLATE; 
                    bdColor = COLOR_BORDER_SLATE; 
                }

                LocalDateTime start = s.getStartDate() != null ? s.getStartDate() : LocalDateTime.now();
                LocalDateTime end = s.getEndDate() != null ? s.getEndDate() : start.plusDays(DEFAULT_ROADMAP_DAYS);

                events.add(CalendarEventResponse.builder()
                        .id(PREFIX_SPRINT_ID + s.getId())
                        .originalId(s.getId())
                        .title("Sprint: " + s.getName())
                        .start(start)
                        .end(end)
                        .allDay(true)
                        .type(LABEL_SPRINT)
                        .backgroundColor(bgColor)
                        .borderColor(bdColor)
                        .textColor(COLOR_DARK_GRAY)
                        .statusName(s.getStatus().name())
                        .build());
            }
        }

        return events;
    }

    @Override
    public byte[] exportWorkloadDistributionToExcel(
            List<WorkloadResponse> data, 
            String viewType, 
            String groupBy,
            Integer sprintId,
            LocalDate from,
            LocalDate to
    ) {
        try (Workbook workbook = ExcelHelper.createWorkbook()) {
            Sheet sheet = workbook.createSheet(EXCEL_SHEET_WORKLOAD);

            int rowIdx = 0;

            // Xay dung phan thong tin chung cua bao cao
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(EXCEL_TITLE_WORKLOAD);
            
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            Row dateRow = sheet.createRow(rowIdx++);
            dateRow.createCell(0).setCellValue(EXCEL_LABEL_DATE_EXPORT + LocalDate.now().toString());

            Row configRow = sheet.createRow(rowIdx++);
            String unitText = LABEL_HOURS.equalsIgnoreCase(viewType) ? EXCEL_UNIT_HOURS : EXCEL_UNIT_POINTS;
            String groupText = LABEL_PRIORITY.equalsIgnoreCase(groupBy) ? EXCEL_GROUP_PRIORITY : EXCEL_GROUP_STATUS;
            configRow.createCell(0).setCellValue(EXCEL_LABEL_UNIT + unitText);
            configRow.createCell(3).setCellValue(EXCEL_LABEL_GROUP_BY + groupText);

            Row filterRow = sheet.createRow(rowIdx++);
            String timeRange = (from != null ? from.toString() : EXCEL_NA) + EXCEL_TEXT_TO + (to != null ? to.toString() : EXCEL_NA);
            filterRow.createCell(0).setCellValue(EXCEL_LABEL_TIME_RANGE + timeRange);
            
            if (sprintId != null) {
                filterRow.createCell(3).setCellValue(EXCEL_LABEL_SPRINT_FILTER + sprintId);
            } else {
                filterRow.createCell(3).setCellValue(EXCEL_LABEL_SPRINT_ALL);
            }

            rowIdx++; 

            // Chuan bi tieu de dong cho cac cot du lieu
            String unit = LABEL_HOURS.equalsIgnoreCase(viewType) ? EXCEL_UNIT_SUFFIX_HOURS : EXCEL_UNIT_SUFFIX_POINTS;
            Set<String> dynamicHeaders = new LinkedHashSet<>();
            for (WorkloadResponse userRes : data) {
                if (userRes.getBreakdowns() != null) {
                    for (WorkloadResponse.WorkloadBreakdown bd : userRes.getBreakdowns()) {
                        dynamicHeaders.add(bd.getStackName());
                    }
                }
            }
            List<String> headerList = new ArrayList<>(dynamicHeaders);
            Collections.sort(headerList);

            // Ghi tieu de cac cot vao bao cao
            Row headerRow = sheet.createRow(rowIdx++);
            CellStyle headerStyle = ExcelHelper.createHeaderStyle(workbook);

            int colIdx = 0;
            createCell(headerRow, colIdx++, EXCEL_HEADER_EMP_ID, headerStyle);
            createCell(headerRow, colIdx++, EXCEL_HEADER_EMP_NAME, headerStyle);
            createCell(headerRow, colIdx++, EXCEL_HEADER_TOTAL + unit + ")", headerStyle);

            for (String colName : headerList) {
                createCell(headerRow, colIdx++, colName + " (" + unit + ")", headerStyle);
            }

            // Ghi du lieu vao tung dong cua bao cao
            for (WorkloadResponse item : data) {
                Row row = sheet.createRow(rowIdx++);
                int cellIdx = 0;

                row.createCell(cellIdx++).setCellValue(item.getUserId());
                row.createCell(cellIdx++).setCellValue(item.getUserName());
                row.createCell(cellIdx++).setCellValue(item.getTotalLoad());

                Map<String, Double> breakdownMap = new HashMap<>();
                if (item.getBreakdowns() != null) {
                    breakdownMap = item.getBreakdowns().stream()
                            .collect(Collectors.toMap(WorkloadResponse.WorkloadBreakdown::getStackName, WorkloadResponse.WorkloadBreakdown::getValue));
                }

                for (String headerKey : headerList) {
                    Double val = breakdownMap.getOrDefault(headerKey, 0.0);
                    row.createCell(cellIdx++).setCellValue(val);
                }
            }

            for (int i = 0; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
            }

            return ExcelHelper.workbookToBytes(workbook);

        } catch (IOException e) {
            throw new RuntimeException(ERROR_EXPORT_RUNTIME + e.getMessage());
        }
    }

    @Override
    public byte[] exportEpicProgressToExcel(
            List<EpicProgressResponse> data, 
            Integer projectId, 
            Integer sprintId, 
            LocalDate from, 
            LocalDate to
    ) {
        try (Workbook workbook = ExcelHelper.createWorkbook()) {
            Sheet sheet = workbook.createSheet(EXCEL_SHEET_EPIC_PROGRESS);

            int rowIdx = 0;

            // Xay dung phan thong tin chung cua bao cao
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(EXCEL_TITLE_EPIC_PROGRESS);
            
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            Row dateRow = sheet.createRow(rowIdx++);
            dateRow.createCell(0).setCellValue(EXCEL_LABEL_DATE_EXPORT_EPIC + LocalDate.now().toString());

            Row contextRow = sheet.createRow(rowIdx++);
            contextRow.createCell(0).setCellValue(EXCEL_LABEL_PROJECT_ID + projectId);
            if (sprintId != null) {
                contextRow.createCell(3).setCellValue(EXCEL_LABEL_SPRINT_FILTER_EPIC + sprintId);
            } else {
                contextRow.createCell(3).setCellValue(EXCEL_LABEL_SPRINT_ALL);
            }

            Row timeRow = sheet.createRow(rowIdx++);
            String timeRange = (from != null ? from.toString() : EXCEL_NA) + EXCEL_TEXT_TO + (to != null ? to.toString() : EXCEL_NA);
            timeRow.createCell(0).setCellValue(EXCEL_LABEL_TIME_RANGE + timeRange);

            Row summaryRow = sheet.createRow(rowIdx++);
            summaryRow.createCell(0).setCellValue(EXCEL_LABEL_TOTAL_EPICS + data.size());

            rowIdx++;

            // Ghi tieu de cac cot vao bao cao
            Row headerRow = sheet.createRow(rowIdx++);
            String[] columns = {
                "Mã Epic", "Tên Epic", 
                "Tổng Task", "Xong (Task)", "% Task", 
                "Tổng Points", "Xong (Points)", "% Points"
            };
            
            CellStyle headerStyle = ExcelHelper.createHeaderStyle(workbook);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle percentStyle = workbook.createCellStyle();
            percentStyle.setDataFormat(workbook.createDataFormat().getFormat(EXCEL_PERCENT_FORMAT));

            // Ghi du lieu vao tung dong cua bao cao
            for (EpicProgressResponse epic : data) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;

                row.createCell(col++).setCellValue(epic.getEpicCode());
                row.createCell(col++).setCellValue(epic.getEpicName());
                
                row.createCell(col++).setCellValue(epic.getTotalTasks());
                row.createCell(col++).setCellValue(epic.getCompletedTasks());
                
                Cell taskPerCell = row.createCell(col++);
                taskPerCell.setCellValue(epic.getTaskProgressPercent() / PERCENTAGE_MULTIPLIER); 
                taskPerCell.setCellStyle(percentStyle);

                row.createCell(col++).setCellValue(epic.getTotalPoints());
                row.createCell(col++).setCellValue(epic.getCompletedPoints());
                
                Cell pointPerCell = row.createCell(col++);
                pointPerCell.setCellValue(epic.getPointProgressPercent() / PERCENTAGE_MULTIPLIER);
                pointPerCell.setCellStyle(percentStyle);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return ExcelHelper.workbookToBytes(workbook);

        } catch (IOException e) {
            throw new RuntimeException(ERROR_EXPORT_EPIC_RUNTIME + e.getMessage());
        }
    }

    // --- CAC HAM PRIVATE HO TRO NGHIEP VU ---

    private void createCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private Double calculatePercentage(Long count, Long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((double) count / total * PERCENTAGE_ROUNDING_FACTOR) / PERCENTAGE_MULTIPLIER;
    }

    private List<TaskSummaryResponse> mapList(List<Task> tasks) {
        return tasks.stream()
                .map(projectService::mapToTaskSummaryResponse) 
                .collect(Collectors.toList());
    }

    private String getPriorityColor(TaskPriority priority) {
        switch (priority) {
            case URGENT:
                return COLOR_URGENT; 
            case HIGH:
                return COLOR_HIGH; 
            case MEDIUM:
                return COLOR_MEDIUM; 
            case LOW:
                return COLOR_LOW; 
            default:
                return COLOR_DEFAULT_GRAY;
        }
    }

    private String getTypeColor(TaskType type) {
        switch (type) {
            case BUG:
                return COLOR_URGENT; 
            case STORY:
                return COLOR_LOW; 
            case EPIC:
                return COLOR_PURPLE; 
            case SUBTASK:
                return COLOR_DARK_GRAY_BLUE; 
            case TASK:
                return COLOR_MEDIUM; 
            default:
                return COLOR_DEFAULT_GRAY; 
        }
    }

    private User createUnassignedUser() {
        return User.builder().id(0).fullName(LABEL_UNASSIGNED).build();
    }

    private String getStackColor(Task sampleTask, String groupBy) {
        if (LABEL_PRIORITY.equalsIgnoreCase(groupBy)) {
            if (sampleTask.getPriority() == null) {
                return COLOR_DEFAULT_GRAY;
            }
            return getPriorityColor(sampleTask.getPriority());
        } else {
            return (sampleTask.getStatus() != null) ? sampleTask.getStatus().getColor() : COLOR_DEFAULT_GRAY;
        }
    }
}