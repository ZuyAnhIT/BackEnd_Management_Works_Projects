package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectMemberSimpleResponse {
    private Integer memberId;
    private Integer userId;
    private String fullName;
    private String email;
    private String roleName;
    private MemberStatus status;
}
