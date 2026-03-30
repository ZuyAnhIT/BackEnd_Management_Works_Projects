package com.quanlyduan.project_manager_api.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    // Khai bao hang so de loai bo hardcode
    public static final String ERROR_PROJECT_NOT_FOUND = "Project not found with ID: ";
    public static final String ERROR_PROJECT_NOT_FOUND_GENERAL = "Project not found";
    public static final String ERROR_NO_ACTIVE_SPRINT = "No active sprint found for this project.";

    public static final String ROLE_GUEST = "GUEST";
    public static final String REGEX_SPECIAL_CHARS = "[^a-zA-Z0-9\\s]";
    public static final String SPACE_DELIMITER = "\\s+";

    public static final String WORKLOAD_LOW = "LOW";
    public static final String WORKLOAD_OPTIMAL = "OPTIMAL";
    public static final String WORKLOAD_HIGH = "HIGH";
    public static final String WORKLOAD_OVERLOADED = "OVERLOADED";

    public static final String RISK_NONE = "NONE";
    public static final String RISK_LOW = "LOW";
    public static final String RISK_MEDIUM = "MEDIUM";
    public static final String RISK_HIGH = "HIGH";
    public static final String RISK_CRITICAL = "CRITICAL";

    public static final String SCENARIO_OPTIMISTIC = "Optimistic";
    public static final String SCENARIO_LIKELY = "Likely";
    public static final String SCENARIO_PESSIMISTIC = "Pessimistic";

    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_DOING = "Doing";

    public static final String MSG_COMPLETED_SIMILAR_TASKS = "Completed %d similar tasks (%s)";
    public static final String MSG_WORKLOAD_LOW = "Current workload is low";
    public static final String MSG_WORKLOAD_OVERLOADED = "Currently overloaded (%d pts)";
    public static final String MSG_ASSIGN_OVERLOAD_WARNING = "Assigning this would cause overload";
    public static final String MSG_AVAILABLE_ASSIGNMENT = "Available for assignment";

    public static final String MSG_RISK_COMPLETED = "Project has completed all tasks.";
    public static final String MSG_RISK_ON_TRACK = "Project is on schedule.";
    public static final String MSG_RISK_LATE_WARNING = "Project is at risk of being late by approximately %d days.";
    public static final String MSG_RISK_PESSIMISTIC_WARNING = "Progress is stable, but at risk of delay in pessimistic scenario.";
    public static final String MSG_RISK_CRITICAL_DELAY = "WARNING: Project is critically delayed (%d days). Consider reducing scope or adding resources.";
    public static final String MSG_RISK_NO_DEADLINE = "Project deadline is not set.";

    public static final int SCORE_WORKLOAD_LOW = 40;
    public static final int SCORE_WORKLOAD_OPTIMAL = 20;
    public static final int SCORE_WORKLOAD_HIGH = 5;
    public static final int PENALTY_OVERLOAD = 20;
    public static final int PENALTY_ASSIGN_OVERLOAD = 10;
    public static final int MAX_RECOMMENDATIONS = 5;
    public static final int MIN_KEYWORD_LENGTH = 3;
    public static final int MAX_RELEVANCE_SCORE = 40;

    // Khai bao cac bien phu thuoc
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;

    // Constructor thay the cho @RequiredArgsConstructor
    public AnalyticsServiceImpl(ProjectRepository projectRepository,
                                ProjectMemberRepository projectMemberRepository,
                                SprintRepository sprintRepository,
                                TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional(readOnly = true)
    public List<AssigneeRecommendationResponse> getAssigneeRecommendations(Integer projectId, AssigneeRecommendationRequest request) {
        // Kiem tra du an ton tai
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND + projectId);
        }

        List<AssigneeRecommendationResponse> results = new ArrayList<>();

        // Lay danh sach thanh vien du an (Loai bo Guest)
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId).stream()
                .filter(pm -> !pm.getRole().getRoleCode().contains(ROLE_GUEST))
                .collect(Collectors.toList());

        // Trich xuat tu khoa chinh tu tieu de task
        String keyword = extractMainKeyword(request.getTitle());

        // Duyet qua tung thanh vien de cham diem phu hop
        for (ProjectMember pm : members) {
            User user = pm.getUser();
            double score = 0;
            List<String> reasons = new ArrayList<>();

            // Tinh diem dua tren kinh nghiem lam task tuong tu
            int similarCount = 0;
            if (!keyword.isEmpty()) {
                similarCount = taskRepository.countCompletedTasksByKeyword(projectId, user.getId(), keyword);
            }
            
            if (similarCount > 0) {
                double relevancePoints = Math.min(similarCount * 5.0, MAX_RELEVANCE_SCORE);
                score += relevancePoints;
                reasons.add(String.format(MSG_COMPLETED_SIMILAR_TASKS, similarCount, keyword));
            }

            // Tinh diem dua tren khoi luong cong viec hien tai
            int currentLoad = taskRepository.getCurrentSprintWorkload(projectId, user.getId());
            String workloadStatus;

            if (currentLoad < 5) {
                score += SCORE_WORKLOAD_LOW;
                workloadStatus = WORKLOAD_LOW;
                reasons.add(MSG_WORKLOAD_LOW);
            } else if (currentLoad < 12) {
                score += SCORE_WORKLOAD_OPTIMAL;
                workloadStatus = WORKLOAD_OPTIMAL;
            } else if (currentLoad < 20) {
                score += SCORE_WORKLOAD_HIGH;
                workloadStatus = WORKLOAD_HIGH;
            } else {
                score -= PENALTY_OVERLOAD;
                workloadStatus = WORKLOAD_OVERLOADED;
                reasons.add(String.format(MSG_WORKLOAD_OVERLOADED, currentLoad));
            }

            // Phat diem neu viec gan them task gay qua tai
            if (request.getStoryPoints() != null && (currentLoad + request.getStoryPoints() > 20)) {
                score -= PENALTY_ASSIGN_OVERLOAD;
                reasons.add(MSG_ASSIGN_OVERLOAD_WARNING);
            }

            // Tong hop ket qua cham diem
            double finalScore = Math.max(0, score);
            String reasonText = reasons.isEmpty() ? MSG_AVAILABLE_ASSIGNMENT : String.join(". ", reasons) + ".";

            // Map du lieu tra ve thong qua helper
            results.add(buildAssigneeRecommendationResponse(user, finalScore, currentLoad, similarCount, workloadStatus, reasonText));
        }

        // Sap xep theo diem so giam dan va lay danh sach tot nhat
        return results.stream()
                .sorted(Comparator.comparingDouble(AssigneeRecommendationResponse::getMatchScore).reversed())
                .limit(MAX_RECOMMENDATIONS)
                .collect(Collectors.toList());
    }

    // Ham private ho tro cho getAssigneeRecommendations
    private String extractMainKeyword(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        String cleanText = text.replaceAll(REGEX_SPECIAL_CHARS, "");
        return Arrays.stream(cleanText.split(SPACE_DELIMITER))
                .filter(w -> w.length() > MIN_KEYWORD_LENGTH)
                .max(Comparator.comparingInt(String::length))
                .orElse("");
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectForecastResponse getProjectForecast(Integer projectId) {
        // Lay thong tin du an
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_PROJECT_NOT_FOUND_GENERAL));

        // Tinh toan van toc (Velocity) lich su dua tren 5 Sprint gan nhat
        List<Sprint> completedSprints = sprintRepository.findTop5ByProject_IdAndStatusOrderByEndDateDesc(
                projectId, SprintStatus.COMPLETED
        );

        List<Integer> velocities = new ArrayList<>();
        double avgSprintDurationDays = 14.0;

        if (!completedSprints.isEmpty()) {
            long totalDays = 0;
            for (Sprint s : completedSprints) {
                int points = taskRepository.sumCompletedPointsBySprintId(s.getId());
                velocities.add(points);
                
                if (s.getStartDate() != null && s.getEndDate() != null) {
                    totalDays += ChronoUnit.DAYS.between(s.getStartDate(), s.getEndDate());
                }
            }
            if (totalDays > 0) {
                avgSprintDurationDays = (double) totalDays / completedSprints.size();
            }
        }

        // Xu ly mac dinh neu chua co lich su
        if (velocities.isEmpty()) {
            velocities.add(10);
        }

        // Xac dinh cac chi so van toc
        double minVelocity = velocities.stream().mapToInt(v -> v).min().orElse(10);
        double maxVelocity = velocities.stream().mapToInt(v -> v).max().orElse(10);
        double avgVelocity = velocities.stream().mapToInt(v -> v).average().orElse(10.0);
        
        if (minVelocity == 0) minVelocity = 1;
        if (avgVelocity == 0) avgVelocity = 1;

        // Lay khoi luong cong viec con lai (Remaining Backlog)
        int remainingPoints = taskRepository.sumRemainingPoints(projectId);
        
        // Neu khong con cong viec nao thi du an da hoan thanh
        if (remainingPoints == 0) {
            return buildProjectForecastResponse(0, 0, null, RISK_NONE, MSG_RISK_COMPLETED, null, null, null);
        }

        // Tinh toan 3 kich ban du bao
        LocalDate now = LocalDate.now();
        LocalDate deadline = project.getDueDate();

        ProjectForecastResponse.ForecastScenario optimistic = calculateScenario(SCENARIO_OPTIMISTIC, maxVelocity, remainingPoints, avgSprintDurationDays, now, deadline);
        ProjectForecastResponse.ForecastScenario likely = calculateScenario(SCENARIO_LIKELY, avgVelocity, remainingPoints, avgSprintDurationDays, now, deadline);
        ProjectForecastResponse.ForecastScenario pessimistic = calculateScenario(SCENARIO_PESSIMISTIC, minVelocity, remainingPoints, avgSprintDurationDays, now, deadline);

        // Danh gia rui ro tong quan
        String riskLevel = RISK_LOW;
        String riskMsg = MSG_RISK_ON_TRACK;

        if (deadline != null) {
            if (likely.isLate()) {
                riskLevel = RISK_HIGH;
                riskMsg = String.format(MSG_RISK_LATE_WARNING, likely.getDaysLate());
            } else if (pessimistic.isLate()) {
                riskLevel = RISK_MEDIUM;
                riskMsg = MSG_RISK_PESSIMISTIC_WARNING;
            }
            
            // Canh bao nghiem trong neu tre hon 30% thoi gian
            if (likely.isLate() && likely.getDaysLate() > 30) {
                riskLevel = RISK_CRITICAL;
                riskMsg = String.format(MSG_RISK_CRITICAL_DELAY, likely.getDaysLate());
            }
        } else {
            riskMsg = MSG_RISK_NO_DEADLINE;
        }

        // Map thong tin ra response DTO thong qua helper
        return buildProjectForecastResponse(remainingPoints, Math.round(avgVelocity * 100.0) / 100.0, deadline, riskLevel, riskMsg, optimistic, likely, pessimistic);
    }

    // Ham private ho tro cho getProjectForecast
    private ProjectForecastResponse.ForecastScenario calculateScenario(String name, double velocity, int remainingPoints, double sprintDuration, LocalDate now, LocalDate deadline) {
        double sprintsNeeded = remainingPoints / velocity;
        long daysNeeded = (long) Math.ceil(sprintsNeeded * sprintDuration);
        LocalDate completionDate = now.plusDays(daysNeeded);
        
        boolean isLate = false;
        int daysLate = 0;
        
        if (deadline != null && completionDate.isAfter(deadline)) {
            isLate = true;
            daysLate = (int) ChronoUnit.DAYS.between(deadline, completionDate);
        }

        return buildForecastScenario(name, Math.round(velocity * 100.0) / 100.0, Math.round(sprintsNeeded * 10.0) / 10.0, completionDate, isLate, daysLate);
    }

    @Override
    @Transactional(readOnly = true)
    public StandupReportResponse getDailyStandupReport(Integer projectId) {
        // Tim Sprint dang hoat dong
        Sprint activeSprint = sprintRepository.findActiveSprintByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_NO_ACTIVE_SPRINT));

        // Lay tat ca task trong Sprint
        List<Task> tasks = taskRepository.findBySprint_Id(activeSprint.getId());

        // Nhom cac task theo nguoi dung duoc phan cong
        Map<User, List<Task>> tasksByUser = tasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(Task::getAssignee));

        List<StandupReportResponse.MemberUpdate> memberUpdates = new ArrayList<>();
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);

        // Phan loai trang thai cong viec cua tung thanh vien
        for (Map.Entry<User, List<Task>> entry : tasksByUser.entrySet()) {
            User user = entry.getKey();
            List<Task> userTasks = entry.getValue();

            List<String> completed = new ArrayList<>();
            List<String> inProgress = new ArrayList<>();
            List<String> todo = new ArrayList<>();

            for (Task t : userTasks) {
                if (Boolean.TRUE.equals(t.getStatus().getIsCompletedStatus())) {
                    if (t.getCompletedAt() != null && t.getCompletedAt().isAfter(yesterday)) {
                        completed.add(t.getTaskCode() + " " + t.getTitle());
                    }
                } else if (STATUS_IN_PROGRESS.equalsIgnoreCase(t.getStatus().getName()) 
                        || STATUS_DOING.equalsIgnoreCase(t.getStatus().getName())) {
                    inProgress.add(t.getTaskCode() + " " + t.getTitle());
                } else {
                    todo.add(t.getTaskCode() + " " + t.getTitle());
                }
            }

            // Chi dua vao bao cao neu thanh vien co hoat dong dang ghi nhan
            if (!completed.isEmpty() || !inProgress.isEmpty()) {
                memberUpdates.add(buildMemberUpdate(user, completed, inProgress, todo));
            }
        }

        // Map ket qua ra response DTO
        return buildStandupReportResponse(activeSprint.getName(), LocalDate.now(), memberUpdates);
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private AssigneeRecommendationResponse buildAssigneeRecommendationResponse(User user, double finalScore, int currentLoad, int similarCount, String workloadStatus, String reasonText) {
        return AssigneeRecommendationResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .matchScore(finalScore)
                .currentWorkloadPoints(currentLoad)
                .similarTasksCompleted(similarCount)
                .workloadStatus(workloadStatus)
                .reason(reasonText)
                .build();
    }

    private ProjectForecastResponse buildProjectForecastResponse(int remainingPoints, double avgVelocity, LocalDate deadline, String riskLevel, String riskMsg, ProjectForecastResponse.ForecastScenario optimistic, ProjectForecastResponse.ForecastScenario likely, ProjectForecastResponse.ForecastScenario pessimistic) {
        return ProjectForecastResponse.builder()
                .totalBacklogPoints(remainingPoints)
                .averageVelocity(avgVelocity)
                .projectDueDate(deadline)
                .riskLevel(riskLevel)
                .riskMessage(riskMsg)
                .optimistic(optimistic)
                .likely(likely)
                .pessimistic(pessimistic)
                .build();
    }

    private ProjectForecastResponse.ForecastScenario buildForecastScenario(String name, double velocityUsed, double sprintsNeeded, LocalDate completionDate, boolean isLate, int daysLate) {
        return ProjectForecastResponse.ForecastScenario.builder()
                .name(name)
                .velocityUsed(velocityUsed)
                .sprintsNeeded(sprintsNeeded)
                .completionDate(completionDate)
                .isLate(isLate)
                .daysLate(daysLate)
                .build();
    }

    private StandupReportResponse buildStandupReportResponse(String sprintName, LocalDate reportDate, List<StandupReportResponse.MemberUpdate> memberUpdates) {
        return StandupReportResponse.builder()
                .sprintName(sprintName)
                .reportDate(reportDate)
                .members(memberUpdates)
                .build();
    }

    private StandupReportResponse.MemberUpdate buildMemberUpdate(User user, List<String> completed, List<String> inProgress, List<String> todo) {
        return StandupReportResponse.MemberUpdate.builder()
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .completedTasks(completed)
                .inProgressTasks(inProgress)
                .todoTasks(todo)
                .build();
    }
}