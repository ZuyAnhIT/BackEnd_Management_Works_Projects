package com.quanlyduan.project_manager_api.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.request.CreateTagRequest;
import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTagRequest;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.TagResponse;
import com.quanlyduan.project_manager_api.service.TagService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * Controller xử lý các nghiệp vụ liên quan đến quản lý Nhãn (Tag) trong Dự án.
 * Các thao tác tuân thủ hệ thống phân cấp: Company -> Workspace -> Project.
 */
@RestController
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects/{projectId}")
@CrossOrigin("*")
public class TagController {

    // Khai báo các câu thông báo trả về (Response Messages)
    private static final String MSG_FETCH_LIST_SUCCESS = "Successfully retrieved the tag list.";
    private static final String MSG_CREATE_SUCCESS = "Tag created successfully.";
    private static final String MSG_UPDATE_SUCCESS = "Tag updated successfully.";
    private static final String MSG_DELETE_SUCCESS = "Tag deleted successfully.";
    private static final String MSG_ASSIGN_SUCCESS = "Tag assigned successfully.";
    private static final String MSG_REMOVE_SUCCESS = "Tag removed successfully.";

    private final TagService tagService;

    // Khởi tạo thủ công để tiêm phụ thuộc (Dependency Injection)
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // ========================================================================
    // QUẢN LÝ DANH MỤC NHÃN (TAG CRUD)
    // ========================================================================

    /**
     * Lấy danh sách các nhãn thuộc dự án với các bộ lọc nâng cao.
     */
    @Operation(summary = "Get list of tags with advanced filtering")
    @GetMapping("/tags")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> names,
            @RequestParam(required = false) Integer createdById,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo) {

        TagFilterRequest filterRequest = createFilterRequest(keyword, names, createdById, createdFrom, createdTo);
        List<TagResponse> tags = tagService.getProjectTags(companyId, workspaceId, projectId, filterRequest);
        
        return ResponseEntity.ok(ApiResponse.success(MSG_FETCH_LIST_SUCCESS, tags));
    }

    /**
     * Tạo mới một nhãn cho dự án.
     */
    @Operation(summary = "Create tag")
    @PostMapping("/tags")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @PathVariable Integer companyId, 
            @PathVariable Integer workspaceId, 
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateTagRequest request) {
            
        TagResponse tag = tagService.createTag(companyId, workspaceId, projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MSG_CREATE_SUCCESS, tag));
    }

    /**
     * Cập nhật thông tin chi tiết (tên, màu sắc) của một nhãn.
     */
    @Operation(summary = "Update tag details")
    @PutMapping("/tags/{tagId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer tagId,
            @Valid @RequestBody UpdateTagRequest request) {

        TagResponse updatedTag = tagService.updateTag(companyId, workspaceId, projectId, tagId, request);
        return ResponseEntity.ok(ApiResponse.success(MSG_UPDATE_SUCCESS, updatedTag));
    }

    /**
     * Xóa một nhãn khỏi danh mục của dự án.
     */
    @Operation(summary = "Delete a tag")
    @DeleteMapping("/tags/{tagId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer tagId) {

        tagService.deleteTag(companyId, workspaceId, projectId, tagId);
        return ResponseEntity.ok(ApiResponse.success(MSG_DELETE_SUCCESS, null));
    }

    // ========================================================================
    // QUẢN LÝ NHÃN THEO CÔNG VIỆC (TASK-TAG ASSIGNMENT)
    // ========================================================================

    /**
     * Gán nhãn cho một công việc (Task) cụ thể.
     */
    @Operation(summary = "Assign tag to a task")
    @PostMapping("/tasks/{taskId}/tags/{tagId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> assignTag(
            @PathVariable Integer companyId, 
            @PathVariable Integer workspaceId, 
            @PathVariable Integer projectId,
            @PathVariable Integer taskId, 
            @PathVariable Integer tagId) {
            
        List<TagResponse> updatedTags = tagService.assignTagToTask(companyId, workspaceId, projectId, taskId, tagId);
        return ResponseEntity.ok(ApiResponse.success(MSG_ASSIGN_SUCCESS, updatedTags));
    }

    /**
     * Gỡ bỏ nhãn khỏi một công việc (Task).
     */
    @Operation(summary = "Remove tag from a task")
    @DeleteMapping("/tasks/{taskId}/tags/{tagId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> removeTag(
            @PathVariable Integer companyId, 
            @PathVariable Integer workspaceId, 
            @PathVariable Integer projectId,
            @PathVariable Integer taskId, 
            @PathVariable Integer tagId) {

        List<TagResponse> updatedTags = tagService.removeTagFromTask(companyId, workspaceId, projectId, taskId, tagId);
        return ResponseEntity.ok(ApiResponse.success(MSG_REMOVE_SUCCESS, updatedTags));
    }

    // --- CÁC HÀM HỖ TRỢ NỘI BỘ ---

    /**
     * Đóng gói các tham số lọc vào đối tượng Request để truyền xuống tầng Service.
     */
    private TagFilterRequest createFilterRequest(String keyword, List<String> names, Integer createdById, 
                                                 LocalDateTime createdFrom, LocalDateTime createdTo) {
        TagFilterRequest filterRequest = new TagFilterRequest();
        filterRequest.setKeyword(keyword);
        filterRequest.setNames(names);
        filterRequest.setCreatedById(createdById);
        filterRequest.setCreatedFrom(createdFrom);
        filterRequest.setCreatedTo(createdTo);
        return filterRequest;
    }
}