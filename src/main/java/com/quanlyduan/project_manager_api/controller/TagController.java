package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateTagRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

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
@RequestMapping("/api/companies/{companyId}/workspaces/{workspaceId}/projects/{projectId}")
@CrossOrigin("*")
@Tag(name = "Tag Management", description = "APIs for managing tags within projects")
public class TagController {
    private final TagService tagService;
    
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

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
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved the card list", tags));
    }

    @PostMapping("/tags")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @PathVariable Integer companyId, @PathVariable Integer workspaceId, @PathVariable Integer projectId,
            @Valid @RequestBody CreateTagRequest request) {
        TagResponse tag = tagService.createTag(companyId, workspaceId, projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created tag successfully", tag));
    }
    
    @PostMapping("/tasks/{taskId}/tags/{tagId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> assignTag(
            @PathVariable Integer companyId, @PathVariable Integer workspaceId, @PathVariable Integer projectId,
            @PathVariable Integer taskId, @PathVariable Integer tagId) {
        // 2. Hứng giá trị trả về từ Service
        List<TagResponse> updatedTags = tagService.assignTagToTask(companyId, workspaceId, projectId, taskId, tagId);
        return ResponseEntity.ok(ApiResponse.success("Tag assigned successfully", updatedTags));
    }

    @DeleteMapping("/tasks/{taskId}/tags/{tagId}")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'task:edit')")
    public ResponseEntity<ApiResponse<Object>> removeTag(
            @PathVariable Integer companyId, @PathVariable Integer workspaceId, @PathVariable Integer projectId,
            @PathVariable Integer taskId, @PathVariable Integer tagId) {

        List<TagResponse> updatedTags = tagService.removeTagFromTask(companyId, workspaceId, projectId, taskId, tagId);
        return ResponseEntity.ok(ApiResponse.success("Card removed successfully", updatedTags));
    }

    
}