// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/TagFilterRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa các tiêu chí để lọc danh sách Thẻ (Tag).
 * Được sử dụng để truyền tham số tìm kiếm từ Controller xuống Service/Repository (thường dùng với Specification).
 */
@Data
public class TagFilterRequest {

    // ========================================================================
    // 1. TÌM KIẾM TỪ KHÓA (TEXT SEARCH)
    // ========================================================================
    
    // Từ khóa tìm kiếm chung.
    // Logic xử lý: Thường dùng để tìm gần đúng (LIKE %keyword%) trong Tên thẻ hoặc Mô tả.
    private String keyword; 

    // ========================================================================
    // 2. LỌC THEO THUỘC TÍNH (ATTRIBUTE FILTER)
    // ========================================================================

    // Danh sách tên thẻ cụ thể.
    // Logic xử lý: Dùng toán tử IN để lọc chính xác (ví dụ: user chọn nhiều checkbox).
    private List<String> names; 

    // ID của người tạo thẻ.
    // Logic xử lý: Lọc chính xác theo ID người dùng (Equal).
    private Integer createdById;

    // ========================================================================
    // 3. LỌC THEO THỜI GIAN (DATE RANGE FILTER)
    // ========================================================================

    // Thời điểm bắt đầu (Từ ngày).
    // Logic xử lý: Tìm các thẻ được tạo SAU hoặc BẰNG thời điểm này (>=).
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;

    // Thời điểm kết thúc (Đến ngày).
    // Logic xử lý: Tìm các thẻ được tạo TRƯỚC hoặc BẰNG thời điểm này (<=).
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;
}