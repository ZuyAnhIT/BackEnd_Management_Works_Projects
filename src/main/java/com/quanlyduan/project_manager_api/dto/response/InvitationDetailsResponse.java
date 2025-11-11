// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/InvitationDetailsResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDetailsResponse {

    /**
     * Email của người được mời (để điền vào form)
     */
    private String email;

    /**
     * Tên công ty mời (để hiển thị "Công ty X đã mời bạn")
     */
    private String companyName;

    /**
     * Mấu chốt: Cho Frontend biết email này đã có tài khoản hay chưa
     */
    private boolean accountExists;
}