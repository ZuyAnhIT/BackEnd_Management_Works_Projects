package com.quanlyduan.project_manager_api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.dto.request.InviteProjectMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.ProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateProjectStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.BoardColumnResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.ProjectBacklogResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectInvitationDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectInvitationResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.ProjectResponse;
import com.quanlyduan.project_manager_api.dto.response.TaskSummaryResponse;
import com.quanlyduan.project_manager_api.model.ProjectInvitation;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

/**
 * Service quan ly toan bo vong doi cua Du an (Project) va cac che do hien thi cong viec.
 * Ho tro cac mo hinh quan ly linh hoat: Kanban Board, Backlog, List View va Calendar.
 */
public interface ProjectService {

    // ======================================================
    // 1. QUAN LY DU AN (PROJECT LIFECYCLE)
    // ======================================================

    /**
     * Khoi tao Du an moi kem theo anh bia (Cover Image).
     * * @param companyId ID Cong ty chu quan
     * @param workspaceId ID Khong gian lam viec chua du an
     * @param request Thong tin cau hinh du an
     * @param creatorId ID nguoi thuc hien tao
     * @param coverImageFile Tep tin hinh anh minh hoa
     * @return Thong tin du an sau khi khoi tao
     */
    ProjectResponse createProject(Integer companyId, Integer workspaceId, ProjectRequest request, Integer creatorId,
                                MultipartFile coverImageFile);

    /**
     * Truy xuat ho so chi tiet cua mot du an cu the.
     * * @param companyId ID Cong ty (kiem soat pham vi)
     * @param workspaceId ID Workspace
     * @param projectId ID Du an can lay thong tin
     * @return DTO chua toan bo thong so du an
     */
    ProjectResponse getProjectDetails(Integer companyId, Integer workspaceId, Integer projectId);

    /**
     * Cap nhat thong tin hanh chinh va hinh anh cua du an (Partial Update).
     * * @return Thong tin du an sau khi cap nhat
     */
    ProjectResponse updateProject(Integer companyId, Integer workspaceId, Integer projectId,
                                UpdateProjectRequest request, MultipartFile coverImageFile);

    /**
     * Thay doi trang thai van hanh cua du an (vi du: Tu ACTIVE sang COMPLETED).
     * * @param request Trang thai moi can thiet lap
     * @return Thong tin du an voi trang thai moi
     */
    ProjectResponse updateProjectStatus(Integer companyId, Integer workspaceId, Integer projectId,
                                UpdateProjectStatusRequest request);

    /**
     * Loai bo du an khoi he thong (Soft Delete).
     * Logic nghiep vu: Chuyen trang thai sang CANCELLED de bao toan lich su.
     * * @param projectId ID du an can loai bo
     */
    void deleteProject(Integer companyId, Integer workspaceId, Integer projectId);

    // ======================================================
    // 2. TRA CUU VA TIM KIEM (LISTING & DISCOVERY)
    // ======================================================

    /**
     * Lay danh sach du an trong mot Workspace co phan trang.
     * * @param status Bo loc theo trang thai du an
     * @return Trang danh sach cac du an
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
     * Tim kiem du an nang cao dua tren nhieu tieu chi phoi hop.
     * * @param searchName Tim theo ten
     * @param searchCode Tim theo ma code (vi du: PROJ-01)
     * @param searchManager Tim theo ten nguoi quan ly
     * @return Ket qua tim kiem phan trang
     */
    PageResponseDTO<ProjectResponse> searchProjects(
                        Integer companyId,
                        Integer workspaceId,
                        String searchName, String searchCode, String searchManager, ProjectStatus searchStatus,
                        int page, int size, String sortBy, String sortDir);

    // ======================================================
    // 3. QUAN LY THANH VIEN (PROJECT MEMBERSHIP)
    // ======================================================

    /**
     * Moi thanh vien tham gia vao doi ngu du an.
     * Ho tro moi nguoi dung noi bo cong ty hoac doi tac ben ngoai.
     * * @param projectId ID du an phat hanh loi moi
     * @param request Email va vai tro du kien
     * @return Entity loi moi du an
     */
    ProjectInvitation inviteMemberToProject(Integer projectId, InviteProjectMemberRequest request);

    /**
     * Danh sach nhan su dang tham gia vao du an.
     * * @return Trang danh sach thanh vien du an
     */
    PageResponseDTO<ProjectMemberResponse> getProjectMembers(Integer projectId, int page, int size, String sortBy,
                        String sortDir);

