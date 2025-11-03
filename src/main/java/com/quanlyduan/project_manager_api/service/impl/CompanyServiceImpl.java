package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.CongTy;
import com.quanlyduan.project_manager_api.model.CongTyThanhVien;
import com.quanlyduan.project_manager_api.model.NguoiDung;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.repository.CongTyRepository;
import com.quanlyduan.project_manager_api.repository.CongTyThanhVienRepository;
import com.quanlyduan.project_manager_api.repository.NguoiDungRepository;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CongTyRepository congTyRepository;
    private final CongTyThanhVienRepository congTyThanhVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final RoleRepository roleRepository;

    // Định nghĩa mã role mặc định cho người tạo công ty
    private static final String COMPANY_ADMIN_ROLE_CODE = "COMPANY_ADMIN";

    @Override
    @Transactional
    public CongTy createCompany(CreateCompanyRequest request) {
        // 1. Lấy người dùng đang đăng nhập (người tạo)
        NguoiDung creator = getCurrentAuthenticatedUser();

        // 2. Kiểm tra tên công ty đã tồn tại chưa
        if (congTyRepository.existsByTenCongTy(request.getTenCongTy())) {
            throw new BadRequestException("Tên công ty này đã tồn tại");
        }

        // 3. Tìm Role "COMPANY_ADMIN" trong CSDL
        Role adminRole = roleRepository.findFirstByMaRole(COMPANY_ADMIN_ROLE_CODE)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy Role: " + COMPANY_ADMIN_ROLE_CODE + ". Vui lòng cấu hình CSDL."
                ));

        // 4. Tạo công ty mới
        CongTy newCompany = CongTy.builder()
                .tenCongTy(request.getTenCongTy())
                .moTa(request.getMoTa())
                .diaChi(request.getDiaChi())
                .soDienThoai(request.getSoDienThoai())
                .email(request.getEmail())
                .website(request.getWebsite())
                .nguoiTaoId(creator.getIdNguoiDung())
                .trangThai(CompanyStatus.HOAT_DONG)
                .build();
        
        CongTy savedCompany = congTyRepository.save(newCompany);

        // 5. Thêm người tạo làm thành viên đầu tiên với vai trò Admin
        CongTyThanhVien membership = CongTyThanhVien.builder()
                .congTy(savedCompany)
                .nguoiDung(creator)
                .role(adminRole)
                .trangThai(MemberStatus.HOAT_DONG)
                .build();
        
        congTyThanhVienRepository.save(membership);

        return savedCompany;
    }
    
    // --- Private Helper Method ---
    // (Helper này lấy từ UserServiceImpl, bạn có thể tách ra 1 class Util chung)
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