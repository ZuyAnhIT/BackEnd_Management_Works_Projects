package com.quanlyduan.project_manager_api.service.impl;

import java.util.stream.Collectors;

import com.quanlyduan.project_manager_api.dto.request.CreateTagRequest;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.Tag;

import com.quanlyduan.project_manager_api.model.User;
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


@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final ProjectHierarchyValidator validator;
    private final SecurityService securityService;

    public TagServiceImpl(TagRepository tagRepository, ProjectHierarchyValidator validator, SecurityService securityService) {
        this.tagRepository = tagRepository;
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
    public TagResponse createTag(Integer companyId, Integer workspaceId, Integer projectId, CreateTagRequest request) {
        Project project = validator.validateProject(companyId, workspaceId, projectId);
        if (tagRepository.existsByNameAndProject_Id(request.getName(), projectId)) {
            throw new BadRequestException("Tag này đã tồn tại.");
        }
        User creator = securityService.getCurrentAuthenticatedUser();
        Tag tag = Tag.builder().project(project).name(request.getName())
                .color(request.getColor() != null ? request.getColor() : "#95a5a6")
                .description(request.getDescription()).createdBy(creator).build();
        return mapToResponse(tagRepository.save(tag));
    }

}
