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
    
    // /**
    //  * Kiểm tra xem Công ty có được phép mời thêm Nhân viên mới không.
    //  * Dựa trên số lượng Member hiện tại so với max_users của Gói cước.
    //  *
    //  * @param companyId ID của Công ty cần kiểm tra
    //  */
    // void validateUserInvitationQuota(Integer companyId);

    // /**
    //  * Kiểm tra xem Công ty có được phép tải thêm File với dung lượng này không.
    //  * Dựa trên current_storage_bytes + fileSizeToUpload so với max_storage_gb.
    //  *
    //  * @param companyId ID của Công ty cần kiểm tra
    //  * @param fileSizeToUpload Kích thước file (tính bằng bytes) chuẩn bị tải lên
    //  */
    // void validateStorageQuota(Integer companyId, long fileSizeToUpload);
}