// File: com.quanlyduan.project_manager_api.controller.EpicController.java
package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.EpicResponse;
import com.quanlyduan.project_manager_api.service.EpicService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class EpicController {

    private final EpicService epicService;
    
    public EpicController(EpicService epicService) {
        this.epicService = epicService;
    }

    // --- 1. LẤY DANH SÁCH EPIC (KÈM FILTER) ---
    @GetMapping("/{projectId}/epics")
    @PreAuthorize("@securityService.hasPermission('project', #projectId, 'project:view')")
    public ResponseEntity<ApiResponse<List<EpicResponse>>> getEpicsByProject(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String keyword) {

        List<EpicResponse> epics = epicService.getEpicsByProject(projectId, keyword);
        return ResponseEntity.ok(ApiResponse.success("Fetched project epics", epics));
    }
}