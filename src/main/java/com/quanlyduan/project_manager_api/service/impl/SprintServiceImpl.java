// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/SprintServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.SprintDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.SprintResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.SprintService;
import com.quanlyduan.project_manager_api.service.TaskService; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final TaskService taskService; 

    // *** CONSTRUCTOR THỦ CÔNG ***
    public SprintServiceImpl(SprintRepository sprintRepository,
                             ProjectRepository projectRepository,
                             TaskRepository taskRepository,
                             UserRepository userRepository,
                             SecurityService securityService,
                             TaskService taskService) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.taskService = taskService;
    }

    // US-S3-6: Tạo Sprint
    @Override
    @Transactional
    public SprintResponse createSprint(Integer projectId, CreateSprintRequest request) {
        Integer currentUserId = securityService.getCurrentUserId();
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án")); // Đã dịch
        
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng tạo")); // Đã dịch

        Sprint sprint = Sprint.builder()
                .project(project)
                .name(request.getName())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.NOT_STARTED)
                .createdBy(creator)
                .build();
        
        Sprint savedSprint = sprintRepository.save(sprint);

        // US-S3-6: Chuyển task từ backlog vào sprint mới
        if (request.getTaskIds() != null && !request.getTaskIds().isEmpty()) {
            List<Task> tasksToUpdate = taskRepository.findAllById(request.getTaskIds());
            for (Task task : tasksToUpdate) {
                task.setSprint(savedSprint);
            }
            taskRepository.saveAll(tasksToUpdate);
        }

        return mapToSprintResponse(savedSprint, Collections.emptyList()); 
    }

    // US-S3-8: Bắt đầu Sprint
    @Override
    @Transactional
    public SprintResponse startSprint(Integer projectId, Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint")); // Đã dịch

        // Validation: Đảm bảo sprint này thuộc đúng project trên URL
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Sprint không thuộc về dự án này."); // Đã dịch
        }
        if (sprint.getStatus() != SprintStatus.NOT_STARTED) {
            throw new BadRequestException("Sprint đã được bắt đầu hoặc đã hoàn thành"); // Đã dịch
        }

        sprint.setStatus(SprintStatus.IN_PROGRESS);
        // Tự động gán ngày bắt đầu nếu chưa có
        if (sprint.getStartDate() == null) {
            sprint.setStartDate(java.time.LocalDate.now());
        }
        
        Sprint savedSprint = sprintRepository.save(sprint);
        
        // Lấy các task liên quan để trả về
        List<Task> tasks = taskRepository.findBySprintIdWithDetails(savedSprint.getId()); 
        return mapToSprintResponse(savedSprint, tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintResponse> getSprintsByProject(Integer projectId, String status) {
        
        List<Sprint> sprints;

        if (status != null && !status.trim().isEmpty()) {
            try {
                SprintStatus statusEnum = SprintStatus.valueOf(status.toUpperCase());
                // Cần đảm bảo Repository có hàm này hoặc dùng logic lọc bằng Java
                // Tạm thời dùng logic lọc Java để tránh lỗi biên dịch Repo
                sprints = sprintRepository.findAll().stream()
                        .filter(s -> s.getProject().getId().equals(projectId) && s.getStatus() == statusEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Trạng thái không hợp lệ: " + status); // Đã dịch
            }
        } else {
            // Lấy tất cả sprint của project
            // Cần đảm bảo Repository có hàm findByProject_Id...
            // Tạm thời dùng logic an toàn:
             sprints = sprintRepository.findAll().stream()
                        .filter(s -> s.getProject().getId().equals(projectId))
                        .collect(Collectors.toList());
        }

        return sprints.stream()
                .map(sprint -> mapToSprintResponse(sprint, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SprintResponse completeSprint(Integer projectId, Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint")); // Đã dịch

        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Sprint không thuộc về dự án này."); // Đã dịch
        }

        if (sprint.getStatus() != SprintStatus.IN_PROGRESS) {
            throw new BadRequestException("Chỉ có thể hoàn thành các Sprint đang diễn ra."); // Đã dịch
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setEndDate(java.time.LocalDate.now()); 
        
        Sprint savedSprint = sprintRepository.save(sprint);
        
        List<Task> tasks = taskRepository.findBySprintIdWithDetails(savedSprint.getId());
        return mapToSprintResponse(savedSprint, tasks);
    }

    @Override
    @Transactional
    public SprintResponse cancelSprint(Integer projectId, Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint")); // Đã dịch

        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Sprint không thuộc về dự án này."); // Đã dịch
        }

        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy Sprint đã hoàn thành."); // Đã dịch
        }

        sprint.setStatus(SprintStatus.CANCELLED);
        
        Sprint savedSprint = sprintRepository.save(sprint);
        
        List<Task> tasks = taskRepository.findBySprintIdWithDetails(savedSprint.getId());
        return mapToSprintResponse(savedSprint, tasks);
    }

    // *** ĐÃ SỬA: BỔ SUNG PHƯƠNG THỨC THIẾU ***
    @Override
    @Transactional(readOnly = true)
    public Integer getProjectIdBySprint(Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint với ID: " + sprintId)); // Đã dịch
        return sprint.getProject().getId();
    }

    // === HÀM HELPER MAPPING ===
    private SprintResponse mapToSprintResponse(Sprint sprint, List<Task> tasks) {
        // Dùng mapToTaskSummaryResponse để tránh vòng lặp và nhẹ dữ liệu
        List<TaskSummaryResponse> taskDTOs = tasks.stream()
                .map(this::mapToTaskSummaryResponse) 
                .collect(Collectors.toList());

        return SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus().name())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .projectId(sprint.getProject().getId())
                // .tasks(taskDTOs) // Nếu SprintResponse có trường tasks thì bỏ comment dòng này
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SprintDetailsResponse getSprintDetails(Integer projectId, Integer sprintId) { 
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint với ID: " + sprintId)); // Đã dịch

        // KIỂM TRA BẢO MẬT (IDOR)
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Không tìm thấy Sprint này trong dự án được chỉ định"); // Đã dịch
        }

        List<Task> tasks = taskRepository.findBySprintIdWithDetails(sprintId);

        List<TaskSummaryResponse> taskDTOs = tasks.stream()
                .map(this::mapToTaskSummaryResponse) 
                .collect(Collectors.toList());

        return SprintDetailsResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .projectId(sprint.getProject().getId())
                .tasks(taskDTOs) 
                .build();
    }

    // --- HÀM HELPER (TÁI SỬ DỤNG & SỬA LOGIC STATUS) ---
    private TaskSummaryResponse mapToTaskSummaryResponse(Task task) {
        User assignee = task.getAssignee();
        Epic epic = task.getEpic();
        
        // Lấy đối tượng ProjectStatus
        ProjectStatus status = task.getStatus(); 

        return TaskSummaryResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .taskType(task.getTaskType())
                
                // Đọc từ đối tượng status
                .statusId(status != null ? status.getId() : null)
                .statusName(status != null ? status.getName() : "N/A")
                .statusColor(status != null ? status.getColor() : "#FFFFFF")

                .priority(task.getPriority())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .assigneeId(assignee != null ? assignee.getId() : null)
                .assigneeName(assignee != null ? assignee.getFullName() : null)
                .assigneeAvatarUrl(assignee != null ? assignee.getAvatarUrl() : null)
                .epicId(epic != null ? epic.getId() : null)
                .epicName(epic != null ? epic.getName() : null)
                .epicColor(epic != null ? epic.getColor() : null)
                .storyPoints(task.getStoryPoints())
                .dueDate(task.getDueDate())
                .sortOrder(task.getSortOrder())
                .build();
    }
}