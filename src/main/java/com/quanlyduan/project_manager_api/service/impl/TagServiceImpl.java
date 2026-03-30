package com.quanlyduan.project_manager_api.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.CreateTagRequest;
import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTagRequest;
import com.quanlyduan.project_manager_api.dto.response.TagResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.Tag;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.TagRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.specification.TagSpecification;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.TagService;
import com.quanlyduan.project_manager_api.validation.ProjectHierarchyValidator;

@Service
public class TagServiceImpl implements TagService {

    // Khai bao hang so de loai bo hardcode
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_ASSIGN_TAG = "ASSIGN_TAG";
    public static final String ACTION_REMOVE_TAG = "REMOVE_TAG";

    public static final String ENTITY_TAG = "TAG";
    public static final String ENTITY_TASK = "TASK";

    public static final String DESC_CREATE_TAG = "Create new Tag";
    public static final String DESC_UPDATE_TAG = "Update Tag";
    public static final String DESC_DELETE_TAG = "Delete Tag";
    public static final String DESC_ASSIGN_TAG = "Assign Tag to Task";
    public static final String DESC_REMOVE_TAG = "Remove Tag from Task";

    public static final String ERROR_TAG_NAME_EXISTS = "Tag name '%s' already exists in this project.";
    public static final String ERROR_TAG_NAME_IN_USE = "Tag name '%s' is already in use by another tag.";
    public static final String ERROR_TAG_ALREADY_ASSIGNED = "This task has already been assigned to this tag.";
    public static final String ERROR_TAG_NOT_ASSIGNED = "This task is not assigned to this tag, cannot remove.";

    public static final String DEFAULT_TAG_COLOR = "#95a5a6";

    // Khai bao cac bien phu thuoc
    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;
    private final ProjectHierarchyValidator validator;
    private final SecurityService securityService;

    // Constructor khoi tao thu cong thay the cho @RequiredArgsConstructor
    public TagServiceImpl(TagRepository tagRepository, 
                          TaskRepository taskRepository, 
                          ProjectHierarchyValidator validator, 
                          SecurityService securityService) {
        this.tagRepository = tagRepository;
        this.taskRepository = taskRepository;
        this.validator = validator;
        this.securityService = securityService;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getProjectTags(Integer companyId, Integer workspaceId, Integer projectId, TagFilterRequest filter) {
        // Kiem tra tinh hop le cua du an theo he thong phan cap
        validator.validateProject(companyId, workspaceId, projectId);
        
        // Khoi tao bo loc dong va lay danh sach the (tag)
        Specification<Tag> spec = TagSpecification.getFilterSpec(projectId, filter);
        
        // Chuyen doi Entity sang DTO
        return tagRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_CREATE, entityType = ENTITY_TAG, description = DESC_CREATE_TAG)
    public TagResponse createTag(Integer companyId, Integer workspaceId, Integer projectId, CreateTagRequest request) {
        // Xac thuc va lay thong tin du an hien tai
        Project project = validator.validateProject(companyId, workspaceId, projectId);
        
        // Kiem tra trung ten the trong cung mot du an
        if (tagRepository.existsByNameAndProject_Id(request.getName(), projectId)) {
            throw new BadRequestException(String.format(ERROR_TAG_NAME_EXISTS, request.getName()));
        }
        
        // Lay thong tin nguoi tao
        User creator = securityService.getCurrentAuthenticatedUser();
        
        // Khoi tao the moi
        Tag tag = Tag.builder()
                .project(project)
                .name(request.getName())
                .color(request.getColor() != null ? request.getColor() : DEFAULT_TAG_COLOR)
                .description(request.getDescription())
                .createdBy(creator)
                .build();
        
        // Luu the vao co so du lieu va tra ve DTO
        return mapToResponse(tagRepository.save(tag));
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_TAG, description = DESC_UPDATE_TAG)
    public TagResponse updateTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId, UpdateTagRequest request) {
        // Kiem tra va lay thong tin the (tag) tu he thong phan cap
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);

        // Cap nhat ten the neu co su thay doi va kiem tra tinh duy nhat cua ten moi
        if (request.getName() != null && !request.getName().isEmpty()
                && !Objects.equals(request.getName(), tag.getName())) {

            if (tagRepository.existsByNameAndProject_IdAndIdNot(request.getName(), projectId, tagId)) {
                throw new BadRequestException(String.format(ERROR_TAG_NAME_IN_USE, request.getName()));
            }
            tag.setName(request.getName());
        }

        // Cap nhat mau sac va mo ta
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }

        Tag updatedTag = tagRepository.save(tag);
        return mapToResponse(updatedTag);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_DELETE, entityType = ENTITY_TAG, description = DESC_DELETE_TAG)
    public void deleteTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId) {
        // Kiem tra va lay the (tag) hop le
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);
        
        // Thuc hien xoa cung the ra khoi he thong
        tagRepository.delete(tag);
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_ASSIGN_TAG, entityType = ENTITY_TASK, description = DESC_ASSIGN_TAG)
    public List<TagResponse> assignTagToTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId) {
        // Xac thuc thong tin cong viec va the (tag)
        Task task = validator.validateTask(companyId, workspaceId, projectId, taskId);
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);

        // Kiem tra the da duoc gan cho cong viec nay chua
        if (task.getTags().contains(tag)) {
            throw new BadRequestException(ERROR_TAG_ALREADY_ASSIGNED);
        }

        // Gan the vao cong viec
        task.getTags().add(tag);
        taskRepository.save(task);

        // Tra ve danh sach the hien tai cua cong viec
        return task.getTags().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @LogActivity(action = ACTION_REMOVE_TAG, entityType = ENTITY_TASK, description = DESC_REMOVE_TAG)
    public List<TagResponse> removeTagFromTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId) {
        // Xac thuc thong tin cong viec va the (tag)
        Task task = validator.validateTask(companyId, workspaceId, projectId, taskId);
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);

        // Kiem tra the co ton tai tren cong viec de go hay khong
        if (!task.getTags().contains(tag)) {
            throw new BadRequestException(ERROR_TAG_NOT_ASSIGNED);
        }

        // Go the khoi cong viec
        task.getTags().remove(tag);
        taskRepository.save(task);

        // Tra ve danh sach the con lai tren cong viec
        return task.getTags().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---
    
    private TagResponse mapToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .description(tag.getDescription())
                .projectId(tag.getProject().getId())
                .createdById(tag.getCreatedBy() != null ? tag.getCreatedBy().getId() : null)
                .createdByName(tag.getCreatedBy() != null ? tag.getCreatedBy().getFullName() : null)
                .createdByAvatar(tag.getCreatedBy() != null ? tag.getCreatedBy().getAvatarUrl() : null)
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}