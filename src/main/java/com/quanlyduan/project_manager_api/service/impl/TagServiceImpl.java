package com.quanlyduan.project_manager_api.service.impl;

import java.util.Objects;
import java.util.stream.Collectors;

import com.quanlyduan.project_manager_api.dto.request.CreateTagRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTagRequest;
import com.quanlyduan.project_manager_api.exception.BadRequestException;

import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Tag;

import com.quanlyduan.project_manager_api.model.User;

import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.repository.TagRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.specification.TagSpecification;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.TagService;
import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.dto.response.TagResponse;
import com.quanlyduan.project_manager_api.util.ProjectHierarchyValidator;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.quanlyduan.project_manager_api.model.Project;


@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;
    private final ProjectHierarchyValidator validator;
    private final SecurityService securityService;

    public TagServiceImpl(TagRepository tagRepository,TaskRepository taskRepository, ProjectHierarchyValidator validator,SecurityService securityService) {
        this.tagRepository = tagRepository;
        this.taskRepository = taskRepository;
        this.validator = validator;
        this.securityService = securityService;
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
    public TagResponse createTag(Integer companyId, Integer workspaceId, Integer projectId, CreateTagRequest request) {
        Project project = validator.validateProject(companyId, workspaceId, projectId);
        if (tagRepository.existsByNameAndProject_Id(request.getName(), projectId)) {
            throw new BadRequestException("Tag already exists.");
        }
        User creator = securityService.getCurrentAuthenticatedUser();
        Tag tag = Tag.builder().project(project).name(request.getName())
                .color(request.getColor() != null ? request.getColor() : "#95a5a6")
                .description(request.getDescription()).createdBy(creator).build();
        return mapToResponse(tagRepository.save(tag));
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

    @Override
    @Transactional
    public TagResponse updateTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId, UpdateTagRequest request) {

        // 1. Validate Hierarchy
        validator.validateProject(companyId, workspaceId, projectId);

        // 2. Tìm Tag
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with ID: " + tagId));

        // 3. Bảo mật: Tag phải thuộc Project này
        if (!tag.getProject().getId().equals(projectId)) {
            throw new BadRequestException("This tag does not belong to the current project.");
        }

        // 4. Validate tên trùng (nếu có đổi tên)
        if (request.getName() != null && !request.getName().isEmpty()
                && !Objects.equals(request.getName(), tag.getName())) {

            // Kiểm tra trùng lặp với tag KHÁC
            if (tagRepository.existsByNameAndProject_IdAndIdNot(request.getName(), projectId, tagId)) {
                throw new BadRequestException("Tag name '" + request.getName() + "' is already in use by another tag in this project.");
            }
            tag.setName(request.getName());
        }

        // 5. Cập nhật các trường khác
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }

        // 6. Lưu và trả về
        Tag updatedTag = tagRepository.save(tag);
        return mapToResponse(updatedTag);
    }
}
