// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/MyProjectResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin tóm tắt về Dự án (Project) mà người dùng đang tham gia.
 * Được sử dụng chủ yếu trong API Dashboard (/api/dashboard/my-projects) để hiển thị danh sách nhanh.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyProjectResponse {

    // ========================================================================
    // 1. THÔNG TIN DỰ ÁN (PROJECT INFO)
    // ========================================================================

    // ID định danh của dự án
    private Integer projectId;

    // Tên dự án hiển thị
    private String projectName;

    // Mô tả ngắn gọn về dự án
    private String description;

    // Đường dẫn ảnh bìa của dự án (nếu có)
    private String coverImage;

    // Mã màu đại diện cho dự án (ví dụ để hiển thị tag hoặc background)
    private String color;

    // ========================================================================
    // 2. THÔNG TIN CẤP CHA (HIERARCHY INFO)
    // ========================================================================

    // ID của Không gian làm việc (Phòng ban) chứa dự án này
    private Integer workspaceId;

    // Tên của Không gian làm việc
    private String workspaceName;

    // ID của Công ty chứa dự án này
    private Integer companyId;

    // Tên của Công ty
    private String companyName;

    // ========================================================================
    // 3. NGỮ CẢNH NGƯỜI DÙNG (USER CONTEXT)
    // ========================================================================

    // Tên vai trò của người dùng hiện tại trong dự án này (ví dụ: "PROJECT_ADMIN", "MEMBER").
    // Frontend dùng thông tin này để hiển thị quyền hạn tương ứng trên thẻ dự án.
    private String myRoleName;
}