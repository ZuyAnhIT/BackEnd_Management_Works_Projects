package com.quanlyduan.project_manager_api.dto.request;

import java.time.LocalDate;

import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu de cap nhat thong tin Du an.
 * Ho tro co che Partial Update: Chi nhung truong co gia tri (khong null) moi duoc cap nhat vao he thong.
 */
@Getter
@Setter
public class UpdateProjectRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int NAME_MIN_SIZE = 1;
    public static final int NAME_MAX_SIZE = 255;
    
    public static final String NAME_SIZE_MSG = "Project name must be between " 
            + NAME_MIN_SIZE + " and " + NAME_MAX_SIZE + " characters";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY & BASIC INFO)
    // ======================================================

    /**
     * Ten moi cua du an.
     */
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE, message = NAME_SIZE_MSG)
    private String name;
    
    /**
     * Ma dinh danh du an moi (vi du: WEB, APP).
     * Service se kiem tra tinh duy nhat trong Workspace neu gia tri nay thay doi.
     */
    private String projectCode; 
    
    /**
     * Mo ta chi tiet ve noi dung hoac pham vi du an.
     */
    private String description;

    /**
     * Muc tieu cot loi moi cua du an.
     */
    private String goal;

    /**
     * Duong dan URL den anh bia (Cover Image) moi.
     */
    private String coverImageUrl;
    
    // ======================================================
    // 2. CAU HINH & QUAN LY (CONFIG & MANAGEMENT)
    // ======================================================

    /**
     * Muc do uu tien moi (LOW, MEDIUM, HIGH, URGENT).
     */
    private ProjectPriority priority; 

    /**
     * ID cua Nguoi quan ly du an (Project Manager) moi.
     */
    private Integer managerId; 

    /**
     * ID cua Loai du an (ProjectType) moi.
     */
    private Integer projectTypeId; 
    
    /**
     * Cau hinh bang cong viec (Board) duoi dang chuoi JSON.
     */
    private String boardConfig; 

    // ======================================================
    // 3. CAC MOC THOI GIAN (TIMELINE)
    // ======================================================

    /**
     * Ngay bat dau trien khai moi.
     */
    private LocalDate startDate;

    /**
     * Han chot hoan thanh (Due Date) moi.
     */
    private LocalDate dueDate;

    /**
     * Thoi diem thuc te du an hoan thanh (Completed At).
     */
    private LocalDate completedAt;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public UpdateProjectRequest() {
    }
}