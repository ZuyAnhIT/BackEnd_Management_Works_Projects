package com.quanlyduan.project_manager_api.service;


import com.quanlyduan.project_manager_api.model.CongTyThanhVien;
import com.quanlyduan.project_manager_api.model.NguoiDung;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.repository.CongTyThanhVienRepository;
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


    // --- Private Helper Method ---
    private NguoiDung getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UsernameNotFoundException("Không tìm thấy thông tin người dùng đã xác thực.");
        }
        String email = authentication.getName();
        return nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }
}