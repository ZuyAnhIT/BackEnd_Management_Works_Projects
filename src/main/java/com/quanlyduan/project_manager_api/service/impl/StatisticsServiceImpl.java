// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/StatisticsServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.EpicProgressResponse;
import com.quanlyduan.project_manager_api.dto.response.PriorityDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.RoadmapItemResponse;
import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;
import com.quanlyduan.project_manager_api.dto.response.StatusDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskTypeDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkloadResponse;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.EpicStatus;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.repository.EpicRepository;
import com.quanlyduan.project_manager_api.repository.SprintRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.specification.EpicSpecification;
import com.quanlyduan.project_manager_api.repository.specification.SprintSpecification;
import com.quanlyduan.project_manager_api.repository.specification.TaskSpecification;
import com.quanlyduan.project_manager_api.service.StatisticsService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.ProjectStatus;
import com.quanlyduan.project_manager_api.model.Sprint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final TaskRepository taskRepository;
    private final ProjectServiceImpl projectService; // Để dùng mapper
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;

    public StatisticsServiceImpl(TaskRepository taskRepository, ProjectServiceImpl projectService, EpicRepository epicRepository, SprintRepository sprintRepository) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.epicRepository = epicRepository;
        this.sprintRepository = sprintRepository;
    }

    // 1. Thống kê tổng quan
    @Override
    @Transactional(readOnly = true)
    public StatisticsResponse getOverviewStatistics(
            Integer projectId, Integer assigneeId,
            LocalDate from, LocalDate to,
            String keyword, Integer filterAssigneeId,
            TaskPriority priority, TaskType taskType, List<Integer> statusIds
    ) {
        // Xử lý assigneeId: 
        // Nếu API gọi từ "/me" -> assigneeId có giá trị (User đang login)
        // Nếu API gọi từ "/projects" -> assigneeId = null, nhưng user có thể lọc theo `filterAssigneeId`
        Integer finalAssigneeId = (assigneeId != null) ? assigneeId : filterAssigneeId;

        // 1. Tạo Base Specification (Chứa các điều kiện chung: Project, User, Priority...)
        Specification<Task> baseSpec = TaskSpecification.filterBase(
                projectId, finalAssigneeId, keyword, priority, taskType, statusIds
        );

        // 2. --- TÍNH TOÁN CÁC CHỈ SỐ ---

        // A. Số lượng TẠO mới (Base + CreatedAt Between)
        Specification<Task> createdSpec = baseSpec.and(TaskSpecification.createdBetween(from, to));
        long createdCount = taskRepository.count(createdSpec);
        
        // Lấy danh sách chi tiết (Top 5 mới nhất)
        Pageable top5Newest = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        List<Task> createdTasks = taskRepository.findAll(createdSpec, top5Newest).getContent();


        // B. Số lượng HOÀN THÀNH (Base + CompletedAt Between)
        Specification<Task> completedSpec = baseSpec.and(TaskSpecification.completedBetween(from, to));
        long completedCount = taskRepository.count(completedSpec);
        
        Pageable top5Completed = PageRequest.of(0, 5, Sort.by("completedAt").descending());
        List<Task> completedTasksList = taskRepository.findAll(completedSpec, top5Completed).getContent();


        // C. Số lượng CẬP NHẬT (Base + UpdatedAt Between)
        Specification<Task> updatedSpec = baseSpec.and(TaskSpecification.updatedBetween(from, to));
        long updatedCount = taskRepository.count(updatedSpec);
        
        Pageable top5Updated = PageRequest.of(0, 5, Sort.by("updatedAt").descending());
        List<Task> updatedTasks = taskRepository.findAll(updatedSpec, top5Updated).getContent();


        // D. SẮP ĐẾN HẠN (Base + DueDate Between + Not Done)
        // "Sắp đến hạn" thường là tính từ Hiện tại -> Tương lai gần (ví dụ: 3 ngày tới hoặc 7 ngày tới)
        // Hoặc theo khoảng 'to' mà user chọn. Ở đây ta lấy theo khoảng user chọn để linh hoạt.
        Specification<Task> dueSoonSpec = baseSpec.and(TaskSpecification.dueBetweenAndNotDone(from, to));
        
        // Nếu user chọn khoảng quá khứ, dueSoon có thể = 0. 
        // Nếu muốn luôn hiển thị "Sắp đến hạn tính từ bây giờ", bạn có thể override `from` = LocalDate.now() ở đây.
        // Ví dụ: LocalDate dueFrom = LocalDate.now();
        //        LocalDate dueTo = dueFrom.plusDays(7);
        //        Specification<Task> dueSoonSpec = baseSpec.and(TaskSpecification.dueBetweenAndNotDone(dueFrom, dueTo));
        
        long dueSoonCount = taskRepository.count(dueSoonSpec);
        
        // Lấy danh sách (Top 10 sắp hết hạn nhất -> Sort DueDate ASC)
        Pageable top10Due = PageRequest.of(0, 10, Sort.by("dueDate").ascending());
        List<Task> dueSoonTasks = taskRepository.findAll(dueSoonSpec, top10Due).getContent();


        // 3. --- MAPPING & TRẢ VỀ ---
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

    // 2. Dữ liệu phân bổ trạng thái (Pie Chart Data)
    @Transactional(readOnly = true)
    public List<StatusDistributionResponse> getTaskStatusDistribution(Integer projectId, Integer assigneeId) {

        // 1. Gọi Repo lấy dữ liệu thô (Group By)
        List<Object[]> results = taskRepository.countTasksByStatusGroup(projectId, assigneeId);

        // 2. Tính tổng số task để tính phần trăm
        long totalTasks = results.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        // 3. Map sang DTO
        List<StatusDistributionResponse> responseList = new ArrayList<>();

        for (Object[] row : results) {
            ProjectStatus status = (ProjectStatus) row[0]; // Phần tử 0 là Entity Status
            Long count = (Long) row[1]; // Phần tử 1 là Count

            // Xử lý trường hợp status bị null (nếu có task chưa gán status)
            if (status == null) {
                responseList.add(StatusDistributionResponse.builder()
                        .statusId(null)
                        .statusName("Unassigned") // Hoặc "No Status"
                        .color("#95a5a6") // Màu xám
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

    // 3. Dữ liệu phân bổ mức độ ưu tiên (Priority Distribution)
    @Override
    @Transactional(readOnly = true)
    public List<PriorityDistributionResponse> getTaskPriorityDistribution(Integer projectId, Integer assigneeId) {

        // 1. Gọi Repo lấy dữ liệu thô
        List<Object[]> results = taskRepository.countTasksByPriorityGroup(projectId, assigneeId);

        // 2. Tính tổng để tính phần trăm
        long totalTasks = results.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        List<PriorityDistributionResponse> responseList = new ArrayList<>();

        for (Object[] row : results) {
            TaskPriority priority = (TaskPriority) row[0];
            Long count = (Long) row[1];

            // Xử lý trường hợp priority null (nếu có data cũ)
            if (priority == null) {
                responseList.add(PriorityDistributionResponse.builder()
                        .priorityName("Unknown")
                        .priorityCode("UNKNOWN")
                        .color("#bdc3c7") // Xám nhạt
                        .taskCount(count)
                        .percentage(calculatePercentage(count, totalTasks))
                        .build());
                continue;
            }

            responseList.add(PriorityDistributionResponse.builder()
                    .priorityName(priority.name()) // Hoặc switch-case để lấy tên đẹp hơn
                    .priorityCode(priority.name())
                    .color(getPriorityColor(priority)) // Hàm lấy màu
                    .taskCount(count)
                    .percentage(calculatePercentage(count, totalTasks))
                    .build());
        }

        return responseList;
    }

    // 4. Dữ liệu phân bổ loại công việc (Task Type Distribution)
    @Override
    @Transactional(readOnly = true)
    public List<TaskTypeDistributionResponse> getTaskTypeDistribution(Integer projectId, Integer assigneeId) {

        // 1. Query dữ liệu thô
        List<Object[]> results = taskRepository.countTasksByTypeGroup(projectId, assigneeId);

        // 2. Tính tổng
        long totalTasks = results.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        List<TaskTypeDistributionResponse> responseList = new ArrayList<>();

        for (Object[] row : results) {
            TaskType type = (TaskType) row[0];
            Long count = (Long) row[1];

            if (type == null)
                continue; // Bỏ qua nếu dữ liệu lỗi không có type

            responseList.add(TaskTypeDistributionResponse.builder()
                    .typeName(type.name()) // Hoặc format tên đẹp hơn (Title Case)
                    .typeCode(type.name())
                    .color(getTypeColor(type)) // Hàm lấy màu
                    .taskCount(count)
                    .percentage(calculatePercentage(count, totalTasks))
                    .build());
        }

        return responseList;
    }

    // 5. Dữ liệu phân bổ công việc (Workload Distribution - Stacked Bar Chart)
    @Override
    @Transactional(readOnly = true)
    public List<WorkloadResponse> getWorkloadDistribution(
            Integer projectId, String viewType, String groupBy,
            Integer sprintId, LocalDate from, LocalDate to, List<Integer> statusIds) {

        // 1. Tận dụng Specification cũ để lọc dữ liệu (DRY - Don't Repeat Yourself)
        // Chúng ta tái sử dụng filterTasksForCalendar vì nó có logic lọc theo Date
        // Range rất tốt
        Specification<Task> spec = TaskSpecification.filterTasksForCalendar(
                projectId, from, to, null, null, null, null // null các param không dùng
        );

        // Nếu có lọc theo sprint hoặc status cụ thể thì add thêm
        if (sprintId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sprint").get("id"), sprintId));
        }
        if (statusIds != null && !statusIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").get("id").in(statusIds));
        }

        // 2. Lấy dữ liệu thô
        List<Task> tasks = taskRepository.findAll(spec);

        // 3. Gom nhóm theo User (Assignee)
        Map<User, List<Task>> tasksByUser = tasks.stream()
                .collect(
                        Collectors.groupingBy(t -> t.getAssignee() != null ? t.getAssignee() : createUnassignedUser()));

        // 4. Xử lý từng User để tạo cột (Bar)
        List<WorkloadResponse> response = new ArrayList<>();

        for (Map.Entry<User, List<Task>> entry : tasksByUser.entrySet()) {
            User user = entry.getKey();
            List<Task> userTasks = entry.getValue();

            // 4a. Gom nhóm con (Breakdown) theo Status hoặc Priority
            Map<String, List<Task>> tasksByStackKey = new HashMap<>();

            if ("PRIORITY".equalsIgnoreCase(groupBy)) {
                tasksByStackKey = userTasks.stream()
                        .collect(Collectors.groupingBy(t -> t.getPriority() != null ? t.getPriority().name() : "NONE"));
            } else {
                // Mặc định group by STATUS
                tasksByStackKey = userTasks.stream()
                        .collect(Collectors
                                .groupingBy(t -> t.getStatus() != null ? t.getStatus().getName() : "Unknown"));
            }

            // 4b. Tính toán giá trị cho từng Stack Segment
            List<WorkloadResponse.WorkloadBreakdown> breakdowns = new ArrayList<>();
            double totalLoad = 0;

            for (Map.Entry<String, List<Task>> stackEntry : tasksByStackKey.entrySet()) {
                String stackName = stackEntry.getKey();
                List<Task> stackTasks = stackEntry.getValue();

                // Tính tổng value (Points hoặc Hours)
                double value = 0;
                if ("HOURS".equalsIgnoreCase(viewType)) {
                    value = stackTasks.stream()
                            .map(t -> t.getEstimatedHours() != null ? t.getEstimatedHours() : BigDecimal.ZERO)
                            .mapToDouble(BigDecimal::doubleValue).sum();
                } else {
                    // Mặc định POINTS
                    value = stackTasks.stream()
                            .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                            .sum();
                }

                // Lấy màu (Lấy từ Status object hoặc map cứng cho Priority)
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

        // Sắp xếp theo tổng load giảm dần (người bận nhất lên đầu)
        response.sort((a, b) -> Double.compare(b.getTotalLoad(), a.getTotalLoad()));

        return response;
    }


    // 6.Tiến độ của các Epic trong dự án
    @Override
    @Transactional(readOnly = true)
    public List<EpicProgressResponse> getEpicProgress(
            Integer projectId, 
            Integer sprintId, 
            LocalDate from, LocalDate to,
            List<Integer> statusIds // Filter: Chỉ tính các task thuộc trạng thái này (nếu cần)
    ) {
        
        // 1. Lấy danh sách Task theo bộ lọc (Sử dụng lại Specification)
        // Lưu ý: assigneeId = null để lấy toàn bộ team
        Specification<Task> spec = TaskSpecification.filterTasksForCalendar(
                projectId, from, to, null, null, null, null
        );

        // Filter bổ sung (Sprint, Status)
        if (sprintId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sprint").get("id"), sprintId));
        }
        if (statusIds != null && !statusIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").get("id").in(statusIds));
        }

        // Chỉ lấy những task CÓ Epic
        spec = spec.and((root, query, cb) -> cb.isNotNull(root.get("epic")));

        List<Task> tasks = taskRepository.findAll(spec);

        // 2. Gom nhóm theo Epic
        Map<Epic, List<Task>> tasksByEpic = tasks.stream()
                .collect(Collectors.groupingBy(Task::getEpic));

        // 3. Tính toán KPI cho từng Epic
        List<EpicProgressResponse> response = new ArrayList<>();

        for (Map.Entry<Epic, List<Task>> entry : tasksByEpic.entrySet()) {
            Epic epic = entry.getKey();
            List<Task> epicTasks = entry.getValue();

            // Tính tổng
            long totalTasks = epicTasks.size();
            long totalPoints = epicTasks.stream().mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0).sum();

            // Tính hoàn thành (Dựa vào flag isCompletedStatus trong ProjectStatus)
            long completedTasks = epicTasks.stream()
                    .filter(t -> t.getStatus() != null && Boolean.TRUE.equals(t.getStatus().getIsCompletedStatus()))
                    .count();
            
            long completedPoints = epicTasks.stream()
                    .filter(t -> t.getStatus() != null && Boolean.TRUE.equals(t.getStatus().getIsCompletedStatus()))
                    .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                    .sum();

            // Tính phần trăm
            double taskPercent = (totalTasks == 0) ? 0 : (double) completedTasks / totalTasks * 100;
            double pointPercent = (totalPoints == 0) ? 0 : (double) completedPoints / totalPoints * 100;

            response.add(EpicProgressResponse.builder()
                    .epicId(epic.getId())
                    .epicName(epic.getName())
                    .epicCode(epic.getEpicCode())
                    .color(epic.getColor())
                    
                    .totalTasks(totalTasks)
                    .completedTasks(completedTasks)
                    .taskProgressPercent(Math.round(taskPercent * 100.0) / 100.0) // Làm tròn 2 số thập phân
                    
                    .totalPoints(totalPoints)
                    .completedPoints(completedPoints)
                    .pointProgressPercent(Math.round(pointPercent * 100.0) / 100.0)
                    .build());
        }
        
        // Sắp xếp theo tiến độ giảm dần (hoặc theo tên)
        response.sort((a, b) -> Double.compare(b.getTaskProgressPercent(), a.getTaskProgressPercent()));

        return response;
    }

    // ======================================================
    // API ROADMAP / TIMELINE (GANTT CHART)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<RoadmapItemResponse> getProjectRoadmap(
            Integer projectId,
            String viewType,
            // Epic Filters
            List<Integer> epicIds,
            List<EpicStatus> epicStatuses,
            // Sprint Filters
            List<Integer> sprintIds,
            List<SprintStatus> sprintStatuses,
            // Common Filters
            String keyword,
            LocalDate from,
            LocalDate to
    ) {
        List<RoadmapItemResponse> roadmapItems = new ArrayList<>();

        // -------------------------------------------------
        // 1. XỬ LÝ EPICS
        // -------------------------------------------------
        if ("EPIC".equalsIgnoreCase(viewType) || "ALL".equalsIgnoreCase(viewType)) {
            Specification<Epic> spec = EpicSpecification.filterEpicsForRoadmap(
                    projectId, epicIds, epicStatuses, keyword, from, to
            );
            List<Epic> epics = epicRepository.findAll(spec);

            for (Epic epic : epics) {
                // Tính toán tiến độ Epic
                List<Task> tasks = taskRepository.findByEpicId(epic.getId());
                long total = tasks.size();
                long completed = tasks.stream()
                        .filter(t -> t.getStatus() != null && Boolean.TRUE.equals(t.getStatus().getIsCompletedStatus()))
                        .count();
                double progress = (total == 0) ? 0 : (double) completed / total * 100;

                // Xử lý thời gian hiển thị
                LocalDateTime start = epic.getStartDate() != null ? epic.getStartDate().atStartOfDay() : epic.getCreatedAt();
                LocalDateTime end = epic.getDueDate() != null ? epic.getDueDate().atStartOfDay() : start.plusDays(14);

                roadmapItems.add(RoadmapItemResponse.builder()
                        .id("epic-" + epic.getId())
                        .originalId(epic.getId())
                        .title(epic.getName())
                        .type("EPIC")
                        .startDate(start)
                        .endDate(end)
                        .progress(Math.round(progress * 100.0) / 100.0)
                        .status(epic.getStatus().name())
                        .color(epic.getColor() != null ? epic.getColor() : "#9b59b6") // Tím
                        .totalTasks(total)
                        .completedTasks(completed)
                        .build());
            }
        }

        // -------------------------------------------------
        // 2. XỬ LÝ SPRINTS
        // -------------------------------------------------
        if ("SPRINT".equalsIgnoreCase(viewType) || "ALL".equalsIgnoreCase(viewType)) {
            Specification<Sprint> spec = SprintSpecification.filterSprintsForRoadmap(
                    projectId, sprintIds, sprintStatuses, keyword, from, to
            );
            List<Sprint> sprints = sprintRepository.findAll(spec);

            for (Sprint sprint : sprints) {
                // Tính toán tiến độ Sprint
                long total = taskRepository.countBySprint_Id(sprint.getId());
                long incompleteCount = taskRepository.findIncompleteTasksBySprintId(sprint.getId()).size();
                long completed = total - incompleteCount;
                double progress = (total == 0) ? 0 : (double) completed / total * 100;

                // Màu sắc
                String color;
                if (sprint.getStatus() == SprintStatus.IN_PROGRESS) color = "#2ecc71"; // Xanh lá
                else if (sprint.getStatus() == SprintStatus.COMPLETED) color = "#95a5a6"; // Xám
                else color = "#3498db"; // Xanh dương

                // Thời gian hiển thị
                LocalDateTime start = sprint.getStartDate() != null ? sprint.getStartDate() : LocalDateTime.now();
                LocalDateTime end = sprint.getEndDate() != null ? sprint.getEndDate() : start.plusDays(14);

                roadmapItems.add(RoadmapItemResponse.builder()
                        .id("sprint-" + sprint.getId())
                        .originalId(sprint.getId())
                        .title(sprint.getName())
                        .type("SPRINT")
                        .startDate(start)
                        .endDate(end)
                        .progress(Math.round(progress * 100.0) / 100.0)
                        .status(sprint.getStatus().name())
                        .color(color)
                        .totalTasks(total)
                        .completedTasks(completed)
                        .build());
            }
        }

        return roadmapItems;
    }

    // HEPER METHODS

    // Helper tính phần trăm làm tròn 2 chữ số
    private Double calculatePercentage(Long count, Long total) {
        if (total == 0)
            return 0.0;
        return Math.round((double) count / total * 10000.0) / 100.0;
    }

    // Helper: Map List<Entity> -> List<DTO>
    private List<TaskSummaryResponse> mapList(List<Task> tasks) {
        return tasks.stream()
                .map(projectService::mapToTaskSummaryResponse) // Cần đảm bảo hàm này là PUBLIC trong ProjectService
                .collect(Collectors.toList());
    }

    // Helper: Định nghĩa màu sắc cho từng mức ưu tiên (Chuẩn Jira/Traffic Light)
    private String getPriorityColor(TaskPriority priority) {
        switch (priority) {
            case URGENT:
                return "#e74c3c"; // Đỏ (Nguy hiểm)
            case HIGH:
                return "#e67e22"; // Cam (Cảnh báo)
            case MEDIUM:
                return "#3498db"; // Xanh dương (Bình thường)
            case LOW:
                return "#2ecc71"; // Xanh lá (Thư thả)
            default:
                return "#95a5a6";
        }
    }

    // Helper: Định nghĩa màu sắc chuẩn cho từng loại task
    private String getTypeColor(TaskType type) {
        switch (type) {
            case BUG:
                return "#e74c3c"; // Đỏ
            case STORY:
                return "#2ecc71"; // Xanh lá
            case EPIC:
                return "#9b59b6"; // Tím
            case SUBTASK:
                return "#34495e"; // Xám xanh
            case TASK:
                return "#3498db"; // Xanh dương
            default:
                return "#95a5a6"; // Xám nhạt
        }

    }

    // Helper: Tạo User "Unassigned" giả để nhóm task chưa có assignee
    private User createUnassignedUser() {
        return User.builder().id(0).fullName("Unassigned").build();
    }

    // Helper: Lấy màu cho từng Stack Segment dựa trên groupBy
    private String getStackColor(Task sampleTask, String groupBy) {
        if ("PRIORITY".equalsIgnoreCase(groupBy)) {
            // Map màu Priority cứng (như bài trước)
            if (sampleTask.getPriority() == null)
                return "#ccc";
            switch (sampleTask.getPriority()) {
                case URGENT:
                    return "#e74c3c";
                case HIGH:
                    return "#e67e22";
                case MEDIUM:
                    return "#3498db";
                case LOW:
                    return "#2ecc71";
                default:
                    return "#95a5a6";
            }
        } else {
            // Group by Status -> Lấy màu động từ DB
            return (sampleTask.getStatus() != null) ? sampleTask.getStatus().getColor() : "#ccc";
        }

    }
}