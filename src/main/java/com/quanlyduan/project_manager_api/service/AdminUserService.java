package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.AdminUserResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;

public interface AdminUserService {

    /**
     * Lay danh sach toan bo nguoi dung trong he thong co ho tro tim kiem va phan trang.
     *
     * @param keyword Tu khoa tim kiem (Email, Ten, SĐT)
     * @param status Trang thai tai khoan (ACTIVE, BANNED, UNVERIFIED)
     * @param page Trang hien tai
     * @param size So ban ghi / trang
     * @param sortBy Truong can sap xep
     * @param sortDir Huong sap xep
     * @return Danh sach nguoi dung da phan trang
     */
    PageResponseDTO<AdminUserResponse> searchUsers(
            String keyword, String status, int page, int size, String sortBy, String sortDir);

    
    /**
     * Thay doi trang thai cua nguoi dung (Khoa / Mo khoa).
     */
    void changeUserStatus(Integer userId, String newStatus);

    /**
     * Cap hoac Thu hoi quyen System Admin cua mot nguoi dung.
     */
    void toggleSystemAdminRole(Integer userId, boolean isAssign);

    
}