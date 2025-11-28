package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.request.CreateTagRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thẻ thành công", tags));
    }

    @Operation(summary = "Tạo thẻ mới")
    @PostMapping("/tags")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:edit')")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @PathVariable Integer companyId, @PathVariable Integer workspaceId, @PathVariable Integer projectId,
            @Valid @RequestBody CreateTagRequest request) {
        TagResponse tag = tagService.createTag(companyId, workspaceId, projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo thành công", tag));
    }

}
