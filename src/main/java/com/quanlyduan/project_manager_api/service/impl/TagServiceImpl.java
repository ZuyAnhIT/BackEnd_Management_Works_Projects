package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateTagRequest;
import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTagRequest;
import com.quanlyduan.project_manager_api.dto.response.TagResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.Tag;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.TagRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.specification.TagSpecification;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.TagService;
import com.quanlyduan.project_manager_api.util.ProjectHierarchyValidator;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;
    private final ProjectHierarchyValidator validator;
    private final SecurityService securityService;

    public TagServiceImpl(TagRepository tagRepository, TaskRepository taskRepository, ProjectHierarchyValidator validator, SecurityService securityService) {
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
            throw new BadRequestException("Tag name '" + request.getName() + "' already exists in this project.");
        }
        
        User creator = securityService.getCurrentAuthenticatedUser();
        Tag tag = Tag.builder()
                .project(project)
                .name(request.getName())
                .color(request.getColor() != null ? request.getColor() : "#95a5a6")
                .description(request.getDescription())
                .createdBy(creator)
                .build();
        
        // Hibernate sẽ tự động điền createdAt và updatedAt khi save
        return mapToResponse(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public TagResponse updateTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId, UpdateTagRequest request) {
        // Sử dụng Validator 4 tham số để check full hierarchy
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);

        // Validate tên trùng (nếu có đổi tên)
        if (request.getName() != null && !request.getName().isEmpty()
                && !Objects.equals(request.getName(), tag.getName())) {
            
            // Cần đảm bảo Repository có hàm này: boolean existsByNameAndProject_IdAndIdNot(String name, Integer projectId, Integer id);
            if (tagRepository.existsByNameAndProject_IdAndIdNot(request.getName(), projectId, tagId)) {
                throw new BadRequestException("Tag name '" + request.getName() + "' is already in use by another tag.");
            }
            tag.setName(request.getName());
        }

        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }

        // Hibernate sẽ tự động cập nhật updatedAt khi save
        Tag updatedTag = tagRepository.save(tag);
        return mapToResponse(updatedTag);
    }

    @Override
    @Transactional
    public void deleteTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId) {
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);
        tagRepository.delete(tag);
    }

    @Override
    @Transactional
    public List<TagResponse> assignTagToTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId) {
        Task task = validator.validateTask(companyId, workspaceId, projectId, taskId);
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);

        if (task.getTags().contains(tag)) {
            throw new BadRequestException("This task has already been assigned to this tag.");
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

        if (!task.getTags().contains(tag)) {
            throw new BadRequestException("This task is not assigned to this tag, cannot remove.");
        }

        task.getTags().remove(tag);
        taskRepository.save(task);

        return task.getTags().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- Helper Mapping (Cập nhật đầy đủ) ---
    private TagResponse mapToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .description(tag.getDescription())
                .projectId(tag.getProject().getId())
                
                // Audit info
                .createdById(tag.getCreatedBy() != null ? tag.getCreatedBy().getId() : null)
                .createdByName(tag.getCreatedBy() != null ? tag.getCreatedBy().getFullName() : null)
                .createdByAvatar(tag.getCreatedBy() != null ? tag.getCreatedBy().getAvatarUrl() : null)
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt()) // Thêm dòng này
                .build();
    }
}