package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.service.CompanyService;
import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.service.CompanyService;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.InvitationService;

import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.UUID;
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

    private final CongTyLoiMoiRepository congTyLoiMoiRepository;
    private final EmailService emailService;

    private final InvitationService invitationService;

    @Value("${app.frontend.url}") // Thêm URL frontend vào application.properties
    private String frontendUrl;


    // LOGIC TAO CONG TY
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



    // LOGIC TAO LOI MOI THANH VIEN VAO CONG TY
    @Override
    @Transactional
    public void inviteMember(Integer congTyId, InviteMemberRequest request) {
        // (Logic của hàm này không thay đổi, vì nó không dùng 2 hàm helper kia)
        
        // 1. Lấy thông tin
        NguoiDung admin = getCurrentAuthenticatedUser();
        CongTy congTy = congTyRepository.findById(congTyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));
        
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Role"));
        
        // 2. Validate
        if (role.getCapDo() != RoleLevel.COMPANY) {
            throw new BadRequestException("Role không hợp lệ (Không phải cấp độ Công ty)");
        }
        
        String invitedEmail = request.getEmail();
        if (admin.getEmail().equals(invitedEmail)) {
            throw new BadRequestException("Bạn không thể tự mời chính mình");
        }

        // 3. Kiểm tra xem đã là thành viên chưa
        if (congTyThanhVienRepository.existsByCongTy_IdCongTyAndNguoiDung_Email(congTyId, invitedEmail)) {
            throw new BadRequestException("Người dùng này đã là thành viên của công ty");
        }
        
        // 4. Kiểm tra xem đã có lời mời PENDING chưa
        if (congTyLoiMoiRepository.existsByCongTy_IdCongTyAndEmailAndTrangThai(congTyId, invitedEmail, InvitationStatus.PENDING)) {
            throw new BadRequestException("Lời mời đã được gửi trước đó và đang chờ chấp nhận");
        }

        // 5. Tạo lời mời
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(3); // Lời mời hết hạn sau 3 ngày

        CongTyLoiMoi loiMoi = CongTyLoiMoi.builder()
                .congTy(congTy)
                .email(invitedEmail)
                .role(role)
                .nguoiMoi(admin)
                .token(token)
                .trangThai(InvitationStatus.PENDING)
                .ngayHetHan(expiryDate)
                .build();
        
        congTyLoiMoiRepository.save(loiMoi);

        // 6. Gửi Email (Logic giữ nguyên)
        String acceptUrl = frontendUrl + "/accept-invitation?token=" + token;
        String emailBody = String.format(
            "Chào bạn,<br><br>%s đã mời bạn tham gia công ty %s với vai trò %s.<br>" +
            "Vui lòng click vào <a href=\"%s\">đây</a> để chấp nhận lời mời.<br><br>" +
            "Link sẽ hết hạn sau 3 ngày.",
            admin.getHoTen(), congTy.getTenCongTy(), role.getTenRole(), acceptUrl
        );

        emailService.sendEmail(invitedEmail, "Lời mời tham gia công ty " + congTy.getTenCongTy(), emailBody);
    }


    // LOGIC XAC THUC TOKEN LOI MOI
    @Override
    @Transactional
    public void acceptInvitation(AcceptInvitationRequest request) {
        // 1. Xác thực token lời mời (SỬ DỤNG SERVICE CHUNG)
        CongTyLoiMoi loiMoi = invitationService.validateInvitationToken(request.getInvitationToken());
        
        // 2. Lấy người dùng đang đăng nhập
        NguoiDung currentUser = getCurrentAuthenticatedUser();

        // 3. Kiểm tra xem lời mời này có đúng là dành cho người đang đăng nhập không
        if (!currentUser.getEmail().equals(loiMoi.getEmail())) {
            throw new BadRequestException("Lời mời này dành cho một tài khoản email khác.");
        }
        
        // 4. Kiểm tra (lần nữa) xem họ đã là thành viên chưa
        if (congTyThanhVienRepository.existsByCongTy_IdCongTyAndNguoiDung_Email(
                loiMoi.getCongTy().getIdCongTy(), currentUser.getEmail())) {
            throw new BadRequestException("Bạn đã là thành viên của công ty này");
        }

        // 5. Thêm thành viên vào công ty (SỬ DỤNG SERVICE CHUNG)
        invitationService.addMemberToCompany(currentUser, loiMoi.getCongTy(), loiMoi.getRole());
        
        // 6. Cập nhật lời mời
        loiMoi.setTrangThai(InvitationStatus.ACCEPTED);
        congTyLoiMoiRepository.save(loiMoi);
    }
}