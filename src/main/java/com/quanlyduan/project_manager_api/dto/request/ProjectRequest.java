package com.quanlyduan.project_manager_api.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * Payload tạo Project mới (US7) – phiên bản refactor "viết lại" theo yêu cầu:
 * - Chấp nhận boardConfig là JSON (JsonNode) để client gửi JSON trực tiếp, không cần escape.
 * - priority là String tự do; service sẽ parse, nếu invalid sẽ dùng mặc định MEDIUM (thân thiện hơn).
 * - managerId / projectTypeId: nếu null hoặc <= 0, coi như không gửi để tránh 404 không cần thiết.
 */
@Data
public class ProjectRequest {
    @NotBlank(message = "Tên dự án không được để trống")
    private String name;

    @NotBlank(message = "Project name must not be blank")
    private String projectCode;

    private String description;
    private String goal;
    private String coverImageUrl;
    private JsonNode boardConfig; // JSON object, sent directly by client

    private Integer projectTypeId; // optional
    private Integer managerId;     // optional

    // Optional planning fields
    private String priority;       // e.g. LOW, MEDIUM, HIGH, URGENT (invalid -> default MEDIUM)
    private LocalDate startDate;
    private LocalDate dueDate;
}
