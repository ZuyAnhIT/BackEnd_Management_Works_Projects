// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/CompanyServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
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
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.InvitationDetailsResponse;
import com.quanlyduan.project_manager_api.model.*;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.repository.*;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.InvitationService;

import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository; 
    private final CompanyMemberRepository companyMemberRepository; 
    private final UserRepository userRepository; 
    private final RoleRepository roleRepository;
    private final CompanyInvitationRepository companyInvitationRepository; 
    private final EmailService emailService;
    private final SecurityService securityService;
    private final InvitationService invitationService;
    
    private final ProjectRepository projectRepository;

    @Value("${app.frontend.url}") // Thêm URL frontend vào application.properties
    private String frontendUrl;

    public CompanyServiceImpl(CompanyRepository companyRepository, 
                              CompanyMemberRepository companyMemberRepository, 
                              UserRepository userRepository, 
                              RoleRepository roleRepository, 
                              CompanyInvitationRepository companyInvitationRepository, 
                              EmailService emailService, 
                              SecurityService securityService, 
                              InvitationService invitationService, 
                              ProjectRepository projectRepository) {
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyInvitationRepository = companyInvitationRepository;
        this.emailService = emailService;
        this.securityService = securityService;
        this.invitationService = invitationService;
        this.projectRepository = projectRepository;
    }

    // LOGIC TAO CONG TY
    @Override
    @Transactional
    public Company createCompany(CreateCompanyRequest request) { // Đã dịch
        // 1. Lấy người dùng đang đăng nhập (người tạo)
        User creator = getCurrentAuthenticatedUser(); // Đã dịch

        // 2. Kiểm tra tên công ty đã tồn tại chưa
        if (companyRepository.existsByName(request.getCompanyName())) { // Đã dịch
            throw new BadRequestException("Tên công ty này đã tồn tại"); // Đã dịch
        }

        // 3. Tìm Role "COMPANY_ADMIN" trong CSDL
        Role adminRole = roleRepository.findFirstByRoleCode(RoleCode.COMPANY_ADMIN.name()) // SỬ DỤNG ENUM
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy vai trò: " + RoleCode.COMPANY_ADMIN.name() + ". Vui lòng cấu hình trong cơ sở dữ liệu."
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
            throw new BadRequestException("Không tìm thấy thông tin người dùng đã xác thực."); // Đã dịch
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email) // Đã dịch
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email:" + email)); // Đã dịch
    }

    // LOGIC TAO LOI MOI THANH VIEN VAO CONG TY
    @Override
    @Transactional
    public void inviteMember(Integer companyId, InviteMemberRequest request) { // Đã dịch

        // 1. Lấy thông tin
        User admin = getCurrentAuthenticatedUser(); // Đã dịch
        Company company = companyRepository.findById(companyId) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty")); // Đã dịch

        // *** SỬA LOGIC: Tìm Role bằng roleCode (từ DTO) thay vì roleId ***
        Role role = roleRepository.findFirstByRoleCode(request.getRoleCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò cho mã: " + request.getRoleCode())); // Đã
                                                                                                                        // dịch

        // 2. Validate
        if (role.getLevel() != RoleLevel.COMPANY) { // Đã dịch
            throw new BadRequestException("Vai trò không hợp lệ (Không phải vai trò cấp CÔNG TY)"); // Đã dịch
        }

        String invitedEmail = request.getEmail();
        if (admin.getEmail().equals(invitedEmail)) {
            throw new BadRequestException("Bạn không thể tự mời mình"); // Đã dịch
        }

        // 3. Kiểm tra xem đã là thành viên chưa
        if (companyMemberRepository.existsByCompany_IdAndUser_Email(companyId, invitedEmail)) { // Đã dịch
            throw new BadRequestException("Người dùng này đã là thành viên của công ty"); // Đã dịch
        }

        // 4. Kiểm tra xem đã có lời mời PENDING chưa
        if (companyInvitationRepository.existsByCompany_IdAndEmailAndStatus(companyId, invitedEmail,
                InvitationStatus.PENDING)) { // Đã dịch
            throw new BadRequestException("Một lời mời đã được gửi và đang chờ phản hồi"); // Đã dịch
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
            "Xin chào,<br><br>%s đã mời bạn tham gia công ty %s với vai trò %s.<br>" +
            "Vui lòng nhấp vào <a href=\"%s\">đây</a> để chấp nhận lời mời.<br><br>" +
            "Liên kết này sẽ hết hạn sau 3 ngày.",
            admin.getFullName(), company.getName(), role.getRoleName(), acceptUrl
        );

        emailService.sendEmail(invitedEmail, "Lời mời tham gia " + company.getName(), emailBody);
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
            throw new BadRequestException("Lời mời này dành cho một tài khoản email khác."); // Đã dịch
        }

        // 4. Kiểm tra (lần nữa) xem họ đã là thành viên chưa
        if (companyMemberRepository.existsByCompany_IdAndUser_Email( // Đã dịch
                invitation.getCompany().getId(), currentUser.getEmail())) { // Đã dịch
            throw new BadRequestException("Bạn đã là thành viên của công ty này"); // Đã dịch
        }

        // 5. Thêm thành viên vào công ty (SỬ DỤNG SERVICE CHUNG)
        invitationService.addMemberToCompany(currentUser, invitation.getCompany(), invitation.getRole()); // Đã dịch

        // 6. Cập nhật lời mời
        invitation.setStatus(InvitationStatus.ACCEPTED); // Đã dịch
        companyInvitationRepository.save(invitation); // Đã dịch
    }

    // LOGIC XEM DANH SACH THANH VIEN TRONG CONG TY
    // LOGIC XEM DANH SACH THANH VIEN TRONG CONG TY
    @Override
    @Transactional(readOnly = true) 
    public List<CompanyMemberResponse> getCompanyMembers(Integer companyId) { 

        // 3. Tạo danh sách trả về
        List<CompanyMemberResponse> responseList = new ArrayList<>();

        // 4. Lấy danh sách thành viên (Active/Inactive)
        List<CompanyMember> members = companyMemberRepository.findByCompany_Id(companyId); 
        
        for (CompanyMember member : members) { 
            CompanyMemberResponse dto = CompanyMemberResponse.builder()
                    .memberId(member.getId()) 
                    .userId(member.getUser().getId()) 
                    .fullName(member.getUser().getFullName()) 
                    .email(member.getUser().getEmail())
                    .avatarUrl(member.getUser().getAvatarUrl()) 
                    .roleName(member.getRole().getRoleName()) 
                    .jobTitle(member.getJobTitle()) 
                    .joinedAt(member.getJoinedAt()) 
                    .status(mapMemberStatus(member.getStatus())) // *** SỬA LOGIC Ở HÀM HELPER NÀY ***
                    .build();
            responseList.add(dto);
        }

        // 5. Lấy danh sách lời mời (Pending)
        List<CompanyInvitation> invitations = companyInvitationRepository 
                .findByCompany_IdAndStatus(companyId, InvitationStatus.PENDING); 

        for (CompanyInvitation invitation : invitations) { 
            CompanyMemberResponse dto = CompanyMemberResponse.builder()
                    .memberId(null) 
                    .userId(null) 
                    .fullName("Đang chờ...") 
                    .email(invitation.getEmail()) 
                    .avatarUrl(null) 
                    .roleName(invitation.getRole().getRoleName()) 
                    .jobTitle(null) 
                    .joinedAt(invitation.getCreatedAt()) 
                    .status(CombinedMemberStatus.PENDING) // Trạng thái PENDING giữ nguyên
                    .build();
            responseList.add(dto);
        }
        // 6. Trả về danh sách tổng hợp
        return responseList;
    }
    
    // --- Private Helper Method ---
    
    // *** ĐÃ SỬA LẠI LOGIC HÀM NÀY ***
    private CombinedMemberStatus mapMemberStatus(MemberStatus status) {
        // Chuyển đổi trực tiếp từ MemberStatus (ACTIVE, SUSPENDED, REMOVED)
        // sang CombinedMemberStatus (ACTIVE, SUSPENDED, REMOVED)
        switch (status) {
            case ACTIVE:
                return CombinedMemberStatus.ACTIVE;
            case SUSPENDED:
                return CombinedMemberStatus.SUSPENDED;
            case REMOVED:
                return CombinedMemberStatus.REMOVED;
            default:
                // Xử lý dự phòng, mặc dù không bao giờ nên xảy ra
                return CombinedMemberStatus.REMOVED; 
        }
    }

    // LOGIC LAY THONG TIN CHI TIET CONG TY
    @Override
    @Transactional(readOnly = true)
    public CompanyDetailsResponse getCompanyDetails(Integer companyId) { 


        // 3. Lấy thông tin công ty
        Company company = companyRepository.findById(companyId) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty với ID: " + companyId)); // Đã dịch

        // 4. Map sang DTO và trả về
        return mapCompanyToDetailsDto(company); // Đã dịch
    }

    // --- Private Helper Methods ---

    // (Helper mapMemberStatus)

    // Helper mới để map CongTy sang DTO
    private CompanyDetailsResponse mapCompanyToDetailsDto(Company company) { // Đã dịch
        return CompanyDetailsResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .companyCode(company.getCompanyCode())
                .description(company.getDescription())
                .logo(company.getLogoUrl())
                .address(company.getAddress())
                .phoneNumber(company.getPhoneNumber())
                .email(company.getEmail())
                .website(company.getWebsite())
                .build();
    }

    // LOGIC CAP NHAT THONG TIN CONG TY
    @Override
    @Transactional
    public CompanyDetailsResponse updateCompany(Integer companyId, UpdateCompanyRequest request) { // Đã dịch

        // 1. Lấy công ty
        Company company = companyRepository.findById(companyId) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty với ID: " + companyId)); // Đã dịch

        // 2. Kiểm tra nghiệp vụ (ví dụ: tên công ty mới nếu có)
        if (request.getCompanyName() != null && !request.getCompanyName().equals(company.getName())) { // Đã dịch
            if (companyRepository.existsByName(request.getCompanyName())) { // Đã dịch
                throw new BadRequestException("Tên công ty này đã tồn tại"); // Đã dịch
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

    // LOGIC CAP NHAT VAI TRO THANH VIEN CAP CONG TY
  @Override
@Transactional
public CompanyMember updateCompanyMemberRole(Integer companyId, Integer memberId, String newRoleCode) {
    // 1. Lấy thông tin thành viên
    CompanyMember member = companyMemberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên với ID: " + memberId));

    // 2. Kiểm tra bảo mật (IDOR)
    if (!member.getCompany().getId().equals(companyId)) {
        throw new ResourceNotFoundException("Không tìm thấy thành viên này trong công ty");
    }

    // 3. Không cho phép đổi vai trò của chính mình
    User admin = securityService.getCurrentAuthenticatedUser();
    if (admin.getId().equals(member.getUser().getId())) {
        throw new BadRequestException("Bạn không thể thay đổi vai trò của chính mình.");
    }

    // ⭐ 4. Không thể cập nhật vai trò nếu đã REMOVED
    if (member.getStatus() == MemberStatus.REMOVED) {
        throw new BadRequestException("Không thể cập nhật vai trò vì thành viên này đã bị REMOVED.");
    }

    // 5. Tìm vai trò mới
    Role newRole = roleRepository.findFirstByRoleCode(newRoleCode)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với mã: " + newRoleCode));

    // 6. Vai trò phải là cấp công ty
    if (newRole.getLevel() != RoleLevel.COMPANY) {
        throw new BadRequestException("Vai trò không hợp lệ (Không phải vai trò cấp CÔNG TY)");
    }

    // ⭐ 7. Không cho phép cập nhật nếu TRÙNG vai trò
    if (member.getRole().getId().equals(newRole.getId())) {
        throw new BadRequestException("Vai trò mới trùng với vai trò hiện tại — không có gì để cập nhật.");
    }

    // 8. Cập nhật vai trò
    member.setRole(newRole);
    return companyMemberRepository.save(member);
}


    // LOGIC XOA MEM THANH VIEN
    @Override
    @Transactional
    public void removeMemberFromCompany(Integer companyId, Integer userId) {
        // 1. Kiểm tra xem có tự xóa chính mình không
        User admin = getCurrentAuthenticatedUser();
        if (admin.getId().equals(userId)) {
            throw new BadRequestException("Bạn không thể tự rút khỏi công ty."); // Đã dịch
        }

        // 2. Tìm thành viên (kể cả inactive) để xóa
        CompanyMember member = companyMemberRepository.findByCompany_IdAndUser_Id(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên trong công ty này")); // Đã dịch

        // 3. Kiểm tra xem họ đã bị xóa chưa
        if (member.getStatus() == MemberStatus.REMOVED) {
            throw new BadRequestException("Thành viên này đã bị xóa."); // Đã dịch
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên với ID: " + memberId)); // Đã dịch

        // 2. KIỂM TRA BẢO MẬT (IDOR): Đảm bảo thành viên này thuộc đúng công ty
        if (!member.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("Không tìm thấy thành viên trong công ty này"); // Đã dịch (Hoặc dùng AccessDeniedException)
        }

        // 3. Map và trả về
        return mapToCompanyMemberResponse(member);
    }

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


    // LOGIC CAP NHAT TRANG THAI THANH VIEN
    @Override
@Transactional
public CompanyMemberResponse updateMemberStatus(Integer companyId, Integer memberId, UpdateMemberStatusRequest request) {
    // 1. Lấy thông tin thành viên
    CompanyMember member = companyMemberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thành viên với ID: " + memberId));

    // 2. Kiểm tra bảo mật (IDOR)
    if (!member.getCompany().getId().equals(companyId)) {
        throw new ResourceNotFoundException("Không tìm thấy thành viên trong công ty này");
    }

    // 3. Không cho phép đổi status của chính mình
    User admin = securityService.getCurrentAuthenticatedUser();
    if (admin.getId().equals(member.getUser().getId())) {
        throw new BadRequestException("Bạn không thể thay đổi trạng thái của mình.");
    }

    MemberStatus newStatus = request.getNewStatus();
    // ⭐ 5. Không cho phép thay đổi nếu trạng thái hiện tại đã là REMOVED
    if (member.getStatus() == MemberStatus.REMOVED) {
        throw new BadRequestException("Không thể thay đổi trạng thái vì thành viên này đã bị REMOVED.");
    }
    // ⭐ 4. Không cho phép cập nhật nếu TRÙNG trạng thái
    if (member.getStatus() == newStatus) {
        throw new BadRequestException("Trạng thái mới trùng với trạng thái hiện tại — không có gì để cập nhật.");
    }

    

    // 6. Không được dùng endpoint này để set REMOVED
    if (newStatus == MemberStatus.REMOVED) {
        throw new BadRequestException("Vui lòng sử dụng endpoint 'Remove Member' để loại bỏ thành viên.");
    }

    // 7. Cập nhật trạng thái
    member.setStatus(newStatus);
    CompanyMember updatedMember = companyMemberRepository.save(member);

    return mapToCompanyMemberResponse(updatedMember);
}


    // LOGIC LAY CHI TIET LOI MOI (PUBLIC)
    @Override
    @Transactional(readOnly = true)
    public InvitationDetailsResponse getInvitationDetails(String token) {
        // 1. Xác thực token (tái sử dụng logic từ InvitationService)
        // Hàm này sẽ tự động ném lỗi 404 hoặc 400 nếu token sai/hết hạn
        CompanyInvitation invitation = invitationService.validateInvitationToken(token);

        // 2. Lấy thông tin
        String email = invitation.getEmail();
        String companyName = invitation.getCompany().getName();

        // 3. Kiểm tra user có tồn tại không (Mấu chốt)
        boolean accountExists = userRepository.existsByEmail(email);

        // 4. Trả về DTO cho frontend
        return InvitationDetailsResponse.builder()
                .email(email)
                .companyName(companyName)
                .accountExists(accountExists)
                .build();
    }
}
