package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet cua mot Epic (Su thi/Muc tieu lon).
 * Bao gom ca thong tin ho so va cac chi so tien do duoc tong hop tu cac Task con.
 */
@Getter
@Setter
@Builder
public class EpicResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final double MIN_PROGRESS = 0.0;
    public static final double MAX_PROGRESS = 100.0;

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================
    
    private Integer id;
    
    /** ID cua Du an (Project) truc thuoc. */
    private Integer projectId;
    
    /** Ma dinh danh Epic (vi du: "PROJ-E-01"). */
    private String epicCode; 

    // ======================================================
    // 2. THONG TIN CO BAN (BASIC INFO)
    // ======================================================
    
    private String name;
    private String description;
    
    /** Ma mau hien thi de phan biet cac Epic tren giao dien (vi du: #8E44AD). */
    private String color;
    
    /** Trang thai hien tai (vi du: OPEN, IN_PROGRESS, DONE). */
    private String status;

    // ======================================================
    // 3. THONG TIN THOI GIAN (TIMELINE)
    // ======================================================
    
    private LocalDate startDate;
    private LocalDate dueDate;
    
    /** Thoi diem khoi tao Epic trong he thong. */
    private LocalDateTime createdAt; 

    // ======================================================
    // 4. CHI SO TIEN DO (METRICS)
    // ======================================================
    
    /** Tong so luong Task thuoc Epic nay. */
    private Integer totalTasks;
    
    /** So luong Task da hoan thanh (thong qua co isCompleted). */
    private Integer tasksCompleted;
    
    /** * Ty le hoan thanh (Scale 0.0 - 100.0).
     * Dung de ve thanh Progress Bar tren Dashboard hoac Project List.
     */
    private Double progressPercentage;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public EpicResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong dung cach.
     */
    public EpicResponse(Integer id, Integer projectId, String epicCode, String name, 
                        String description, String color, String status, 
                        LocalDate startDate, LocalDate dueDate, LocalDateTime createdAt, 
                        Integer totalTasks, Integer tasksCompleted, Double progressPercentage) {
        this.id = id;
        this.projectId = projectId;
        this.epicCode = epicCode;
        this.name = name;
        this.description = description;
        this.color = color;
        this.status = status;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.totalTasks = totalTasks;
        this.tasksCompleted = tasksCompleted;
        this.progressPercentage = progressPercentage;
    }
}