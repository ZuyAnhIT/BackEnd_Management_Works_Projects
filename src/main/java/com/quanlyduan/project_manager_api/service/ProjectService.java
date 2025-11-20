
/* =============================
 * File: ProjectService.java
 * Path: service/
 * ============================= */

package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;

import java.util.List;

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
     * Lấy danh sách dự án trong workspace (Phân trang & Sắp xếp).
     * @param companyId ID công ty
     * @param workspaceId ID không gian
     * @param page Trang số mấy
     * @param size Kích thước trang
     * @param sortBy Trường sắp xếp
     * @param sortDir Hướng sắp xếp
     * @return PageResponseDTO
     */
    PageResponseDTO<ProjectResponse> listProjectsByWorkspace(Integer companyId, Integer workspaceId, int page, int size, String sortBy, String sortDir);

    /**
     * US9: Xóa dự án (soft delete) – chuyển trạng thái Project sang CANCELLED.
     * Nghiệp vụ:
     * - Xác thực workspace thuộc companyId (sai → 400) và project thuộc workspace (sai → 400).
     * - Yêu cầu quyền project:delete tại Controller bằng @PreAuthorize (project-level permission).
     * - Không xóa cứng; chỉ set status = CANCELLED và lưu.
     */
    void deleteProject(Integer companyId, Integer workspaceId, Integer projectId);  
    ProjectResponse getProjectDetails(Integer companyId, Integer workspaceId, Integer projectId);

    /**
     * Update project status (except CANCELLED which is reserved for delete endpoint).
     */
    ProjectResponse updateProjectStatus(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectStatusRequest request);

    /**
     * Update project general information (excluding auto fields and status).
     */
    ProjectResponse updateProject(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectRequest request);

    /**
     * Lấy danh sách Backlog (tất cả task) của một dự án.
     * @param companyId ID công ty (để kiểm tra)
     * @param workspaceId ID không gian (để kiểm tra)
     * @param projectId ID dự án
     * @return Danh sách TaskSummaryResponse DTO
     */
    List<TaskSummaryResponse> getProjectBacklog(Integer companyId, Integer workspaceId, Integer projectId);

    /**
     * Lấy danh sách thành viên của dự án (Phân trang & Sắp xếp).
     * @param projectId ID dự án
     * @param page Trang số mấy
     * @param size Kích thước trang
     * @param sortBy Trường sắp xếp
     * @param sortDir Hướng sắp xếp
     * @return PageResponseDTO
     */
    PageResponseDTO<ProjectMemberResponse> getProjectMembers(Integer projectId, int page, int size, String sortBy, String sortDir);


    /**
     * Cập nhật vai trò (Role) của một thành viên trong dự án.
     * @param projectId ID dự án
     * @param memberId ID của bản ghi ProjectMember
     * @param newRoleCode Mã vai trò mới (ví dụ: "PROJECT_MEMBER")
     * @return ProjectMemberResponse DTO đã cập nhật
     */
    ProjectMemberResponse updateProjectMemberRole(Integer projectId, Integer memberId, String newRoleCode);
}
