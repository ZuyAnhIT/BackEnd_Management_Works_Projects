// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateTaskEpicRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import lombok.Data;

@Data
public class UpdateTaskEpicRequest {
    
    // ID của Epic muốn gán vào Task. 
    // Nếu là null, có nghĩa là gỡ Task khỏi Epic hiện tại.
    private Integer epicId;
}