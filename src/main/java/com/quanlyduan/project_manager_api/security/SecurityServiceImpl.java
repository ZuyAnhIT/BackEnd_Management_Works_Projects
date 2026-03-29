package com.quanlyduan.project_manager_api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.Sprint;
import com.quanlyduan.project_manager_api.model.Task;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.SprintRepository;
import com.quanlyduan.project_manager_api.repository.TaskRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository;
import com.quanlyduan.project_manager_api.repository.WorkspaceRepository;

/**
 * Thuc thi cac nghiep vu bao mat va kiem tra quyen han da tang.
 * Ho tro co che thua ke quyen: Company -> Workspace -> Project -> Task/Sprint.
 */
@Service("securityService")
@Transactional(readOnly = true)
public class SecurityServiceImpl implements SecurityService {

    // Cac hang so pham vi (Scopes)
    private static final String SCOPE_COMPANY = "company";
    private static final String SCOPE_WORKSPACE = "workspace";
    private static final String SCOPE_PROJECT = "project";
    private static final String SCOPE_TASK = "task";
    private static final String SCOPE_SPRINT = "sprint";
    
    // Cac thong bao loi
    private static final String ERR_TENANT_SUSPENDED = "Your business account has been temporarily suspended. Please contact the administrator.";
    private static final String ERR_USER_NOT_FOUND = "User not found with ID: %d";

    private final CompanyMemberRepository companyMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final CompanyRepository companyRepository;

    public SecurityServiceImpl(CompanyMemberRepository companyMemberRepository,
                               WorkspaceMemberRepository workspaceMemberRepository,
                               ProjectMemberRepository projectMemberRepository,
                               UserRepository userRepository,
                               TaskRepository taskRepository,
                               WorkspaceRepository workspaceRepository,
                               ProjectRepository projectRepository,
                               SprintRepository sprintRepository,
                               CompanyRepository companyRepository) {
        this.companyMemberRepository = companyMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.workspaceRepository = workspaceRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.companyRepository = companyRepository;
    }

    // ========================================================================
    // IDENTITY: XAC THUC NGUOI DUNG
    // ========================================================================

    @Override
    public String getCurrentUserEmail() {
        UserPrincipal user = getCurrentUserPrincipal();
        return (user != null) ? user.getEmail() : null;
    }

    @Override
    public Integer getCurrentUserId() {
        UserPrincipal user = getCurrentUserPrincipal();
        return (user != null) ? user.getId() : null;
    }