    /**
     * Tim kiem thanh vien trong pham vi du an (Loc theo Ten, Email, Vai tro).
     * * @return Ket qua tim kiem thanh vien phan trang
     */
    PageResponseDTO<ProjectMemberResponse> searchProjectMembers(
                        Integer projectId,
                        String searchName, String searchEmail, String searchRoleName, String searchPhone,
                        int page, int size, String sortBy, String sortDir);

    /**
     * Thay doi phan quyen cho mot thanh vien cu the trong du an.
     * * @param memberId ID ban ghi thanh vien
     * @param newRoleCode Ma vai tro moi (vi du: PROJECT_ADMIN)
     * @return Ho so thanh vien sau khi cap nhat quyen
     */
    ProjectMemberResponse updateProjectMemberRole(Integer projectId, Integer memberId, String newRoleCode);

    // ======================================================
    // 4. QUAN LY BOARD VA BACKLOG (AGILE VIEWS)
    // ======================================================

    /**
     * Lay du lieu che do Backlog (Sprints dang hoat dong va cac Task chua gan).
     * * @return Cau truc Backlog gom Sprints va Task List
     */
    ProjectBacklogResponse getProjectBacklog(
                        Integer companyId, Integer workspaceId, Integer projectId,
                        String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType,
                        int page, int size, String sortBy, String sortDir);

    /**
     * Lay du lieu che do Kanban/Scrum Board theo tung Sprint.
     * * @param sprintId ID Sprint can xem (neu null se lay Sprint hien tai)
     * @return Danh sach cac cot trang thai kem theo Task tuong ung
     */
    List<BoardColumnResponse> getProjectBoard(
                        Integer companyId, Integer workspaceId, Integer projectId,
                        Integer sprintId, String keyword, Integer assigneeId, 
                        TaskPriority priority, TaskType taskType);

    /**
     * Lay danh sach cong viec duoi dang bang (List View) voi bo loc da dieu kien.
     * * @return Trang danh sach tom tat cac Task
     */
    PageResponseDTO<TaskSummaryResponse> getProjectTaskList(
                        Integer companyId, Integer workspaceId, Integer projectId,
                        Integer sprintId, String search, Integer assigneeId, TaskPriority priority,
                        List<Integer> statusIds,
                        int page, int size, String sortBy, String sortDir);

    /**
     * Nhom cac cong viec theo tieu chi chi dinh (vi du: theo Assignee, theo Priority).
     * * @param groupBy Truong du lieu can nhom (assignee, priority, taskType)
     * @return Map chua danh sach Task theo tung nhom
     */
    Map<String, List<TaskSummaryResponse>> getTasksGroupedBy(
                        Integer companyId, Integer workspaceId, Integer projectId,
                        String groupBy, Integer sprintId, String search);

    /**
     * Lay du lieu cong viec hien thi tren lich (Task Calendar).
     * * @param from Ngay bat dau khoang thoi gian
     * @param to Ngay ket thuc khoang thoi gian
     */
    List<TaskSummaryResponse> getTaskCalendar(
                        Integer companyId, Integer workspaceId, Integer projectId,
                        LocalDate from, LocalDate to,
                        String keyword, Integer assigneeId, TaskPriority priority, TaskType taskType);

    /**
     * Truy xuat cac cong viec da bi luu tru (Archived).
     * * @return Trang danh sach Task da luu tru
     */
    PageResponseDTO<TaskSummaryResponse> getArchivedTasks(
                        Integer projectId, String keyword, Integer assigneeId,
                        TaskPriority priority, TaskType taskType,
                        int page, int size);

    // ======================================================
    // 5. QUY TRINH MOI THAM GIA (INVITATION FLOW)
    // ======================================================

    /**
     * Lay thong tin chi tiet loi moi tu ma Token (API cong khai).
     * * @param token Ma xac thuc loi moi
     * @return Chi tiet loi moi de hien thi tren trang xac nhan
     */
    ProjectInvitationDetailsResponse getProjectInvitationDetails(String token);

    /**
     * Chap nhan loi mời tham gia du an (Dành cho nguoi dung da xac thuc).
     * * @param token Ma xac thuc loi moi hop le
     */
    void acceptProjectInvitation(String token);

    /**
     * Quan ly danh sach cac loi moi da phat hanh tu du an.
     * * @return Trang danh sach cac loi moi
     */
    PageResponseDTO<ProjectInvitationResponse> getProjectInvitations(
                        Integer projectId, String keyword, String status,
                        int page, int size, String sortBy, String sortDir);

    /**
     * Huy bo mot loi moi da gui (khi chua duoc chap nhan).
     * * @param invitationId ID ban ghi loi moi
     */
    void cancelProjectInvitation(Integer projectId, Integer invitationId);
}