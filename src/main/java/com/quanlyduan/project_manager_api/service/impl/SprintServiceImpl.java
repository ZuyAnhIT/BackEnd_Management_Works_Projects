// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/SprintServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateSprintRequest;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
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


    // US-S3-6: Tạo Sprint (HỖ TRỢ QUICK CREATE)
    @Override
    @Transactional
    public SprintResponse createSprint(Integer projectId, CreateSprintRequest request) {
        Integer currentUserId = securityService.getCurrentUserId();
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User creator not found"));

        // LOGIC SINH TÊN TỰ ĐỘNG
        String sprintName = request.getName();
        if (sprintName == null || sprintName.trim().isEmpty()) {
            long count = sprintRepository.countByProject_Id(projectId);
            sprintName = "Sprint " + (count + 1);
        }

        Sprint sprint = Sprint.builder()
                .project(project)
                .name(sprintName) // Sử dụng tên (tự sinh hoặc do user nhập)
                .goal(request.getGoal()) // Có thể null
                .startDate(request.getStartDate()) // Có thể null
                .endDate(request.getEndDate()) // Có thể null
                .status(SprintStatus.NOT_STARTED)
                .createdBy(creator)
                .build();
        
        Sprint savedSprint = sprintRepository.save(sprint);

        // (Logic chuyển task từ backlog giữ nguyên)
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
            sprint.setStartDate(java.time.LocalDateTime.now());
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
        sprint.setEndDate(java.time.LocalDateTime.now()); 
        
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
    
    // LOGIC CAP NHAT THONG TIN SPRINT
    @Override
    @Transactional
    public SprintResponse updateSprint(Integer projectId, Integer sprintId, UpdateSprintRequest request) {
        // 1. Tìm Sprint
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint"));

        // 2. Validate: Sprint thuộc đúng Project
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Sprint không thuộc về dự án này.");
        }

        // 3. Cập nhật thông tin (nếu có)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            sprint.setName(request.getName());
        }
        
        if (request.getGoal() != null) {
            sprint.setGoal(request.getGoal());
        }

        // Cập nhật ngày tháng
        LocalDateTime newStartDate = (request.getStartDate() != null) ? request.getStartDate() : sprint.getStartDate();
        LocalDateTime newEndDate = (request.getEndDate() != null) ? request.getEndDate() : sprint.getEndDate();

        // Validate ngày: Ngày bắt đầu không được sau ngày kết thúc
        if (newStartDate != null && newEndDate != null && newStartDate.isAfter(newEndDate)) {
            throw new BadRequestException("Ngày bắt đầu không thể sau ngày kết thúc.");
        }

        if (request.getStartDate() != null) {
            sprint.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            sprint.setEndDate(request.getEndDate());
        }

        // 4. Lưu và trả về
        Sprint savedSprint = sprintRepository.save(sprint);
        
        // Lấy task để trả về (hoặc danh sách rỗng cho nhẹ)
        // Ở đây ta lấy danh sách rỗng cho nhẹ, vì update thông tin không ảnh hưởng list task
        return mapToSprintResponse(savedSprint, Collections.emptyList());
    }

    // LOGIC XOA SPRINT (SMART DELETE)
    @Override
    @Transactional
    public void deleteSprint(Integer projectId, Integer sprintId) {
        // 1. Tìm Sprint
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Sprint")); // Đã dịch

        // 2. Validate: Sprint thuộc Project
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Sprint không thuộc về dự án này."); // Đã dịch
        }

        // 3. Validate: Không được xóa Sprint đã hoàn thành
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BadRequestException("Không thể xóa Sprint đã hoàn thành."); // Đã dịch
        }

        // 4. Kiểm tra số lượng task
        long taskCount = taskRepository.countBySprint_Id(sprintId);

        // === CASE 1: XÓA HẲN (HARD DELETE) ===
        // Nếu chưa bắt đầu VÀ không có task nào -> Xóa bay màu
        if (sprint.getStatus() == SprintStatus.NOT_STARTED && taskCount == 0) {
            sprintRepository.delete(sprint);
            return;
        }

        // === CASE 2: HỦY (SOFT DELETE / CANCEL) ===
        // Nếu đang chạy HOẶC đã có task -> Chuyển về trạng thái CANCELLED
        
        // B2.1: Đẩy hết task về Backlog (sprint_id = null)
        if (taskCount > 0) {
            taskRepository.moveTasksToBacklogBySprintId(sprintId);
        }

        // B2.2: Cập nhật trạng thái Sprint thành CANCELLED
        sprint.setStatus(SprintStatus.CANCELLED);
        sprint.setEndDate(java.time.LocalDateTime.now()); // Ghi nhận thời điểm hủy
        
        sprintRepository.save(sprint);
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