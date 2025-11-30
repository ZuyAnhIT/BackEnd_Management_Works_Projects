// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/TagServiceImpl.java
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
import com.quanlyduan.project_manager_api.validation.ProjectHierarchyValidator;

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

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public TagServiceImpl(TagRepository tagRepository, TaskRepository taskRepository, ProjectHierarchyValidator validator, SecurityService securityService) {
        this.tagRepository = tagRepository;
        this.taskRepository = taskRepository;
        this.validator = validator;
        this.securityService = securityService;
    }

    // ======================================================
    // 1. LẤY DANH SÁCH TAG (LIST & FILTER)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getProjectTags(Integer companyId, Integer workspaceId, Integer projectId, TagFilterRequest filter) {
        // 1. Validate: Kiểm tra tính hợp lệ của Project (Hierarchy)
        validator.validateProject(companyId, workspaceId, projectId);
        
        // 2. Lấy Specification (Bộ lọc động)
        Specification<Tag> spec = TagSpecification.getFilterSpec(projectId, filter);
        
        // 3. Query DB và Map sang DTO
        return tagRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ======================================================
    // 2. TẠO TAG MỚI (CREATE TAG)
    // ======================================================
    @Override
    @Transactional
    public TagResponse createTag(Integer companyId, Integer workspaceId, Integer projectId, CreateTagRequest request) {
        // 1. Validate và Lấy Project
        Project project = validator.validateProject(companyId, workspaceId, projectId);
        
        // 2. Kiểm tra trùng tên (Trong cùng một dự án)
        if (tagRepository.existsByNameAndProject_Id(request.getName(), projectId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Tag name '" + request.getName() + "' already exists in this project.");
        }
        
        // 3. Lấy thông tin người tạo
        User creator = securityService.getCurrentAuthenticatedUser();
        
        // 4. Tạo Entity
        Tag tag = Tag.builder()
                .project(project)
                .name(request.getName())
                // Mặc định màu xám nếu null
                .color(request.getColor() != null ? request.getColor() : "#95a5a6")
                .description(request.getDescription())
                .createdBy(creator)
                .build();
        
        // 5. Lưu và trả về
        return mapToResponse(tagRepository.save(tag));
    }

    // ======================================================
    // 3. CẬP NHẬT TAG (UPDATE TAG)
    // ======================================================
    @Override
    @Transactional
    public TagResponse updateTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId, UpdateTagRequest request) {
        // 1. Validate và Lấy Tag (kiểm tra full hierarchy)
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);

        // 2. Validate tên trùng (nếu có đổi tên)
        if (request.getName() != null && !request.getName().isEmpty()
                // Chỉ kiểm tra trùng tên nếu tên mới khác tên cũ
                && !Objects.equals(request.getName(), tag.getName())) {

            // Kiểm tra trùng tên với các tag khác trong Project
            if (tagRepository.existsByNameAndProject_IdAndIdNot(request.getName(), projectId, tagId)) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Tag name '" + request.getName() + "' is already in use by another tag.");
            }
            tag.setName(request.getName());
        }

        // 3. Cập nhật các trường Scalar
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }

        // 4. Lưu và trả về
        Tag updatedTag = tagRepository.save(tag);
        return mapToResponse(updatedTag);
    }

    // ======================================================
    // 4. XÓA TAG (DELETE TAG)
    // ======================================================
    @Override
    @Transactional
    public void deleteTag(Integer companyId, Integer workspaceId, Integer projectId, Integer tagId) {
        // 1. Validate và Lấy Tag
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);
        
        // 2. Thực hiện xóa cứng (Hibernate sẽ tự động xử lý các mối quan hệ ManyToMany)
        tagRepository.delete(tag);
    }

    // ======================================================
    // 5. GÁN TAG VÀO TASK (ASSIGN TAG)
    // ======================================================
    @Override
    @Transactional
    public List<TagResponse> assignTagToTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId) {
        // 1. Validate Task và Tag
        Task task = validator.validateTask(companyId, workspaceId, projectId, taskId);
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);

        // 2. Kiểm tra: Đã tồn tại chưa?
        if (task.getTags().contains(tag)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This task has already been assigned to this tag.");
        }

        // 3. Gán và lưu
        task.getTags().add(tag);
        taskRepository.save(task);

        // 4. Trả về danh sách tags mới của Task
        return task.getTags().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ======================================================
    // 6. GỠ TAG KHỎI TASK (REMOVE TAG)
    // ======================================================
    @Override
    @Transactional
    public List<TagResponse> removeTagFromTask(Integer companyId, Integer workspaceId, Integer projectId, Integer taskId, Integer tagId) {
        // 1. Validate Task và Tag
        Task task = validator.validateTask(companyId, workspaceId, projectId, taskId);
        Tag tag = validator.validateTag(companyId, workspaceId, projectId, tagId);

        // 2. Kiểm tra: Có tồn tại để gỡ không?
        if (!task.getTags().contains(tag)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This task is not assigned to this tag, cannot remove.");
        }

        // 3. Gỡ và lưu
        task.getTags().remove(tag);
        taskRepository.save(task);

        // 4. Trả về danh sách tags còn lại của Task
        return task.getTags().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ======================================================
    // ⚙️ PRIVATE HELPER: MAPPER
    // ======================================================
    /**
     * Helper: Map Tag Entity sang TagResponse DTO.
     */
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
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}