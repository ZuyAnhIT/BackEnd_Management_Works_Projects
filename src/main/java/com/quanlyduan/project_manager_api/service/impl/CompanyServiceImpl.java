// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/CompanyServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.exception.AccessDeniedException; // Note: This import was unused in the original
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.common.enums.CombinedMemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.service.CompanyService;
import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
// import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus; // Duplicate import
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.repository.*;
// import com.quanlyduan.project_manager_api.service.CompanyService; // Duplicate import
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.InvitationService;

import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository; // Đã dịch
    private final CompanyMemberRepository companyMemberRepository; // Đã dịch
    private final UserRepository userRepository; // Đã dịch
    private final RoleRepository roleRepository;

    // // Định nghĩa mã role mặc định cho người tạo công ty
    // private static final String COMPANY_ADMIN_ROLE_CODE = "COMPANY_ADMIN";

    private final CompanyInvitationRepository companyInvitationRepository; // Đã dịch
    private final EmailService emailService;

    private final InvitationService invitationService;

    @Value("${app.frontend.url}") // Thêm URL frontend vào application.properties
    private String frontendUrl;

    // LOGIC TAO CONG TY
    @Override
    @Transactional
    public Company createCompany(CreateCompanyRequest request) { // Đã dịch
        // 1. Lấy người dùng đang đăng nhập (người tạo)
        User creator = getCurrentAuthenticatedUser(); // Đã dịch

        // 2. Kiểm tra tên công ty đã tồn tại chưa
        if (companyRepository.existsByName(request.getCompanyName())) { // Đã dịch
            throw new BadRequestException("This company name already exists"); // Đã dịch
        }

        // 3. Tìm Role "COMPANY_ADMIN" trong CSDL
        Role adminRole = roleRepository.findFirstByRoleCode(RoleCode.COMPANY_ADMIN.name()) // SỬ DỤNG ENUM // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + RoleCode.COMPANY_ADMIN.name() + ". Please configure the database." // Đã
                                                                                                                // dịch
                ));

        // 4. Tạo công ty mới
        Company newCompany = Company.builder() // Đã dịch
                .name(request.getCompanyName()) // Đã dịch
                .description(request.getDescription()) // Đã dịch
                .address(request.getAddress()) // Đã dịch
                .phoneNumber(request.getPhoneNumber()) // Đã dịch
                .email(request.getEmail())
                .website(request.getWebsite())
                .createdById(creator.getId()) // Đã dịch
                .status(CompanyStatus.ACTIVE) // Đã dịch
                .build();

        Company savedCompany = companyRepository.save(newCompany); // Đã dịch

        // 5. Thêm người tạo làm thành viên đầu tiên với vai trò Admin
        CompanyMember membership = CompanyMember.builder() // Đã dịch
                .company(savedCompany) // Đã dịch
                .user(creator) // Đã dịch
                .role(adminRole)
                .status(MemberStatus.ACTIVE) // Đã dịch
                .build();

        companyMemberRepository.save(membership); // Đã dịch

        return savedCompany;
    }

    // --- Private Helper Method ---
    // (Helper này lấy từ UserServiceImpl, bạn có thể tách ra 1 class Util chung)
    private User getCurrentAuthenticatedUser() { // Đã dịch
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadRequestException("Authenticated user information not found."); // Đã dịch
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email) // Đã dịch
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email)); // Đã dịch
    }

    // LOGIC TAO LOI MOI THANH VIEN VAO CONG TY
    @Override
    @Transactional
    public void inviteMember(Integer companyId, InviteMemberRequest request) { // Đã dịch

        // 1. Lấy thông tin
        User admin = getCurrentAuthenticatedUser(); // Đã dịch
        Company company = companyRepository.findById(companyId) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Company not found")); // Đã dịch

        // *** SỬA LOGIC: Tìm Role bằng roleCode (từ DTO) thay vì roleId ***
        Role role = roleRepository.findFirstByRoleCode(request.getRoleCode())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found for code: " + request.getRoleCode())); // Đã
                                                                                                                        // dịch

        // 2. Validate
        if (role.getLevel() != RoleLevel.COMPANY) { // Đã dịch
            throw new BadRequestException("Invalid role (Not a COMPANY level role)"); // Đã dịch
        }

        String invitedEmail = request.getEmail();
        if (admin.getEmail().equals(invitedEmail)) {
            throw new BadRequestException("You cannot invite yourself"); // Đã dịch
        }

        // 3. Kiểm tra xem đã là thành viên chưa
        if (companyMemberRepository.existsByCompany_IdAndUser_Email(companyId, invitedEmail)) { // Đã dịch
            throw new BadRequestException("This user is already a member of the company"); // Đã dịch
        }

        // 4. Kiểm tra xem đã có lời mời PENDING chưa
        if (companyInvitationRepository.existsByCompany_IdAndEmailAndStatus(companyId, invitedEmail,
                InvitationStatus.PENDING)) { // Đã dịch
            throw new BadRequestException("An invitation has already been sent and is pending"); // Đã dịch
        }

        // 5. Tạo lời mời
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(3); // Lời mời hết hạn sau 3 ngày

        CompanyInvitation invitation = CompanyInvitation.builder() // Đã dịch
                .company(company) // Đã dịch
                .email(invitedEmail)
                .role(role)
                .invitedBy(admin) // Đã dịch
                .token(token)
                .status(InvitationStatus.PENDING) // Đã dịch
                .expiresAt(expiryDate) // Đã dịch
                .build();

        companyInvitationRepository.save(invitation); // Đã dịch

        // 6. Gửi Email (Logic giữ nguyên)
        String acceptUrl = frontendUrl + "/accept-invitation?token=" + token;
        String emailBody = String.format(
                "Hello,<br><br>%s has invited you to join the company %s with the role %s.<br>" + // Đã dịch
                        "Please click <a href=\"%s\">here</a> to accept the invitation.<br><br>" + // Đã dịch
                        "This link will expire in 3 days.", // Đã dịch
                admin.getFullName(), company.getName(), role.getRoleName(), acceptUrl // Đã dịch
        );

        emailService.sendEmail(invitedEmail, "Invitation to join " + company.getName(), emailBody); // Đã dịch
    }

    // LOGIC XAC THUC TOKEN LOI MOI
    @Override
    @Transactional
    public void acceptInvitation(AcceptInvitationRequest request) {
        // 1. Xác thực token lời mời (SỬ DỤNG SERVICE CHUNG)
        CompanyInvitation invitation = invitationService.validateInvitationToken(request.getInvitationToken()); // Đã
                                                                                                                // dịch

        // 2. Lấy người dùng đang đăng nhập
        User currentUser = getCurrentAuthenticatedUser(); // Đã dịch

        // 3. Kiểm tra xem lời mời này có đúng là dành cho người đang đăng nhập không
        if (!currentUser.getEmail().equals(invitation.getEmail())) {
            throw new BadRequestException("This invitation is for a different email account."); // Đã dịch
        }

        // 4. Kiểm tra (lần nữa) xem họ đã là thành viên chưa
        if (companyMemberRepository.existsByCompany_IdAndUser_Email( // Đã dịch
                invitation.getCompany().getId(), currentUser.getEmail())) { // Đã dịch
            throw new BadRequestException("You are already a member of this company"); // Đã dịch
        }

        // 5. Thêm thành viên vào công ty (SỬ DỤNG SERVICE CHUNG)
        invitationService.addMemberToCompany(currentUser, invitation.getCompany(), invitation.getRole()); // Đã dịch

        // 6. Cập nhật lời mời
        invitation.setStatus(InvitationStatus.ACCEPTED); // Đã dịch
        companyInvitationRepository.save(invitation); // Đã dịch
    }

    // LOGIC XEM DANH SACH THANH VIEN TRONG CONG TY
    @Override
    @Transactional(readOnly = true) // Dùng readOnly=true cho các hàm GET
    public List<CompanyMemberResponse> getCompanyMembers(Integer companyId) { // Đã dịch

        // Bỏ check quyền thủ
        // // 1. Lấy thông tin người dùng hiện tại
        // NguoiDung currentUser = getCurrentAuthenticatedUser();

        // // 2. KIỂM TRA BẢO MẬT: Người dùng có phải là thành viên của công ty này
        // không?
        // // (Chúng ta sẽ nâng cấp lên @PreAuthorize sau, nhưng đây là logic cơ bản)
        // boolean isMember = congTyThanhVienRepository
        // .existsByCongTy_IdCongTyAndNguoiDung_IdNguoiDung(congTyId,
        // currentUser.getIdNguoiDung());

        // if (!isMember) {
        // throw new AccessDeniedException("Bạn không có quyền xem danh sách thành viên
        // của công ty này");
        // }

        // 3. Tạo danh sách trả về
        List<CompanyMemberResponse> responseList = new ArrayList<>();

        // 4. Lấy danh sách thành viên (Active/Inactive)
        List<CompanyMember> members = companyMemberRepository.findByCompany_Id(companyId); // Đã dịch

        for (CompanyMember member : members) { // Đã dịch
            CompanyMemberResponse dto = CompanyMemberResponse.builder()
                    .memberId(member.getId()) // *** BỔ SUNG YÊU CẦU ***
                    .userId(member.getUser().getId()) // Đã dịch
                    .fullName(member.getUser().getFullName()) // Đã dịch
                    .email(member.getUser().getEmail())
                    .avatarUrl(member.getUser().getAvatarUrl()) // Đã dịch
                    .roleName(member.getRole().getRoleName()) // Đã dịch
                    .jobTitle(member.getJobTitle()) // Đã dịch
                    .joinedAt(member.getJoinedAt()) // Đã dịch
                    .status(mapMemberStatus(member.getStatus())) // Helper map status // Đã dịch
                    .build();
            responseList.add(dto);
        }

        // 5. Lấy danh sách lời mời (Pending)
        List<CompanyInvitation> invitations = companyInvitationRepository // Đã dịch
                .findByCompany_IdAndStatus(companyId, InvitationStatus.PENDING); // Đã dịch

        for (CompanyInvitation invitation : invitations) { // Đã dịch
            CompanyMemberResponse dto = CompanyMemberResponse.builder()
                    .memberId(null) // *** BỔ SUNG YÊU CẦU *** (null vì đây là lời mời)
                    .userId(null) // Chưa có user
                    .fullName("Pending...") // Hoặc (loiMoi.getEmail()) // Đã dịch
                    .email(invitation.getEmail()) // Đã dịch
                    .avatarUrl(null) // Đã dịch
                    .roleName(invitation.getRole().getRoleName()) // Role được mời // Đã dịch
                    .jobTitle(null) // Đã dịch
                    .joinedAt(invitation.getCreatedAt()) // Ngày mời // Đã dịch
                    .status(CombinedMemberStatus.PENDING)
                    .build();
            responseList.add(dto);
        }
        // 6. Trả về danh sách tổng hợp
        return responseList;
    }
    // --- Private Helper Method ---

    private CombinedMemberStatus mapMemberStatus(MemberStatus status) {
        if (status == MemberStatus.ACTIVE) { // Đã dịch
            return CombinedMemberStatus.ACTIVE;
        }
        return CombinedMemberStatus.INACTIVE; // Gộp TAM_DUNG và DA_ROI thành INACTIVE
    }

    // LOGIC LAY THONG TIN CHI TIET CONG TY
    @Override
    @Transactional(readOnly = true)
    public CompanyDetailsResponse getCompanyDetails(Integer companyId) { // Đã dịch

        // Bỏ check quyền thủ công
        // // 1. Lấy thông tin người dùng hiện tại
        // NguoiDung currentUser = getCurrentAuthenticatedUser();

        // // 2. KIỂM TRA BẢO MẬT: Người dùng có phải là thành viên của công ty này
        // không?
        // boolean isMember = congTyThanhVienRepository
        // .existsByCongTy_IdCongTyAndNguoiDung_IdNguoiDung(congTyId,
        // currentUser.getIdNguoiDung());

        // if (!isMember) {
        // throw new AccessDeniedException("Bạn không có quyền xem thông tin của công ty
        // này");
        // }

        // 3. Lấy thông tin công ty
        Company company = companyRepository.findById(companyId) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId)); // Đã dịch

        // 4. Map sang DTO và trả về
        return mapCompanyToDetailsDto(company); // Đã dịch
    }

    // --- Private Helper Methods ---

    // (Helper mapMemberStatus)

    // Helper mới để map CongTy sang DTO
    private CompanyDetailsResponse mapCompanyToDetailsDto(Company company) { // Đã dịch
        return CompanyDetailsResponse.builder()
                .companyId(company.getId()) // Đã dịch
                .companyName(company.getName()) // Đã dịch
                .companyCode(company.getCompanyCode()) // Đã dịch
                .description(company.getDescription()) // Đã dịch
                .logo(company.getLogoUrl()) // Đã dịch
                .address(company.getAddress()) // Đã dịch
                .phoneNumber(company.getPhoneNumber()) // Đã dịch
                .email(company.getEmail())
                .website(company.getWebsite())
                .createdById(company.getCreatedById()) // Đã dịch
                .build();
    }

    // LOGIC CAP NHAT THONG TIN CONG TY
    @Override
    @Transactional
    public CompanyDetailsResponse updateCompany(Integer companyId, UpdateCompanyRequest request) { // Đã dịch

        // 1. Lấy công ty
        Company company = companyRepository.findById(companyId) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId)); // Đã dịch

        // 2. Kiểm tra nghiệp vụ (ví dụ: tên công ty mới nếu có)
        if (request.getCompanyName() != null && !request.getCompanyName().equals(company.getName())) { // Đã dịch
            if (companyRepository.existsByName(request.getCompanyName())) { // Đã dịch
                throw new BadRequestException("This company name already exists"); // Đã dịch
            }
            company.setName(request.getCompanyName()); // Đã dịch
        }

        // 3. Cập nhật các trường (nếu chúng không null)
        if (request.getDescription() != null) { // Đã dịch
            company.setDescription(request.getDescription()); // Đã dịch
        }
        if (request.getLogo() != null) {
            company.setLogoUrl(request.getLogo()); // Đã dịch
        }
        if (request.getAddress() != null) { // Đã dịch
            company.setAddress(request.getAddress()); // Đã dịch
        }
        if (request.getPhoneNumber() != null) { // Đã dịch
            company.setPhoneNumber(request.getPhoneNumber()); // Đã dịch
        }
        if (request.getEmail() != null) {
            company.setEmail(request.getEmail());
        }
        if (request.getWebsite() != null) {
            company.setWebsite(request.getWebsite());
        }

        // 4. Lưu và trả về
        Company updatedCompany = companyRepository.save(company); // Đã dịch
        return mapCompanyToDetailsDto(updatedCompany); // Đã dịch
    }

    // LOGIC PHAN QUYEN THANH VIEN CONG TY
    @Override
    @Transactional
    public CompanyMember updateCompanyMemberRole(Integer companyId, Integer memberId, String newRoleCode) {

        // 1. Tìm vai trò mới (cấp COMPANY)
        Role newRole = roleRepository.findByRoleCodeAndLevel(newRoleCode, RoleLevel.COMPANY)
                .orElseThrow(() -> new BadRequestException("Invalid or non-company role code: " + newRoleCode));

        // 2. Tìm thành viên
        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Company member not found with ID: " + memberId));

        // 3. Kiểm tra xem thành viên này có thuộc đúng công ty không
        if (!member.getCompany().getId().equals(companyId)) {
            // Ném lỗi 403 Forbidden
            throw new AccessDeniedException("This member does not belong to this company");
        }

        // 4. *** THÊM BƯỚC KIỂM TRA MỚI TẠI ĐÂY ***
        // Kiểm tra vai trò hiện tại của thành viên
        if (RoleCode.COMPANY_ADMIN.name().equals(member.getRole().getRoleCode())) {
            throw new BadRequestException("Cannot update the role of a COMPANY_ADMIN.");
        }
        // 5. Cập nhật vai trò
        member.setRole(newRole);
        // 6. Lưu và trả về
        return companyMemberRepository.save(member);
    }

    // LOGIC XOA MEM THANH VIEN
    @Override
    @Transactional
    public void removeMemberFromCompany(Integer companyId, Integer userId) {
        // 1. Kiểm tra xem có tự xóa chính mình không
        User admin = getCurrentAuthenticatedUser();
        if (admin.getId().equals(userId)) {
            throw new BadRequestException("You cannot remove yourself from the company."); // Đã dịch
        }

        // 2. Tìm thành viên (kể cả inactive) để xóa
        CompanyMember member = companyMemberRepository.findByCompany_IdAndUser_Id(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this company")); // Đã dịch

        // 3. Kiểm tra xem họ đã bị xóa chưa
        if (member.getStatus() == MemberStatus.REMOVED) {
            throw new BadRequestException("This member has already been removed."); // Đã dịch
        }

        // 4. Thực hiện xóa mềm
        member.setStatus(MemberStatus.REMOVED); // Đã dịch
        companyMemberRepository.save(member);

        // 5. (Nâng cao) Tự động xóa họ khỏi TẤT CẢ Workspace và Project thuộc công ty
        // này
        // (Chúng ta sẽ thêm logic này sau, hiện tại chỉ xóa khỏi công ty)
    }

    // LOGIC XEM CHI TIET THANH VIEN
    @Override
    @Transactional(readOnly = true)
    public CompanyMemberResponse getCompanyMemberDetails(Integer companyId, Integer memberId) {
        // Bảo mật đã được xử lý ở Controller (@PreAuthorize)
        
        // 1. Tìm thành viên bằng ID
        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId)); // Đã dịch

        // 2. KIỂM TRA BẢO MẬT (IDOR): Đảm bảo thành viên này thuộc đúng công ty
        if (!member.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Member not found in this company"); // Đã dịch (Hoặc dùng AccessDeniedException)
        }

        // 3. Map và trả về
        return mapToCompanyMemberResponse(member);
    }

    // *** THÊM HÀM HELPER NÀY ***
    /**
     * Hàm helper (tách ra từ getCompanyMembers) để map CompanyMember sang DTO
     */
    private CompanyMemberResponse mapToCompanyMemberResponse(CompanyMember member) {
        return CompanyMemberResponse.builder()
            .memberId(member.getId()) // ID của bản ghi CompanyMember
            .userId(member.getUser().getId())
            .fullName(member.getUser().getFullName())
            .email(member.getUser().getEmail())
            .avatarUrl(member.getUser().getAvatarUrl())
            .roleName(member.getRole().getRoleName())
            .jobTitle(member.getJobTitle())
            .joinedAt(member.getJoinedAt())
            .status(mapMemberStatus(member.getStatus()))
            .build();
    }
}