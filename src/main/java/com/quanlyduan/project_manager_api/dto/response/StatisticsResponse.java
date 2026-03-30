package com.quanlyduan.project_manager_api.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi du lieu thong ke bien dong cong viec (Task Analytics).
 * Tong hop cac chi so ve Task sap den han, moi tao, hoan thanh va cap nhat trong mot khoang thoi gian.
 */
@Getter
@Setter
@Builder
public class StatisticsResponse {

    // ======================================================
    // 1. THONG TIN NGU CANH (METADATA)
    // ======================================================

    /** Khoang thoi gian thuc hien thong ke (dang chuoi ISO hoac dd/MM/yyyy). */
    private String fromDate;
    private String toDate;

    // ======================================================
    // 2. NHOM QUAN TRONG: SAP DEN HAN (DUE SOON)
    // ======================================================

    /** So luong va Danh sach chi tiet cac Task sap den deadline (vi du: trong 48h toi). */
    private long dueSoonCount;
    private List<TaskSummaryResponse> dueSoonTasks; 

    // ======================================================
    // 3. NHOM MOI TAO (NEWLY CREATED)
    // ======================================================

    /** So luong va Danh sach cac Task moi duoc khoi tao trong ky. */
    private long createdCount;
    private List<TaskSummaryResponse> createdTasks; 

    // ======================================================
    // 4. NHOM HOAN THANH (COMPLETED)
    // ======================================================

    /** So luong va Danh sach cac Task da chuyen sang trang thai hoan thanh. */
    private long completedCount;
    private List<TaskSummaryResponse> completedTasks; 

    // ======================================================
    // 5. NHOM CAP NHAT (RECENTLY UPDATED)
    // ======================================================

    /** So luong va Danh sach cac Task co su thay doi ve noi dung hoac trang thai. */
    private long updatedCount;
    private List<TaskSummaryResponse> updatedTasks;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson co the Deserialize du lieu mot cach minh bach.
     */
    public StatisticsResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public StatisticsResponse(String fromDate, String toDate, long dueSoonCount, 
                              List<TaskSummaryResponse> dueSoonTasks, long createdCount, 
                              List<TaskSummaryResponse> createdTasks, long completedCount, 
                              List<TaskSummaryResponse> completedTasks, long updatedCount, 
                              List<TaskSummaryResponse> updatedTasks) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.dueSoonCount = dueSoonCount;
        this.dueSoonTasks = dueSoonTasks;
        this.createdCount = createdCount;
        this.createdTasks = createdTasks;
        this.completedCount = completedCount;
        this.completedTasks = completedTasks;
        this.updatedCount = updatedCount;
        this.updatedTasks = updatedTasks;
    }
}