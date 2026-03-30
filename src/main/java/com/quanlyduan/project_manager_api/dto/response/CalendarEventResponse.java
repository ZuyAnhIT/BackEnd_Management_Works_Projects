package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi du lieu su kien cho giao dien Lich (Calendar).
 * Cau truc nay tuong thich voi cac thu vien Calendar pho bien, ho tro hien thi Task va Sprint.
 */
@Getter
@Setter
@Builder
public class CalendarEventResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String TYPE_TASK = "TASK";
    public static final String TYPE_SPRINT = "SPRINT";

    // ======================================================
    // 1. CAC TRUONG CHUAN CALENDAR (STANDARD FIELDS)
    // ======================================================
    
    /** ID duy nhat tren giao dien (vi du: "task-101", "sprint-5"). */
    private String id; 
    
    /** ID goc trong Database (dung de truy van chi tiet). */
    private Integer originalId;
    
    /** Tieu de hien thi tren thanh su kien. */
    private String title;
    
    /** Thoi gian bat dau va ket thuc. */
    private LocalDateTime start;
    private LocalDateTime end;
    
    /** Co bao hieu su kien keo dai ca ngay (thuong dung cho Sprint). */
    private boolean allDay;

    // ======================================================
    // 2. GIAO DIEN & PHAN LOAI (UI & TYPE)
    // ======================================================
    
    /** Loai su kien: TASK hoac SPRINT. */
    private String type; 
    
    /** Cau hinh mau sac hien thi dong bo voi trang thai hoac loai su kien. */
    private String backgroundColor;
    private String borderColor;
    private String textColor;

    // ======================================================
    // 3. THONG TIN BO SUNG (METADATA CHO TOOLTIP)
    // ======================================================
    
    /** Ten trang thai hien tai (vi du: "In Progress"). */
    private String statusName;
    
    /** Do uu tien (chi dung cho Task, vi du: "URGENT"). */
    private String priority;
    
    /** Thong tin nguoi thuc hien (chi dung cho Task). */
    private String assigneeName;
    private String assigneeAvatar;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize.
     */
    public CalendarEventResponse() {
    }

    /**
     * Constructor day du phuc vu cho @Builder.
     */
    public CalendarEventResponse(String id, Integer originalId, String title, 
                                 LocalDateTime start, LocalDateTime end, boolean allDay, 
                                 String type, String backgroundColor, String borderColor, 
                                 String textColor, String statusName, String priority, 
                                 String assigneeName, String assigneeAvatar) {
        this.id = id;
        this.originalId = originalId;
        this.title = title;
        this.start = start;
        this.end = end;
        this.allDay = allDay;
        this.type = type;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.textColor = textColor;
        this.statusName = statusName;
        this.priority = priority;
        this.assigneeName = assigneeName;
        this.assigneeAvatar = assigneeAvatar;
    }
}