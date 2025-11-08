package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.Priority;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Output cho project vừa tạo; dùng lại cho detail/list nếu cần. */
@Data
@Builder
public class ProjectResponse {
    private Integer id;
    private Integer workspaceId;

    // Trường name có trong DB (projects.name) — entity cũ chưa có field này,
    // chúng ta vẫn phản hồi theo dữ liệu đã insert bằng native query.
    private String name;

    private String projectCode;
    private String description;
    private String coverImageUrl;
    private String goal;
    private Integer projectTypeId;
    private ProjectStatus status;
    private Priority priority;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer managerId;
    private Integer createdById;
    private JsonNode boardConfig;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
