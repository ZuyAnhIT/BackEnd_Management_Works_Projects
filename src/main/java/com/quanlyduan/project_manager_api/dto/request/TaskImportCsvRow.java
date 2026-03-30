package com.quanlyduan.project_manager_api.dto.request;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO dai dien cho mot dong du lieu trong file CSV khi thuc hien Import Task.
 * Su dung thu vien OpenCSV de anh xa truc tiep cac cot tu file vao doi tuong Java.
 */
@Getter
@Setter
public class TaskImportCsvRow {

    // ======================================================
    // KHAI BAO HANG SO - TEN COT CSV (RULE 6)
    // ======================================================
    public static final String COL_TITLE = "Title";
    public static final String COL_DESC = "Description";
    public static final String COL_ASSIGNEE = "Assignee Email";
    public static final String COL_PRIORITY = "Priority";
    public static final String COL_STATUS = "Status";
    public static final String COL_START_DATE = "Start Date";
    public static final String COL_DUE_DATE = "Due Date";
    public static final String COL_STORY_POINTS = "Story Points";
    public static final String COL_ESTIMATED_HOURS = "Estimated Hours";

    // ======================================================
    // 1. THONG TIN CO BAN (BASIC INFO)
    // ======================================================

    /**
     * Tieu de cong viec (Bat buoc trong file CSV).
     */
    @CsvBindByName(column = COL_TITLE, required = true)
    private String title;

    /**
     * Mo ta chi tiet noi dung cong viec.
     */
    @CsvBindByName(column = COL_DESC)
    private String description;

    // ======================================================
    // 2. THONG TIN THUC THI (EXECUTION)
    // ======================================================

    /**
     * Email cua nguoi duoc giao viec. 
     * Logic: Service se dung email nay de tra cuu User ID tuong ung.
     */
    @CsvBindByName(column = COL_ASSIGNEE)
    private String assigneeEmail; 

    /**
     * Muc do uu tien (vi du: Low, Medium, High).
     */
    @CsvBindByName(column = COL_PRIORITY) 
    private String priority;

    /**
     * Ten trang thai hien tai cua Task (vi du: To Do, In Progress).
     */
    @CsvBindByName(column = COL_STATUS) 
    private String statusName;
    
    // ======================================================
    // 3. THONG TIN THOI GIAN (TIMELINE)
    // ======================================================

    /**
     * Ngay bat dau (De dang String de linh hoat parse dinh dang ngay thang tai Service).
     */
    @CsvBindByName(column = COL_START_DATE) 
    private String startDate;

    /**
     * Han chot hoan thanh (Due Date).
     */
    @CsvBindByName(column = COL_DUE_DATE) 
    private String dueDate;

    // ======================================================
    // 4. CHI SO DO LUONG (METRICS)
    // ======================================================

    /**
     * Diem uoc luong do phuc tap (Story Points).
     */
    @CsvBindByName(column = COL_STORY_POINTS)
    private Integer storyPoints;
    
    /**
     * Thoi gian uoc tinh thuc hien (don vi: Gio).
     */
    @CsvBindByName(column = COL_ESTIMATED_HOURS)
    private Double estimatedHours;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh bat buoc phai co de OpenCSV co the khoi tao doi tuong.
     */
    public TaskImportCsvRow() {
    }
}