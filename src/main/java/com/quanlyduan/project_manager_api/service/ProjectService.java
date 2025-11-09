package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;

/**
 * Service cho Project – US7 chỉ yêu cầu tạo mới Project.
 */
public interface ProjectService {

    /**
     * US7: Tạo Project mới trong Workspace.
     * Nghiệp vụ chính:
     * - Kiểm tra tồn tại Workspace theo workspaceId (404 nếu không có).
     * - Kiểm tra unique projectCode trong cùng workspace (400 nếu trùng).
     * - Gán các quan hệ bằng reference: workspace, createdBy, (manager, projectType nếu có).
     * - Dùng mặc định của Entity: status=NEW, priority=MEDIUM, progress=0, createdAt/updatedAt tự sinh.
     * - Lưu và trả về ProjectResponse (không trả Entity trực tiếp để tránh vòng lặp/lazy serialize).
     */
    /**
     * Thêm companyId vào path theo yêu cầu để:
     * - Ràng buộc workspace phải thuộc company tương ứng (tránh truy cập chéo công ty).
     * - Cải thiện hiển thị tham số path trên Swagger (companyId + workspaceId).
     */
    ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request, Integer creatorId);

    /**
     * US8: Lấy danh sách Project trong một Workspace.
     * Nghiệp vụ chính:
     * - Xác thực workspace thuộc đúng companyId (nếu sai → 400) để tránh truy cập chéo công ty.
     * - Yêu cầu quyền workspace:view ở tầng controller (@PreAuthorize).
     * - Trả về danh sách ProjectResponse; có thể loại bỏ các project đã bị CANCELLED nếu muốn.
     */
    java.util.List<ProjectResponse> listProjectsByWorkspace(Integer companyId, Integer workspaceId);
}
