package com.quanlyduan.project_manager_api.service;

import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProfileRequest;
import com.quanlyduan.project_manager_api.dto.response.UserProfileResponse;

/**
 * Service quan ly toan bo nghiep vu lien quan den Ho so nguoi dung (User Profile).
 * Chiu trach nhiem xu ly thong tin ca nhan, anh dai dien va bao mat mat khau.
 */
public interface UserService {

    // ======================================================
    // 1. QUAN LY TAI KHOAN & BAO MAT (ACCOUNT & SECURITY)
    // ======================================================

    /**
     * Thuc hien thay doi mat khau cho nguoi dung hien tai.
     * Logic nghiep vu bao gom: Xac thuc mat khau cu va ma hoa mat khau moi.
     * * @param request Du lieu chua mat khau cu, mat khau moi va xac nhan
     */
    void changePassword(ChangePasswordRequest request);

    // ======================================================
    // 2. QUAN LY HO SO CA NHAN (PROFILE MANAGEMENT)
    // ======================================================

    /**
     * Truy xuat toan bo thong tin ho so cua nguoi dung dang dang nhap.
     * * @return DTO chua chi tiet thong tin ca nhan va trang thai tai khoan
     */
    UserProfileResponse getCurrentUserProfile();

    /**
     * Cap nhat thong tin ca nhan va thay doi anh dai dien (Avatar).
     * Ho tro cap nhat tung phan (Partial Update) va luu tru file vat ly.
     * * @param request Cac truong thong tin can thay doi (Ten, So dien thoai, Ngay sinh...)
     * @param avatarFile Tep tin hinh anh moi tu Client
     * @return Ho so nguoi dung sau khi da duoc cap nhat thanh cong
     */
    UserProfileResponse updateUserProfile(UpdateProfileRequest request, MultipartFile avatarFile);
}