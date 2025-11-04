package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateWorkspaceRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteWorkspaceMemberRequest;
import com.quanlyduan.project_manager_api.dto.response.WorkspaceResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.SecurityService; 
import com.quanlyduan.project_manager_api.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; 
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final KhongGianRepository khongGianRepository;
    private final KhongGianThanhVienRepository khongGianThanhVienRepository;
    private final CongTyRepository congTyRepository;
    private final RoleRepository roleRepository;
    private final SecurityService securityService; 
    private final NguoiDungRepository nguoiDungRepository;
    private final CongTyThanhVienRepository congTyThanhVienRepository;

    private final EmailService emailService;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    // API TAO KHONG GIAN 
    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(Integer congTyId, CreateWorkspaceRequest request) {
        
        // 1. Lấy thông tin người dùng và công ty
        NguoiDung creator = securityService.getCurrentAuthenticatedUser();
        CongTy congTy = congTyRepository.findById(congTyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty"));

        // 2. Kiểm tra nghiệp vụ (tên trùng)
        if (khongGianRepository.existsByCongTy_IdCongTyAndTenKhongGian(congTyId, request.getTenKhongGian())) {
            throw new BadRequestException("Tên không gian đã tồn tại trong công ty này");
        }

        // 3. Tìm Role "WORKSPACE_ADMIN"
        Role workspaceAdminRole = roleRepository.findFirstByMaRole(RoleCode.WORKSPACE_ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy Role: " + RoleCode.WORKSPACE_ADMIN.name() + ". Vui lòng cấu hình CSDL."
                ));

        // 4. Tạo không gian mới
        KhongGian newWorkspace = KhongGian.builder()
                .congTy(congTy)
                .tenKhongGian(request.getTenKhongGian())
                .moTa(request.getMoTa())
                .anhBia(request.getAnhBia())
                .mauSac(request.getMauSac() != null ? request.getMauSac() : "#3498db")
                .nguoiTao(creator)
                .trangThai(WorkspaceStatus.HOAT_DONG)
                .build();
        
        KhongGian savedWorkspace = khongGianRepository.save(newWorkspace);

        // 5. Tự động gán người tạo làm Admin của không gian
        KhongGianThanhVien membership = KhongGianThanhVien.builder()
                .khongGian(savedWorkspace)
                .nguoiDung(creator)
                .role(workspaceAdminRole)
                .trangThai(MemberStatus.HOAT_DONG)
                .build();
        
        khongGianThanhVienRepository.save(membership);

        // 6. Map Entity sang DTO và trả về
        return mapToWorkspaceResponse(savedWorkspace);
    }


    // LOGIC HIỂN THỊ DANH SÁCH KHÔNG GIAN TRONG CÔNG TY
    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesByCompany(Integer congTyId) {
        // 1. Lấy danh sách Entity từ CSDL
        // (Bảo mật sẽ được xử lý ở tầng Controller bằng @PreAuthorize)
        List<KhongGian> workspaces = khongGianRepository.findByCongTy_IdCongTy(congTyId);

        // 2. Chuyển đổi (map) danh sách Entity sang danh sách DTO
        return workspaces.stream()
                .map(this::mapToWorkspaceResponse) // Tái sử dụng helper đã tạo
                .collect(Collectors.toList());
    }

    // LOGIC XEM CHI TIET PHONG BAN
    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceDetails(Integer workspaceId) {
        // Bảo mật đã được xử lý bởi @PreAuthorize ở tầng Controller.
        // Tầng service chỉ cần thực hiện logic tìm kiếm.
        
        KhongGian workspace = khongGianRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc với ID: " + workspaceId));
                
        // Tái sử dụng helper đã tạo
        return mapToWorkspaceResponse(workspace);
    }


    // LOGIC MOI THANH VIEN VAO PHONG BAN
    @Override
    @Transactional
    public void inviteMemberToWorkspace(Integer congTyId, Integer khongGianId, InviteWorkspaceMemberRequest request) {
        
        // *** THÊM DÒNG NÀY *** (Lấy admin hiện tại để biết ai là người mời)
        NguoiDung admin = securityService.getCurrentAuthenticatedUser();
        String emailToInvite = request.getEmail();

        // 1. Lấy thông tin người dùng được mời
        NguoiDung userToInvite = nguoiDungRepository.findByEmail(emailToInvite)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy người dùng với email: " + emailToInvite
                ));

        // 2. KIỂM TRA ĐIỀU KIỆN (như bạn yêu cầu)
        boolean isCompanyMember = congTyThanhVienRepository
            .existsByCongTy_IdCongTyAndNguoiDung_IdNguoiDung(congTyId, userToInvite.getIdNguoiDung());
            
        if (!isCompanyMember) {
            throw new BadRequestException(
                "Người này chưa thuộc Công ty. Vui lòng liên hệ Admin Công ty để mời vào trước."
            );
        }

        // 3. Lấy thông tin Workspace và Role
        KhongGian khongGian = khongGianRepository.findById(khongGianId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy không gian làm việc"));

        Role workspaceRole = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Role"));

        // 4. Validate Role
        if (workspaceRole.getCapDo() != RoleLevel.WORKSPACE) {
            throw new BadRequestException("Role không hợp lệ (Không phải cấp độ Không gian làm việc)");
        }
        
        // 5. Kiểm tra xem đã là thành viên của Workspace chưa
        Optional<KhongGianThanhVien> existingMembership = khongGianThanhVienRepository
            .findByKhongGian_IdKhongGianAndNguoiDung_IdNguoiDung(khongGianId, userToInvite.getIdNguoiDung());

        if (existingMembership.isPresent()) {
            throw new BadRequestException("Người dùng này đã là thành viên của không gian làm việc");
        }

        // 6. Thêm thành viên vào không gian
        KhongGianThanhVien newMembership = KhongGianThanhVien.builder()
                .khongGian(khongGian)
                .nguoiDung(userToInvite)
                .role(workspaceRole)
                .trangThai(MemberStatus.HOAT_DONG)
                .build();
        
        khongGianThanhVienRepository.save(newMembership);
        
        // *** LOGIC GỬI EMAIL ***
        sendWorkspaceNotificationEmail(admin, userToInvite, khongGian, workspaceRole);
    }

    // *** HÀM HELPER  ***
    /**
     * Gửi email thông báo cho người dùng khi họ được thêm vào không gian làm việc.
     */
    private void sendWorkspaceNotificationEmail(NguoiDung admin, NguoiDung userAdded, KhongGian khongGian, Role role) {
        try {
            // Tạo link chi tiết
            String workspaceUrl = String.format("%s/companies/%d/workspaces/%d", 
                frontendUrl, 
                khongGian.getCongTy().getIdCongTy(), 
                khongGian.getIdKhongGian());

            String emailBody = String.format(
                "<p>Chào %s,</p>" +
                "<p>Bạn vừa được %s thêm vào không gian làm việc <strong>%s</strong>.</p>" +
                "<ul>" +
                "<li><strong>Vai trò của bạn:</strong> %s</li>" +
                "<li><strong>Công ty:</strong> %s</li>" +
                "</ul>" +
                "<p>Bạn có thể truy cập không gian làm việc ngay bây giờ bằng cách nhấp vào <a href=\"%s\">liên kết này</a>.</p>" +
                "<p>Cảm ơn,<br>Đội ngũ Project Manager</p>",
                userAdded.getHoTen(),
                admin.getHoTen(),
                khongGian.getTenKhongGian(),
                role.getTenRole(),
                khongGian.getCongTy().getTenCongTy(),
                workspaceUrl
            );

            emailService.sendEmail(
                userAdded.getEmail(), 
                String.format("Bạn đã được thêm vào không gian: %s", khongGian.getTenKhongGian()), 
                emailBody
            );

        } catch (Exception e) {
            // (Nên log lỗi này ra)
            System.err.println("Lỗi khi gửi email thông báo thêm vào workspace: " + e.getMessage());
            // Không ném lỗi ra ngoài để không làm hỏng giao dịch chính
        }
    }


    /**
     * Hàm helper để chuyển đổi Entity KhongGian sang WorkspaceResponse DTO.
     * @param kg Entity KhongGian
     * @return WorkspaceResponse DTO
     */
    private WorkspaceResponse mapToWorkspaceResponse(KhongGian kg) {
        return WorkspaceResponse.builder()
                .idKhongGian(kg.getIdKhongGian())
                .congTyId(kg.getCongTy().getIdCongTy()) // Lấy ID an toàn
                .tenKhongGian(kg.getTenKhongGian())
                .moTa(kg.getMoTa())
                .anhBia(kg.getAnhBia())
                .mauSac(kg.getMauSac())
                .nguoiTaoId(kg.getNguoiTao().getIdNguoiDung()) // Lấy ID an toàn
                .trangThai(kg.getTrangThai().name()) // Trả về tên Enum (String)
                .ngayTao(kg.getNgayTao())
                .build();
    }
}