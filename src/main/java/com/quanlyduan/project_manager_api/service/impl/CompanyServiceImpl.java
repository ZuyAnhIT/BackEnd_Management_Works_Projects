// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/CompanyServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.quanlyduan.project_manager_api.aop.ActivityLogContext;
import com.quanlyduan.project_manager_api.aop.LogActivity;
import com.quanlyduan.project_manager_api.dto.request.AcceptInvitationRequest;
import com.quanlyduan.project_manager_api.dto.request.CreateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.InviteMemberRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateCompanyRequest;
import com.quanlyduan.project_manager_api.dto.request.UpdateMemberStatusRequest;
import com.quanlyduan.project_manager_api.dto.response.CompanyDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyInvitationResponse;
import com.quanlyduan.project_manager_api.dto.response.CompanyMemberResponse;
import com.quanlyduan.project_manager_api.dto.response.InvitationDetailsResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.Company;
import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.CombinedMemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.specification.CompanyMemberSpecification;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.CompanyService;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.FileStorageService;
import com.quanlyduan.project_manager_api.service.InvitationService;
import com.quanlyduan.project_manager_api.util.SortUtils;

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
    private final FileStorageService fileStorageService;

    private final ProjectRepository projectRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public CompanyServiceImpl(CompanyRepository companyRepository,
                              CompanyMemberRepository companyMemberRepository,
                              UserRepository userRepository,
                              RoleRepository roleRepository,
                              CompanyInvitationRepository companyInvitationRepository,
                              EmailService emailService,
                              SecurityService securityService,
                              InvitationService invitationService,
                              ProjectRepository projectRepository,
                            FileStorageService fileStorageService) {
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyInvitationRepository = companyInvitationRepository;
        this.emailService = emailService;
        this.securityService = securityService;
        this.invitationService = invitationService;
        this.projectRepository = projectRepository;
        this.fileStorageService = fileStorageService;
    }

    // =================================================================================
    // 🏢 LOGIC TẠO CÔNG TY (CREATE COMPANY)
    // =================================================================================
    @Override
    @Transactional
    @LogActivity(action = "CREATE", entityType = "COMPANY", description = "Create new Company") 
    public Company createCompany(CreateCompanyRequest request) {
        // 1. Lấy người dùng đang đăng nhập (người tạo)
        User creator = getCurrentAuthenticatedUser();

        // 2. Kiểm tra tên công ty đã tồn tại chưa
        if (companyRepository.existsByName(request.getCompanyName())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Company name already exists.");
        }

        // 3. Tìm Role "COMPANY_ADMIN" trong CSDL
        Role adminRole = roleRepository.findFirstByRoleCode(RoleCode.COMPANY_ADMIN.name()) // SỬ DỤNG ENUM
                .orElseThrow(() -> new ResourceNotFoundException(
                        // Sửa thông báo sang tiếng Anh
                        "Role not found: " + RoleCode.COMPANY_ADMIN.name() + ". Please configure in the database."
                ));

        // 4. Tạo công ty mới
        Company newCompany = Company.builder()
                .name(request.getCompanyName())
                .description(request.getDescription())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .website(request.getWebsite())
                .createdById(creator.getId())
                .status(CompanyStatus.ACTIVE)
                .build();

        Company savedCompany = companyRepository.save(newCompany);

        // 5. Thêm người tạo làm thành viên đầu tiên với vai trò Admin
        CompanyMember membership = CompanyMember.builder()
                .company(savedCompany)
                .user(creator)
                .role(adminRole)
                .status(MemberStatus.ACTIVE)
                .build();

        companyMemberRepository.save(membership);

        return savedCompany;
    }

    // =================================================================================
    // ✉️ LOGIC TẠO LỜI MỜI THÀNH VIÊN VÀO CÔNG TY (INVITE MEMBER)
    // =================================================================================
    @Override
    @Transactional
    @LogActivity(action = "INVITE", entityType = "COMPANY_MEMBER", description = "Invite member to Company") // <-- THÊM
    public CompanyInvitation inviteMember(Integer companyId, InviteMemberRequest request) {

        // 1. Lấy thông tin cần thiết: Admin (người mời) và Công ty
        User admin = getCurrentAuthenticatedUser();
        
        Company company = companyRepository.findById(companyId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        // Tìm Role bằng roleCode (từ DTO)
        Role role = roleRepository.findFirstByRoleCode(request.getRoleCode())
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Role not found for code: " + request.getRoleCode()));

        // 2. Validate
        // Kiểm tra xem vai trò có phải là vai trò cấp CÔNG TY không
        if (role.getLevel() != RoleLevel.COMPANY) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Invalid role (Not a COMPANY level role).");
        }

        String invitedEmail = request.getEmail();
        // Không cho phép tự mời chính mình
        if (admin.getEmail().equals(invitedEmail)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("You cannot invite yourself.");
        }

        // 3. Kiểm tra xem đã là thành viên chưa
        if (companyMemberRepository.existsByCompany_IdAndUser_Email(companyId, invitedEmail)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This user is already a member of the company.");
        }

        // 4. Kiểm tra xem đã có lời mời PENDING chưa
        if (companyInvitationRepository.existsByCompany_IdAndEmailAndStatus(companyId, invitedEmail,
                InvitationStatus.PENDING)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("An invitation has already been sent and is awaiting response.");
        }

        // 5. Tạo lời mời (Token, Ngày hết hạn)
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(3); // Lời mời hết hạn sau 3 ngày

        CompanyInvitation invitation = CompanyInvitation.builder()
                .company(company)
                .email(invitedEmail)
                .role(role)
                .invitedBy(admin)
                .token(token)
                .status(InvitationStatus.PENDING)
                .expiresAt(expiryDate)
                .build();

        CompanyInvitation companyInvitation = companyInvitationRepository.save(invitation);
        
        // 6. Gửi Email (Nội dung email giữ nguyên tiếng Việt như logic cũ)
        String acceptUrl = frontendUrl + "/accept-invitation?token=" + token;
        String emailBody = String.format(
            "Xin chào,<br><br>%s đã mời bạn tham gia công ty %s với vai trò %s.<br>" +
            "Vui lòng nhấp vào <a href=\"%s\">đây</a> để chấp nhận lời mời.<br><br>" +
            "Liên kết này sẽ hết hạn sau 3 ngày.",
            admin.getFullName(), company.getName(), role.getRoleName(), acceptUrl
        );

        emailService.sendEmail(invitedEmail, "Lời mời tham gia " + company.getName(), emailBody);
        return companyInvitation;
    }

    // =================================================================================
    // ✅ LOGIC CHẤP NHẬN LỜI MỜI (ACCEPT INVITATION)
    // =================================================================================
    @Override
    @Transactional
    @LogActivity(action = "JOIN", entityType = "COMPANY_MEMBER", description = "Accept company invitation")
    public CompanyDetailsResponse acceptInvitation(AcceptInvitationRequest request) {
        // 1. Xác thực token lời mời (SỬ DỤNG SERVICE CHUNG)
        // Hàm này đã xử lý các lỗi Token hết hạn/không tồn tại
        CompanyInvitation invitation = invitationService.validateInvitationToken(request.getInvitationToken());

        // 2. Lấy người dùng đang đăng nhập
        User currentUser = getCurrentAuthenticatedUser();

        // 3. Kiểm tra xem lời mời này có đúng là dành cho người đang đăng nhập không
        if (!currentUser.getEmail().equals(invitation.getEmail())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This invitation is for a different email account.");
        }

        // 4. Kiểm tra (lần nữa) xem họ đã là thành viên chưa
        if (companyMemberRepository.existsByCompany_IdAndUser_Email(
                invitation.getCompany().getId(), currentUser.getEmail())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("You are already a member of this company.");
        }

        // 5. Thêm thành viên vào công ty (SỬ DỤNG SERVICE CHUNG)
        invitationService.addMemberToCompany(currentUser, invitation.getCompany(), invitation.getRole());

        // 6. Cập nhật lời mời
        invitation.setStatus(InvitationStatus.ACCEPTED);
        companyInvitationRepository.save(invitation);

        String welcomeMsg = String.format("has joined the company <strong>%s</strong> as <strong>%s</strong> 🎉", 
                invitation.getCompany().getName(), 
                invitation.getRole().getRoleName());
        
        ActivityLogContext.setDetail(welcomeMsg);
        return mapCompanyToDetailsDto(invitation.getCompany());
    }

    // =================================================================================
    // 👤 LOGIC XEM CHI TIẾT THÀNH VIÊN (GET MEMBER DETAILS)
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public CompanyMemberResponse getCompanyMemberDetails(Integer companyId, Integer memberId) {
        // Bảo mật đã được xử lý ở Controller (@PreAuthorize)

        // 1. Tìm thành viên bằng ID
        CompanyMember member = companyMemberRepository.findById(memberId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        // 2. KIỂM TRA BẢO MẬT (IDOR): Đảm bảo thành viên này thuộc đúng công ty
        if (!member.getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("Member not found in this company.");
        }

        // 3. Map và trả về
        return mapToCompanyMemberResponse(member);
    }

    // =================================================================================
    // 🔄 LOGIC CẬP NHẬT VAI TRÒ THÀNH VIÊN CẤP CÔNG TY (UPDATE MEMBER ROLE)
    // =================================================================================
    @Override
    @Transactional
    @LogActivity(action = "UPDATE_ROLE", entityType = "COMPANY_MEMBER", description = "Update member role")
    public CompanyMember updateCompanyMemberRole(Integer companyId, Integer memberId, String newRoleCode) {
        // 1. Lấy thông tin thành viên
        CompanyMember member = companyMemberRepository.findById(memberId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        // 2. Kiểm tra bảo mật (IDOR): Đảm bảo thành viên thuộc công ty đang thao tác
        if (!member.getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("Member not found in this company.");
        }

        // 3. Không cho phép đổi vai trò của chính mình
        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("You cannot change your own role.");
        }

        // ⭐ 4. Không thể cập nhật vai trò nếu đã REMOVED
        if (member.getStatus() == MemberStatus.REMOVED) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Cannot update role because this member has been REMOVED.");
        }

        // 5. Tìm vai trò mới
        Role newRole = roleRepository.findFirstByRoleCode(newRoleCode)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with code: " + newRoleCode));

        // 6. Vai trò phải là cấp công ty
        if (newRole.getLevel() != RoleLevel.COMPANY) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Invalid role (Not a COMPANY level role).");
        }

        // ⭐ 7. Không cho phép cập nhật nếu TRÙNG vai trò
        if (member.getRole().getId().equals(newRole.getId())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("The new role is the same as the current role — nothing to update.");
        }
        
        // 8. Cập nhật vai trò
        member.setRole(newRole);
        return companyMemberRepository.save(member);
    }

    // =================================================================================
    // ⏸️ LOGIC CẬP NHẬT TRẠNG THÁI THÀNH VIÊN (UPDATE MEMBER STATUS)
    // =================================================================================
    @Override
    @Transactional
    public CompanyMemberResponse updateMemberStatus(Integer companyId, Integer memberId, UpdateMemberStatusRequest request) {
        // 1. Lấy thông tin thành viên
        CompanyMember member = companyMemberRepository.findById(memberId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        // 2. Kiểm tra bảo mật (IDOR)
        if (!member.getCompany().getId().equals(companyId)) {
            // Sửa thông báo sang tiếng Anh
            throw new ResourceNotFoundException("Member not found in this company.");
        }

        // 3. Không cho phép đổi status của chính mình
        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("You cannot change your own status.");
        }

        MemberStatus newStatus = request.getNewStatus();

        // ⭐ 4. Không cho phép cập nhật nếu TRÙNG trạng thái
        if (member.getStatus() == newStatus) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("New status is the same as the current status — nothing to update.");
        }

        // ⭐ 5. Không cho phép thay đổi nếu trạng thái hiện tại đã là REMOVED
        if (member.getStatus() == MemberStatus.REMOVED) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Cannot change status because this member has been REMOVED.");
        }

        // 6. Không được dùng endpoint này để set REMOVED (chỉ dùng cho ACTIVE/SUSPENDED)
        if (newStatus == MemberStatus.REMOVED) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Please use the 'Remove Member' endpoint to remove a member.");
        }

        // 7. Cập nhật trạng thái
        member.setStatus(newStatus);
        CompanyMember updatedMember = companyMemberRepository.save(member);

        return mapToCompanyMemberResponse(updatedMember);
    }

    // =================================================================================
    // ❌ LOGIC XÓA MỀM THÀNH VIÊN (REMOVE MEMBER)
    // =================================================================================
    @Override
    @Transactional
    @LogActivity(action = "REMOVE", entityType = "COMPANY_MEMBER", description = "Remove member from Company")
    public void removeMemberFromCompany(Integer companyId, Integer userId) {
        // 1. Kiểm tra xem có tự xóa chính mình không
        User admin = getCurrentAuthenticatedUser();
        if (admin.getId().equals(userId)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("You cannot remove yourself from the company.");
        }

        // 2. Tìm thành viên (kể cả inactive) để xóa
        CompanyMember member = companyMemberRepository.findByCompany_IdAndUser_Id(companyId, userId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this company."));

        // 3. Kiểm tra xem họ đã bị xóa chưa
        if (member.getStatus() == MemberStatus.REMOVED) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This member has already been removed.");
        }

        // 4. Thực hiện xóa mềm
        member.setStatus(MemberStatus.REMOVED); // Đặt trạng thái là REMOVED
        companyMemberRepository.save(member);

        // 5. (Nâng cao) Tự động xóa họ khỏi TẤT CẢ Workspace và Project thuộc công ty này
        // (Logic này chưa triển khai)
    }

    // =================================================================================
    // 📝 LOGIC LẤY CHI TIẾT CÔNG TY (GET COMPANY DETAILS)
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public CompanyDetailsResponse getCompanyDetails(Integer companyId) {

        // 1. Lấy thông tin công ty
        Company company = companyRepository.findById(companyId)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        // 2. Map sang DTO và trả về
        return mapCompanyToDetailsDto(company);
    }

    // =================================================================================
    // ✏️ LOGIC CẬP NHẬT THÔNG TIN CÔNG TY (UPDATE COMPANY)
    // =================================================================================
    @Override
    @Transactional
    @LogActivity(action = "UPDATE", entityType = "COMPANY", description = "Update Company Info")
    public CompanyDetailsResponse updateCompany(Integer companyId, UpdateCompanyRequest request, MultipartFile logoFile) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        StringBuilder changes = new StringBuilder();

        // 1. Name
        if (request.getCompanyName() != null && !request.getCompanyName().equals(company.getName())) {
             if (companyRepository.existsByName(request.getCompanyName())) {
                  throw new BadRequestException("Company name already exists.");
             }
             if (changes.length() > 0) changes.append(", ");
             changes.append(String.format("renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"", company.getName(), request.getCompanyName()));
             company.setName(request.getCompanyName());
        }

        // 2. Description
        if (request.getDescription() != null && !request.getDescription().equals(company.getDescription())) {
             if (changes.length() > 0) changes.append(", ");
             changes.append("updated description");
             company.setDescription(request.getDescription());
        }

        // 3. Other fields
        if (request.getAddress() != null && !request.getAddress().equals(company.getAddress())) {
             company.setAddress(request.getAddress());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(company.getPhoneNumber())) {
             company.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null && !request.getEmail().equals(company.getEmail())) {
             company.setEmail(request.getEmail());
        }
        if (request.getWebsite() != null && !request.getWebsite().equals(company.getWebsite())) {
             company.setWebsite(request.getWebsite());
        }

        // 4. Logo
        if (logoFile != null && !logoFile.isEmpty()) {
            String logoPath = fileStorageService.storeFile(logoFile, "company-logos");
            if (changes.length() > 0) changes.append(", ");
            changes.append("updated logo");
            company.setLogoUrl(logoPath);
        } else if (request.getLogo() != null && !request.getLogo().equals(company.getLogoUrl())) {
            company.setLogoUrl(request.getLogo());
        }

        // Set log
        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        } else {
            //  ActivityLogContext.setDetail("updated company info");
        }

        Company savedCompany = companyRepository.save(company);
        return mapCompanyToDetailsDto(savedCompany);
    }

    // =================================================================================
    // 🔎 LOGIC LẤY DANH SÁCH THÀNH VIÊN CƠ BẢN (LIST MEMBERS)
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CompanyMemberResponse> getCompanyMembers(Integer companyId, int page, int size, String sortBy, String sortDir) {

        // 1. Cấu hình Map ánh xạ cho việc sắp xếp
        Map<String, String> sortMapping = Map.of(
            "joinedAt", "joinedAt",          // Ngày tham gia (Mặc định)
            "name", "user.fullName",         // Tên người dùng
            "email", "user.email",           // Email
            "role", "role.roleName",         // Tên vai trò
            "jobTitle", "jobTitle",          // Chức vụ
            "phone", "user.phoneNumber"      // Số điện thoại
        );

        // 2. Tạo Pageable (Sử dụng hàm helper chung)
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", sortMapping);

        // 3. Gọi Repository lấy dữ liệu phân trang
        Page<CompanyMember> membersPage = companyMemberRepository.findByCompany_Id(companyId, pageable);

        // 4. Map sang DTO
        Page<CompanyMemberResponse> dtoPage = membersPage.map(this::mapToCompanyMemberResponse);

        // 5. Trả về
        return new PageResponseDTO<>(dtoPage);
    }

    // =================================================================================
    // 🔍 LOGIC TÌM KIẾM NÂNG CAO THÀNH VIÊN (SEARCH MEMBERS)
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CompanyMemberResponse> searchCompanyMembers(
            Integer companyId,
            String searchName, String searchEmail, String searchJobTitle, String searchRoleName, MemberStatus searchStatus, String searchPhone,
            int page, int size, String sortBy, String sortDir) {

        // 1. Cấu hình Map ánh xạ (Giống hàm trên)
        Map<String, String> sortMapping = Map.of(
            "joinedAt", "joinedAt",
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "jobTitle", "jobTitle",
            "phone", "user.phoneNumber"
        );

        // 2. Tạo Pageable
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "joinedAt", sortMapping);

        // 3. Tạo Specification (Bộ lọc động)
        Specification<CompanyMember> spec = CompanyMemberSpecification.filterMembers(
            companyId, searchName, searchEmail, searchJobTitle, searchRoleName, searchStatus, searchPhone
        );

        // 4. Gọi Repository với Specification
        Page<CompanyMember> membersPage = companyMemberRepository.findAll(spec, pageable);

        // 5. Map sang DTO
        Page<CompanyMemberResponse> dtoPage = membersPage.map(this::mapToCompanyMemberResponse);

        // 6. Trả về
        return new PageResponseDTO<>(dtoPage);
    }

    // =================================================================================
    // ⏳ LOGIC LẤY DANH SÁCH LỜI MỜI (CÓ LỌC & TÌM KIẾM) - ĐÃ NÂNG CẤP
    // =================================================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CompanyInvitationResponse> getCompanyInvitations(
            Integer companyId, 
            String keyword, 
            String statusStr, 
            int page, int size, String sortBy, String sortDir
    ) {
        // 1. Cấu hình Map ánh xạ cho việc sắp xếp
        Map<String, String> sortMapping = Map.of(
            "createdAt", "createdAt",       
            "email", "email",              
            "role", "role.roleName",        
            "expiresAt", "expiresAt"        
        );

        // 2. Tạo Pageable
        Pageable pageable = createPageable(page, size, sortBy, sortDir, "createdAt", sortMapping);

        // 3. Xử lý bộ lọc
        // - Keyword: Nếu null thì gán rỗng để tìm tất cả
        String searchKeyword = (keyword != null) ? keyword.trim() : "";
        
        // - Status: Mặc định là PENDING nếu không truyền hoặc truyền sai
        InvitationStatus status = InvitationStatus.PENDING;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = InvitationStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Nếu status sai, giữ mặc định PENDING
            }
        }

        // 4. Gọi Repository lấy dữ liệu
        Page<CompanyInvitation> invitationPage = companyInvitationRepository
                .findByCompany_IdAndStatusAndEmailContainingIgnoreCase(companyId, status, searchKeyword, pageable);

        // 5. Map sang DTO (Dùng hàm helper để code gọn hơn)
        Page<CompanyInvitationResponse> dtoPage = invitationPage.map(this::mapToCompanyInvitationResponse);

        // 6. Trả về kết quả
        return new PageResponseDTO<>(dtoPage);
    }


    // =================================================================================
    // 🔗 LOGIC LẤY CHI TIẾT LỜI MỜI (PUBLIC) - Dùng cho trang chấp nhận
    // =================================================================================
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

    // =================================================================================
    // ❌ HỦY LỜI MỜI CÔNG TY (CANCEL INVITATION)
    // =================================================================================
    @Override
    @Transactional
    public void cancelCompanyInvitation(Integer companyId, Integer invitationId) {
        // 1. Tìm lời mời
        CompanyInvitation invitation = companyInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with ID: " + invitationId));

        // 2. Validate: Lời mời phải thuộc đúng Company này
        if (!invitation.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Invitation does not belong to the specified company.");
        }

        // 3. Xóa (Xóa cứng vì lời mời chưa được dùng coi như rác)
        companyInvitationRepository.delete(invitation);
    }


    // =================================================================================
    // ⚙️ PRIVATE HELPER METHODS
    // =================================================================================

    /**
     * Helper: Lấy thông tin người dùng đang đăng nhập.
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Authenticated user information not found.");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email:" + email));
    }

    /**
     * Helper: Tạo Pageable chung cho các hàm Listing/Searching.
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDir, String defaultSortField, Map<String, String> sortMapping) {
        Sort sort = SortUtils.createSort(sortBy, sortDir, defaultSortField, sortMapping);
        return PageRequest.of(page, size, sort);
    }

    /**
     * Helper: Map trạng thái thành viên (MemberStatus) sang CombinedMemberStatus (DTO).
     */
    private CombinedMemberStatus mapMemberStatus(MemberStatus status) {
        switch (status) {
            case ACTIVE: return CombinedMemberStatus.ACTIVE;
            case SUSPENDED: return CombinedMemberStatus.SUSPENDED;
            case REMOVED: return CombinedMemberStatus.REMOVED;
            default: return CombinedMemberStatus.REMOVED;
        }
    }

    /**
     * Helper: Map CompanyMember Entity sang CompanyMemberResponse DTO.
     */
    private CompanyMemberResponse mapToCompanyMemberResponse(CompanyMember member) {
        return CompanyMemberResponse.builder()
            .memberId(member.getId())
            .userId(member.getUser().getId())
            .fullName(member.getUser().getFullName())
            .email(member.getUser().getEmail())
            .phoneNumber(member.getUser().getPhoneNumber()) // Bổ sung SĐT
            .avatarUrl(member.getUser().getAvatarUrl())
            .roleName(member.getRole().getRoleName())
            .jobTitle(member.getJobTitle())
            .joinedAt(member.getJoinedAt())
            .status(mapMemberStatus(member.getStatus()))
            .build();
    }

    /**
     * Helper: Map Company Entity sang CompanyDetailsResponse DTO.
     */
    private CompanyDetailsResponse mapCompanyToDetailsDto(Company company) {
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

     // --- HELPER METHOD: Chuyển đổi Entity sang DTO ---
    private CompanyInvitationResponse mapToCompanyInvitationResponse(CompanyInvitation inv) {
        String link = frontendUrl + "/accept-invitation?token=" + inv.getToken();
        
        return CompanyInvitationResponse.builder()
                .id(inv.getId())
                .email(inv.getEmail())
                // Null-safe check cho Role
                .roleName(inv.getRole() != null ? inv.getRole().getRoleName() : "UNKNOWN")
                // Null-safe check cho Người mời
                .invitedByName(inv.getInvitedBy() != null ? inv.getInvitedBy().getFullName() : "System")
                .status(inv.getStatus().name())
                .expiresAt(inv.getExpiresAt())
                .invitationLink(link)
                .build();
    }
}