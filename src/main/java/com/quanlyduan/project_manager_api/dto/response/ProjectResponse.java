package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin Project sau khi tạo (US7) hoặc dùng về sau cho GET.
 * Dùng DTO để tránh trả trực tiếp Entity (vòng lặp/lazy) nhưng vẫn giữ mapping tối thiểu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Integer id;
    private Integer workspaceId;

    private String name;
    private String projectCode;
    private String description;
    private String goal;
    private String coverImageUrl;

    private String status;
    private String priority;

    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate completedAt;

    private BigDecimal progress;

    private Integer managerId;
    private String managerName;

    private Integer createdById;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

