// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/AuthServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.quanlyduan.project_manager_api.dto.request.GoogleLoginRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.ResetPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.ProjectInvitation;
import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.model.Role;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.UserRole;
import com.quanlyduan.project_manager_api.model.AuthToken;
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository;
import com.quanlyduan.project_manager_api.repository.ProjectInvitationRepository;
import com.quanlyduan.project_manager_api.repository.ProjectMemberRepository;
import com.quanlyduan.project_manager_api.repository.RoleRepository;
import com.quanlyduan.project_manager_api.repository.UserRepository;
import com.quanlyduan.project_manager_api.repository.UserRoleRepository;
import com.quanlyduan.project_manager_api.repository.AuthTokenRepository;
import com.quanlyduan.project_manager_api.security.UserPrincipal;
import com.quanlyduan.project_manager_api.security.jwt.JwtTokenProvider;
import com.quanlyduan.project_manager_api.service.AuthService;
import com.quanlyduan.project_manager_api.service.EmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.quanlyduan.project_manager_api.dto.request.ForgotPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.LoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LogoutRequest;
import com.quanlyduan.project_manager_api.service.InvitationService;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromInviteRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromProjectInviteRequest;
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TokenStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.io.IOException;

import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-token-expiration-min}")
    private long refreshTokenExpirationMin;

    // TIÊM SERVICE/REPOSITORY MỚI
    private final InvitationService invitationService;
    private final CompanyInvitationRepository companyInvitationRepository;

    private final ProjectInvitationRepository projectInvitationRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final long OTP_EXPIRATION_MINUTES = 10;
    private static final long RESET_TOKEN_EXPIRATION_MINUTES = 60;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
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

    // ======================================================
    // 1. ĐĂNG KÝ (REGISTER)
    // ======================================================
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Kiểm tra email tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This email is already in use.");
        }

        // 2. Hash mật khẩu
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Tạo User mới
        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(hashedPassword)
                .status(UserStatus.ACTIVE)
                .isEmailVerified(false) // Cần xác thực email
                .build();

        // 4. Lưu người dùng
        User savedUser = userRepository.save(newUser);

        // 5. Lấy Role USER từ DB
        Role userRole = roleRepository.findFirstByRoleCode("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role USER not found. Please configure DB."));

        // 6. Tạo UserRole và lưu
        UserRole userRoleEntity = UserRole.builder()
                .user(savedUser)
                .role(userRole)
                .build();
        userRoleRepository.save(userRoleEntity);

        // 7. Gửi email xác thực
        sendVerificationEmail(savedUser);
    }

    // ======================================================
    // 2. ĐĂNG NHẬP (LOGIN)
    // ======================================================
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. Xác thực người dùng (username/password)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Nếu xác thực thành công, set vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Lấy thông tin User
        User user = userRepository.findByEmail(request.getEmail())
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Error: User not found after successful login."));

        // 4. Tạo Access Token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        // 5. Tạo Refresh Token
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(authentication);

        // 6. Lưu Refresh Token vào CSDL
        saveRefreshTokenToDB(user, refreshTokenString);

        // 7. Trả về Response
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .build();
    }

    // ======================================================
    // 3. ĐĂNG XUẤT (LOGOUT)
    // ======================================================
    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        // 1. Tìm Refresh Token trong CSDL
        AuthToken storedToken = authTokenRepository
                .findByTokenAndTokenType(request.getRefreshToken(), TokenType.REFRESH)
                .orElse(null);

        if (storedToken == null) {
            // Nếu không tìm thấy token, trả về thành công (để idempotency)
            return;
        }

        // 2. Xóa token khỏi CSDL
        authTokenRepository.delete(storedToken);
    }


    // ======================================================
    // 4. XÁC THỰC MAIL (VERIFY EMAIL)
    // ======================================================
    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        // 1. Tìm người dùng
        User user = userRepository.findByEmail(request.getEmail())
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // 2. Kiểm tra nếu đã xác thực
        if (user.getIsEmailVerified()) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This email is already verified.");
        }

        // 3. Tìm token (OTP)
        AuthToken token = authTokenRepository.findByTokenAndTokenType(request.getOtp(), TokenType.EMAIL_VERIFICATION)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Invalid OTP."));

        // 4. Kiểm tra token có đúng của người dùng này không
        if (!token.getUser().getId().equals(user.getId())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Invalid OTP.");
        }

        // 5. Kiểm tra token hết hạn
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("The OTP has expired.");
        }

        // 6. Xác thực thành công
        user.setIsEmailVerified(true);
        userRepository.save(user);

        // 7. Xóa token đã sử dụng
        authTokenRepository.delete(token);
    }

    // ======================================================
    // 5. QUÊN MẬT KHẨU (FORGOT PASSWORD)
    // ======================================================
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // 1. Tìm người dùng
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        // 2. Bảo mật: Nếu không tìm thấy, âm thầm thoát (tránh dò tìm email)
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        // 3. Tạo một token reset duy nhất
        String tokenString = UUID.randomUUID().toString();

        // 4. Lưu token vào CSDL
        AuthToken resetToken = AuthToken.builder()
                .user(user)
                .token(tokenString)
                .tokenType(TokenType.RESET_PASSWORD)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRATION_MINUTES))
                .build();

        authTokenRepository.save(resetToken);

        // 5. Gửi email
        sendPasswordResetEmail(user, tokenString);
    }

    // ======================================================
    // 6. ĐẶT LẠI MẬT KHẨU (RESET PASSWORD)
    // ======================================================
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 1. Tìm token trong CSDL
        AuthToken resetToken = authTokenRepository.findByTokenAndTokenType(request.getToken(), TokenType.RESET_PASSWORD)
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset code."));

        // 2. Kiểm tra token đã hết hạn chưa
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            authTokenRepository.delete(resetToken);
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("The password reset code has expired.");
        }

        // 3. Kiểm tra token đã được sử dụng/thu hồi chưa
        if (resetToken.getStatus() != TokenStatus.ACTIVE) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Invalid or already used reset code.");
        }

        // 4. Lấy người dùng liên quan
        User user = resetToken.getUser();

        // 5. Hash và đặt mật khẩu mới
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);

        // 6. Đánh dấu token này là đã thu hồi (REVOKED)
        resetToken.setStatus(TokenStatus.REVOKED);
        authTokenRepository.save(resetToken);

        // 7. Thu hồi tất cả Refresh Token của người dùng này (Buộc đăng xuất mọi thiết bị)
        authTokenRepository.revokeAllUserRefreshTokens(user.getId());
    }

    // ======================================================
    // 7. ĐĂNG NHẬP BẰNG GOOGLE (LOGIN WITH GOOGLE)
    // ======================================================
    @Override
    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        try {
            // 1. Xác thực id_token với máy chủ Google
            GoogleIdToken idToken = googleIdTokenVerifier.verify(request.getGoogleToken());
            if (idToken == null) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Invalid Google ID token.");
            }

            // 2. Lấy thông tin người dùng từ token
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");
            String avatarUrl = (String) payload.get("picture");
            boolean emailVerified = payload.getEmailVerified();

            if (!emailVerified) {
                // Sửa thông báo sang tiếng Anh
                throw new BadRequestException("Google email is not verified.");
            }

            // 3. Gọi logic Đăng ký hoặc Đăng nhập chung
            return processOAuthUser(email, fullName, avatarUrl);

        } catch (GeneralSecurityException | IOException e) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Could not verify Google token: " + e.getMessage());
        }
    }


    // ======================================================
    // 8. ĐĂNG KÝ TỪ LỜI MỜI CÔNG TY (REGISTER FROM COMPANY INVITE)
    // ======================================================
    @Override
    @Transactional
    public LoginResponse registerFromInvite(RegisterFromInviteRequest request) {
        // 1. Xác thực token lời mời (SỬ DỤNG SERVICE CHUNG)
        CompanyInvitation invitation = invitationService.validateInvitationToken(request.getInvitationToken());
        String invitedEmail = invitation.getEmail();

        // 2. Kiểm tra email (phòng trường hợp người dùng cũ cố tình gọi API này)
        if (userRepository.existsByEmail(invitedEmail)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Email already exists. Please login to accept the invitation.");
        }

        // 3. Tạo User mới
        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(invitedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true) // Tự động xác thực
                .build();

        User savedUser = userRepository.save(newUser);

        // 4. GÁN QUYỀN 'USER' CẤP HỆ THỐNG
        Role userRole = roleRepository.findFirstByRoleCode("USER")
            .orElseThrow(() -> new ResourceNotFoundException("Role USER not found. Please configure DB."));

        UserRole userRoleEntity = UserRole.builder()
            .user(savedUser)
            .role(userRole)
            .build();
        userRoleRepository.save(userRoleEntity);

        // 5. Thêm người dùng vào công ty (SỬ DỤNG SERVICE CHUNG)
        invitationService.addMemberToCompany(savedUser, invitation.getCompany(), invitation.getRole());

        // 6. Cập nhật lời mời
        invitation.setStatus(InvitationStatus.ACCEPTED);
        companyInvitationRepository.save(invitation);

        // 7. Tự động đăng nhập và trả về token
        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        saveRefreshTokenToDB(savedUser, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // ======================================================
    // 9. ĐĂNG KÝ TỪ LỜI MỜI DỰ ÁN (REGISTER FROM PROJECT INVITE)
    // ======================================================
    @Override
    @Transactional
    public LoginResponse registerFromProjectInvite(RegisterFromProjectInviteRequest request) {
        // 1. Xác thực token lời mời (Tương tự validateInvitationToken nhưng cho Project)
        ProjectInvitation invitation = projectInvitationRepository.findByToken(request.getInvitationToken())
                // Sửa thông báo sang tiếng Anh
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token."));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This invitation has already been processed or cancelled.");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            projectInvitationRepository.save(invitation);
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("This invitation has expired.");
        }

        String invitedEmail = invitation.getEmail();

        // 2. Kiểm tra email
        if (userRepository.existsByEmail(invitedEmail)) {
            // Sửa thông báo sang tiếng Anh
            throw new BadRequestException("Email already exists. Please login to accept the invitation.");
        }

        // 3. Tạo User mới
        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(invitedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true)
                .build();

        User savedUser = userRepository.save(newUser);

        // 4. Gán quyền 'USER' hệ thống
        Role userRole = roleRepository.findFirstByRoleCode("USER")
            .orElseThrow(() -> new ResourceNotFoundException("Role USER not found."));

        UserRole userRoleEntity = UserRole.builder()
            .user(savedUser)
            .role(userRole)
            .build();
        userRoleRepository.save(userRoleEntity);

        // 5. KHÁC BIỆT: Thêm vào DỰ ÁN
        ProjectMember projectMember = ProjectMember.builder()
                .project(invitation.getProject())
                .user(savedUser)
                .role(invitation.getRole())
                .status(MemberStatus.ACTIVE)
                .build();
        projectMemberRepository.save(projectMember);

        // 6. Cập nhật lời mời
        invitation.setStatus(InvitationStatus.ACCEPTED);
        projectInvitationRepository.save(invitation);

        // 7. Tự động đăng nhập và trả về token
        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        saveRefreshTokenToDB(savedUser, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // ======================================================
    // ⚙️ PRIVATE HELPER METHODS
    // ======================================================

    /**
     * Helper: Lưu Refresh Token vào CSDL.
     */
    private void saveRefreshTokenToDB(User user, String refreshToken) {
        AuthToken token = AuthToken.builder()
                .user(user)
                .token(refreshToken)
                .tokenType(TokenType.REFRESH)
                .status(TokenStatus.ACTIVE)
                // Đã sửa logic này để dùng .plusMinutes()
                .expiresAt(LocalDateTime.now().plusMinutes(refreshTokenExpirationMin))
                .build();
        authTokenRepository.save(token);
    }

    /**
     * Helper: Tạo OTP 6 chữ số ngẫu nhiên.
     */
    private String generateOtp() {
        Random random = new Random();
        int otpNumber = 100000 + random.nextInt(900000);
        return String.valueOf(otpNumber);
    }

    /**
     * Helper: Gửi Email xác thực tài khoản.
     */
    private void sendVerificationEmail(User user) {
        // 1. Tạo OTP
        String otp = generateOtp();

        // 2. Tạo đối tượng Token
        AuthToken verificationToken = AuthToken.builder()
                .user(user)
                .token(otp)
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                .build();

        // 3. Lưu Token
        authTokenRepository.save(verificationToken);

        // 4. Gửi mail (Sửa nội dung mail sang tiếng Anh)
        String emailBody = "Hello " + user.getFullName() + ",\n\n"
                + "Your account verification OTP is: <h3>" + otp + "</h3>\n"
                + "This code will expire in 10 minutes.\n\n"
                + "Thank you.";

        emailService.sendEmail(user.getEmail(), "Verify Your Account", emailBody);
    }

    /**
     * Helper: Gửi Email đặt lại mật khẩu.
     */
    private void sendPasswordResetEmail(User user, String token) {
        try {
            // Tạo link reset
            String resetUrl = String.format("%s/reset-password?token=%s", frontendUrl, token);

            // Sửa nội dung mail sang tiếng Anh
            String emailBody = String.format(
                "<p>Hello %s,</p>" +
                "<p>You requested a password reset. Please click the link below to create a new password:</p>" +
                "<p><a href=\"%s\">Reset Password</a></p>" +
                "<p>This link will expire in %d minutes.</p>" +
                "<p>If you did not request this action, please ignore this email.</p>",
                user.getFullName(),
                resetUrl,
                RESET_TOKEN_EXPIRATION_MINUTES
            );

            emailService.sendEmail(
                user.getEmail(),
                "Password Reset Request",
                emailBody
            );

        } catch (Exception e) {
            System.err.println("Error sending password reset email: " + e.getMessage());
        }
    }

    /**
     * Helper: Đăng nhập/Đăng ký người dùng OAuth (Google).
     */
    private LoginResponse processOAuthUser(String email, String fullName, String avatarUrl) {
        // 1. Tìm xem user đã tồn tại chưa
        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            // 2a. User đã tồn tại -> Cập nhật thông tin và Đăng nhập
            user = userOptional.get();
            user.setFullName(fullName);
            user.setAvatarUrl(avatarUrl);
            user.setStatus(UserStatus.ACTIVE);
            user.setIsEmailVerified(true);
            userRepository.save(user);
        } else {
            // 2b. User chưa tồn tại -> Đăng ký
            User newUser = User.builder()
                .email(email)
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                // Tạo mật khẩu ngẫu nhiên cho user OAuth
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .isEmailVerified(true) // Google đã xác thực
                .status(UserStatus.ACTIVE)
                .build();
            user = userRepository.save(newUser);

            // Gán quyền USER
            Role userRole = roleRepository.findFirstByRoleCode("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role USER not found. Please configure DB."));

            // *** SỬA LỖI LOGIC: Cần kiểm tra trước khi thêm (tránh lỗi nếu user đã có role) ***
            if (userRoleRepository.findByUser_Id(user.getId()).isEmpty()) {
                UserRole userRoleEntity = UserRole.builder()
                    .user(user)
                    .role(userRole)
                    .build();
                userRoleRepository.save(userRoleEntity);
            }
        }

        // 3. Tạo UserPrincipal và Authentication
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities()
        );

        // 4. Tạo JWT của chúng ta
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // 5. Lưu Refresh Token và trả về
        saveRefreshTokenToDB(user, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}