// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ImportResultResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor; // Thêm dòng này
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; // Thêm dòng này
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTaskResultResponse {
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<ImportError> errors;

    @Data
    @Builder
    @NoArgsConstructor      // Quan trọng: Thêm dòng này
    @AllArgsConstructor     // Quan trọng: Thêm dòng này để fix lỗi "is not public"
    public static class ImportError {
        private int rowIndex;
        private String columnName;
        private String message;
    }
}