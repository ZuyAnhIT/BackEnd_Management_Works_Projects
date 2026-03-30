package com.quanlyduan.project_manager_api.service.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.quanlyduan.project_manager_api.dto.request.ForgotPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.GoogleLoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LogoutRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromInviteRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromProjectInviteRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.ResetPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.AuthToken;
import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.ProjectInvitation;
import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.UserRole;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TokenStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;
import com.quanlyduan.project_manager_api.repository.AuthTokenRepository;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository;
import com.quanlyduan.project_manager_api.repository.ProjectInvitationRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.UserRoleRepository;
import com.quanlyduan.project_manager_api.security.UserPrincipal;
import com.quanlyduan.project_manager_api.security.jwt.JwtTokenProvider;
import com.quanlyduan.project_manager_api.service.AuthService;
import com.quanlyduan.project_manager_api.service.EmailService;
import com.quanlyduan.project_manager_api.service.InvitationService;

@Service
public class AuthServiceImpl implements AuthService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ROLE_USER = "USER";

    public static final String ERROR_EMAIL_IN_USE = "This email is already in use.";
    public static final String ERROR_ROLE_USER_NOT_FOUND = "Role USER not found. Please configure DB.";
    public static final String ERROR_USER_NOT_FOUND_AFTER_LOGIN = "Error: User not found after successful login.";
    public static final String ERROR_USER_NOT_FOUND_EMAIL = "User not found with email: ";
    public static final String ERROR_EMAIL_ALREADY_VERIFIED = "This email is already verified.";
    public static final String ERROR_INVALID_OTP = "Invalid OTP.";
    public static final String ERROR_OTP_EXPIRED = "The OTP has expired.";
    public static final String ERROR_INVALID_RESET_CODE = "Invalid or expired reset code.";
    public static final String ERROR_RESET_CODE_EXPIRED = "The password reset code has expired.";
    public static final String ERROR_RESET_CODE_USED = "Invalid or already used reset code.";
    public static final String ERROR_INVALID_GOOGLE_TOKEN = "Invalid Google ID token.";
    public static final String ERROR_GOOGLE_EMAIL_NOT_VERIFIED = "Google email is not verified.";
    public static final String ERROR_VERIFY_GOOGLE_TOKEN = "Could not verify Google token: ";
    public static final String ERROR_EMAIL_EXISTS_INVITE = "Email already exists. Please login to accept the invitation.";
    public static final String ERROR_INVALID_INVITATION_TOKEN = "Invalid invitation token.";
    public static final String ERROR_INVITATION_PROCESSED = "This invitation has already been processed or cancelled.";
    public static final String ERROR_INVITATION_EXPIRED = "This invitation has expired.";

    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_PICTURE = "picture";

    public static final String SUBJECT_VERIFY_ACCOUNT = "Verify Your Account";
    public static final String SUBJECT_RESET_PASSWORD = "Password Reset Request";

    public static final String EMAIL_VERIFY_BODY_TEMPLATE = "Hello %s,\n\nYour account verification OTP is: <h3>%s</h3>\nThis code will expire in %d minutes.\n\nThank you.";
    public static final String EMAIL_RESET_BODY_TEMPLATE = "<p>Hello %s,</p><p>You requested a password reset. Please click the link below to create a new password:</p><p><a href=\"%s/reset-password?token=%s\">Reset Password</a></p><p>This link will expire in %d minutes.</p><p>If you did not request this action, please ignore this email.</p>";

    public static final long OTP_EXPIRATION_MINUTES = 10;
    public static final long RESET_TOKEN_EXPIRATION_MINUTES = 60;
    public static final int OTP_BASE_VALUE = 100000;
    public static final int OTP_RANDOM_BOUND = 900000;

    // Khai bao cac bien phu thuoc va gia tri tu file cau hinh
    @Value("${jwt.refresh-token-expiration-min}")
    private long refreshTokenExpirationMin;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final InvitationService invitationService;
    private final CompanyInvitationRepository companyInvitationRepository;
    private final ProjectInvitationRepository projectInvitationRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // Constructor thay the cho @RequiredArgsConstructor
    public AuthServiceImpl(UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           AuthTokenRepository authTokenRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService,
                           GoogleIdTokenVerifier googleIdTokenVerifier,
                           RoleRepository roleRepository,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider,
                           InvitationService invitationService,
                           CompanyInvitationRepository companyInvitationRepository,
                           ProjectInvitationRepository projectInvitationRepository,
                           ProjectMemberRepository projectMemberRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.invitationService = invitationService;
        this.companyInvitationRepository = companyInvitationRepository;
        this.projectInvitationRepository = projectInvitationRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // Kiem tra email da ton tai chua
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(ERROR_EMAIL_IN_USE);
        }

        // Ma hoa mat khau va tao nguoi dung moi
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = buildUserEntity(request.getFullName(), request.getEmail(), hashedPassword, false);
        User savedUser = userRepository.save(newUser);

        // Cap quyen mac dinh la USER
        Role userRole = roleRepository.findFirstByRoleCode(ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_USER_NOT_FOUND));

        UserRole userRoleEntity = buildUserRoleEntity(savedUser, userRole);
        userRoleRepository.save(userRoleEntity);

        // Goi ham gui email xac thuc
        sendVerificationEmail(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Xac thuc thong tin dang nhap
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Luu thong tin xac thuc vao nguyen canh bao mat
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Lay thong tin nguoi dung de sinh token
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND_AFTER_LOGIN));

        // Sinh access token va refresh token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(authentication);

        // Luu refresh token vao co so du lieu de quan ly
        saveRefreshTokenToDB(user, refreshTokenString);

        return buildLoginResponse(accessToken, refreshTokenString);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        // Tim token trong co so du lieu
        AuthToken storedToken = authTokenRepository
                .findByTokenAndTokenType(request.getRefreshToken(), TokenType.REFRESH)
                .orElse(null);

        // Xoa token neu tim thay de vo hieu hoa phien dang nhap
        if (storedToken != null) {
            authTokenRepository.delete(storedToken);
        }
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        // Kiem tra nguoi dung va trang thai xac thuc
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_USER_NOT_FOUND_EMAIL + request.getEmail()));

        if (user.getIsEmailVerified()) {
            throw new BadRequestException(ERROR_EMAIL_ALREADY_VERIFIED);
        }

        // Tim va kiem tra tinh hop le cua ma OTP
        AuthToken token = authTokenRepository.findByTokenAndTokenType(request.getOtp(), TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVALID_OTP));

        if (!token.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(ERROR_INVALID_OTP);
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ERROR_OTP_EXPIRED);
        }

        // Cap nhat trang thai xac thuc va xoa ma OTP da dung
        user.setIsEmailVerified(true);
        userRepository.save(user);
        authTokenRepository.delete(token);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        // Thoat am tham neu khong tim thay nguoi dung de bao mat
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        // Tao token khoi phuc duy nhat
        String tokenString = UUID.randomUUID().toString();
        AuthToken resetToken = buildAuthTokenEntity(user, tokenString, TokenType.RESET_PASSWORD, RESET_TOKEN_EXPIRATION_MINUTES);
        authTokenRepository.save(resetToken);

        // Gui email chua duong dan khoi phuc
        sendPasswordResetEmail(user, tokenString);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Tim token khoi phuc
        AuthToken resetToken = authTokenRepository.findByTokenAndTokenType(request.getToken(), TokenType.RESET_PASSWORD)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVALID_RESET_CODE));

        // Kiem tra han su dung va trang thai cua token
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            authTokenRepository.delete(resetToken);
            throw new BadRequestException(ERROR_RESET_CODE_EXPIRED);
        }

        if (resetToken.getStatus() != TokenStatus.ACTIVE) {
            throw new BadRequestException(ERROR_RESET_CODE_USED);
        }

        // Cap nhat mat khau moi
        User user = resetToken.getUser();
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);

        // Thu hoi token va toan bo cac refresh token cua nguoi dung
        resetToken.setStatus(TokenStatus.REVOKED);
        authTokenRepository.save(resetToken);
        authTokenRepository.revokeAllUserRefreshTokens(user.getId());
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        try {
            // Xac thuc token voi may chu Google
            GoogleIdToken idToken = googleIdTokenVerifier.verify(request.getGoogleToken());
            if (idToken == null) {
                throw new BadRequestException(ERROR_INVALID_GOOGLE_TOKEN);
            }

            // Lay thong tin nguoi dung tu token tra ve
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String fullName = (String) payload.get(CLAIM_NAME);
            String avatarUrl = (String) payload.get(CLAIM_PICTURE);
            boolean emailVerified = payload.getEmailVerified();

            if (!emailVerified) {
                throw new BadRequestException(ERROR_GOOGLE_EMAIL_NOT_VERIFIED);
            }

            // Tien hanh xu ly dang nhap hoac dang ky moi
            return processOAuthUser(email, fullName, avatarUrl);

        } catch (GeneralSecurityException | IOException e) {
            throw new BadRequestException(ERROR_VERIFY_GOOGLE_TOKEN + e.getMessage());
        }
    }

    @Override
    @Transactional
    public LoginResponse registerFromInvite(RegisterFromInviteRequest request) {
        // Xac thuc token loi moi tu service dung chung
        CompanyInvitation invitation = invitationService.validateInvitationToken(request.getInvitationToken());
        String invitedEmail = invitation.getEmail();

        // Kiem tra tinh trang email hien tai
        if (userRepository.existsByEmail(invitedEmail)) {
            throw new BadRequestException(ERROR_EMAIL_EXISTS_INVITE);
        }

        // Tao nguoi dung moi voi mat khau ma hoa va tu dong xac thuc
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User savedUser = userRepository.save(buildUserEntity(request.getFullName(), invitedEmail, hashedPassword, true));

        // Gan quyen truy cap he thong mac dinh
        Role userRole = roleRepository.findFirstByRoleCode(ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_USER_NOT_FOUND));

        userRoleRepository.save(buildUserRoleEntity(savedUser, userRole));

        // Them vao cong ty va cap nhat trang thai loi moi
        invitationService.addMemberToCompany(savedUser, invitation.getCompany(), invitation.getRole());
        invitation.setStatus(InvitationStatus.ACCEPTED);
        companyInvitationRepository.save(invitation);

        // Tu dong dang nhap vao he thong sau khi hoan tat
        return autoLoginUser(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse registerFromProjectInvite(RegisterFromProjectInviteRequest request) {
        // Xac thuc loi moi tham gia du an
        ProjectInvitation invitation = projectInvitationRepository.findByToken(request.getInvitationToken())
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_INVALID_INVITATION_TOKEN));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException(ERROR_INVITATION_PROCESSED);
        }
        
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            projectInvitationRepository.save(invitation);
            throw new BadRequestException(ERROR_INVITATION_EXPIRED);
        }

        String invitedEmail = invitation.getEmail();

        // Kiem tra su ton tai cua email
        if (userRepository.existsByEmail(invitedEmail)) {
            throw new BadRequestException(ERROR_EMAIL_EXISTS_INVITE);
        }

        // Tao tai khoan nguoi dung moi
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User savedUser = userRepository.save(buildUserEntity(request.getFullName(), invitedEmail, hashedPassword, true));

        // Gan quyen he thong
        Role userRole = roleRepository.findFirstByRoleCode(ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_USER_NOT_FOUND));

        userRoleRepository.save(buildUserRoleEntity(savedUser, userRole));

        // Gan quyen thanh vien cho du an
        ProjectMember projectMember = buildProjectMemberEntity(invitation, savedUser);
        projectMemberRepository.save(projectMember);

        // Cap nhat trang thai phan hoi
        invitation.setStatus(InvitationStatus.ACCEPTED);
        projectInvitationRepository.save(invitation);

        // Dang nhap nguoi dung tu dong va tra ve ket qua
        return autoLoginUser(savedUser);
    }

    // --- CAC HAM PRIVATE HO TRO NGHIEP VU ---

    private void saveRefreshTokenToDB(User user, String refreshToken) {
        AuthToken token = buildAuthTokenEntity(user, refreshToken, TokenType.REFRESH, refreshTokenExpirationMin);
        authTokenRepository.save(token);
    }

    private String generateOtp() {
        Random random = new Random();
        int otpNumber = OTP_BASE_VALUE + random.nextInt(OTP_RANDOM_BOUND);
        return String.valueOf(otpNumber);
    }

    private void sendVerificationEmail(User user) {
        String otp = generateOtp();
        AuthToken verificationToken = buildAuthTokenEntity(user, otp, TokenType.EMAIL_VERIFICATION, OTP_EXPIRATION_MINUTES);
        authTokenRepository.save(verificationToken);

        String emailBody = String.format(EMAIL_VERIFY_BODY_TEMPLATE, user.getFullName(), otp, OTP_EXPIRATION_MINUTES);
        emailService.sendEmail(user.getEmail(), SUBJECT_VERIFY_ACCOUNT, emailBody);
    }

    private void sendPasswordResetEmail(User user, String token) {
        try {
            String emailBody = String.format(EMAIL_RESET_BODY_TEMPLATE, user.getFullName(), frontendUrl, token, RESET_TOKEN_EXPIRATION_MINUTES);
            emailService.sendEmail(user.getEmail(), SUBJECT_RESET_PASSWORD, emailBody);
        } catch (Exception e) {
            // Bo qua ngoai le xay ra khi gui thu do loi he thong ben ngoai, dam bao khong anh huong den luong hoat dong chinh
        }
    }

    private LoginResponse processOAuthUser(String email, String fullName, String avatarUrl) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setFullName(fullName);
            user.setAvatarUrl(avatarUrl);
            user.setStatus(UserStatus.ACTIVE);
            user.setIsEmailVerified(true);
            userRepository.save(user);
        } else {
            String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
            User newUser = buildUserEntity(fullName, email, randomPassword, true);
            newUser.setAvatarUrl(avatarUrl);
            user = userRepository.save(newUser);

            Role userRole = roleRepository.findFirstByRoleCode(ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException(ERROR_ROLE_USER_NOT_FOUND));

            if (userRoleRepository.findByUser_Id(user.getId()).isEmpty()) {
                userRoleRepository.save(buildUserRoleEntity(user, userRole));
            }
        }

        return autoLoginUser(user);
    }

    private LoginResponse autoLoginUser(User user) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        saveRefreshTokenToDB(user, refreshToken);

        return buildLoginResponse(accessToken, refreshToken);
    }

    // --- LOGIC MAPPING (ENTITY <-> DTO) ---

    private User buildUserEntity(String fullName, String email, String password, boolean isEmailVerified) {
        return User.builder()
                .fullName(fullName)
                .email(email)
                .password(password)
                .status(UserStatus.ACTIVE)
                .isEmailVerified(isEmailVerified)
                .build();
    }

    private UserRole buildUserRoleEntity(User user, Role role) {
        return UserRole.builder()
                .user(user)
                .role(role)
                .build();
    }

    private AuthToken buildAuthTokenEntity(User user, String token, TokenType tokenType, long expirationMinutes) {
        return AuthToken.builder()
                .user(user)
                .token(token)
                .tokenType(tokenType)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .build();
    }

    private ProjectMember buildProjectMemberEntity(ProjectInvitation invitation, User user) {
        return ProjectMember.builder()
                .project(invitation.getProject())
                .user(user)
                .role(invitation.getRole())
                .status(MemberStatus.ACTIVE)
                .build();
    }

    private LoginResponse buildLoginResponse(String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}