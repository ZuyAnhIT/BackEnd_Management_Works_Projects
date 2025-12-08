// File: src/main/java/com.quanlyduan.project_manager_api/service/ProjectService.java
package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.request.InviteProjectMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.dto.response.ActivityLogResponse;
import com.quanlyduan.project_manager_api.dto.response.BoardColumnResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectBacklogResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectInvitationDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interface Service quản lý các nghiệp vụ liên quan đến Dự án (Project).
 * Bao gồm CRUD, quản lý thành viên, và dữ liệu Board/Backlog.
 */
public interface ProjectService {

        // ========================================================================
        // 1. QUẢN LÝ DỰ ÁN (CRUD & UPDATE)
        // ========================================================================

        /**
         * Tạo dự án mới (có hỗ trợ upload ảnh bìa).
         */
        ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request, Integer creatorId,
                        MultipartFile coverImageFile);

        /**
         * Lấy thông tin chi tiết của một dự án.
         */
        ProjectResponse getProjectDetails(Integer companyId, Integer workspaceId, Integer projectId);

        /**
         * Cập nhật thông tin dự án và ảnh bìa (Partial Update).
         */
        ProjectResponse updateProject(Integer companyId, Integer workspaceId, Integer projectId,
                        UpdateProjectRequest request, MultipartFile coverImageFile);

        /**
         * Update project status (trạng thái vòng đời).
         */
        ProjectResponse updateProjectStatus(Integer companyId, Integer workspaceId, Integer projectId,
                        UpdateProjectStatusRequest request);

        /**
         * Xóa dự án (soft delete) – chuyển trạng thái Project sang CANCELLED.
         */
        void deleteProject(Integer companyId, Integer workspaceId, Integer projectId);

        // ========================================================================
        // 2. DANH SÁCH & TÌM KIẾM DỰ ÁN (LISTING & SEARCH)
        // ========================================================================

        /**
         * Lấy danh sách dự án trong workspace (Phân trang & Sắp xếp cơ bản).
         */
        PageResponseDTO<ProjectResponse> listProjectsByWorkspace(
                        Integer companyId,
                        Integer workspaceId,
                        ProjectStatus status,
                        int page,
                        int size,
                        String sortBy,
                        String sortDir);

        /**
         * Tìm kiếm dự án trong workspace (Nâng cao: Tên, Mã, Quản lý, Trạng thái).
         */
        PageResponseDTO<ProjectResponse> searchProjects(
                        Integer companyId,
                        Integer workspaceId,
                        String searchName, String searchCode, String searchManager, ProjectStatus searchStatus,
                        int page, int size, String sortBy, String sortDir);

        // ========================================================================
        // 3. QUẢN LÝ THÀNH VIÊN (MEMBERSHIP)
        // ========================================================================

        /**
         * Gửi lời mời tham gia dự án (Xử lý cả nội bộ và bên ngoài).
         */
        void inviteMemberToProject(Integer projectId, InviteProjectMemberRequest request);

        /**
         * Lấy danh sách thành viên của dự án (Phân trang & Sắp xếp cơ bản).
         */
        PageResponseDTO<ProjectMemberResponse> getProjectMembers(Integer projectId, int page, int size, String sortBy,
                        String sortDir);

        /**
         * Tìm kiếm thành viên trong dự án (Nâng cao).
         */
        PageResponseDTO<ProjectMemberResponse> searchProjectMembers(
                        Integer projectId,
                        String searchName, String searchEmail, String searchRoleName, String searchPhone,
                        int page, int size, String sortBy, String sortDir);

        /**
         * Cập nhật vai trò (Role) của một thành viên trong dự án.
         */
        ProjectMemberResponse updateProjectMemberRole(Integer projectId, Integer memberId, String newRoleCode);

        // ========================================================================
        // 4. QUẢN LÝ TASK VÀ BOARD (BACKLOG & BOARD VIEW)
        // ========================================================================

        /**
         * Lấy dữ liệu màn hình Backlog (Active Sprints + Unassigned Tasks) - Có Phân
         * trang.
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
                        String sortDir);

        /**
         * Lấy dữ liệu Board (Các cột trạng thái và task bên trong) - Dùng cho
         * Scrum/Kanban Board.
         */
        List<BoardColumnResponse> getProjectBoard(
                        Integer companyId,
                        Integer workspaceId,
                        Integer projectId,
                        Integer sprintId,
                        String keyword,
                        Integer assigneeId,
                        TaskPriority priority,
                        TaskType taskType);

        // Hàm lấy danh sách Task (List View)
        PageResponseDTO<TaskSummaryResponse> getProjectTaskList(
                        Integer companyId, Integer workspaceId, Integer projectId,
                        Integer sprintId, String search, Integer assigneeId, TaskPriority priority,
                        List<Integer> statusIds,
                        int page, int size, String sortBy, String sortDir);

        // Hàm nhóm Task (Grouping View)
        Map<String, List<TaskSummaryResponse>> getTasksGroupedBy(
                        Integer companyId, Integer workspaceId, Integer projectId,
                        String groupBy, Integer sprintId, String search);

        // Lấy dữ liệu lịch công việc (Task Calendar)                
        List<TaskSummaryResponse> getTaskCalendar(
                        Integer companyId, Integer workspaceId, Integer projectId,
                        LocalDate from, LocalDate to,
                        String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType);

        // ========================================================================
        // 5. HỖ TRỢ LUỒNG MỜI (INVITATION FLOW HELPERS)
        // ========================================================================

        /**
         * Lấy thông tin chi tiết của lời mời (Public API).
         * Dùng để kiểm tra token và quyết định luồng UI (Register/Login).
         */
        ProjectInvitationDetailsResponse getProjectInvitationDetails(String token);

        /**
         * Chấp nhận lời mời (Dành cho user đã login).
         * Tạo ProjectMember và đánh dấu lời mời là ACCEPTED.
         */
        void acceptProjectInvitation(String token);

        // Activity Logs
         List<ActivityLogResponse> getRecentActivities(Integer companyId, Integer workspaceId, Integer projectId);
         
}