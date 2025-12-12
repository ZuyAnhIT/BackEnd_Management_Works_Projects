// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/AnalyticsServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.AssigneeRecommendationRequest;
import com.quanlyduan.project_manager_api.dto.response.AssigneeRecommendationResponse;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.service.AnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    public AnalyticsServiceImpl(ProjectRepository projectRepository,
                                ProjectMemberRepository projectMemberRepository,
                                TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
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

    private String extractMainKeyword(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String cleanText = text.replaceAll("[^a-zA-Z0-9\\s]", "");
        return Arrays.stream(cleanText.split("\\s+"))
                .filter(w -> w.length() > 3)
                .max(Comparator.comparingInt(String::length))
                .orElse("");
    }
}