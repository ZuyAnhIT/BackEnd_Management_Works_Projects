package com.quanlyduan.project_manager_api.controller;

import com.quanlyduan.project_manager_api.dto.response.ActivityLogResponse;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.service.impl.ActivityServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityServiceImpl activityService;

    // API linh hoạt cho cả 4 cấp độ
    // scope: COMPANY, WORKSPACE, PROJECT, USER
    // id: ID của đối tượng tương ứng
    @GetMapping("/{scope}/{id}")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getActivities(
            @PathVariable String scope,
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<ActivityLogResponse> logs = activityService.getLogsByContext(scope, id, page, size);
        return ResponseEntity.ok(ApiResponse.success("Activities retrieved successfully", logs));
    }
}