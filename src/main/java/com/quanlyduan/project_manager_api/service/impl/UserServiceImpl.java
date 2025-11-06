package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.ChangePasswordRequest;
import com.quanlyduan.project_manager_api.dto.response.CompanyMembershipDTO;
import com.quanlyduan.project_manager_api.dto.response.UserProfileResponse;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceMembershipDTO;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.model.NguoiDung;
import com.quanlyduan.project_manager_api.repository.CongTyThanhVienRepository;
import com.quanlyduan.project_manager_api.repository.KhongGianThanhVienRepository;
import com.quanlyduan.project_manager_api.repository.NguoiDungRepository;
import com.quanlyduan.project_manager_api.repository.NguoiDungRoleRepository;
import com.quanlyduan.project_manager_api.service.UserService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service

public class UserServiceImpl implements UserService {

    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;
    private final NguoiDungRoleRepository nguoiDungRoleRepository;
    private final CongTyThanhVienRepository congTyThanhVienRepository;
    private final KhongGianThanhVienRepository khongGianThanhVienRepository;

    public UserServiceImpl(NguoiDungRepository nguoiDungRepository, PasswordEncoder passwordEncoder, NguoiDungRoleRepository nguoiDungRoleRepository, CongTyThanhVienRepository congTyThanhVienRepository, KhongGianThanhVienRepository khongGianThanhVienRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
        this.nguoiDungRoleRepository = nguoiDungRoleRepository;
        this.congTyThanhVienRepository = congTyThanhVienRepository;
        this.khongGianThanhVienRepository = khongGianThanhVienRepository;
    }
    // (Sau này sẽ inject TokenRepository để hủy Refresh Token)

    // LOGIC THAY DOI MAT KHAU
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 1. Lấy thông tin người dùng đang đăng nhập
        NguoiDung currentUser = getCurrentAuthenticatedUser();

        // 2. Validate mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getMatKhau())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }

        // 3. Validate mật khẩu mới
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getMatKhau())) {
            throw new BadRequestException("Mật khẩu mới phải khác mật khẩu cũ");
        }

        // 4. Validate mật khẩu xác nhận
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        // 5. Hash và cập nhật mật khẩu mới
        currentUser.setMatKhau(passwordEncoder.encode(request.getNewPassword()));

        // 6. Lưu vào CSDL
        nguoiDungRepository.save(currentUser);

        // 7. (Nâng cao) Thu hồi tất cả Refresh Token
        // Đây là bước quan trọng để bảo mật. Khi đổi mật khẩu,
        // tất cả các phiên đăng nhập ở thiết bị khác sẽ bị buộc đăng xuất.
        // tokenRepository.revokeAllUserRefreshTokens(currentUser.getIdNguoiDung());
        // (Chúng ta sẽ implement chi tiết hàm revokeAll... này sau)
    }

    // --- Private Helper Method ---

    // LOGIC LAY THONG TIN DAY DU
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        // 1. Lấy người dùng (từ token)
        NguoiDung currentUser = getCurrentAuthenticatedUser();

        // 2. Lấy vai trò cấp Hệ thống
        List<String> systemRoles = nguoiDungRoleRepository.findByNguoiDung_IdNguoiDung(currentUser.getIdNguoiDung())
                .stream()
                .map(role -> role.getRole().getMaRole())
                .collect(Collectors.toList());

        // 3. Lấy vai trò cấp Công ty
        List<CompanyMembershipDTO> companyRoles = congTyThanhVienRepository.findByNguoiDung_IdNguoiDung(currentUser.getIdNguoiDung())
                .stream()
                .map(ctv -> new CompanyMembershipDTO(
                        ctv.getCongTy().getIdCongTy(),
                        ctv.getCongTy().getTenCongTy(),
                        ctv.getRole().getMaRole()
                ))
                .collect(Collectors.toList());

        // 4. Lấy vai trò cấp Không gian
        List<WorkspaceMembershipDTO> workspaceRoles = khongGianThanhVienRepository.findByNguoiDung_IdNguoiDung(currentUser.getIdNguoiDung())
                .stream()
                .map(kgtv -> new WorkspaceMembershipDTO(
                        kgtv.getKhongGian().getIdKhongGian(),
                        kgtv.getKhongGian().getTenKhongGian(),
                        kgtv.getKhongGian().getCongTy().getIdCongTy(),
                        kgtv.getRole().getMaRole()
                ))
                .collect(Collectors.toList());

        // 5. Xây dựng và trả về DTO
        return UserProfileResponse.builder()
                .id(currentUser.getIdNguoiDung())
                .hoTen(currentUser.getHoTen())
                .email(currentUser.getEmail())
                .anhDaiDien(currentUser.getAnhDaiDien())
                .systemRoles(systemRoles)
                .companyMemberships(companyRoles)
                .workspaceMemberships(workspaceRoles)
                .build();
    }

    // LOGIC LAY NGUOI DUNG HIEN TAI
    private NguoiDung getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadRequestException("Không tìm thấy thông tin người dùng đã xác thực.");
        }
        
        String email = authentication.getName();
        return nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }
}