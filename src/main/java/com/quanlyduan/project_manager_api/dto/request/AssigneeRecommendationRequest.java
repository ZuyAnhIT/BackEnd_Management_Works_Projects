package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import lombok.Data;
import java.util.List;

@Data
public class AssigneeRecommendationRequest {
    private String title;           // VD: "Fix lỗi thanh toán VNPAY"
    private String description;     // VD: "API trả về lỗi 500 khi..."
    private TaskType taskType;      // VD: BUG
    private List<String> tags;      // VD: ["Backend", "Payment", "Java"]
    private Integer storyPoints;    // VD: 5 (Để tính xem giao xong có bị overload không)
}