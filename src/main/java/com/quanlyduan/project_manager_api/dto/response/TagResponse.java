package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet cua mot The (Tag) trong du an.
 * Tag duoc su dung de phan loai Task (vi du: "Backend", "Urgent") va ho tro loc du lieu.
 */
@Getter
@Setter
@Builder
public class TagResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================

    /** ID dinh danh duy nhat cua Tag. */
    private Integer id;

    /** ID cua Du an (Project) so huu Tag nay. */
    private Integer projectId;

    // ======================================================
    // 2. THONG TIN CO BAN (BASIC INFO)
    // ======================================================

    /** Ten hien thi cua Tag (vi du: "UI/UX", "High Priority"). */
    private String name;

    /** Ma mau HEX dung de to mau cho nhan Tag tren giao dien (vi du: "#e74c3c"). */
    private String color;

    /** Mo ta chi tiet ve y nghia hoac cach su dung Tag nay. */
    private String description;

    // ======================================================
    // 3. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================

    /** Thong tin nguoi khoi tao Tag. */
    private Integer createdById;
    private String createdByName;
    private String createdByAvatar;

    /** Thoi diem tao va lan cap nhat cuoi cung cua ban ghi. */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson Deserialize JSON mot cach minh bach.
     */
    public TagResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public TagResponse(Integer id, Integer projectId, String name, String color, 
                       String description, Integer createdById, String createdByName, 
                       String createdByAvatar, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.color = color;
        this.description = description;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.createdByAvatar = createdByAvatar;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}