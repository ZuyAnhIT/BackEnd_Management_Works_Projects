package com.quanlyduan.project_manager_api.service;


import com.quanlyduan.project_manager_api.model.CongTyThanhVien;
import com.quanlyduan.project_manager_api.model.KhongGianThanhVien;
import com.quanlyduan.project_manager_api.model.NguoiDung;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.repository.CongTyThanhVienRepository;
import com.quanlyduan.project_manager_api.repository.KhongGianThanhVienRepository;
import com.quanlyduan.project_manager_api.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("securityService") // Đặt tên Bean là "securityService"
@RequiredArgsConstructor
public class SecurityService {

    private final CongTyThanhVienRepository congTyThanhVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final KhongGianThanhVienRepository khongGianThanhVienRepository;
    
    // // Mã role chuẩn
    // private static final String COMPANY_ADMIN_ROLE = "COMPANY_ADMIN";

    /**
     * Kiểm tra xem người dùng hiện tại có phải là Admin của một công ty cụ thể không.
     * @param congTyId ID của công ty cần kiểm tra
     * @return true nếu là Admin, ngược lại ném AccessDeniedException
     */
    public boolean isCompanyAdmin(Integer congTyId) {
        // 1. Lấy người dùng đang đăng nhập
        NguoiDung currentUser = getCurrentAuthenticatedUser();

        // 2. Tìm thông tin thành viên của họ trong công ty
        Optional<CongTyThanhVien> membership = congTyThanhVienRepository
            .findByCongTy_IdCongTyAndNguoiDung_IdNguoiDung(congTyId, currentUser.getIdNguoiDung());

        if (membership.isEmpty()) {
            return false; // Không phải thành viên
        }
        
        // 3. Kiểm tra xem role của họ có phải là "COMPANY_ADMIN" không
        return RoleCode.COMPANY_ADMIN.name().equals(membership.get().getRole().getMaRole());
    }

    // (Chúng ta cũng sẽ dùng hàm này để kiểm tra xem có phải là MEMBER không)
    public boolean isCompanyMember(Integer congTyId) {
        NguoiDung currentUser = getCurrentAuthenticatedUser();
        return congTyThanhVienRepository
            .existsByCongTy_IdCongTyAndNguoiDung_IdNguoiDung(congTyId, currentUser.getIdNguoiDung());
    }


    // *** THÊM PHƯƠNG THỨC NÀY ***
    /**
     * Kiểm tra xem người dùng hiện tại có phải là thành viên của một không gian làm việc cụ thể
     * VÀ không gian đó có thuộc công ty trong URL hay không.
     * (Để ngăn chặn lỗi bảo mật Insecure Direct Object Reference - IDOR)
     *
     * @param congTyId ID công ty từ URL
     * @param khongGianId ID không gian từ URL
     * @return true nếu người dùng là thành viên hợp lệ
     */
    public boolean isWorkspaceMember(Integer congTyId, Integer khongGianId) {
        NguoiDung currentUser = getCurrentAuthenticatedUser();

        // 1. Kiểm tra xem người dùng có phải là thành viên của không gian không
        Optional<KhongGianThanhVien> membership = khongGianThanhVienRepository
            .findByKhongGian_IdKhongGianAndNguoiDung_IdNguoiDung(khongGianId, currentUser.getIdNguoiDung());

        if (membership.isEmpty()) {
            return false; // Không phải thành viên của không gian này
        }

        // 2. Kiểm tra xem không gian đó có thực sự thuộc công ty trong URL không
        // Điều này đảm bảo người dùng không thể thử /api/companies/1/workspaces/99
        // (nếu workspace 99 thuộc công ty 2)
        return membership.get().getKhongGian().getCongTy().getIdCongTy().equals(congTyId);
    }


    /**
     * Kiểm tra xem người dùng hiện tại có phải là Admin của một không gian làm việc cụ thể không.
     * @param congTyId ID công ty từ URL (để bảo mật)
     * @param khongGianId ID không gian từ URL
     * @return true nếu là Admin của không gian
     */
    public boolean isWorkspaceAdmin(Integer congTyId, Integer khongGianId) {
        NguoiDung currentUser = getCurrentAuthenticatedUser();

        Optional<KhongGianThanhVien> membership = khongGianThanhVienRepository
            .findByKhongGian_IdKhongGianAndNguoiDung_IdNguoiDung(khongGianId, currentUser.getIdNguoiDung());

        if (membership.isEmpty()) {
            return false; // Không phải thành viên
        }

        // 1. Kiểm tra Role
        boolean isAdmin = RoleCode.WORKSPACE_ADMIN.name().equals(membership.get().getRole().getMaRole());
        
        // 2. Kiểm tra xem không gian đó có thuộc công ty trong URL không (bảo mật IDOR)
        boolean isCorrectCompany = membership.get().getKhongGian().getCongTy().getIdCongTy().equals(congTyId);

        return isAdmin && isCorrectCompany;
    }


    /**
     * Kiểm tra xem người dùng có quyền quản lý thành viên không gian (thêm/xóa).
     * Quyền này thuộc về (Admin Công ty) HOẶC (Admin Không gian).
     */
    public boolean canManageWorkspaceMembers(Integer congTyId, Integer khongGianId) {
        // 1. Kiểm tra xem có phải là Admin công ty không
        if (isCompanyAdmin(congTyId)) {
            return true;
        }
        
        // 2. Nếu không, kiểm tra xem có phải là Admin không gian không
        return isWorkspaceAdmin(congTyId, khongGianId);
    }

    // --- Private Helper Method ---
    public NguoiDung getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UsernameNotFoundException("Không tìm thấy thông tin người dùng đã xác thực.");
        }
        String email = authentication.getName();
        return nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }


}