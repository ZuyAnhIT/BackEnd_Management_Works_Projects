package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet ve Tep tin dinh kem (Attachment) cua Task.
 * Cung cap day du thong tin ve file, nguoi tai len va ngu canh phan cap de quan ly tai nguyen.
 */
@Getter
@Setter
@Builder
public class TaskAttachmentResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH & PHAN CAP (IDENTITY & HIERARCHY)
    // ======================================================

    /** ID dinh danh duy nhat cua ban ghi Attachment. */
    private Integer id;

    /** ID cua Task (Cong viec) chua tep tin nay. */
    private Integer taskId;

    /** ID cua Du an, Khong gian lam viec va Cong ty de quan ly han muc luu tru. */
    private Integer projectId;
    private Integer workspaceId;
    private Integer companyId;

    // ======================================================
    // 2. THONG TIN TEP TIN (FILE METADATA)
    // ======================================================

    /** Ten tep tin goc (vi du: "Requirement_v1.pdf"). */
    private String fileName;

    /** Loai tep tin (MIME type hoac extension, vi du: "application/pdf"). */
    private String fileType;

    /** Kich thuoc tep tin tinh bang Bytes. */
    private Long fileSize;

    /** Duong dan URL truc tiep de tai xuong hoac xem file (CDN/Storage link). */
    private String fileUrl;

    // ======================================================
    // 3. THONG TIN TAI LEN (UPLOAD INFO)
    // ======================================================

    /** ID va Ten hien thi cua nguoi thuc hien tai tep tin nay len he thong. */
    private Integer uploadedById;
    private String uploadedByName;

    /** Thoi diem tep tin duoc tai len thanh cong. */
    private LocalDateTime uploadedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson Deserialize JSON mot cach minh bach.
     */
    public TaskAttachmentResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public TaskAttachmentResponse(Integer id, Integer taskId, Integer projectId, 
                                  Integer workspaceId, Integer companyId, String fileName, 
                                  String fileType, Long fileSize, String fileUrl, 
                                  Integer uploadedById, String uploadedByName, 
                                  LocalDateTime uploadedAt) {
        this.id = id;
        this.taskId = taskId;
        this.projectId = projectId;
        this.workspaceId = workspaceId;
        this.companyId = companyId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileUrl = fileUrl;
        this.uploadedById = uploadedById;
        this.uploadedByName = uploadedByName;
        this.uploadedAt = uploadedAt;
    }
}