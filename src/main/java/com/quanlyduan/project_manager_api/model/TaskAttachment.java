package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity luu tru thong tin ve Tep dinh kem (Attachment) cua Cong viec.
 * Quan ly duong dan luu tru, loai tep va dung luong de phuc vu thong ke tai nguyen.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "task_attachments")
public class TaskAttachment {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Task so huu tep dinh kem nay. Su dung LAZY fetch de toi uu. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** Nguoi dung thuc hien tai tep len he thong. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false, updatable = false)
    private User uploadedBy;

    // ======================================================
    // 3. THONG TIN TEP TIN (FILE DETAILS)
    // ======================================================
    
    /** Ten goc cua tep tin khi nguoi dung tai len. */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /** * Duong dan vat ly hoac Key tren Storage Service (S3/Cloudinary/MinIO). 
     * Day la thong tin quan trong de truy xuat file thuc te.
     */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    /** Dinh dang tep (MIME Type), vi du: "application/pdf", "image/jpeg". */
    @Column(name = "file_type")
    private String fileType;

    /** * Kich thuoc tep tin tinh bang Bytes. 
     * Dung de tinh toan tong dung luong luu tru cua toan Cong ty. 
     */
    @Column(name = "file_size")
    private Long fileSize;

    // ======================================================
    // 4. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    /** Thoi diem tep duoc tai len thanh cong (tu dong sinh boi Hibernate). */
    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false, nullable = false)
    private LocalDateTime uploadedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay cho Hibernate.
     */
    public TaskAttachment() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public TaskAttachment(Integer id, Task task, User uploadedBy, String fileName, 
                          String filePath, String fileType, Long fileSize, 
                          LocalDateTime uploadedAt) {
        this.id = id;
        this.task = task;
        this.uploadedBy = uploadedBy;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }
}