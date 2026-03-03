package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi người dùng tạo một bình luận mới cho Task.
 */
@Data
public class CommentRequest {

    // ==========================================
    // REQUEST DATA
    // ==========================================

    /**
     * Nội dung bình luận.
     * Bắt buộc phải có, không được để trống hoặc chỉ chứa các ký tự khoảng trắng.
     */
    @NotBlank(message = "Comment content must not be blank")
    private String content;

}