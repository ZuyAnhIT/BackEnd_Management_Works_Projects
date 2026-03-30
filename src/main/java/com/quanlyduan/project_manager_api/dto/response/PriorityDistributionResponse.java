package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi du lieu phan bo cong viec theo đo uu tien (Priority Distribution).
 * Thuong duoc su dung de hien thi bieu do tron (Pie Chart) tren Dashboard cua Project hoac Company.
 */
@Getter
@Setter
@Builder
public class PriorityDistributionResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final double MIN_PERCENT = 0.0;
    public static final double MAX_PERCENT = 100.0;

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY INFO)
    // ======================================================
    
    /** Ten hien thi cua do uu tien (vi du: "Thap", "Trung binh", "Khan cap"). */
    private String priorityName; 

    /** Ma Code cua Enum (vi du: LOW, MEDIUM, HIGH, URGENT). */
    private String priorityCode; 

    /** Ma mau HEX dai dien de dong bo mau sac tren bieu do (vi du: "#e74c3c"). */
    private String color;

    // ======================================================
    // 2. CHI SO THONG KE (METRICS)
    // ======================================================

    /** Tong so luong Task thuoc do uu tien nay. */
    private Long taskCount; 

    /** * Ty le phan tram so voi tong so Task.
     * Scale: 0.0 - 100.0. 
     */
    private Double percentage;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Giup Jackson Deserialize du lieu mot cach minh bach.
     */
    public PriorityDistributionResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public PriorityDistributionResponse(String priorityName, String priorityCode, String color, 
                                        Long taskCount, Double percentage) {
        this.priorityName = priorityName;
        this.priorityCode = priorityCode;
        this.color = color;
        this.taskCount = taskCount;
        this.percentage = percentage;
    }
}