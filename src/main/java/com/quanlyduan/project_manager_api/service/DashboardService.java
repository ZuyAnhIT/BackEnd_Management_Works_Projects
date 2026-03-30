package com.quanlyduan.project_manager_api.service;

import java.util.List;

import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.MyProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.MyTaskResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;

/**
 * Service quan ly cac nghiep vu hien thi tren trang Tong quan (Dashboard) ca nhan.
 * Tap hop cac tai nguyen (Cong ty, Phong ban, Du an, Cong viec) ma nguoi dung hien tai dang truc tiep tham gia.
 */
public interface DashboardService {

    // ======================================================
    // 1. DU LIEU TO CHUC (ORGANIZATION DATA)
    // ======================================================

    /**
     * Truy xuat danh sach cac Cong ty ma nguoi dung hien tai la thanh vien.
     * * @return Danh sach tom tat thong tin cac Cong ty (Tenant)
     */
    List<MyCompanyResponse> getMyCompanies();

    /**
     * Truy xuat danh sach cac Workspace (Phong ban) ma nguoi dung co quyen truy cap.
     * * @return Danh sach tom tat cac Khong gian lam viec
     */
    List<MyWorkspaceResponse> getMyWorkspaces();

    /**
     * Truy xuat danh sach cac Du an (Project) ma nguoi dung dang dong gop.
     * * @return Danh sach tom tat cac Du an dang trien khai
     */
    List<MyProjectResponse> getMyProjects();

    // ======================================================
    // 2. DU LIEU CONG VIEC (PERSONAL TASK DATA)
    // ======================================================

    /**
     * Truy xuat danh sach cac cong viec dang thuc hien (Active Tasks) duoc giao cho nguoi dung.
     * Thuong dung de hien thi trong Widget "Cong viec cua toi" tren Dashboard.
     * * @return Danh sach cac Task chua hoan thanh (In-progress/To-do)
     */
    List<MyTaskResponse> getMyTasks();
}