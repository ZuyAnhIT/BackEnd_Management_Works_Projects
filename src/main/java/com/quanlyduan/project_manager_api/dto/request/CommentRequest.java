package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhan du lieu khi nguoi dung gui mot binh luan moi cho cong viec (Task).
 * Dam bao noi dung thao luan luon duoc cung cap truoc khi luu vao he thong.
 */
@Getter
@Setter
public class CommentRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String CONTENT_BLANK_MSG = "Comment content must not be blank";

    // ======================================================
    // THONG TIN YEU CAU (REQUEST DATA)
    // ======================================================

    /**
     * Noi dung cua binh luan.
     * Bat buoc phai co, khong duoc de trong hoac chi chua cac ky tu khoang trang.
     */
    @NotBlank(message = CONTENT_BLANK_MSG)
    private String content;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh giup Jackson co the khoi tao doi tuong tu chuoi JSON.
     */
    public CommentRequest() {
    }
}