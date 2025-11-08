package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class CommentRequest {

    @NotBlank(message = "Comment content must not be blank")
    private String content;

    // Danh sách ID của những người được @mention
    // Frontend sẽ gửi [1, 5, 12] nếu @mention 3 người
    private List<Integer> mentionedUserIds;
}