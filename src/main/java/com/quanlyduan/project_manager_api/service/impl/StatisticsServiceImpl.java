// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/StatisticsServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.response.EpicProgressResponse;
import com.quanlyduan.project_manager_api.dto.response.PriorityDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.StatisticsResponse;
import com.quanlyduan.project_manager_api.dto.response.StatusDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskTypeDistributionResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkloadResponse;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.specification.TaskSpecification;
import com.quanlyduan.project_manager_api.service.StatisticsService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.model.Epic;
import com.quanlyduan.project_manager_api.model.ProjectStatus;

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

    public StatisticsServiceImpl(TaskRepository taskRepository, ProjectServiceImpl projectService) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
    }

    // 1. Thống kê tuần (Weekly Statistics)
    @Transactional(readOnly = true)
    public StatisticsResponse getWeeklyStatistics(Integer projectId, Integer assigneeId) {

        // 1. Cấu hình thời gian
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        LocalDateTime dueSoonLimit = endDate.plusDays(3); // Sắp hết hạn trong 3 ngày tới

        // 2. Giới hạn số lượng items trả về trong list (Ví dụ: Top 10)
        // Để tránh payload quá nặng
        Pageable limit = PageRequest.of(0, 10);

        // 3. --- TRUY VẤN DỮ LIỆU ---

        // A. Task Sắp đến hạn (Due Soon)
        List<Task> dueTasks = taskRepository.findTasksDueSoon(projectId, assigneeId, endDate, dueSoonLimit);
        long dueCount = dueTasks.size(); // Hoặc count riêng nếu list bị limit

        // B. Task Đã tạo (Created)
        long createdCount = taskRepository.countCreatedTasks(projectId, assigneeId, startDate, endDate);
        List<Task> createdTasks = taskRepository.findCreatedTasks(projectId, assigneeId, startDate, endDate, limit);

        // C. Task Đã hoàn thành (Completed)
        long completedCount = taskRepository.countCompletedTasks(projectId, assigneeId, startDate, endDate);
        List<Task> completedTasks = taskRepository.findCompletedTasks(projectId, assigneeId, startDate, endDate, limit);

        // D. Task Đã cập nhật (Updated)
        long updatedCount = taskRepository.countUpdatedTasks(projectId, assigneeId, startDate, endDate);
        List<Task> updatedTasks = taskRepository.findUpdatedTasks(projectId, assigneeId, startDate, endDate, limit);

        // 4. --- MAPPING DTO ---
        // Sử dụng hàm helper để code gọn hơn

        return StatisticsResponse.builder()
                .fromDate(startDate.toString())
                .toDate(endDate.toString())

                .dueSoonCount(dueCount)
                .dueSoonTasks(mapList(dueTasks))

                .createdCount(createdCount)
                .createdTasks(mapList(createdTasks))

                .completedCount(completedCount)
                .completedTasks(mapList(completedTasks))

                .updatedCount(updatedCount)
                .updatedTasks(mapList(updatedTasks))
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