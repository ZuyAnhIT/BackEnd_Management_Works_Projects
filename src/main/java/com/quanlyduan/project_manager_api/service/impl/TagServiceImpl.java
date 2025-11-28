package com.quanlyduan.project_manager_api.service.impl;

import java.util.stream.Collectors;

import com.quanlyduan.project_manager_api.model.Tag;

import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.TagRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.specification.TagSpecification;
import com.quanlyduan.project_manager_api.service.TagService;
import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.dto.response.TagResponse;
import com.quanlyduan.project_manager_api.util.ProjectHierarchyValidator;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quanlyduan.project_manager_api.exception.BadRequestException;


@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;
    private final ProjectHierarchyValidator validator;

    public TagServiceImpl(TagRepository tagRepository,TaskRepository taskRepository, ProjectHierarchyValidator validator) {
        this.tagRepository = tagRepository;
        this.taskRepository = taskRepository;
        this.validator = validator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getProjectTags(Integer companyId, Integer workspaceId, Integer projectId, TagFilterRequest filter) {
        validator.validateProject(companyId, workspaceId, projectId);

       Specification<Tag> spec = TagSpecification.getFilterSpec(projectId, filter);
        return tagRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

     @Override
    @Transactional
    public List<TagResponse> assignTagToTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId) {
        Task task = validator.validateTask(companyId, workspaceId, projectId, taskId);
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);

        // 1. KIỂM TRA TRÙNG LẶP
        // Lưu ý: Để hàm contains hoạt động đúng, file Model Tag và Task phải cấu hình equals/hashCode theo ID (như đã sửa ở bước trước)
        if (task.getTags().contains(tag)) {
            throw new BadRequestException("This task has already been assigned to a tag."); // Báo lỗi user
            // Hoặc nếu muốn lờ đi và trả về list luôn thì: return task.getTags().stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        task.getTags().add(tag);
        taskRepository.save(task);

        return task.getTags().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public List<TagResponse> removeTagFromTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId) {
        Task task = validator.validateTask(companyId, workspaceId, projectId, taskId);
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);

        // 1. KIỂM TRA TỒN TẠI
        if (!task.getTags().contains(tag)) {
            throw new BadRequestException("This task is not assigned to a tag and cannot be deleted.");
        }

        task.getTags().remove(tag);
        taskRepository.save(task);

        return task.getTags().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    // --- Helper Mapping (Đã cập nhật thêm thông tin người tạo) ---
    private TagResponse mapToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .description(tag.getDescription())
                .projectId(tag.getProject().getId())
                
                // Mapping thông tin Audit mới thêm
                .createdById(tag.getCreatedBy().getId())
                .createdByName(tag.getCreatedBy().getFullName())
                .createdByAvatar(tag.getCreatedBy().getAvatarUrl())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
