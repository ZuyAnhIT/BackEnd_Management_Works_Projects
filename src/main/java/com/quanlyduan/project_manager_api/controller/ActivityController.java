package com.quanlyduan.project_manager_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.ActivityLogResponse;
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.service.impl.ActivityServiceImpl;

/**
 * Controller quản lý việc truy xuất lịch sử hoạt động (Activity Log) của hệ thống.
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    // Khai báo các hằng số mặc định cho phân trang
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE = "20";

    private final ActivityServiceImpl activityService;

    // Khởi tạo thủ công để tiêm (inject) phụ thuộc thay vì dùng RequiredArgsConstructor
    public ActivityController(ActivityServiceImpl activityService) {
        this.activityService = activityService;
    }

    /**
     * Lấy danh sách lịch sử hoạt động linh hoạt theo từng cấp độ.
     * Các cấp độ (scope) được hỗ trợ: COMPANY, WORKSPACE, PROJECT, USER.
     */
    @GetMapping("/{scope}/{id}")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getActivities(
            @PathVariable String scope,
            @PathVariable Integer id,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size) {

        List<ActivityLogResponse> logs = activityService.getLogsByContext(scope, id, page, size);
        
        return ResponseEntity.ok(ApiResponse.success("Activities retrieved successfully.", logs));
    }
}