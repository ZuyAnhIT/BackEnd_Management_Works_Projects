// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/MyCompanyResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi thông tin tóm tắt về một Công ty mà người dùng đang tham gia.
 * Thường được sử dụng trong trang Dashboard (Tổng quan) hoặc danh sách "Công ty của tôi".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCompanyResponse {

    // ========================================================================
    // 1. THÔNG TIN CÔNG TY (COMPANY INFO)
    // ========================================================================

    // ID định danh của công ty
    private Integer companyId;

    // Tên hiển thị của công ty
    private String companyName;

    // Mã định danh công ty (ví dụ: "TECH-V")
    private String companyCode;

    // Mô tả ngắn gọn
    private String description;

    // Đường dẫn ảnh logo công ty
    private String logoUrl;

    // ========================================================================
    // 2. THÔNG TIN TƯ CÁCH THÀNH VIÊN (MEMBERSHIP INFO)
    // ========================================================================

    // Mã vai trò của người dùng trong công ty này (ví dụ: "COMPANY_ADMIN").
    // Frontend dùng để hiển thị badge hoặc quyền hạn.
    private String roleCode;

    // Trạng thái thành viên (ví dụ: "ACTIVE", "SUSPENDED").
    // Dùng để tô màu trạng thái trên giao diện.
    private String memberStatus;

    // Chức danh công việc cụ thể (ví dụ: "HR Manager").
    private String jobTitle;

    // Phòng ban trực thuộc (dạng text).
    private String department;

    // Thời điểm người dùng bắt đầu tham gia công ty.
    private LocalDateTime joinedAt;
}