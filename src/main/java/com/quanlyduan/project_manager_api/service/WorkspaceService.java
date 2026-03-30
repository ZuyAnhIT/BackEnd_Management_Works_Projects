package com.quanlyduan.project_manager_api.service;

import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateWorkspaceStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.model.WorkspaceMember;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;

/**
 * Service quan ly toan bo nghiep vu lien quan den Khong gian lam viec (Workspace).
 * Xu ly vong doi cua Workspace va ma tran nhan su thuoc cac phong ban trong Cong ty.
 */
public interface WorkspaceService {

    // ======================================================
    // 1. QUAN LY KHONG GIAN LAM VIEC (CRUD & LIFECYCLE)
    // ======================================================

    /**
     * Khoi tao mot Khong gian lam viec moi kem theo anh bia hien thi.
     * * @param companyId ID Cong ty chu quan
     * @param request Thong tin cau hinh Workspace
     * @param coverImageFile Tep tin hinh anh minh hoa
     * @return DTO chua thong tin Workspace vua tao
     */
    WorkspaceResponse createWorkspace(Integer companyId, CreateWorkspaceRequest request, MultipartFile coverImageFile); 

    /**
     * Lay danh sach cac Khong gian lam viec thuoc pham vi mot Cong ty.
     * * @return Trang du lieu danh sach Workspace
     */
    PageResponseDTO<WorkspaceResponse> getWorkspacesByCompany(Integer companyId, int page, int size, String sortBy, String sortDir);
    
    /**
     * Tim kiem nang cao Workspace trong Cong ty dua tren nhieu tieu chi.
     * * @param searchName Loc theo ten
     * @param searchCode Loc theo ma dinh danh (vi du: "DEV", "HR")
     * @param searchStatus Loc theo trang thai hoat dong
     */
    PageResponseDTO<WorkspaceResponse> searchWorkspaces(
            Integer companyId, 
            String searchName, String searchCode, String searchDescription, WorkspaceStatus searchStatus,
            int page, int size, String sortBy, String sortDir
    );

    /**
     * Truy xuat ho so chi tiet cua mot Khong gian lam viec cu the.
     */
    WorkspaceResponse getWorkspaceDetails(Integer workspaceId);

    /**
     * Cap nhat thong tin hanh chinh va hinh anh cua Workspace (Partial Update).
     */
    WorkspaceResponse updateWorkspace(Integer workspaceId, UpdateWorkspaceRequest request, MultipartFile coverImageFile);

    /**
     * Thay doi trang thai van hanh cua Workspace (vi du: Tu ACTIVE sang ARCHIVED).
     */
    WorkspaceResponse updateWorkspaceStatus(Integer companyId, Integer workspaceId, UpdateWorkspaceStatusRequest request);

    /**
     * Loai bo Workspace khoi he thong (Soft Delete).
     */
    void deleteWorkspace(Integer workspaceId);

    // ======================================================
    // 2. QUAN LY THANH VIEN (MEMBERSHIP & ROLES)
    // ======================================================

    /**
     * Moi hoac them truc tiep mot nhan su vao Khong gian lam viec.
     * * @param request Email nhan su va vai tro du kien tai Workspace
     * @return Entity thanh vien sau khi thiet lap quan he
     */
    WorkspaceMember inviteMemberToWorkspace(Integer companyId, Integer workspaceId, InviteWorkspaceMemberRequest request);

    /**
     * Lay danh sach nhan su dang tham gia vao mot Workspace cu the.
     */
    PageResponseDTO<WorkspaceMemberResponse> getWorkspaceMembers(Integer workspaceId, int page, int size, String sortBy, String sortDir);

    /**
     * Tim kiem thanh vien trong pham vi phong ban (Loc theo Ten, Email, Role, SDT).
     */
    PageResponseDTO<WorkspaceMemberResponse> searchWorkspaceMembers(
            Integer workspaceId, 
            String searchName, String searchEmail, String searchRoleName, String searchPhone,
            int page, int size, String sortBy, String sortDir
    );

    /**
     * Truy xuat ho so chi tiet cua mot thanh vien trong Workspace.
     */
    WorkspaceMemberResponse getWorkspaceMemberDetails(Integer workspaceId, Integer memberId);

    /**
     * Thay doi trang thai hoat dong cua thanh vien ben trong Workspace (Tam dung/Khoi phuc).
     */
    WorkspaceMemberResponse updateWorkspaceMemberStatus(Integer companyId, Integer workspaceId, Integer memberId, UpdateMemberStatusRequest request);

    /**
     * Thay doi vai tro phan quyen (Role) cua thanh vien trong pham vi Workspace.
     */
    WorkspaceMemberResponse updateWorkspaceMemberRole(Integer companyId, Integer workspaceId, Integer memberId, String newRoleCode);
    
    /**
     * Ngung tu cach thanh vien cua nguoi dung khoi Workspace (Soft Delete).
     */
    void removeMemberFromWorkspace(Integer companyId, Integer workspaceId, Integer memberId);
}