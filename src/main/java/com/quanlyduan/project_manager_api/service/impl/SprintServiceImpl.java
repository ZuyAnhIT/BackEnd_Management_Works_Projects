// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/SprintServiceImpl.java
// (MỚI)
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateSprintRequest;
import com.quanlyduan.project_manager_api.dto.response.SprintResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.SprintService;
import com.quanlyduan.project_manager_api.service.TaskService; // Sẽ cần TaskService để map DTO
import lombok.RequiredArgsConstructor;
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


    // Tạm thời inject service, lý tưởng hơn là dùng 1 Mapper chung
    private final TaskService taskService; 

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
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User creator not found"));

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
            // Dùng @Query đã định nghĩa trong TaskRepository
            taskRepository.updateSprintForTasks(savedSprint, request.getTaskIds());
        }

        return mapToSprintResponse(savedSprint, Collections.emptyList()); // Trả về sprint rỗng (vì task vừa được gán)
    }

    // US-S3-8: Bắt đầu Sprint
    @Override
    @Transactional
    public SprintResponse startSprint(Integer projectId, Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // === THÊM BƯỚC VALIDATION QUAN TRỌNG ===
        // Đảm bảo sprint này thuộc đúng project trên URL
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Sprint ID and Project ID mismatch.");
        }
        if (sprint.getStatus() != SprintStatus.NOT_STARTED) {
            throw new BadRequestException("Sprint has already been started or is completed");
        }

        // Kiểm tra xem có Sprint nào khác đang chạy không
        if (sprintRepository.existsByProject_IdAndStatus(sprint.getProject().getId(), SprintStatus.IN_PROGRESS)) {
            throw new BadRequestException("Another sprint is already in progress for this project");
        }

        sprint.setStatus(SprintStatus.IN_PROGRESS);
        // Tự động gán ngày bắt đầu nếu chưa có
        if (sprint.getStartDate() == null) {
            sprint.setStartDate(java.time.LocalDate.now());
        }
        
        Sprint savedSprint = sprintRepository.save(sprint);
        
        // Lấy các task liên quan để trả về
        List<Task> tasks = taskRepository.findBySprint_IdOrderBySortOrderAsc(savedSprint.getId());
        return mapToSprintResponse(savedSprint, tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintResponse> getSprintsByProject(Integer projectId, String status) {
        
        List<Sprint> sprints;

        if (status != null && !status.trim().isEmpty()) {
            // Trường hợp 1: Client có cung cấp 'status'
            try {
                // Chuyển String (ví dụ "COMPLETED") sang Enum (SprintStatus.COMPLETED)
                SprintStatus statusEnum = SprintStatus.valueOf(status.toUpperCase());
                // Gọi hàm repository có lọc
                sprints = sprintRepository.findByProject_IdAndStatusOrderByStartDateDesc(projectId, statusEnum);
            } catch (IllegalArgumentException e) {
                // Nếu client gửi status bậy (ví dụ "ABC")
                throw new BadRequestException("Invalid status value: " + status);
            }
        } else {
            // Trường hợp 2: Client không cung cấp 'status' -> Lấy tất cả
            sprints = sprintRepository.findByProject_IdOrderByStartDateDesc(projectId);
        }

        // 2. Map sang DTO (giữ nguyên logic cũ)
        return sprints.stream()
                .map(sprint -> mapToSprintResponse(sprint, Collections.emptyList()))
                .collect(Collectors.toList());
    }
        @Override
    @Transactional
    public SprintResponse completeSprint(Integer projectId, Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // 1. Validation: Đảm bảo sprint này thuộc đúng project trên URL
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Sprint ID and Project ID mismatch.");
        }

        // 2. Validation: Chỉ có thể Hoàn thành Sprint đang "IN_PROGRESS"
        if (sprint.getStatus() != SprintStatus.IN_PROGRESS) {
            throw new BadRequestException("Only sprints that are IN_PROGRESS can be completed.");
        }

        // 3. Cập nhật trạng thái và ngày kết thúc
        sprint.setStatus(SprintStatus.COMPLETED);
        // Tự động gán ngày kết thúc nếu chưa có (hoặc ghi đè)
        sprint.setEndDate(java.time.LocalDate.now()); 
        
        Sprint savedSprint = sprintRepository.save(sprint);
        
        // 4. Lấy các task liên quan để trả về (tương tự startSprint)
        // (Trong tương lai, bạn có thể thêm logic di chuyển task chưa xong về Backlog ở đây)
        List<Task> tasks = taskRepository.findBySprint_IdOrderBySortOrderAsc(savedSprint.getId());
        return mapToSprintResponse(savedSprint, tasks);
    }
    @Override
    @Transactional
    public SprintResponse cancelSprint(Integer projectId, Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // 1. Validation: Đảm bảo sprint này thuộc đúng project trên URL
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Sprint ID and Project ID mismatch.");
        }

        // 2. Validation: Không thể Hủy Sprint đã Hoàn thành
        SprintStatus currentStatus = sprint.getStatus();
        if (currentStatus == SprintStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a sprint that is already COMPLETED.");
        }
        // (Vẫn có thể hủy sprint đang CANCELLED - không sao cả, kết quả vẫn vậy)

        // 3. Cập nhật trạng thái
        sprint.setStatus(SprintStatus.CANCELLED);
        
        Sprint savedSprint = sprintRepository.save(sprint);
        
        // 4. Lấy các task liên quan để trả về
        // (Logic nghiệp vụ: Bạn có thể muốn thêm code ở đây để
        //  di chuyển các task của sprint này về Backlog)
        List<Task> tasks = taskRepository.findBySprint_IdOrderBySortOrderAsc(savedSprint.getId());
        return mapToSprintResponse(savedSprint, tasks);
    }
    // Helper cho Security
    @Override
    @Transactional(readOnly = true)
    public Integer getProjectIdBySprint(Integer sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        return sprint.getProject().getId();
    }

    // === HÀM HELPER MAPPING ===
    private SprintResponse mapToSprintResponse(Sprint sprint, List<Task> tasks) {
        List<TaskResponse> taskDTOs = tasks.stream()
                .map(taskService::mapToTaskResponse) // Tái sử dụng hàm map của TaskService
                .collect(Collectors.toList());

        return SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus().name())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .projectId(sprint.getProject().getId())
                .tasks(taskDTOs)
                .build();
    }
}
