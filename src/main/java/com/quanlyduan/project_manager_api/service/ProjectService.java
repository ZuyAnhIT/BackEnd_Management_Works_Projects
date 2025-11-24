
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.BoardColumnResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectBacklogResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service cho Project – US7 chỉ yêu cầu tạo mới Project.
 */
public interface ProjectService {

    /**
     * Tạo dự án mới (có hỗ trợ upload ảnh bìa).
     */
    ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request, Integer creatorId, MultipartFile coverImageFile);
    
    
    PageResponseDTO<ProjectResponse> listProjectsByWorkspace(
            Integer companyId, 
            Integer workspaceId, 
            ProjectStatus status, 
            int page, 
            int size, 
            String sortBy, 
            String sortDir
    );

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
     * Cập nhật thông tin dự án và ảnh bìa.
     */
    ProjectResponse updateProject(Integer companyId, Integer workspaceId, Integer projectId, UpdateProjectRequest request, MultipartFile coverImageFile);

   /**
     * Lấy dữ liệu màn hình Backlog.
     * - Active Sprints: Lấy hết (có lọc keyword).
     * - Backlog Tasks: Lấy phân trang + lọc keyword + sắp xếp.
     */
    ProjectBacklogResponse getProjectBacklog(
            Integer companyId, 
            Integer workspaceId, 
            Integer projectId,
            String keyword,     
            Integer assigneeId, 
            TaskPriority priority, 
            TaskType taskType,     
            int page,           
            int size,           
            String sortBy,      
            String sortDir      
    );


    /**
     * Cập nhật vai trò (Role) của một thành viên trong dự án.
     * @param projectId ID dự án
     * @param memberId ID của bản ghi ProjectMember
     * @param newRoleCode Mã vai trò mới (ví dụ: "PROJECT_MEMBER")
     * @return ProjectMemberResponse DTO đã cập nhật
     */
    ProjectMemberResponse updateProjectMemberRole(Integer projectId, Integer memberId, String newRoleCode);

    // API 1: LẤY DANH SÁCH CƠ BẢN
    /**
     * Lấy danh sách thành viên của dự án (Chỉ phân trang & sắp xếp).
     */
    PageResponseDTO<ProjectMemberResponse> getProjectMembers(Integer projectId, int page, int size, String sortBy, String sortDir);

    // API 2: TÌM KIẾM NÂNG CAO (*** MỚI ***)
    /**
     * Tìm kiếm thành viên trong dự án.
     */
    PageResponseDTO<ProjectMemberResponse> searchProjectMembers(
            Integer projectId, 
            String searchName, String searchEmail, String searchRoleName, String searchPhone,
            int page, int size, String sortBy, String sortDir
    );

    /**
     * Tìm kiếm dự án trong workspace (Nâng cao).
     */
    PageResponseDTO<ProjectResponse> searchProjects(
            Integer companyId, 
            Integer workspaceId, 
            String searchName, String searchCode, String searchManager, ProjectStatus searchStatus,
            int page, int size, String sortBy, String sortDir
    );
    //US-S4-Kanban Board
    List<BoardColumnResponse> getProjectBoard(
        Integer companyId,
        Integer workspaceId,
        Integer projectId,
        String search,
        Integer assigneeId,
        String priority,
        List<String> statusNames
);
}
