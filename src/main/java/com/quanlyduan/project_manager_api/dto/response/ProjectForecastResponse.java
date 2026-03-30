package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi du bao tien do du an (Project Forecasting).
 * Dua tren du lieu Velocity thuc te de tinh toan cac kich ban hoan thanh va rui ro tre han.
 */
@Getter
@Setter
@Builder
public class ProjectForecastResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String RISK_LOW = "LOW";
    public static final String RISK_MEDIUM = "MEDIUM";
    public static final String RISK_HIGH = "HIGH";
    public static final String RISK_CRITICAL = "CRITICAL";

    // ======================================================
    // 1. THONG TIN CO BAN (FOUNDATION METRICS)
    // ======================================================
    
    /** Tong so Story Points con ton dong trong Backlog. */
    private Integer totalBacklogPoints; 

    /** Toc do hoan thanh trung binh cua Team (Points/Sprint). */
    private Double averageVelocity; 

    /** Han chot ket thuc du an theo ke hoach ban dau. */
    private LocalDate projectDueDate; 
    
    // ======================================================
    // 2. ĐANH GIA RUI RO TONG QUAN (RISK ASSESSMENT)
    // ======================================================
    
    /** * Muc do rui ro tre han (Risk Level).
     * Gia tri: LOW, MEDIUM, HIGH, CRITICAL.
     */
    private String riskLevel; 

    /** Thong bao tom tat ve rui ro de AI hoac User doc nhanh (Insights). */
    private String riskMessage; 

    // ======================================================
    // 3. CAC KICH BAN DU BAO (FORECAST SCENARIOS)
    // ======================================================
    
    private ForecastScenario optimistic;  // Kich ban tot nhat
    private ForecastScenario likely;      // Kich ban kha thi nhat
    private ForecastScenario pessimistic; // Kich ban xau nhat

    // ======================================================
    // INNER CLASS: CHI TIET KICH BAN (RULE 8)
    // ======================================================

    @Getter
    @Setter
    @Builder
    public static class ForecastScenario {
        
        /** Ten kich ban (vi du: "Optimistic", "Likely"). */
        private String name; 

        /** Chi so Velocity duoc su dung de tinh toan cho kich ban nay. */
        private Double velocityUsed; 

        /** So luong Sprint can thiet de hoan thanh toan bo Backlog. */
        private Double sprintsNeeded; 

        /** Ngay hoan thanh du kien (Estimated Completion Date). */
        private LocalDate completionDate; 

        /** Co bao hieu kich ban nay co lam du an bi tre deadline khong. */
        private boolean isLate; 

        /** So ngay tre so voi projectDueDate (bang 0 neu dung han). */
        private Integer daysLate;

        // Constructor viet tay cho Inner Class
        public ForecastScenario() {}

        public ForecastScenario(String name, Double velocityUsed, Double sprintsNeeded, 
                                LocalDate completionDate, boolean isLate, Integer daysLate) {
            this.name = name;
            this.velocityUsed = velocityUsed;
            this.sprintsNeeded = sprintsNeeded;
            this.completionDate = completionDate;
            this.isLate = isLate;
            this.daysLate = daysLate;
        }
    }

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public ProjectForecastResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public ProjectForecastResponse(Integer totalBacklogPoints, Double averageVelocity, 
                                   LocalDate projectDueDate, String riskLevel, 
                                   String riskMessage, ForecastScenario optimistic, 
                                   ForecastScenario likely, ForecastScenario pessimistic) {
        this.totalBacklogPoints = totalBacklogPoints;
        this.averageVelocity = averageVelocity;
        this.projectDueDate = projectDueDate;
        this.riskLevel = riskLevel;
        this.riskMessage = riskMessage;
        this.optimistic = optimistic;
        this.likely = likely;
        this.pessimistic = pessimistic;
    }
}