package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi tien do cua mot Epic (Tinh nang lon).
 * Cung cap cac chi so KPI ve so luong Task va Story Points de theo doi hieu suat Agile.
 */
@Getter
@Setter
@Builder
public class EpicProgressResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final double MIN_PROGRESS = 0.0;
    public static final double MAX_PROGRESS = 100.0;

    // ======================================================
    // 1. THONG TIN EPIC (EPIC IDENTITY)
    // ======================================================
    
    private Integer epicId;
    private String epicName;
    private String epicCode;

    /** Ma mau hien thi cua Epic tren bieu do tien do (vi du: "#f1c40f"). */
    private String color;

    // ======================================================
    // 2. KPI: SO LUONG CONG VIEC (TASK METRICS)
    // ======================================================

    /** Tong so luong Task thuoc Epic nay. */
    private long totalTasks;

    /** So luong Task da chuyen sang trang thai Hoan thanh (Done). */
    private long completedTasks;

    /** * Phan tram hoan thanh dua tren so luong Task.
     * Cong thuc: (completedTasks / totalTasks) * 100. 
     */
    private double taskProgressPercent;

    // ======================================================
    // 3. KPI: DIEM CAU CHUYEN (STORY POINT METRICS)
    // ======================================================

    /** Tong so Story Points cua tat ca Task trong Epic. */
    private long totalPoints;

    /** So Story Points cua cac Task da hoan thanh. */
    private long completedPoints;

    /** * Phan tram hoan thanh dua tren khoi luong cong viec (Story Points).
     * Day la chi so quan trong nhat de danh gia tien do thuc te trong Agile.
     */
    private double pointProgressPercent;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public EpicProgressResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong dung cach.
     */
    public EpicProgressResponse(Integer epicId, String epicName, String epicCode, String color, 
                                long totalTasks, long completedTasks, double taskProgressPercent, 
                                long totalPoints, long completedPoints, double pointProgressPercent) {
        this.epicId = epicId;
        this.epicName = epicName;
        this.epicCode = epicCode;
        this.color = color;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.taskProgressPercent = taskProgressPercent;
        this.totalPoints = totalPoints;
        this.completedPoints = completedPoints;
        this.pointProgressPercent = pointProgressPercent;
    }
}