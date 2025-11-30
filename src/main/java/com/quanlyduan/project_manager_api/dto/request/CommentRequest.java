// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CommentRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi người dùng tạo một bình luận mới cho Task.
 */
@Data
public class CommentRequest {

    // Nội dung bình luận (Bắt buộc, không được để trống hoặc chỉ chứa khoảng trắng)
    @NotBlank(message = "Comment content must not be blank")
    private String content;
}