    @Override
    public User getCurrentAuthenticatedUser() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            throw new UsernameNotFoundException("Authenticated user information not found.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(ERR_USER_NOT_FOUND, userId)));
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() 
                || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    // ========================================================================
    // CORE PERMISSION: CO CHE KIEM TRA QUYEN TONG QUAT
    // ========================================================================

    @Override
    public boolean hasPermission(String scope, Integer targetId, String permissionCode) {
        Integer userId = getCurrentUserId();
        if (userId == null || targetId == null || scope == null || permissionCode == null) {
            return false;
        }

        // 1. Kiem tra trang thai hoat dong cua Tenant (Company)
        validateTenantStatus(scope, targetId);

        // 2. Kiem tra quyen theo tung cap do (Co thua ke tu tren xuong)
        switch (scope.toLowerCase()) {
            case SCOPE_COMPANY:
                return companyMemberRepository.checkCompanyPermission(userId, targetId, permissionCode);

            case SCOPE_WORKSPACE:
                // Check truc tiep tai Workspace, neu khong co thi check len cap Company
                if (workspaceMemberRepository.checkWorkspacePermission(userId, targetId, permissionCode)) return true;
                return hasCompanyPermission(getCompanyIdFromWorkspace(targetId), permissionCode);

            case SCOPE_PROJECT:
                // Check Project -> Workspace -> Company
                if (projectMemberRepository.checkProjectPermission(userId, targetId, permissionCode)) return true;
                Integer wsId = getWorkspaceIdFromProject(targetId);
                return hasWorkspacePermission(wsId, permissionCode);

            case SCOPE_TASK:
                Task task = taskRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Task not found."));
                return hasProjectPermission(task.getProject().getId(), permissionCode);

            case SCOPE_SPRINT:
                Sprint sprint = sprintRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Sprint not found."));
                return hasProjectPermission(sprint.getProject().getId(), permissionCode);

            default:
                throw new IllegalArgumentException("Unknown permission scope: " + scope);
        }
    }

    // ========================================================================
    // RESOURCE SPECIFIC: KIEM TRA QUYEN TREN TUNG TAI NGUYEN
    // ========================================================================

    @Override
    public boolean hasSystemPermission(String permissionCode) {
        Integer userId = getCurrentUserId();
        return userId != null && userRepository.countSystemPermission(userId, permissionCode) > 0;
    }

    @Override
    public boolean hasCompanyPermission(Integer companyId, String permissionCode) {
        Integer userId = getCurrentUserId();
        return userId != null && companyId != null && 
               companyMemberRepository.checkCompanyPermission(userId, companyId, permissionCode);
    }

    @Override
    public boolean hasWorkspacePermission(Integer workspaceId, String permissionCode) {
        return hasPermission(SCOPE_WORKSPACE, workspaceId, permissionCode);
    }

    @Override
    public boolean hasProjectPermission(Integer projectId, String permissionCode) {
        return hasPermission(SCOPE_PROJECT, projectId, permissionCode);
    }

    @Override
    public boolean hasTaskPermission(Integer taskId, String permissionCode) {
        return hasPermission(SCOPE_TASK, taskId, permissionCode);
    }

    @Override
    public boolean hasSprintPermission(Integer sprintId, String permissionCode) {
        return hasPermission(SCOPE_SPRINT, sprintId, permissionCode);
    }

    // ========================================================================
    // HELPERS: CAC HAM TRUY VAN NGUOC & VALIDATION
    // ========================================================================

    /**
     * Truy nguoc tu doi tuong muc tieu de kiem tra xem Cong ty chu quan co dang bi khoa hay khong.
     */
    private void validateTenantStatus(String scope, Integer targetId) {
        Integer companyId = null;
        switch (scope.toLowerCase()) {
            case SCOPE_COMPANY: companyId = targetId; break;
            case SCOPE_WORKSPACE: companyId = getCompanyIdFromWorkspace(targetId); break;
            case SCOPE_PROJECT: companyId = getCompanyIdFromWorkspace(getWorkspaceIdFromProject(targetId)); break;
            case SCOPE_TASK: 
                Task t = taskRepository.findById(targetId).orElse(null);
                if (t != null) companyId = getCompanyIdFromWorkspace(t.getProject().getWorkspace().getId());
                break;
            case SCOPE_SPRINT:
                Sprint s = sprintRepository.findById(targetId).orElse(null);
                if (s != null) companyId = getCompanyIdFromWorkspace(s.getProject().getWorkspace().getId());
                break;
        }

        if (companyId != null) {
            Company company = companyRepository.findById(companyId).orElse(null);
            if (company != null && "SUSPENDED".equalsIgnoreCase(company.getStatus().toString())) {
                throw new BadRequestException(ERR_TENANT_SUSPENDED);
            }
        }
    }

    private Integer getCompanyIdFromWorkspace(Integer workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found."))
                .getCompany().getId();
    }

    private Integer getWorkspaceIdFromProject(Integer projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."))
                .getWorkspace().getId();
    }

    // ========================================================================
    // COMPATIBILITY: CAC HAM TIEN ICH VAI TRO (ROLES)
    // ========================================================================

    @Override
    public boolean isCompanyAdmin(Integer companyId) {
        return hasCompanyPermission(companyId, "company:manage_roles");
    }

    @Override
    public boolean isCompanyMember(Integer companyId) {
        return hasCompanyPermission(companyId, "company:view");
    }

    @Override
    public boolean isWorkspaceAdmin(Integer companyId, Integer workspaceId) {
        return getCompanyIdFromWorkspace(workspaceId).equals(companyId) && 
               hasPermission(SCOPE_WORKSPACE, workspaceId, "workspace:edit");
    }

    @Override
    public boolean isWorkspaceMember(Integer companyId, Integer workspaceId) {
        return getCompanyIdFromWorkspace(workspaceId).equals(companyId) && 
               hasPermission(SCOPE_WORKSPACE, workspaceId, "workspace:view");
    }

    @Override
    public boolean canManageWorkspaceMembers(Integer companyId, Integer workspaceId) {
        return hasPermission(SCOPE_WORKSPACE, workspaceId, "workspace:invite_member");
    }
}