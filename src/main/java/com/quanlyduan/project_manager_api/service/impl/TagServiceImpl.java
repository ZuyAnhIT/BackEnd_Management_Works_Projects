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
    public void assignTagToTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId) {
        Task task = validator.validateTask(companyId, workspaceId, projectId, taskId);
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);
        task.getTags().add(tag);
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void removeTagFromTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId) {
        Task task = validator.validateTask(companyId, workspaceId, projectId, taskId);
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);
        task.getTags().remove(tag);
        taskRepository.save(task);
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
