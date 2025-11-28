package com.quanlyduan.project_manager_api.dto.response;
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class SubTaskResponse {
    private Integer id;
    private Integer parentTaskId;
    private String title;
    private String description;
    private SubTaskStatus status;
    private Integer assigneeId;
    private String assigneeName;
    private String assigneeAvatar;
    private BigDecimal estimatedHours;
    private Integer sortOrder;
}
