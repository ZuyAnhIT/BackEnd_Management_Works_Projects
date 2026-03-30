package com.quanlyduan.project_manager_api.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi du lieu Tai trong cong viec (Workload Analytics).
 * Cung cap thong tin de ve bieu do cot chong (Stacked Bar Chart), 
 * giup quan ly nguon luc va can bang khoi luong viec giua cac thanh vien.
 */
@Getter
@Setter
@Builder
public class WorkloadResponse {

    // ======================================================
    // 1. THONG TIN NHAN SU (MEMBER INFO)
    // ======================================================

    /** ID dinh danh cua nguoi dung (User). */
    private Integer userId; 
    
    /** Ten hien thi tren truc X cua bieu do. */
    private String userName; 
    
    /** Duong dan anh dai dien de hien thi kem theo ten tren bieu do. */
    private String avatarUrl;

    // ======================================================
    // 2. CHI SO TAI TRONG (LOAD METRICS)
    // ======================================================

    /** * Tong tai trong cua thanh vien (Chieu cao tong cua cot).
     * Don vi co the la Story Points hoac Estimated Hours.
     */
    private double totalLoad; 

    /** * Danh sach cac doan tai trong chong len nhau (Stacks).
     * Giup phan tich chi tiet tong tai trong den tu dau (Status, Priority...).
     */
    private List<WorkloadBreakdown> breakdowns;

    // ======================================================
    // INNER CLASS: CHI TIET DOAN TAI TRONG (RULE 8)
    // ======================================================

    @Getter
    @Setter
    @Builder
    public static class WorkloadBreakdown {
        
        /** Ten cua doan chong (vi du: "In Progress", "High Priority"). */
        private String stackName; 
        
        /** Ma mau HEX dai dien cho doan nay tren bieu do (vi du: "#3498db"). */
        private String color;
        
        /** Gia tri tai trong cua doan (tuong ung voi Story Points hoac Hours). */
        private double value;
        
        /** * So luong Task thuc te nam trong doan nay.
         * Dung de hien thi Tooltip (vi du: "15 Points - 4 Tasks").
         */
        private int taskCount; 

        // Constructor viet tay cho Inner Class
        public WorkloadBreakdown() {}

        public WorkloadBreakdown(String stackName, String color, double value, int taskCount) {
            this.stackName = stackName;
            this.color = color;
            this.value = value;
            this.taskCount = taskCount;
        }
    }

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public WorkloadResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public WorkloadResponse(Integer userId, String userName, String avatarUrl, 
                            double totalLoad, List<WorkloadBreakdown> breakdowns) {
        this.userId = userId;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
        this.totalLoad = totalLoad;
        this.breakdowns = breakdowns;
    }
}