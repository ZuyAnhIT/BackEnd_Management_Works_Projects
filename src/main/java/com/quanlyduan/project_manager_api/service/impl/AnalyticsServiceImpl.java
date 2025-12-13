// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/AnalyticsServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.AssigneeRecommendationRequest;
import com.quanlyduan.project_manager_api.dto.response.AssigneeRecommendationResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectForecastResponse;
import com.quanlyduan.project_manager_api.dto.response.StandupReportResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.SprintRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.service.AnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository; 
    
    public AnalyticsServiceImpl(ProjectRepository projectRepository,
                                ProjectMemberRepository projectMemberRepository,
                                SprintRepository sprintRepository,
                                TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssigneeRecommendationResponse> getAssigneeRecommendations(Integer projectId, AssigneeRecommendationRequest request) {
        // 1. Kiểm tra dự án tồn tại
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }

        List<AssigneeRecommendationResponse> results = new ArrayList<>();

        // 2. Lấy danh sách thành viên dự án
        // LƯU Ý: Đảm bảo ProjectMemberRepository đã có hàm findByProject_Id(Integer id) trả về List
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId).stream()
                // SỬA LỖI: .getCode() -> .getRoleCode()
                .filter(pm -> !pm.getRole().getRoleCode().contains("GUEST")) 
                .collect(Collectors.toList());

        // 3. Trích xuất từ khóa chính từ tiêu đề task
        String keyword = extractMainKeyword(request.getTitle());

        // 4. Duyệt qua từng thành viên để chấm điểm
        for (ProjectMember pm : members) {
            User user = pm.getUser();
            double score = 0;
            List<String> reasons = new ArrayList<>();

            // --- A. RELEVANCE SCORE ---
            int similarCount = 0;
            if (!keyword.isEmpty()) {
                similarCount = taskRepository.countCompletedTasksByKeyword(projectId, user.getId(), keyword);
            }
            
            if (similarCount > 0) {
                double relevancePoints = Math.min(similarCount * 5.0, 40.0);
                score += relevancePoints;
                reasons.add("Completed " + similarCount + " similar tasks (" + keyword + ")");
            }

            // --- B. WORKLOAD SCORE ---
            int currentLoad = taskRepository.getCurrentSprintWorkload(projectId, user.getId());
            String workloadStatus;

            if (currentLoad < 5) {
                score += 40; 
                workloadStatus = "LOW";
                reasons.add("Current workload is low");
            } else if (currentLoad < 12) {
                score += 20; 
                workloadStatus = "OPTIMAL";
            } else if (currentLoad < 20) {
                score += 5; 
                workloadStatus = "HIGH";
            } else {
                score -= 20; 
                workloadStatus = "OVERLOADED";
                reasons.add("Currently overloaded (" + currentLoad + " pts)");
            }

            // --- C. PENALTY CHECK ---
            if (request.getStoryPoints() != null && (currentLoad + request.getStoryPoints() > 20)) {
                score -= 10;
                reasons.add("Assigning this would cause overload");
            }

            // --- D. BUILD RESULT ---
            double finalScore = Math.max(0, score);
            
            String reasonText = reasons.isEmpty() 
                    ? "Available for assignment" 
                    : String.join(". ", reasons) + ".";

            results.add(AssigneeRecommendationResponse.builder()
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .avatarUrl(user.getAvatarUrl())
                    .matchScore(finalScore)
                    .currentWorkloadPoints(currentLoad)
                    .similarTasksCompleted(similarCount)
                    .workloadStatus(workloadStatus)
                    .reason(reasonText)
                    .build());
        }

        // 5. Sắp xếp theo điểm số giảm dần và lấy Top 5
        return results.stream()
                .sorted(Comparator.comparingDouble(AssigneeRecommendationResponse::getMatchScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectForecastResponse getProjectForecast(Integer projectId) {
        // 1. Lấy thông tin dự án
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // 2. Tính toán Velocity Lịch sử (Dựa trên 5 Sprint gần nhất)
        List<Sprint> completedSprints = sprintRepository.findTop5ByProject_IdAndStatusOrderByEndDateDesc(
                projectId, SprintStatus.COMPLETED
        );

        List<Integer> velocities = new ArrayList<>();
        double avgSprintDurationDays = 14.0; // Mặc định 2 tuần nếu chưa có dữ liệu

        if (!completedSprints.isEmpty()) {
            long totalDays = 0;
            for (Sprint s : completedSprints) {
                // Lấy tổng point hoàn thành của từng sprint
                int points = taskRepository.sumCompletedPointsBySprintId(s.getId());
                velocities.add(points);
                
                // Tính độ dài trung bình sprint thực tế
                if (s.getStartDate() != null && s.getEndDate() != null) {
                    totalDays += java.time.temporal.ChronoUnit.DAYS.between(s.getStartDate(), s.getEndDate());
                }
            }
            if (totalDays > 0) {
                avgSprintDurationDays = (double) totalDays / completedSprints.size();
            }
        }

        // Xử lý trường hợp dự án mới chưa có sprint nào xong -> Giả định velocity mặc định
        if (velocities.isEmpty()) {
            velocities.add(10); // Giả định 10 point/sprint cho dự án mới
        }

        // 3. Xác định 3 chỉ số Velocity: Min, Max, Avg
        double minVelocity = velocities.stream().mapToInt(v -> v).min().orElse(10);
        double maxVelocity = velocities.stream().mapToInt(v -> v).max().orElse(10);
        double avgVelocity = velocities.stream().mapToInt(v -> v).average().orElse(10.0);
        
        // Tránh chia cho 0
        if (minVelocity == 0) minVelocity = 1; 
        if (avgVelocity == 0) avgVelocity = 1;

        // 4. Lấy khối lượng công việc còn lại (Remaining Backlog)
        int remainingPoints = taskRepository.sumRemainingPoints(projectId);
        
        // Nếu backlog trống, dự án coi như xong
        if (remainingPoints == 0) {
            return ProjectForecastResponse.builder()
                    .riskLevel("NONE")
                    .riskMessage("Dự án đã hoàn thành hết công việc.")
                    .build();
        }

        // 5. Tính toán 3 Kịch bản
        LocalDate now = LocalDate.now();
        LocalDate deadline = project.getDueDate(); // Có thể null

        // A. Kịch bản TỐT NHẤT (Optimistic) - Dùng Max Velocity
        var optimistic = calculateScenario("Optimistic", maxVelocity, remainingPoints, avgSprintDurationDays, now, deadline);

        // B. Kịch bản KHẢ THI (Likely) - Dùng Avg Velocity
        var likely = calculateScenario("Likely", avgVelocity, remainingPoints, avgSprintDurationDays, now, deadline);

        // C. Kịch bản XẤU NHẤT (Pessimistic) - Dùng Min Velocity
        var pessimistic = calculateScenario("Pessimistic", minVelocity, remainingPoints, avgSprintDurationDays, now, deadline);

        // 6. Đánh giá rủi ro tổng quan (Dựa trên kịch bản Khả thi)
        String riskLevel = "LOW";
        String riskMsg = "Dự án đang đi đúng tiến độ.";

        if (deadline != null) {
            if (likely.isLate()) {
                riskLevel = "HIGH";
                riskMsg = "Dự án có nguy cơ trễ hạn khoảng " + likely.getDaysLate() + " ngày.";
            } else if (pessimistic.isLate()) {
                riskLevel = "MEDIUM";
                riskMsg = "Tiến độ ổn, nhưng nếu gặp rủi ro (tốc độ thấp nhất) thì sẽ trễ hạn.";
            }
            
            // Nếu trễ quá 30% thời gian
            if (likely.isLate() && likely.getDaysLate() > 30) {
                riskLevel = "CRITICAL";
                riskMsg = "CẢNH BÁO: Dự án trễ hạn nghiêm trọng (" + likely.getDaysLate() + " ngày). Cần cắt giảm scope hoặc thêm nguồn lực.";
            }
        } else {
            riskMsg = "Dự án chưa thiết lập Deadline.";
        }

        return ProjectForecastResponse.builder()
                .totalBacklogPoints(remainingPoints)
                .averageVelocity(Math.round(avgVelocity * 100.0) / 100.0)
                .projectDueDate(deadline)
                .riskLevel(riskLevel)
                .riskMessage(riskMsg)
                .optimistic(optimistic)
                .likely(likely)
                .pessimistic(pessimistic)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StandupReportResponse getDailyStandupReport(Integer projectId) {
        // 1. Tìm Active Sprint
        Sprint activeSprint = sprintRepository.findActiveSprintByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("No active sprint found for this project."));

        // 2. Lấy tất cả task trong Sprint này (bao gồm cả task đã xong)
        // Chúng ta dùng taskRepository.findBySprint_Id (đã có sẵn hoặc tạo thêm)
        List<Task> tasks = taskRepository.findBySprint_Id(activeSprint.getId());

        // 3. Nhóm Task theo User (Assignee)
        Map<User, List<Task>> tasksByUser = tasks.stream()
                .filter(t -> t.getAssignee() != null) // Bỏ qua task chưa gán ai
                .collect(Collectors.groupingBy(Task::getAssignee));

        List<StandupReportResponse.MemberUpdate> memberUpdates = new ArrayList<>();
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24); // Mốc 24h trước

        // 4. Duyệt qua từng User để phân loại công việc
        for (Map.Entry<User, List<Task>> entry : tasksByUser.entrySet()) {
            User user = entry.getKey();
            List<Task> userTasks = entry.getValue();

            List<String> completed = new ArrayList<>();
            List<String> inProgress = new ArrayList<>();
            List<String> todo = new ArrayList<>();

            for (Task t : userTasks) {
                // A. ĐÃ XONG (Done trong 24h qua)
                if (Boolean.TRUE.equals(t.getStatus().getIsCompletedStatus())) {
                    if (t.getCompletedAt() != null && t.getCompletedAt().isAfter(yesterday)) {
                        completed.add(t.getTaskCode() + " " + t.getTitle());
                    }
                    // Nếu xong lâu rồi thì bỏ qua, không báo cáo lại
                } 
                // B. ĐANG LÀM
                else if ("In Progress".equalsIgnoreCase(t.getStatus().getName()) 
                        || "Doing".equalsIgnoreCase(t.getStatus().getName())) {
                    inProgress.add(t.getTaskCode() + " " + t.getTitle());
                }
                // C. SẼ LÀM (To Do)
                else {
                    todo.add(t.getTaskCode() + " " + t.getTitle());
                }
            }

            // Chỉ thêm vào báo cáo nếu user có hoạt động
            if (!completed.isEmpty() || !inProgress.isEmpty()) {
                memberUpdates.add(StandupReportResponse.MemberUpdate.builder()
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .completedTasks(completed)
                        .inProgressTasks(inProgress)
                        .todoTasks(todo)
                        .build());
            }
        }

        return StandupReportResponse.builder()
                .sprintName(activeSprint.getName())
                .reportDate(LocalDate.now())
                .members(memberUpdates)
                .build();
    }

    // METHOD HEPLER
    private String extractMainKeyword(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String cleanText = text.replaceAll("[^a-zA-Z0-9\\s]", "");
        return Arrays.stream(cleanText.split("\\s+"))
                .filter(w -> w.length() > 3)
                .max(Comparator.comparingInt(String::length))
                .orElse("");
    }

    // Helper tính toán kịch bản
    private ProjectForecastResponse.ForecastScenario calculateScenario(
            String name, double velocity, int remainingPoints, double sprintDuration, LocalDate now, LocalDate deadline) {
        
        double sprintsNeeded = remainingPoints / velocity;
        long daysNeeded = (long) Math.ceil(sprintsNeeded * sprintDuration);
        LocalDate completionDate = now.plusDays(daysNeeded);
        
        boolean isLate = false;
        int daysLate = 0;
        
        if (deadline != null && completionDate.isAfter(deadline)) {
            isLate = true;
            daysLate = (int) java.time.temporal.ChronoUnit.DAYS.between(deadline, completionDate);
        }

        return ProjectForecastResponse.ForecastScenario.builder()
                .name(name)
                .velocityUsed(Math.round(velocity * 100.0) / 100.0)
                .sprintsNeeded(Math.round(sprintsNeeded * 10.0) / 10.0)
                .completionDate(completionDate)
                .isLate(isLate)
                .daysLate(daysLate)
                .build();
    }
}