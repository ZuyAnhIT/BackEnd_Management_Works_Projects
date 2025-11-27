package com.quanlyduan.project_manager_api.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TagFilterRequest {
    // Tìm kiếm chung (tên hoặc mô tả)
    private String keyword; 
    
    // Tìm kiếm chính xác theo danh sách tên (VD: user chọn checkbox nhiều tag)
    private List<String> names; 

    // Lọc theo người tạo
    private Integer createdById;

    // Lọc theo khoảng thời gian tạo
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;
    
    // Lưu ý: Nếu sau này DB thêm cột status/updatedAt thì bổ sung vào đây
    // private String status; 
    // private LocalDateTime updatedFrom;
}