package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi goi y nguoi thuc hien (Assignee) dua tren phan tich cua AI.
 * Cung cap cac chi so ve khoi luong cong viec va kinh nghiem de ho tro ra quyet dinh giao viec.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class AssigneeRecommendationResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final double MIN_MATCH_SCORE = 0.0;
    public static final double MAX_MATCH_SCORE = 100.0;

    // ======================================================
    // 1. THONG TIN NHAN SU (PERSONNEL INFO)
    // ======================================================
    
    private Integer userId;
    
    /** Ten day du cua ung vien duoc goi y. */
    private String fullName;
    
    /** Duong dan URL den anh dai dien. */
    private String avatarUrl;

    // ======================================================
    // 2. PHAN TICH NANG LUC & TAI NGUYEN (AI METRICS)
    // ======================================================

    /** * Diem phu hop tong the (Scale 0 - 100).
     * Được AI tinh toan dua tren ky nang, kinh nghiem va khoi luong viec hien tai.
     */
    private Double matchScore; 

    /** Tong so diem cong viec (Points) ma nguoi nay dang dam nhan. */
    private Integer currentWorkloadPoints; 

    /** So luong cac Task co tinh chat tuong tu ma nguoi nay da hoan thanh truoc do. */
    private Integer similarTasksCompleted; 

    /** * Trang thai tai nguyen hien tai.
     * Gia tri: "LOW", "OPTIMAL", "HIGH", "OVERLOADED".
     */
    private String workloadStatus;

    // ======================================================
    // 3. KET LUAN & GIAI THICH (INSIGHTS)
    // ======================================================

    /** * Ly do tai sao AI goi y nguoi nay.
     * Van ban ngon ngu tu nhien (Human readable text) de hien thi len Tooltip hoac Modal.
     */
    private String reason; 

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON.
     */
    public AssigneeRecommendationResponse() {
    }
}