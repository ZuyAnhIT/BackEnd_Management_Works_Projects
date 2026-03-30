package com.quanlyduan.project_manager_api.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi ket qua nhap du lieu (Import) Cong viec tu file (Excel/CSV).
 * Cung cap cac chi so thong ke va danh sach chi tiet cac loi phat sinh trong qua trinh xu ly.
 */
@Getter
@Setter
@Builder
public class ImportTaskResultResponse {

    // ======================================================
    // 1. CHI SO THONG KE (SUMMARY METRICS)
    // ======================================================
    
    /** Tong so dong du lieu duoc doc tu file. */
    private int totalRows;

    /** So luong ban ghi da nhap vao he thong thanh cong. */
    private int successCount;

    /** So luong ban ghi bi loi va khong the nhap duoc. */
    private int errorCount;

    // ======================================================
    // 2. CHI TIET LOI (ERROR DETAILS)
    // ======================================================

    /** Danh sach chi tiet cac loi tai tung dong va tung cot. */
    private List<ImportError> errors;

    // ======================================================
    // INNER CLASS: CHI TIET LOI TUNG DONG (RULE 8)
    // ======================================================

    @Getter
    @Setter
    @Builder
    public static class ImportError {
        
        /** Vi tri dong bi loi trong file (thuong bat dau tu 1 hoac 2). */
        private int rowIndex;

        /** Ten cot phat sinh loi (vi du: "Deadline", "Assignee"). */
        private String columnName;

        /** Noi dung thong bao loi chi tiet. */
        private String message;

        // Constructor viet tay cho Inner Class (Fix loi visibility)
        public ImportError() {}

        public ImportError(int rowIndex, String columnName, String message) {
            this.rowIndex = rowIndex;
            this.columnName = columnName;
            this.message = message;
        }
    }

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public ImportTaskResultResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public ImportTaskResultResponse(int totalRows, int successCount, int errorCount, 
                                    List<ImportError> errors) {
        this.totalRows = totalRows;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.errors = errors;
    }
}