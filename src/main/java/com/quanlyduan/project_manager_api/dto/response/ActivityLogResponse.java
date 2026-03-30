package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO phan hoi lich su hoat dong (Activity Log) cua he thong.
 * Cung cap cai nhin chi tiet ve: Ai da lam gi, Tren doi tuong nao va Vao luc nao.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {

    // ======================================================
    // 1. NGUOI THUC HIEN (ACTOR INFO)
    // ======================================================
    
    private Integer id;
    
    /** Ten hien thi cua nguoi thuc hien hanh dong. */
    private String userName;
    
    /** Duong dan URL den anh dai dien cua nguoi dung. */
    private String userAvatar;

    // ======================================================
    // 2. CHI TIET HANH DONG (OPERATION INFO)
    // ======================================================

    /** Ten hanh dong (vi du: CREATE, UPDATE, DELETE, MOVE). */
    private String action;

    /** Mo ta chi tiet ve thay doi (vi du: "Thay doi trang thai tu To Do sang Doing"). */
    private String description;

    // ======================================================
    // 3. DOI TUONG BI TAC DONG (TARGET ENTITY)
    // ======================================================

    /** Loai doi tuong (vi du: TASK, PROJECT, SPRINT, COMMENT). */
    private String entityType;

    /** Ten cua doi tuong (vi du: "Fix bug login"). */
    private String entityName;

    /** Ma code cua doi tuong (neu co, vi du: TASK-101). */
    private String entityCode;

    /** ID cua doi tuong trong Database de thuc hien dieu huong (Deep link). */
    private Integer entityId;

    // ======================================================
    // 4. DAU MOC THOI GIAN (CHRONOLOGY)
    // ======================================================

    /** Thoi diem chinh xac xay ra hanh dong. */
    private LocalDateTime timestamp;

    /** * Khoang thoi gian cach hien tai (vi du: "2 minutes ago", "Vua xong").
     * Truong nay thuong duoc tinh toan san tai Backend de Frontend hien thi ngay.
     */
    private String timeAgo; 
}