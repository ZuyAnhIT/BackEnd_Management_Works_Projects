// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/AuthServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier; 
import com.quanlyduan.project_manager_api.dto.request.GoogleLoginRequest;
import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.ResetPasswordRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
// import com.quanlyduan.project_manager_api.model.CongTy; // Not used
import com.quanlyduan.project_manager_api.model.CompanyInvitation; // Đã dịch
import com.quanlyduan.project_manager_api.model.Role;
// import com.quanlyduan.project_manager_api.model.CongTyThanhVien; // Not used
import com.quanlyduan.project_manager_api.model.User; // Đã dịch
import com.quanlyduan.project_manager_api.model.UserRole;
import com.quanlyduan.project_manager_api.model.AuthToken; // Đã dịch
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository; // Đã dịch
import com.quanlyduan.project_manager_api.repository.RoleRepository;
// import com.quanlyduan.project_manager_api.repository.CongTyThanhVienRepository; // Not used
import com.quanlyduan.project_manager_api.repository.UserRepository; // Đã dịch
import com.quanlyduan.project_manager_api.repository.UserRoleRepository;
import com.quanlyduan.project_manager_api.repository.AuthTokenRepository; // Đã dịch
import com.quanlyduan.project_manager_api.security.UserPrincipal;
import com.quanlyduan.project_manager_api.security.jwt.JwtTokenProvider;
import com.quanlyduan.project_manager_api.service.AuthService;
import com.quanlyduan.project_manager_api.service.EmailService;
// import lombok.RequiredArgsConstructor; // Đã xóa

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
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;

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
// @RequiredArgsConstructor // Đã xóa
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

    // TIÊM SERVICE MỚI
    private final InvitationService invitationService;
    private final CompanyInvitationRepository companyInvitationRepository; // Đã dịch

    // *** THÊM VALUE NÀY ***
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final long OTP_EXPIRATION_MINUTES = 10;
    private static final long RESET_TOKEN_EXPIRATION_MINUTES = 60;
    
    // *** THÊM CONSTRUCTOR THỦ CÔNG (Theo yêu cầu) ***
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
                           CompanyInvitationRepository companyInvitationRepository) {
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
    }

    // LOIGIC DANG KY
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Kiểm tra email tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email này đã được sử dụng");
        }
    
        // 2. Hash mật khẩu
        String hashedPassword = passwordEncoder.encode(request.getPassword());
    
        // 3. Tạo User mới
        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(hashedPassword)
                .status(UserStatus.ACTIVE)
                .isEmailVerified(false)
                .build();
    
        // 4. Lưu người dùng
        User savedUser = userRepository.save(newUser);
    
        // ✅ 5. Lấy Role USER từ DB
        Role userRole = roleRepository.findFirstByRoleCode("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò USER. Vui lòng cấu hình CSDL.")); // Đã dịch
    
        // ✅ 6. Tạo UserRole và lưu
        UserRole userRoleEntity = UserRole.builder()
                .user(savedUser)
                .role(userRole)
                .build();
        userRoleRepository.save(userRoleEntity);
    
        // 7. Gửi email xác thực
        sendVerificationEmail(savedUser);
    }
    
    // LOGIC DANG NHAP
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

        // 3. Lấy thông tin NguoiDung (chúng ta cần Id để lưu RefreshToken)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi: Không tìm thấy người dùng sau khi đăng nhập"));
                
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
    
    // --- Private Helper Methods ---

    private void saveRefreshTokenToDB(User user, String refreshToken) {
        
        AuthToken token = AuthToken.builder()
                .user(user)
                .token(refreshToken)
                .tokenType(TokenType.REFRESH)
                .status(TokenStatus.ACTIVE)
                
                // Đổi logic ở dòng này từ .plusMillis() sang .plusMinutes()
                .expiresAt(LocalDateTime.now().plusMinutes(refreshTokenExpirationMin))
                
                .build();
        authTokenRepository.save(token);
    }


    // LOGIC DANG XUAT
    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        // 1. Tìm Refresh Token trong CSDL
        AuthToken storedToken = authTokenRepository
                .findByTokenAndTokenType(request.getRefreshToken(), TokenType.REFRESH)
                .orElse(null); // Không ném lỗi, chỉ đơn giản là không tìm thấy

        if (storedToken == null) {
            // Nếu không tìm thấy token, có thể nó đã bị đăng xuất ở thiết bị khác
            // Hoặc client gửi rác. Cứ trả về thành công.
            return; 
        }

        // 2. Xóa token khỏi CSDL
        authTokenRepository.delete(storedToken);
        
        // (Cách 2: Nếu bạn muốn giữ lại lịch sử)
        // storedToken.setStatus(TokenStatus.REVOKED);
        // authTokenRepository.save(storedToken);
    }
        

    // LOGIC XAC THUC MAIL
    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        // 1. Tìm người dùng
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + request.getEmail()));

        // 2. Kiểm tra nếu đã xác thực
        if (user.getIsEmailVerified()) {
            throw new BadRequestException("Email này đã được xác minh");
        }
        
        // 3. Tìm token (OTP)
        AuthToken token = authTokenRepository.findByTokenAndTokenType(request.getOtp(), TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException("OTP không hợp lệ"));

        // 4. Kiểm tra token có đúng của người dùng này không
        if (!token.getUser().getId().equals(user.getId())) {
             throw new BadRequestException("OTP không hợp lệ");
        }

        // 5. Kiểm tra token hết hạn
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            // (Nên có logic gửi lại OTP ở đây)
            throw new BadRequestException("Mã OTP đã hết hạn");
        }

        // 6. Xác thực thành công
        user.setIsEmailVerified(true);
        userRepository.save(user);

        // 7. Xóa token đã sử dụng
        authTokenRepository.delete(token);
    }

    // --- Private Helper Methods ---

    private void sendVerificationEmail(User user) {
        // 1. Tạo OTP
        String otp = generateOtp();

        // 2. Tạo đối tượng Token
        AuthToken verificationToken = AuthToken.builder()
                .user(user)
                .token(otp) // Lưu OTP vào trường token
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                .build();

        // 3. Lưu Token
        authTokenRepository.save(verificationToken);

       String emailBody = "Xin chào " + user.getFullName() + ",\n\n"
                + "Mã OTP để xác thực tài khoản của bạn là: <h3>" + otp + "</h3>\n"
                + "Mã này sẽ hết hạn sau 10 phút.\n\n"
                + "Cảm ơn bạn.";
        
        emailService.sendEmail(user.getEmail(), "Xác minh tài khoản của bạn", emailBody);
    }

    private String generateOtp() {
        // Tạo OTP 6 chữ số
        Random random = new Random();
        int otpNumber = 100000 + random.nextInt(900000);
        return String.valueOf(otpNumber);
    }


    // LOGIC DANG KY KHI NHAN LOI MOI VOI THANH VIEN CHUA CO TAI KHOAN
    @Override
    @Transactional
    public LoginResponse registerFromInvite(RegisterFromInviteRequest request) {
        // 1. Xác thực token lời mời (SỬ DỤNG SERVICE CHUNG)
        CompanyInvitation invitation = invitationService.validateInvitationToken(request.getInvitationToken());
        String invitedEmail = invitation.getEmail();

        // 2. Kiểm tra email (phòng trường hợp người dùng cũ cố tình gọi API này)
        if (userRepository.existsByEmail(invitedEmail)) {
            throw new BadRequestException("Email này đã tồn tại. Vui lòng đăng nhập để chấp nhận lời mời.");
        }

        // 3. Tạo NguoiDung mới
        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(invitedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true) // Tự động xác thực
                .build();
        
        User savedUser = userRepository.save(newUser);
        
        // 4. *** GÁN QUYỀN 'USER' CẤP HỆ THỐNG ***
        Role userRole = roleRepository.findFirstByRoleCode("USER")
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò USER. Vui lòng cấu hình CSDL."));
        
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

        // 7. Tự động đăng nhập và trả về token (Logic giữ nguyên)
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

    // LOGIC QUEN MAT KHAU
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // 1. Tìm người dùng
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        // 2. Bảo mật: Nếu không tìm thấy, không làm gì cả và âm thầm thoát
        // Điều này ngăn chặn kẻ tấn công dò xem email nào đã tồn tại
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

    // *** THÊM HÀM HELPER NÀY ***
    private void sendPasswordResetEmail(User user, String token) {
        try {
            // Tạo link reset
            String resetUrl = String.format("%s/reset-password?token=%s", frontendUrl, token);

            String emailBody = String.format(
                "<p>Xin chào %s,</p>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấp vào liên kết bên dưới để tạo mật khẩu mới:</p>" +
                "<p><a href=\"%s\">Đặt lại mật khẩu</a></p>" +
                "<p>Liên kết này sẽ hết hạn sau %d phút.</p>" +
                "<p>Nếu bạn không yêu cầu thao tác này, vui lòng bỏ qua email này.</p>",
                user.getFullName(),
                resetUrl,
                RESET_TOKEN_EXPIRATION_MINUTES
            );

            emailService.sendEmail(
                user.getEmail(), 
                "Yêu cầu Đặt lại Mật khẩu",
                emailBody
            );

        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email đặt lại mật khẩu: " + e.getMessage());
        }
    }

    // LOGIC DAT LAI MAT KHAU
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 1. Tìm token trong CSDL
        AuthToken resetToken = authTokenRepository.findByTokenAndTokenType(request.getToken(), TokenType.RESET_PASSWORD)
                .orElseThrow(() -> new ResourceNotFoundException("Mã đặt lại không hợp lệ hoặc đã hết hạn"));

        // 2. Kiểm tra token đã hết hạn chưa
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            authTokenRepository.delete(resetToken); 
            throw new BadRequestException("Mã đặt lại mật khẩu đã hết hạn");
        }

        // 3. Kiểm tra token đã được sử dụng/thu hồi chưa
        if (resetToken.getStatus() != TokenStatus.ACTIVE) {
             throw new BadRequestException("Mã đặt lại không hợp lệ hoặc đã được sử dụng");
        }

        // 4. Lấy người dùng liên quan
        User user = resetToken.getUser();

        // 5. Hash và đặt mật khẩu mới
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);

        // 6. Đánh dấu token này là đã thu hồi
        resetToken.setStatus(TokenStatus.REVOKED);
        authTokenRepository.save(resetToken);

        // 7.(NÂNG CẤP) Thu hồi tất cả Refresh Token của người dùng này
        authTokenRepository.revokeAllUserRefreshTokens(user.getId());
    }


    // LOGIC DANG NHAP BANG GOOGLE
    @Override
    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        try {
            // 1. Xác thực id_token với máy chủ Google
            GoogleIdToken idToken = googleIdTokenVerifier.verify(request.getGoogleToken());
            if (idToken == null) {
                throw new BadRequestException("Mã thông báo ID Google không hợp lệ.");
            }

            // 2. Lấy thông tin người dùng từ token
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");
            String avatarUrl = (String) payload.get("picture");
            boolean emailVerified = payload.getEmailVerified();

            if (!emailVerified) {
                 throw new BadRequestException("Email Google chưa được xác minh.");
            }

            // 3. Gọi logic Đăng ký hoặc Đăng nhập
            return processOAuthUser(email, fullName, avatarUrl);

        } catch (GeneralSecurityException | IOException e) {
            throw new BadRequestException("Không thể xác minh token của Google: " + e.getMessage());
        }
    }

    /**
     * Helper: Tìm người dùng (User) trong CSDL bằng email.
     * Nếu tồn tại, cập nhật thông tin và trả về.
     * Nếu không, tạo mới (đăng ký) và trả về.
     */
    private LoginResponse processOAuthUser(String email, String fullName, String avatarUrl) {
        // 1. Tìm xem user đã tồn tại chưa
        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            // 2a. User đã tồn tại -> Cập nhật thông tin (nếu cần) và Đăng nhập
            user = userOptional.get();
            user.setFullName(fullName);
            user.setAvatarUrl(avatarUrl);
            // Đảm bảo user này active (nếu trước đó họ bị khóa)
            user.setStatus(UserStatus.ACTIVE); 
            user.setIsEmailVerified(true);
            userRepository.save(user);
        } else {
            // 2b. User chưa tồn tại -> Đăng ký
            User newUser = User.builder()
                .email(email)
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Tạo mật khẩu ngẫu nhiên
                .isEmailVerified(true) // Google đã xác thực
                .status(UserStatus.ACTIVE)
                .build();
            user = userRepository.save(newUser);

            
        // ✅ Gán quyền USER
                Role userRole = roleRepository.findFirstByRoleCode("USER")
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò USER. Vui lòng cấu hình CSDL.")); // Đã dịch
                
                // *** SỬA LỖI LOGIC: Cần kiểm tra trước khi thêm ***
                if (userRoleRepository.findByUser_Id(user.getId()).isEmpty()) {
                            UserRole userRoleEntity = UserRole.builder()
                                    .user(user)
                                    .role(userRole)
                                    .build();
                            userRoleRepository.save(userRoleEntity);
                }
        }

        // 3. Tạo UserPrincipal (thông tin để tạo token)
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        
        // 4. Tạo Authentication (phiên đăng nhập tạm thời)
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities()
        );
        
        // 5. Tạo JWT của chính chúng ta
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        // 6. Lưu Refresh Token và trả về
        saveRefreshTokenToDB(user, refreshToken);
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    
}