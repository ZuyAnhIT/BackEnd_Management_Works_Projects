// // File: src/main/java/com/quanlyduan/project_manager_api/service/SecurityService.java
// package com.quanlyduan.project_manager_api.service;


// import com.quanlyduan.project_manager_api.model.CompanyMember; // Đã dịch
// import com.quanlyduan.project_manager_api.model.WorkspaceMember; // Đã dịch
// import com.quanlyduan.project_manager_api.model.User; // Đã dịch
// import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
// import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
// import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository; // Đã dịch
// import com.quanlyduan.project_manager_api.repository.WorkspaceMemberRepository; // Đã dịch
// import com.quanlyduan.project_manager_api.repository.UserRepository; // Đã dịch
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;

// import java.util.Optional;

// @Service("securityService") // Đặt tên Bean là "securityService"
// public class SecurityService {

//     private final CompanyMemberRepository companyMemberRepository; // Đã dịch
//     private final UserRepository userRepository; // Đã dịch
//     private final WorkspaceMemberRepository workspaceMemberRepository; // Đã dịch

//     public SecurityService(CompanyMemberRepository companyMemberRepository, UserRepository userRepository, WorkspaceMemberRepository workspaceMemberRepository) { // Đã dịch
//         this.companyMemberRepository = companyMemberRepository; // Đã dịch
//         this.userRepository = userRepository; // Đã dịch
//         this.workspaceMemberRepository = workspaceMemberRepository; // Đã dịch
//     }

//     // // Mã role chuẩn
//     // private static final String COMPANY_ADMIN_ROLE = "COMPANY_ADMIN";

//     /**
//      * Kiểm tra xem người dùng hiện tại có phải là Admin của một công ty cụ thể không.
//      * @param congTyId ID của công ty cần kiểm tra
//      * @return true nếu là Admin, ngược lại ném AccessDeniedException
//      */
//     public boolean isCompanyAdmin(Integer companyId) { // Đã dịch
//         // 1. Lấy người dùng đang đăng nhập
//         User currentUser = getCurrentAuthenticatedUser(); // Đã dịch

//         // 2. Tìm thông tin thành viên của họ trong công ty
//         Optional<CompanyMember> membership = companyMemberRepository // Đã dịch
//             .findByCompany_IdAndUser_IdAndStatus(companyId, currentUser.getId(), MemberStatus.ACTIVE); // Sửa

//         if (membership.isEmpty()) {
//             return false; // Không phải thành viên
//         }
        
//         // 3. Kiểm tra xem role của họ có phải là "COMPANY_ADMIN" không
//         return RoleCode.COMPANY_ADMIN.name().equals(membership.get().getRole().getRoleCode()); // Đã dịch
//     }

//     // (Chúng ta cũng sẽ dùng hàm này để kiểm tra xem có phải là MEMBER không)
//     public boolean isCompanyMember(Integer companyId) { // Đã dịch
//         User currentUser = getCurrentAuthenticatedUser(); // Đã dịch
//         return companyMemberRepository // Đã dịch
//             .existsByCompany_IdAndUser_IdAndStatus(companyId, currentUser.getId(), MemberStatus.ACTIVE); // Sửa
//     }


//     // *** THÊM PHƯƠNG THỨC NÀY ***
//     /**
//      * Kiểm tra xem người dùng hiện tại có phải là thành viên của một không gian làm việc cụ thể
//      * VÀ không gian đó có thuộc công ty trong URL hay không.
//      * (Để ngăn chặn lỗi bảo mật Insecure Direct Object Reference - IDOR)
//      *
//      * @param congTyId ID công ty từ URL
//      * @param khongGianId ID không gian từ URL
//      * @return true nếu người dùng là thành viên hợp lệ
//      */
//     public boolean isWorkspaceMember(Integer companyId, Integer workspaceId) { // Đã dịch
//         User currentUser = getCurrentAuthenticatedUser(); // Đã dịch

//         // 1. Kiểm tra xem người dùng có phải là thành viên của không gian không
//         Optional<WorkspaceMember> membership = workspaceMemberRepository // Đã dịch
//             .findByWorkspace_IdAndUser_IdAndStatus(workspaceId, currentUser.getId(), MemberStatus.ACTIVE); // Sửa

//         if (membership.isEmpty()) {
//             return false; // Không phải thành viên của không gian này
//         }

//         // 2. Kiểm tra xem không gian đó có thực sự thuộc công ty trong URL không
//         // Điều này đảm bảo người dùng không thể thử /api/companies/1/workspaces/99
//         // (nếu workspace 99 thuộc công ty 2)
//         return membership.get().getWorkspace().getCompany().getId().equals(companyId); // Đã dịch
//     }


//     /**
//      * Kiểm tra xem người dùng hiện tại có phải là Admin của một không gian làm việc cụ thể không.
//      * @param congTyId ID công ty từ URL (để bảo mật)
//      * @param khongGianId ID không gian từ URL
//      * @return true nếu là Admin của không gian
//      */
//     public boolean isWorkspaceAdmin(Integer companyId, Integer workspaceId) { // Đã dịch
//         User currentUser = getCurrentAuthenticatedUser(); // Đã dịch

//         Optional<WorkspaceMember> membership = workspaceMemberRepository // Đã dịch
//             .findByWorkspace_IdAndUser_IdAndStatus(workspaceId, currentUser.getId(), MemberStatus.ACTIVE); // Sửa

//         if (membership.isEmpty()) {
//             return false; // Không phải thành viên
//         }

//         // 1. Kiểm tra Role
//         boolean isAdmin = RoleCode.WORKSPACE_ADMIN.name().equals(membership.get().getRole().getRoleCode()); // Đã dịch
        
//         // 2. Kiểm tra xem không gian đó có thuộc công ty trong URL không (bảo mật IDOR)
//         boolean isCorrectCompany = membership.get().getWorkspace().getCompany().getId().equals(companyId); // Đã dịch

//         return isAdmin && isCorrectCompany;
//     }


//     /**
//      * Kiểm tra xem người dùng có quyền quản lý thành viên không gian (thêm/xóa).
//      * Quyền này thuộc về (Admin Công ty) HOẶC (Admin Không gian).
//      */
//     public boolean canManageWorkspaceMembers(Integer companyId, Integer workspaceId) { // Đã dịch
//         // 1. Kiểm tra xem có phải là Admin công ty không
//         if (isCompanyAdmin(companyId)) { // Đã dịch
//             return true;
//         }
        
//         // 2. Nếu không, kiểm tra xem có phải là Admin không gian không
//         return isWorkspaceAdmin(companyId, workspaceId); // Đã dịch
//     }

//     // --- Private Helper Method ---
//     public User getCurrentAuthenticatedUser() { 
//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//         if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
//             throw new UsernameNotFoundException("Authenticated user information not found."); // Đã dịch
//         }
//         String email = authentication.getName();
//         return userRepository.findByEmail(email) // Đã dịch
//                 .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email)); // Đã dịch
//     }


// }