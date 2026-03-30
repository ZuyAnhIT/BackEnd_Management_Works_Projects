package com.quanlyduan.project_manager_api.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import com.quanlyduan.project_manager_api.model.CompanySubscription;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.SubscriptionPlan;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.CombinedMemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.RoleCode;
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import com.quanlyduan.project_manager_api.model.common.enums.SubscriptionStatus;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository;
import com.quanlyduan.project_manager_api.repository.CompanyMemberRepository;
import com.quanlyduan.project_manager_api.repository.CompanyRepository;
import com.quanlyduan.project_manager_api.repository.CompanySubscriptionRepository;
import com.quanlyduan.project_manager_api.repository.ProjectRepository;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.SubscriptionPlanRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.specification.CompanyMemberSpecification;
import com.quanlyduan.project_manager_api.security.SecurityService;
import com.quanlyduan.project_manager_api.service.CompanyService;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.FileStorageService;
import com.quanlyduan.project_manager_api.service.InvitationService;
import com.quanlyduan.project_manager_api.service.QuotaValidationService;
import com.quanlyduan.project_manager_api.util.SortUtils;

@Service
public class CompanyServiceImpl implements CompanyService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_INVITE = "INVITE";
    public static final String ACTION_JOIN = "JOIN";
    public static final String ACTION_UPDATE_ROLE = "UPDATE_ROLE";
    public static final String ACTION_REMOVE = "REMOVE";
    public static final String ACTION_UPDATE = "UPDATE";

    public static final String ENTITY_COMPANY = "COMPANY";
    public static final String ENTITY_COMPANY_MEMBER = "COMPANY_MEMBER";

    public static final String DESC_CREATE_COMPANY = "Create new Company with 14-days PRO Trial";
    public static final String DESC_INVITE_MEMBER = "Invite new member to Company";
    public static final String DESC_ACCEPT_INVITE = "Accept company invitation";
    public static final String DESC_UPDATE_ROLE = "Update member role";
    public static final String DESC_REMOVE_MEMBER = "Remove member from Company";
    public static final String DESC_UPDATE_COMPANY = "Update Company Info";

    public static final String PLAN_PRO_CODE = "PRO";
    public static final int TRIAL_DAYS = 14;
    public static final int INVITATION_EXPIRE_DAYS = 3;

    public static final String ERROR_COMPANY_EXISTS = "Company name already exists. Please choose another name.";
    public static final String ERROR_ROLE_NOT_FOUND = "Role not found: ";
    public static final String ERROR_SYSTEM_PLAN_MISSING = "System Error: Subscription plan 'PRO' is missing.";
    public static final String ERROR_COMPANY_NOT_FOUND = "Company not found.";
    public static final String ERROR_INVALID_ROLE_LEVEL = "Invalid role configuration: Provided role is not a COMPANY level role.";
    public static final String ERROR_INVITE_SELF = "You cannot invite yourself to the company.";
    public static final String ERROR_ALREADY_MEMBER = "This user is already an active member of the company.";
    public static final String ERROR_PENDING_INVITATION_EXISTS = "An invitation has already been sent to this email and is awaiting response.";
    public static final String ERROR_WRONG_INVITATION_EMAIL = "This invitation is for a different email account.";
    public static final String ERROR_MEMBER_NOT_FOUND_ID = "Member not found with ID: ";
    public static final String ERROR_MEMBER_NOT_IN_COMPANY = "Member not found in this company.";
    public static final String ERROR_CHANGE_OWN_ROLE = "You cannot change your own role.";
    public static final String ERROR_ROLE_MEMBER_REMOVED = "Cannot update role because this member has been REMOVED.";
    public static final String ERROR_SAME_ROLE = "The new role is the same as the current role — nothing to update.";
    public static final String ERROR_CHANGE_OWN_STATUS = "You cannot change your own status.";
    public static final String ERROR_SAME_STATUS = "New status is the same as the current status — nothing to update.";
    public static final String ERROR_STATUS_MEMBER_REMOVED = "Cannot change status because this member has been REMOVED.";
    public static final String ERROR_USE_REMOVE_ENDPOINT = "Please use the 'Remove Member' endpoint to remove a member.";
    public static final String ERROR_REMOVE_SELF = "You cannot remove yourself from the company.";
    public static final String ERROR_ALREADY_REMOVED = "This member has already been removed.";
    public static final String ERROR_INVITATION_NOT_FOUND = "Invitation not found with ID: ";
    public static final String ERROR_INVITATION_WRONG_COMPANY = "Invitation does not belong to the specified company.";
    public static final String ERROR_AUTH_INFO_NOT_FOUND = "Authenticated user information not found.";
    public static final String ERROR_USER_NOT_FOUND_EMAIL = "User not found with email: ";

    public static final String DEFAULT_UNKNOWN = "UNKNOWN";
    public static final String DEFAULT_SYSTEM = "System";

    public static final String EMAIL_WELCOME_SUBJECT = "\uD83C\uDF89 Welcome to our system - Enjoy your 14-day trial of %s plan!";
    public static final String EMAIL_INVITE_SUBJECT = "Invitation to join workspace ";
    
    public static final String EMAIL_WELCOME_TEMPLATE = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">" +
            "  <h2 style=\"color: #2c3e50; text-align: center;\">Welcome to our system!</h2>" +
            "  <p>Hello <b>%s</b>,</p>" +
            "  <p>Congratulations on successfully creating the workspace <b>%s</b>. To help you get the best experience, we have automatically activated a <b>14-day free trial of the %s plan</b> for your company.</p>" +
            "  <div style=\"background-color: #f8f9fa; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0;\">" +
            "    <p style=\"margin: 0;\"><b>Current Plan:</b> %s</p>" +
            "    <p style=\"margin: 5px 0 0 0;\"><b>Trial Expiration:</b> Until %s</p>" +
            "  </div>" +
            "  <p>With the %s plan, you can experience all premium features without any limitations.</p>" +
            "  <p><i>Note: After the trial period ends, the system will automatically downgrade to the <b>FREE</b> plan if you do not upgrade. There will be no unexpected charges.</i></p>" +
            "  <div style=\"text-align: center; margin-top: 30px;\">" +
            "    <a href=\"%s/admin/dashboard\" style=\"background-color: #3498db; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;\">Get Started Now</a>" +
            "  </div>" +
            "  <hr style=\"border: none; border-top: 1px solid #e0e0e0; margin-top: 30px;\">" +
            "  <p style=\"font-size: 12px; color: #7f8c8d; text-align: center;\">If you have any questions, please reply directly to this email for support.</p>" +
            "</div>";

    public static final String EMAIL_INVITE_TEMPLATE = "Hello,<br><br><b>%s</b> has invited you to join the project management system at <b>%s</b> with the role of %s.<br><br>" +
            "Please click <a href=\"%s\" style=\"color: #3498db; font-weight: bold;\">HERE</a> to accept the invitation and set up your account.<br><br>" +
            "<i>Note: This secure link will expire in 3 days.</i>";

    public static final String LOG_JOINED_MSG = "has joined the company <strong>%s</strong> as <strong>%s</strong> \uD83C\uDF89";
    public static final String LOG_RENAMED_MSG = "renamed from \"<strong>%s</strong>\" to \"<strong>%s</strong>\"";
    public static final String LOG_DESC_UPDATED_MSG = "updated description";
    public static final String LOG_LOGO_UPDATED_MSG = "updated logo";

    public static final String SORT_JOINED_AT = "joinedAt";
    public static final String SORT_CREATED_AT = "createdAt";

    // Khai bao cac bien phu thuoc
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyInvitationRepository companyInvitationRepository;
    private final EmailService emailService;
    private final SecurityService securityService;
    private final InvitationService invitationService;
    private final FileStorageService fileStorageService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final QuotaValidationService quotaValidationService;
    private final ProjectRepository projectRepository;

    // Constructor khoi tao thu cong
    public CompanyServiceImpl(CompanyRepository companyRepository,
                              CompanyMemberRepository companyMemberRepository,
                              UserRepository userRepository,
                              RoleRepository roleRepository,
                              CompanyInvitationRepository companyInvitationRepository,
                              EmailService emailService,
                              SecurityService securityService,
                              InvitationService invitationService,
                              ProjectRepository projectRepository,
                              FileStorageService fileStorageService,
                              CompanySubscriptionRepository companySubscriptionRepository,
                              SubscriptionPlanRepository subscriptionPlanRepository,
                              QuotaValidationService quotaValidationService) {
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
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.companySubscriptionRepository = companySubscriptionRepository;
        this.quotaValidationService = quotaValidationService;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    // Logic tao cong ty va kich hoat dung thu 14 ngay
    @Override
    @Transactional
    @LogActivity(action = ACTION_CREATE, entityType = ENTITY_COMPANY, description = DESC_CREATE_COMPANY) 
    public Company createCompany(CreateCompanyRequest request) {
        // Lay thong tin nguoi dang thao tac
        User creator = getCurrentAuthenticatedUser();

        // Kiem tra du lieu dau vao
        if (companyRepository.existsByName(request.getCompanyName())) {
            throw new BadRequestException(ERROR_COMPANY_EXISTS);
        }

        // Chuan bi du lieu he thong
        Role adminRole = roleRepository.findFirstByRoleCode(RoleCode.COMPANY_ADMIN.name())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_NOT_FOUND + RoleCode.COMPANY_ADMIN.name()));

        // Lay goi PRO de cho dung thu
        SubscriptionPlan trialPlan = subscriptionPlanRepository.findByPlanCode(PLAN_PRO_CODE)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_SYSTEM_PLAN_MISSING));

        // Tao ban ghi cong ty
        Company newCompany = Company.builder()
                .name(request.getCompanyName())
                .description(request.getDescription())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .website(request.getWebsite())
                .currentStorageBytes(0L)
                .isVerifiedTenant(false)
                .createdById(creator.getId())
                .status(CompanyStatus.ACTIVE)
                .build();
        
        Company savedCompany = companyRepository.save(newCompany);

        // Cap phat goi cuoc dung thu
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime trialEndDate = now.plusDays(TRIAL_DAYS);

        CompanySubscription subscription = CompanySubscription.builder()
                .company(savedCompany)
                .plan(trialPlan)
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodStart(now)
                .currentPeriodEnd(trialEndDate)
                .cancelAtPeriodEnd(true) 
                .build();
                
        companySubscriptionRepository.save(subscription);

        // Cap quyen quan tri cho nguoi tao
        CompanyMember membership = CompanyMember.builder()
                .company(savedCompany)
                .user(creator)
                .role(adminRole)
                .status(MemberStatus.ACTIVE)
                .build();
                
        companyMemberRepository.save(membership);

        // Gui email chao mung (Bat dong bo)
        sendWelcomeAndTrialEmail(creator, savedCompany, trialPlan, trialEndDate);

        return savedCompany;
    }

    // Logic tao loi moi thanh vien cong ty kem kiem tra han muc
    @Override
    @Transactional
    @LogActivity(action = ACTION_INVITE, entityType = ENTITY_COMPANY_MEMBER, description = DESC_INVITE_MEMBER)
    public CompanyInvitation inviteMember(Integer companyId, InviteMemberRequest request) {
        User admin = getCurrentAuthenticatedUser();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        // Kiem tra han muc nguoi dung
        quotaValidationService.validateUserInvitationQuota(companyId);

        // Kiem tra vai tro va du lieu
        Role role = roleRepository.findFirstByRoleCode(request.getRoleCode())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_NOT_FOUND + request.getRoleCode()));

        if (role.getLevel() != RoleLevel.COMPANY) {
            throw new BadRequestException(ERROR_INVALID_ROLE_LEVEL);
        }

        String invitedEmail = request.getEmail();
        if (admin.getEmail().equals(invitedEmail)) {
            throw new BadRequestException(ERROR_INVITE_SELF);
        }

        if (companyMemberRepository.existsByCompany_IdAndUser_Email(companyId, invitedEmail)) {
            throw new BadRequestException(ERROR_ALREADY_MEMBER);
        }

        if (companyInvitationRepository.existsByCompany_IdAndEmailAndStatus(companyId, invitedEmail, InvitationStatus.PENDING)) {
            throw new BadRequestException(ERROR_PENDING_INVITATION_EXISTS);
        }

        // Tao loi moi
        String token = UUID.randomUUID().toString();
        CompanyInvitation invitation = CompanyInvitation.builder()
                .company(company)
                .email(invitedEmail)
                .role(role)
                .invitedBy(admin)
                .token(token)
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRE_DAYS))
                .build();

        CompanyInvitation savedInvitation = companyInvitationRepository.save(invitation);
        
        // Gui email loi moi
        String acceptUrl = frontendUrl + "/accept-invitation?token=" + token;
        String emailBody = String.format(EMAIL_INVITE_TEMPLATE, admin.getFullName(), company.getName(), role.getRoleName(), acceptUrl);
        emailService.sendEmail(invitedEmail, EMAIL_INVITE_SUBJECT + company.getName(), emailBody);
        
        return savedInvitation;
    }

    // Logic chap nhan loi moi
    @Override
    @Transactional
    @LogActivity(action = ACTION_JOIN, entityType = ENTITY_COMPANY_MEMBER, description = DESC_ACCEPT_INVITE)
    public CompanyDetailsResponse acceptInvitation(AcceptInvitationRequest request) {
        // Xac thuc token loi moi thong qua service dung chung
        CompanyInvitation invitation = invitationService.validateInvitationToken(request.getInvitationToken());

        // Kiem tra nguoi dung dang nhap hien tai
        User currentUser = getCurrentAuthenticatedUser();

        if (!currentUser.getEmail().equals(invitation.getEmail())) {
            throw new BadRequestException(ERROR_WRONG_INVITATION_EMAIL);
        }

        if (companyMemberRepository.existsByCompany_IdAndUser_Email(invitation.getCompany().getId(), currentUser.getEmail())) {
            throw new BadRequestException(ERROR_ALREADY_MEMBER);
        }

        // Them thanh vien va cap nhat trang thai
        invitationService.addMemberToCompany(currentUser, invitation.getCompany(), invitation.getRole());
        invitation.setStatus(InvitationStatus.ACCEPTED);
        companyInvitationRepository.save(invitation);

        // Ghi nhat ky hoat dong
        String welcomeMsg = String.format(LOG_JOINED_MSG, invitation.getCompany().getName(), invitation.getRole().getRoleName());
        ActivityLogContext.setDetail(welcomeMsg);
        
        return mapCompanyToDetailsDto(invitation.getCompany());
    }

    // Logic xem chi tiet thanh vien
    @Override
    @Transactional(readOnly = true)
    public CompanyMemberResponse getCompanyMemberDetails(Integer companyId, Integer memberId) {
        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_MEMBER_NOT_FOUND_ID + memberId));

        if (!member.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(ERROR_MEMBER_NOT_IN_COMPANY);
        }

        return mapToCompanyMemberResponse(member);
    }

    // Logic cap nhat vai tro thanh vien cap cong ty
    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE_ROLE, entityType = ENTITY_COMPANY_MEMBER, description = DESC_UPDATE_ROLE)
    public CompanyMember updateCompanyMemberRole(Integer companyId, Integer memberId, String newRoleCode) {
        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_MEMBER_NOT_FOUND_ID + memberId));

        if (!member.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(ERROR_MEMBER_NOT_IN_COMPANY);
        }

        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            throw new BadRequestException(ERROR_CHANGE_OWN_ROLE);
        }

        if (member.getStatus() == MemberStatus.REMOVED) {
            throw new BadRequestException(ERROR_ROLE_MEMBER_REMOVED);
        }

        Role newRole = roleRepository.findFirstByRoleCode(newRoleCode)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_NOT_FOUND + newRoleCode));

        if (newRole.getLevel() != RoleLevel.COMPANY) {
            throw new BadRequestException(ERROR_INVALID_ROLE_LEVEL);
        }

        if (member.getRole().getId().equals(newRole.getId())) {
            throw new BadRequestException(ERROR_SAME_ROLE);
        }
        
        member.setRole(newRole);
        return companyMemberRepository.save(member);
    }

    // Logic cap nhat trang thai thanh vien
    @Override
    @Transactional
    public CompanyMemberResponse updateMemberStatus(Integer companyId, Integer memberId, UpdateMemberStatusRequest request) {
        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_MEMBER_NOT_FOUND_ID + memberId));

        if (!member.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(ERROR_MEMBER_NOT_IN_COMPANY);
        }

        User admin = securityService.getCurrentAuthenticatedUser();
        if (admin.getId().equals(member.getUser().getId())) {
            throw new BadRequestException(ERROR_CHANGE_OWN_STATUS);
        }

        MemberStatus newStatus = request.getNewStatus();

        if (member.getStatus() == newStatus) {
            throw new BadRequestException(ERROR_SAME_STATUS);
        }

        if (member.getStatus() == MemberStatus.REMOVED) {
            throw new BadRequestException(ERROR_STATUS_MEMBER_REMOVED);
        }

        if (newStatus == MemberStatus.REMOVED) {
            throw new BadRequestException(ERROR_USE_REMOVE_ENDPOINT);
        }

        member.setStatus(newStatus);
        CompanyMember updatedMember = companyMemberRepository.save(member);

        return mapToCompanyMemberResponse(updatedMember);
    }

    // Logic xoa mem thanh vien
    @Override
    @Transactional
    @LogActivity(action = ACTION_REMOVE, entityType = ENTITY_COMPANY_MEMBER, description = DESC_REMOVE_MEMBER)
    public void removeMemberFromCompany(Integer companyId, Integer userId) {
        User admin = getCurrentAuthenticatedUser();
        if (admin.getId().equals(userId)) {
            throw new BadRequestException(ERROR_REMOVE_SELF);
        }

        CompanyMember member = companyMemberRepository.findByCompany_IdAndUser_Id(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_MEMBER_NOT_IN_COMPANY));

        if (member.getStatus() == MemberStatus.REMOVED) {
            throw new BadRequestException(ERROR_ALREADY_REMOVED);
        }

        member.setStatus(MemberStatus.REMOVED);
        companyMemberRepository.save(member);
    }

    // Logic lay chi tiet cong ty
    @Override
    @Transactional(readOnly = true)
    public CompanyDetailsResponse getCompanyDetails(Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        return mapCompanyToDetailsDto(company);
    }

    // Logic cap nhat thong tin cong ty
    @Override
    @Transactional
    @LogActivity(action = ACTION_UPDATE, entityType = ENTITY_COMPANY, description = DESC_UPDATE_COMPANY)
    public CompanyDetailsResponse updateCompany(Integer companyId, UpdateCompanyRequest request, MultipartFile logoFile) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_COMPANY_NOT_FOUND));

        StringBuilder changes = new StringBuilder();

        // Cap nhat ten cong ty
        if (request.getCompanyName() != null && !request.getCompanyName().equals(company.getName())) {
             if (companyRepository.existsByName(request.getCompanyName())) {
                  throw new BadRequestException(ERROR_COMPANY_EXISTS);
             }
             if (changes.length() > 0) changes.append(", ");
             changes.append(String.format(LOG_RENAMED_MSG, company.getName(), request.getCompanyName()));
             company.setName(request.getCompanyName());
        }

        // Cap nhat mo ta
        if (request.getDescription() != null && !request.getDescription().equals(company.getDescription())) {
             if (changes.length() > 0) changes.append(", ");
             changes.append(LOG_DESC_UPDATED_MSG);
             company.setDescription(request.getDescription());
        }

        // Cap nhat cac truong khac
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

        // Cap nhat logo
        if (logoFile != null && !logoFile.isEmpty()) {
            String logoPath = fileStorageService.storeFile(logoFile, "company-logos");
            if (changes.length() > 0) changes.append(", ");
            changes.append(LOG_LOGO_UPDATED_MSG);
            company.setLogoUrl(logoPath);
        } else if (request.getLogo() != null && !request.getLogo().equals(company.getLogoUrl())) {
            company.setLogoUrl(request.getLogo());
        }

        if (changes.length() > 0) {
            ActivityLogContext.setDetail(changes.toString());
        }

        Company savedCompany = companyRepository.save(company);
        return mapCompanyToDetailsDto(savedCompany);
    }

    // Logic lay danh sach thanh vien
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CompanyMemberResponse> getCompanyMembers(Integer companyId, int page, int size, String sortBy, String sortDir) {
        Map<String, String> sortMapping = Map.of(
            SORT_JOINED_AT, SORT_JOINED_AT,
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "jobTitle", "jobTitle",
            "phone", "user.phoneNumber"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, SORT_JOINED_AT, sortMapping);
        Page<CompanyMember> membersPage = companyMemberRepository.findByCompany_Id(companyId, pageable);
        Page<CompanyMemberResponse> dtoPage = membersPage.map(this::mapToCompanyMemberResponse);

        return new PageResponseDTO<>(dtoPage);
    }

    // Logic tim kiem nang cao thanh vien
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CompanyMemberResponse> searchCompanyMembers(
            Integer companyId,
            String searchName, String searchEmail, String searchJobTitle, String searchRoleName, MemberStatus searchStatus, String searchPhone,
            int page, int size, String sortBy, String sortDir) {

        Map<String, String> sortMapping = Map.of(
            SORT_JOINED_AT, SORT_JOINED_AT,
            "name", "user.fullName",
            "email", "user.email",
            "role", "role.roleName",
            "jobTitle", "jobTitle",
            "phone", "user.phoneNumber"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, SORT_JOINED_AT, sortMapping);
        
        Specification<CompanyMember> spec = CompanyMemberSpecification.filterMembers(
            companyId, searchName, searchEmail, searchJobTitle, searchRoleName, searchStatus, searchPhone
        );

        Page<CompanyMember> membersPage = companyMemberRepository.findAll(spec, pageable);
        Page<CompanyMemberResponse> dtoPage = membersPage.map(this::mapToCompanyMemberResponse);

        return new PageResponseDTO<>(dtoPage);
    }

    // Logic lay danh sach loi moi hien tai
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CompanyInvitationResponse> getCompanyInvitations(
            Integer companyId, 
            String keyword, 
            String statusStr, 
            int page, int size, String sortBy, String sortDir
    ) {
        Map<String, String> sortMapping = Map.of(
            SORT_CREATED_AT, SORT_CREATED_AT,
            "email", "email",
            "role", "role.roleName",
            "expiresAt", "expiresAt"
        );

        Pageable pageable = createPageable(page, size, sortBy, sortDir, SORT_CREATED_AT, sortMapping);

        String searchKeyword = (keyword != null) ? keyword.trim() : "";
        InvitationStatus status = InvitationStatus.PENDING;
        
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = InvitationStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Giu nguyen mac dinh neu sai dinh dang
            }
        }

        Page<CompanyInvitation> invitationPage = companyInvitationRepository
                .findByCompany_IdAndStatusAndEmailContainingIgnoreCase(companyId, status, searchKeyword, pageable);

        Page<CompanyInvitationResponse> dtoPage = invitationPage.map(this::mapToCompanyInvitationResponse);

        return new PageResponseDTO<>(dtoPage);
    }

    // Logic lay chi tiet loi moi (Public)
    @Override
    @Transactional(readOnly = true)
    public InvitationDetailsResponse getInvitationDetails(String token) {
        CompanyInvitation invitation = invitationService.validateInvitationToken(token);

        String email = invitation.getEmail();
        String companyName = invitation.getCompany().getName();
        boolean accountExists = userRepository.existsByEmail(email);

        return InvitationDetailsResponse.builder()
                .email(email)
                .companyName(companyName)
                .accountExists(accountExists)
                .build();
    }

    // Logic huy loi moi cong ty
    @Override
    @Transactional
    public void cancelCompanyInvitation(Integer companyId, Integer invitationId) {
        CompanyInvitation invitation = companyInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVITATION_NOT_FOUND + invitationId));

        if (!invitation.getCompany().getId().equals(companyId)) {
            throw new BadRequestException(ERROR_INVITATION_WRONG_COMPANY);
        }

        companyInvitationRepository.delete(invitation);
    }

    // --- CAC HAM PRIVATE HO TRO NGHIEP VU ---

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadRequestException(ERROR_AUTH_INFO_NOT_FOUND);
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(ERROR_USER_NOT_FOUND_EMAIL + email));
    }

    private void sendWelcomeAndTrialEmail(User user, Company company, SubscriptionPlan plan, LocalDateTime trialEndDate) {
        String subject = String.format(EMAIL_WELCOME_SUBJECT, plan.getName());
        String formattedDate = trialEndDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        String emailBody = String.format(EMAIL_WELCOME_TEMPLATE,
            user.getFullName(), company.getName(), plan.getName(), plan.getName(), formattedDate, plan.getName(), frontendUrl
        );

        emailService.sendEmail(user.getEmail(), subject, emailBody);
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir, String defaultSortField, Map<String, String> sortMapping) {
        Sort sort = SortUtils.createSort(sortBy, sortDir, defaultSortField, sortMapping);
        return PageRequest.of(page, size, sort);
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private CombinedMemberStatus mapMemberStatus(MemberStatus status) {
        switch (status) {
            case ACTIVE: return CombinedMemberStatus.ACTIVE;
            case SUSPENDED: return CombinedMemberStatus.SUSPENDED;
            case REMOVED: return CombinedMemberStatus.REMOVED;
            default: return CombinedMemberStatus.REMOVED;
        }
    }

    private CompanyMemberResponse mapToCompanyMemberResponse(CompanyMember member) {
        return CompanyMemberResponse.builder()
            .memberId(member.getId())
            .userId(member.getUser().getId())
            .fullName(member.getUser().getFullName())
            .email(member.getUser().getEmail())
            .phoneNumber(member.getUser().getPhoneNumber())
            .avatarUrl(member.getUser().getAvatarUrl())
            .roleName(member.getRole().getRoleName())
            .jobTitle(member.getJobTitle())
            .joinedAt(member.getJoinedAt())
            .status(mapMemberStatus(member.getStatus()))
            .build();
    }

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

    private CompanyInvitationResponse mapToCompanyInvitationResponse(CompanyInvitation inv) {
        String link = frontendUrl + "/accept-invitation?token=" + inv.getToken();
        
        return CompanyInvitationResponse.builder()
                .id(inv.getId())
                .email(inv.getEmail())
                .roleName(inv.getRole() != null ? inv.getRole().getRoleName() : DEFAULT_UNKNOWN)
                .invitedByName(inv.getInvitedBy() != null ? inv.getInvitedBy().getFullName() : DEFAULT_SYSTEM)
                .status(inv.getStatus().name())
                .expiresAt(inv.getExpiresAt())
                .invitationLink(link)
                .build();
    }
}