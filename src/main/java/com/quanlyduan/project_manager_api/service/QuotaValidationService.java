package com.quanlyduan.project_manager_api.service;

/**
 * Service chuyên chịu trách nhiệm kiểm tra hạn mức (Quota) của Công ty
 * trước khi thực hiện các hành động tạo mới tài nguyên.
 * Nếu vượt hạn mức, các hàm này sẽ ném ra OverageException (HTTP 402).
 */
public interface QuotaValidationService {

    /**
     * Kiểm tra xem Công ty có được phép tạo thêm Dự án mới không.
     * Dựa trên số lượng Project hiện tại so với max_projects của Gói cước.
     *
     * @param companyId ID của Công ty cần kiểm tra
     */
    void validateProjectCreationQuota(Integer companyId);

    /**
     * Kiểm tra xem Công ty có được phép tạo thêm Không gian làm việc (Workspace) mới không.
     * Dựa trên số lượng Workspace hiện tại so với max_workspaces của Gói cước.
     *
     * @param companyId ID của Công ty cần kiểm tra
     */
    void validateWorkspaceCreationQuota(Integer companyId);
    
    /**
     * Kiểm tra giới hạn số lượng Thành viên (User) tối đa của công ty.
     * Dùng trước khi gửi email mời (Invite) thành viên mới.
     * @param companyId ID của công ty
     * @throws com.quanlyduan.project_manager_api.exception.OverageException nếu vượt quá giới hạn
     */
    void validateUserInvitationQuota(Integer companyId);

    /**
     * Kiểm tra giới hạn Dung lượng lưu trữ (Storage) của công ty.
     * Dùng trước khi lưu file vật lý vào server.
     * @param companyId ID của công ty
     * @param fileSizeToUpload Kích thước file (tính bằng Bytes) chuẩn bị upload
     * @throws com.quanlyduan.project_manager_api.exception.OverageException nếu dung lượng file vượt quá khoảng trống còn lại
     */
    void validateStorageQuota(Integer companyId, long fileSizeToUpload);
}