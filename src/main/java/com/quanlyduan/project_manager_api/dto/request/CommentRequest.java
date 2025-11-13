// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CommentRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
// import java.util.List; // SỬA: Xóa import

@Data
public class CommentRequest {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;

    // SỬA: XÓA BỎ
    // private List<Integer> mentionedUserIds;
}