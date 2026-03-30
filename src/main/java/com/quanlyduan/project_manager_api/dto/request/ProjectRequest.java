package com.quanlyduan.project_manager_api.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.JsonNode;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu tu Client de tao moi hoac cap nhat mot Du an (Project).
 * Chua cac thong tin co ban, cau hinh he thong va ke hoach trien khai ban dau.
 */
@Getter
@Setter
public class ProjectRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String NAME_BLANK_MSG = "Project name must not be blank";
    public static final String CODE_BLANK_MSG = "Project code must not be blank";

    // ======================================================
    // 1. THONG TIN CO BAN (BASIC INFO)
    // ======================================================

    /**
     * Ten day du cua du an.
     * Bat buoc phai co de dinh danh du an tren he thong.
     */
    @NotBlank(message = NAME_BLANK_MSG)
    private String name;

    /**
     * Ma dinh danh du an (vi du: WEB, APP, CRM).
     * Dung de tao tien to cho ma cong viec (vi du: WEB-1, WEB-2).
     */
    @NotBlank(message = CODE_BLANK_MSG)
    private String projectCode;

    /**
     * Mo ta chi tiet ve noi dung hoac pham vi cua du an.
     */
    private String description;

    /**
     * Muc tieu cot loi can dat duoc khi ket thuc du an.
     */
    private String goal;

    /**
     * Duong dan URL den anh bia (Cover Image) cua du an.
     */
    private String coverImageUrl;

    // ======================================================
    // 2. CAU HINH & THAM CHIEU (CONFIG & REFERENCES)
    // ======================================================

    /**
     * Cau hinh bang cong viec (Board Configuration).
     * Su dung JsonNode de nhan truc tiep cau truc JSON dong tu Client.
     */
    private JsonNode boardConfig; 

    /**
     * ID cua Loai du an (vi du: Kanban, Scrum).
     */
    private Integer projectTypeId; 

    /**
     * ID cua Nguoi quan ly du an (Project Manager).
     */
    private Integer managerId;     

    // ======================================================
    // 3. THONG TIN KE HOACH (PLANNING)
    // ======================================================

    /**
     * Muc do uu tien cua du an (LOW, MEDIUM, HIGH, URGENT).
     * Neu de null, tang Service se tu dong gan gia tri mac dinh la MEDIUM.
     */
    private ProjectPriority priority;
    
    /**
     * Ngay bat dau du kien trien khai dự án.
     */
    private LocalDate startDate;
    
    /**
     * Ngay ket thuc du kien (Deadline) cua du an.
     */
    private LocalDate dueDate;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public ProjectRequest() {
    }
}