package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi du lieu hien thi tren bieu do Roadmap (Gantt Chart).
 * Dung chung cho ca Epic va Sprint de ve cac thanh bar tren truc thoi gian.
 */
@Getter
@Setter
@Builder
public class RoadmapItemResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String TYPE_EPIC = "EPIC";
    public static final String TYPE_SPRINT = "SPRINT";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================
    
    /** ID duy nhat cho Frontend (vi du: "epic-101", "sprint-5"). */
    private String id; 
    
    /** ID goc trong Database (dung de truy van chi tiet). */
    private Integer originalId;
    
    /** Ten hien thi tren thanh Roadmap (Ten Epic hoac Sprint). */
    private String title;
    
    /** Loai thuc the: EPIC hoac SPRINT. */
    private String type; 

    // ======================================================
    // 2. DU LIEU TRUC X (TIMELINE DATA)
    // ======================================================
    
    /** Thoi gian bat dau va ket thuc de xac dinh do dai thanh bar. */
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    /** * Tien do hoan thanh (0.0 - 100.0). 
     * Frontend dung de ve phan mau dam/nhat ben trong thanh bar. 
     */
    private Double progress; 

    // ======================================================
    // 3. GIAO DIEN & THONG KE (UI & METRICS)
    // ======================================================
    
    /** Trang thai hien tai (vi du: OPEN, IN_PROGRESS, DONE). */
    private String status;
    
    /** Ma mau HEX dai dien cho thanh bar tren Roadmap. */
    private String color;
    
    /** Metadata bo sung dung cho Tooltip khi hover vao thanh bar. */
    private Long totalTasks;
    private Long completedTasks;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public RoadmapItemResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public RoadmapItemResponse(String id, Integer originalId, String title, String type, 
                               LocalDateTime startDate, LocalDateTime endDate, Double progress, 
                               String status, String color, Long totalTasks, Long completedTasks) {
        this.id = id;
        this.originalId = originalId;
        this.title = title;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.status = status;
        this.color = color;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
    }
}