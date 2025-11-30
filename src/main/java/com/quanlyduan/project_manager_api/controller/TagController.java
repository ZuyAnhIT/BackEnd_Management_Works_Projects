// File: src/main/java/com/quanlyduan/project_manager_api/controller/TagController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateTagRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateTagRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.dto.response.TagResponse;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;

import org.springframework.security.access.prepost.PreAuthorize;

import com.quanlyduan.project_manager_api.service.TagService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

@RestController
// Endpoint phụ thuộc vào các cấp cha: Company -> Workspace -> Project
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects/{projectId}")
@CrossOrigin("*")
/**
 * Controller xử lý các nghiệp vụ liên quan đến Tag (Nhãn) của Dự án.
 */
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // ======================================================
    // 1. LẤY DANH SÁCH TAG (LIST & FILTER)
    // ======================================================
    @Operation(summary = "Get list of tags with advanced filtering")
    @GetMapping("/tags")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,

            // 1. Filter Keyword (Tìm theo tên hoặc mô tả)
            @RequestParam(required = false) String keyword,
            // 2. Filter theo danh sách tên chính xác (Cho checkbox multi-select)
            @RequestParam(required = false) List<String> names,
            // 3. Filter theo người tạo
            @RequestParam(required = false) Integer createdById,
            // 4. Filter ngày tạo (From)
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            // 5. Filter ngày tạo (To)
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo
    ) {
        // Đóng gói tham số vào DTO để truyền xuống Service (giữ Service sạch)
        TagFilterRequest filterRequest = new TagFilterRequest();
        filterRequest.setKeyword(keyword);
        filterRequest.setNames(names);
        filterRequest.setCreatedById(createdById);
        filterRequest.setCreatedFrom(createdFrom);
        filterRequest.setCreatedTo(createdTo);

        List<TagResponse> tags = tagService.getProjectTags(companyId, workspaceId, projectId, filterRequest);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved the tag list.", tags));
    }

    // ======================================================
    // 2. TẠO TAG MỚI (CREATE TAG)
    // ======================================================
    @Operation(summary = "Create tag")
    @PostMapping("/tags")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @PathVariable Integer companyId, @PathVariable Integer workspaceId, @PathVariable Integer projectId,
            @Valid @RequestBody CreateTagRequest request) {
        TagResponse tag = tagService.createTag(companyId, workspaceId, projectId, request);
        // Sửa thông báo trả về sang tiếng Anh (201 Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tag created successfully.", tag));
    }

    // ======================================================
    // 3. CẬP NHẬT TAG (UPDATE TAG)
    // ======================================================
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

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Tag updated successfully.", updatedTag));
    }

    // ======================================================
    // 4. XÓA TAG (DELETE TAG)
    // ======================================================
    @Operation(summary = "Delete a tag")
    @DeleteMapping("/tags/{tagId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')") // Giả định delete cần quyền edit cấu trúc
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable Integer companyId,
            @PathVariable Integer workspaceId,
            @PathVariable Integer projectId,
            @PathVariable Integer tagId) {

        tagService.deleteTag(companyId, workspaceId, projectId, tagId);

        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Tag deleted successfully.", null));
    }

    // ======================================================
    // 5. GÁN TAG VÀO TASK (ASSIGN TAG)
    // ======================================================
    @Operation(summary = "Assign tag to a task")
    @PostMapping("/tasks/{taskId}/tags/{tagId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> assignTag(
            @PathVariable Integer companyId, @PathVariable Integer workspaceId, @PathVariable Integer projectId,
            @PathVariable Integer taskId, @PathVariable Integer tagId) {
        // Logic service sẽ gán Tag vào Task
        List<TagResponse> updatedTags = tagService.assignTagToTask(companyId, workspaceId, projectId, taskId, tagId);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Tag assigned successfully.", updatedTags));
    }

    // ======================================================
    // 6. GỠ TAG KHỎI TASK (REMOVE TAG)
    // ======================================================
    @Operation(summary = "Remove tag from a task")
    @DeleteMapping("/tasks/{taskId}/tags/{tagId}") // Dùng DELETE cho quan hệ M-M
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> removeTag(
            @PathVariable Integer companyId, @PathVariable Integer workspaceId, @PathVariable Integer projectId,
            @PathVariable Integer taskId, @PathVariable Integer tagId) {

        List<TagResponse> updatedTags = tagService.removeTagFromTask(companyId, workspaceId, projectId, taskId, tagId);
        // Sửa thông báo trả về sang tiếng Anh
        return ResponseEntity.ok(ApiResponse.success("Tag removed successfully", updatedTags));
    }

}