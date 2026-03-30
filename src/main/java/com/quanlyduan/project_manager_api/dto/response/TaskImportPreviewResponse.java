package com.quanlyduan.project_manager_api.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi ket qua xem truoc (Preview) khi nhap lieu Task tu file Excel hoac CSV.
 * Dung de hien thi tung dong du lieu tho kem theo trang thai hop le va danh sach loi.
 */
@Getter
@Setter
@Builder
public class TaskImportPreviewResponse {

    // ======================================================
    // 1. THONG TIN VI TRI (POSITION INFO)
    // ======================================================
    
    /** Chi so dong trong file Excel/CSV (dung de de dang doi chieu va sua loi). */
    private int rowIndex;

    // ======================================================
    // 2. DU LIEU ĐAU VAO (RAW INPUT DATA)
    // ======================================================
    // Giu nguyen kieu String de tranh loi Parse Exception truoc khi hien thi len UI.
    // Nguoi dung co the nhin thay chinh xac nhung gi ho da nhap sai đe sua.

    /** Tieu de va Mo ta cong viec. */
    private String title;
    private String description;
    
    /** Email cua nguoi duoc giao viec (se dung de tra cuu User ID sau). */
    private String assigneeEmail;
    
    /** Do uu tien va Trang thai (dang chuoi thuan). */
    private String priority;
    private String statusName;
    
    /** Thoi gian du kien (giu dang String de kiem tra format yyyy-MM-dd hoac dd/MM/yyyy). */
    private String startDate;
    private String dueDate;
    
    /** Cac chi so uoc luong. */
    private Integer storyPoints;
    private Double estimatedHours;

    // ======================================================
    // 3. TRANG THAI KIEM ĐINH (VALIDATION STATUS)
    // ======================================================

    /** Co danh dau dong du lieu nay co vuot qua toan bo cac rule kiem đinh hay khong. */
    private boolean isValid; // (Luu y: Lombok tu dong sinh getter la isValid())

    /** Danh sach chi tiet cac ma loi hoac thong bao (vi du: "Email khong ton tai", "Sai dinh dang ngay"). */
    private List<String> errors;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao thu vien phan tich JSON hoat dong on đinh.
     */
    public TaskImportPreviewResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public TaskImportPreviewResponse(int rowIndex, String title, String description, 
                                     String assigneeEmail, String priority, String statusName, 
                                     String startDate, String dueDate, Integer storyPoints, 
                                     Double estimatedHours, boolean isValid, List<String> errors) {
        this.rowIndex = rowIndex;
        this.title = title;
        this.description = description;
        this.assigneeEmail = assigneeEmail;
        this.priority = priority;
        this.statusName = statusName;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.storyPoints = storyPoints;
        this.estimatedHours = estimatedHours;
        this.isValid = isValid;
        this.errors = errors;
    }
}