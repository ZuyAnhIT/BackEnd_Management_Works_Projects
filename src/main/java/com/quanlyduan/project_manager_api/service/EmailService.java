package com.quanlyduan.project_manager_api.service;

/**
 * Service quan ly toan bo nghiep vu gui thong bao qua Email.
 * Ho tro gui cac loai tin nhan xac thuc, thong bao he thong va loi moi gia nhap to chuc.
 */
public interface EmailService {

    // ======================================================
    // 1. GUI EMAIL CO BAN (BASIC EMAIL OPERATIONS)
    // ======================================================

    /**
     * Thuc hien gui mot Email den nguoi nhan cu the.
     * Phuong thuc nay ho tro ca van ban thuan tuy (Plain Text) va dinh dang HTML.
     * * @param to Dia chi Email cua nguoi nhan
     * @param subject Tieu de cua thu dien tu
     * @param body Noi dung chi tiet cua Email
     */
    void sendEmail(String to, String subject, String body);
}