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

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
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


    // ======================================================
    // 1. TẠO SPRINT MỚI (CREATE SPRINT)
    // ======================================================
    @Override
    @Transactional
    public SprintResponse createSprint(Integer projectId, CreateSprintRequest request) {
        Integer currentUserId = securityService.getCurrentUserId();

        // 1. Kiểm tra tồn tại Project và User
        Project project = projectRepository.findById(projectId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User creator = userRepository.findById(currentUserId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("User creator not found"));

        // 2. LOGIC SINH TÊN TỰ ĐỘNG (nếu tên rỗng)
        String sprintName = request.getName();
        if (sprintName == null || sprintName.trim().isEmpty()) {
            long count = sprintRepository.countByProject_Id(projectId);
            sprintName = "Sprint " + (count + 1);
        }

        // 3. Tạo Entity
        Sprint sprint = Sprint.builder()
                .project(project)
                .name(sprintName) // Sử dụng tên (tự sinh hoặc do user nhập)
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.NOT_STARTED)
                .createdBy(creator)
                .build();

        Sprint savedSprint = sprintRepository.save(sprint);

        // 4. LOGIC CHUYỂN TASK TỪ BACKLOG VÀO SPRINT VỪA TẠO
        if (request.getTaskIds() != null && !request.getTaskIds().isEmpty()) {
            List<Task> tasksToUpdate = taskRepository.findAllById(request.getTaskIds());
            for (Task task : tasksToUpdate) {
                // Gán Sprint ID mới
                task.setSprint(savedSprint);
            }
            taskRepository.saveAll(tasksToUpdate);
        }

        // 5. Trả về Response (không kèm task)
        return mapToSprintResponse(savedSprint, Collections.emptyList());
    }

    // ======================================================
    // 2. BẮT ĐẦU SPRINT (START SPRINT)
    // ======================================================
    @Override
    @Transactional
    public SprintResponse startSprint(Integer projectId, Integer sprintId) {
        // 1. Tìm Sprint
        Sprint sprint = sprintRepository.findById(sprintId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found."));

        // 2. Validation
        // Đảm bảo sprint này thuộc đúng project
        if (!sprint.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Sprint does not belong to this project.");
        }
        // Chỉ bắt đầu được Sprint ở trạng thái NOT_STARTED
        if (sprint.getStatus() != SprintStatus.NOT_STARTED) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Sprint has already been started or completed.");
        }

        // 3. Cập nhật
        sprint.setStatus(SprintStatus.IN_PROGRESS);
        // Tự động gán ngày bắt đầu nếu chưa có
        if (sprint.getStartDate() == null) {
            sprint.setStartDate(java.time.LocalDateTime.now());
        }

        Sprint savedSprint = sprintRepository.save(sprint);

        // 4. Lấy các task liên quan để trả về
        List<Task> tasks = taskRepository.findBySprintIdWithDetails(savedSprint.getId());
        return mapToSprintResponse(savedSprint, tasks);
    }

    // ======================================================
    // 3. HOÀN THÀNH SPRINT (COMPLETE SPRINT)
    // ======================================================
    @Override
    @Transactional
    public SprintResponse completeSprint(Integer projectId, Integer sprintId) {
        // 1. Tìm Sprint
        Sprint sprint = sprintRepository.findById(sprintId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found."));

        // 2. Validate
        if (!sprint.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Sprint does not belong to this project.");
        }
        // Chỉ hoàn thành được Sprint đang diễn ra
        if (sprint.getStatus() != SprintStatus.IN_PROGRESS) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Only in-progress Sprints can be completed.");
        }

        // 3. LOGIC DỌN DẸP TASK CHƯA XONG
        // Tìm tất cả task chưa hoàn thành trong Sprint này
        List<Task> incompleteTasks = taskRepository.findIncompleteTasksBySprintId(sprintId);

        if (!incompleteTasks.isEmpty()) {
            // Đẩy hết task chưa hoàn thành về Backlog (sprint = null)
            for (Task task : incompleteTasks) {
                task.setSprint(null);
            }
            taskRepository.saveAll(incompleteTasks);
        }

        // 4. Cập nhật trạng thái Sprint
        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setEndDate(java.time.LocalDateTime.now()); // Chốt thời gian thực tế hoàn thành

        Sprint savedSprint = sprintRepository.save(sprint);

        // 5. Trả về kết quả (chỉ các task đã hoàn thành)
        List<Task> completedTasksOnly = taskRepository.findBySprintIdWithDetails(savedSprint.getId());
        return mapToSprintResponse(savedSprint, completedTasksOnly);
    }

    // ======================================================
    // 4. CẬP NHẬT THÔNG TIN SPRINT (UPDATE SPRINT)
    // ======================================================
    @Override
    @Transactional
    public SprintResponse updateSprint(Integer projectId, Integer sprintId, UpdateSprintRequest request) {
        // 1. Tìm Sprint
        Sprint sprint = sprintRepository.findById(sprintId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found."));

        // 2. Validate: Sprint thuộc đúng Project
        if (!sprint.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Sprint does not belong to this project.");
        }

        // 3. Cập nhật thông tin (nếu có)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            sprint.setName(request.getName());
        }

        if (request.getGoal() != null) {
            sprint.setGoal(request.getGoal());
        }

        // Lấy ngày mới hoặc ngày cũ để kiểm tra hợp lệ
        LocalDateTime newStartDate = (request.getStartDate() != null) ? request.getStartDate() : sprint.getStartDate();
        LocalDateTime newEndDate = (request.getEndDate() != null) ? request.getEndDate() : sprint.getEndDate();

        // Validate ngày: Ngày bắt đầu không được sau ngày kết thúc
        if (newStartDate != null && newEndDate != null && newStartDate.isAfter(newEndDate)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Start date cannot be after end date.");
        }

        if (request.getStartDate() != null) {
            sprint.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            sprint.setEndDate(request.getEndDate());
        }

        // 4. Lưu và trả về
        Sprint savedSprint = sprintRepository.save(sprint);

        // Trả về danh sách task rỗng (vì logic update thông tin không cần kèm task)
        return mapToSprintResponse(savedSprint, Collections.emptyList());
    }

    // ======================================================
    // 5. XÓA SPRINT (SMART DELETE)
    // ======================================================
    @Override
    @Transactional
    public void deleteSprint(Integer projectId, Integer sprintId) {
        // 1. Tìm Sprint
        Sprint sprint = sprintRepository.findById(sprintId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found."));

        // 2. Validate: Sprint thuộc Project
        if (!sprint.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Sprint does not belong to this project.");
        }

        // 3. Validate: Không được xóa Sprint đã hoàn thành
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Cannot delete a completed Sprint.");
        }

        // 4. Kiểm tra số lượng task
        long taskCount = taskRepository.countBySprint_Id(sprintId);

        // === CASE 1: XÓA HẲN (HARD DELETE) ===
        // Nếu chưa bắt đầu VÀ không có task nào -> Xóa
        if (sprint.getStatus() == SprintStatus.NOT_STARTED && taskCount == 0) {
            sprintRepository.delete(sprint);
            return;
        }

        // === CASE 2: HỦY (SOFT DELETE / CANCEL) ===
        // Nếu đang chạy HOẶC đã có task -> Chuyển về trạng thái CANCELLED
        
        // B2.1: Đẩy hết task về Backlog (sprint_id = null)
        if (taskCount > 0) {
            // Giả sử có hàm native query trong Repo để update hàng loạt
            taskRepository.moveTasksToBacklogBySprintId(sprintId);
        }

        // B2.2: Cập nhật trạng thái Sprint thành CANCELLED
        sprint.setStatus(SprintStatus.CANCELLED);
        sprint.setEndDate(java.time.LocalDateTime.now()); // Ghi nhận thời điểm hủy
        
        sprintRepository.save(sprint);
    }

    // ======================================================
    // 6. LẤY DANH SÁCH SPRINT (LIST SPRINTS)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<SprintResponse> getSprintsByProject(Integer projectId, String status) {

        List<Sprint> sprints;

        if (status != null && !status.trim().isEmpty()) {
            try {
                SprintStatus statusEnum = SprintStatus.valueOf(status.toUpperCase());
                // Lấy tất cả sprints và lọc trong bộ nhớ (dùng cho trường hợp không có hàm findByProject_IdAndStatus trong Repo)
                sprints = sprintRepository.findAll().stream()
                        .filter(s -> s.getProject().getId().equals(projectId) && s.getStatus() == statusEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Invalid status: " + status);
            }
        } else {
            // Lấy tất cả sprint của project
            sprints = sprintRepository.findAll().stream()
                    .filter(s -> s.getProject().getId().equals(projectId))
                    .collect(Collectors.toList());
        }

        // Map sang DTO
        return sprints.stream()
                // Truyền Collections.emptyList() vì listing không cần kèm chi tiết task
                .map(sprint -> mapToSprintResponse(sprint, Collections.emptyList()))
                .collect(Collectors.toList());
    }
    
    // ======================================================
    // 7. XEM CHI TIẾT SPRINT (GET DETAILS & CALCULATE)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public SprintDetailsResponse getSprintDetails(Integer projectId, Integer sprintId) {
        // 1. Tìm Sprint
        Sprint sprint = sprintRepository.findById(sprintId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + sprintId));

        // 2. Kiểm tra bảo mật
        if (!sprint.getProject().getId().equals(projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("Sprint not found in the specified project.");
        }

        // 3. Tìm tất cả Task thuộc Sprint (kèm chi tiết)
        List<Task> tasks = taskRepository.findBySprintIdWithDetails(sprintId);

        // 4. Map Task sang DTO
        List<TaskSummaryResponse> taskDTOs = tasks.stream()
                .map(this::mapToTaskSummaryResponse)
                .collect(Collectors.toList());

        // 5. TÍNH TOÁN THỐNG KÊ (Story Points, Count)
        long totalPoints = tasks.stream()
                .mapToLong(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        int count = tasks.size();

        // 6. Đóng gói
        return SprintDetailsResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .projectId(sprint.getProject().getId())
                .tasks(taskDTOs)

                .totalStoryPoints(totalPoints)
                .taskCount(count)

                .build();
    }

    // ======================================================
    // 8. LẤY PROJECT ID TỪ SPRINT ID (HELPER)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public Integer getProjectIdBySprint(Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with ID: " + sprintId));
        return sprint.getProject().getId();
    }

    // ======================================================
    // ⚙️ PRIVATE HELPER METHODS (MAPPERS)
    // ======================================================

    /**
     * Helper: Map Sprint Entity sang SprintResponse DTO.
     */
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
                // .tasks(taskDTOs) // Nếu SprintResponse có trường tasks
                .build();
    }

    /**
     * Helper: Map Task Entity sang TaskSummaryResponse DTO.
     * (Tái sử dụng logic từ ProjectService hoặc TaskService)
     */
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