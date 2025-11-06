// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/AuthServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
// import com.quanlyduan.project_manager_api.model.CongTy; // Not used
import com.quanlyduan.project_manager_api.model.CompanyInvitation; // Đã dịch
// import com.quanlyduan.project_manager_api.model.CongTyThanhVien; // Not used
import com.quanlyduan.project_manager_api.model.User; // Đã dịch
import com.quanlyduan.project_manager_api.model.AuthToken; // Đã dịch
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;
import com.quanlyduan.project_manager_api.repository.CompanyInvitationRepository; // Đã dịch
// import com.quanlyduan.project_manager_api.repository.CongTyThanhVienRepository; // Not used
import com.quanlyduan.project_manager_api.repository.UserRepository; // Đã dịch
import com.quanlyduan.project_manager_api.repository.AuthTokenRepository; // Đã dịch
import com.quanlyduan.project_manager_api.security.UserPrincipal;
import com.quanlyduan.project_manager_api.security.jwt.JwtTokenProvider;
import com.quanlyduan.project_manager_api.service.AuthService;
import com.quanlyduan.project_manager_api.service.EmailService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quanlyduan.project_manager_api.dto.request.LoginRequest;
import com.quanlyduan.project_manager_api.dto.request.LogoutRequest;
import com.quanlyduan.project_manager_api.service.InvitationService;
import com.quanlyduan.project_manager_api.dto.request.RegisterFromInviteRequest;
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
// import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus; // Not used
import com.quanlyduan.project_manager_api.model.common.enums.TokenStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;


import java.util.Random;

@Service
@RequiredArgsConstructor // Tự động @Autowired các trường final
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository; // Đã dịch
    private final AuthTokenRepository authTokenRepository; // Đã dịch
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${jwt.refresh-token-expiration-min}")
    private long refreshTokenExpirationMin;

    // TIÊM SERVICE MỚI
    private final InvitationService invitationService;
    private final CompanyInvitationRepository companyInvitationRepository; // Đã dịch
    
    private static final long OTP_EXPIRATION_MINUTES = 10;
    

    // LOIGIC DANG KY
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Kiểm tra email tồn tại
        if (userRepository.existsByEmail(request.getEmail())) { // Đã dịch
            throw new BadRequestException("This email is already in use"); // Đã dịch
        }

        // 2. Hash mật khẩu
        String hashedPassword = passwordEncoder.encode(request.getPassword()); // Đã dịch

        // 3. Tạo NguoiDung mới
        User newUser = User.builder() // Đã dịch
                .fullName(request.getFullName()) // Đã dịch
                .email(request.getEmail())
                .password(hashedPassword) // Đã dịch
                .status(UserStatus.ACTIVE) // Đã dịch
                .isEmailVerified(false) // Đã dịch
                .build();

        // 4. Lưu người dùng
        User savedUser = userRepository.save(newUser); // Đã dịch

        // 5. Tạo và gửi token xác thực
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
                        request.getPassword() // Đã dịch
                )
        );
        
        // 2. Nếu xác thực thành công, set vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Lấy thông tin NguoiDung (chúng ta cần Id để lưu RefreshToken)
        User user = userRepository.findByEmail(request.getEmail()) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Error: User not found after login")); // Đã dịch
                
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

    private void saveRefreshTokenToDB(User user, String refreshToken) { // Đã dịch
        
        AuthToken token = AuthToken.builder() // Đã dịch
                .user(user) // Đã dịch
                .token(refreshToken)
                .tokenType(TokenType.REFRESH) // Đã dịch
                .status(TokenStatus.ACTIVE) // Đã dịch
                
                // Đổi logic ở dòng này từ .plusMillis() sang .plusMinutes()
                .expiresAt(LocalDateTime.now().plusMinutes(refreshTokenExpirationMin)) // Đã dịch
                
                .build();
        authTokenRepository.save(token); // Đã dịch
    }


    // LOGIC DANG XUAT
    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        // 1. Tìm Refresh Token trong CSDL
        AuthToken storedToken = authTokenRepository // Đã dịch
                .findByTokenAndTokenType(request.getRefreshToken(), TokenType.REFRESH) // Đã dịch
                .orElse(null); // Không ném lỗi, chỉ đơn giản là không tìm thấy

        if (storedToken == null) {
            // Nếu không tìm thấy token, có thể nó đã bị đăng xuất ở thiết bị khác
            // Hoặc client gửi rác. Cứ trả về thành công.
            return; 
        }

        // 2. Xóa token khỏi CSDL
        authTokenRepository.delete(storedToken); // Đã dịch
        
        // (Cách 2: Nếu bạn muốn giữ lại lịch sử)
        // storedToken.setTrangThai(TokenStatus.DA_THU_HOI);
        // tokenRepository.save(storedToken);
    }
        

    // LOGIC XAC THUC MAIL
    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        // 1. Tìm người dùng
        User user = userRepository.findByEmail(request.getEmail()) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail())); // Đã dịch

        // 2. Kiểm tra nếu đã xác thực
        if (user.getIsEmailVerified()) { // Đã dịch
            throw new BadRequestException("This email has already been verified"); // Đã dịch
        }
        
        // 3. Tìm token (OTP)
        AuthToken token = authTokenRepository.findByTokenAndTokenType(request.getOtp(), TokenType.EMAIL_VERIFICATION) // Đã dịch
                .orElseThrow(() -> new ResourceNotFoundException("Invalid OTP")); // Đã dịch

        // 4. Kiểm tra token có đúng của người dùng này không
        if (!token.getUser().getId().equals(user.getId())) { // Đã dịch
             throw new BadRequestException("Invalid OTP"); // Đã dịch
        }

        // 5. Kiểm tra token hết hạn
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) { // Đã dịch
            // (Nên có logic gửi lại OTP ở đây)
            throw new BadRequestException("OTP has expired"); // Đã dịch
        }

        // 6. Xác thực thành công
        user.setIsEmailVerified(true); // Đã dịch
        userRepository.save(user); // Đã dịch

        // 7. Xóa token đã sử dụng
        authTokenRepository.delete(token); // Đã dịch
    }

    // --- Private Helper Methods ---

    private void sendVerificationEmail(User user) { // Đã dịch
        // 1. Tạo OTP
        String otp = generateOtp();

        // 2. Tạo đối tượng Token
        AuthToken verificationToken = AuthToken.builder() // Đã dịch
                .user(user) // Đã dịch
                .token(otp) // Lưu OTP vào trường token
                .tokenType(TokenType.EMAIL_VERIFICATION) // Đã dịch
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES)) // Đã dịch
                .build();

        // 3. Lưu Token
        authTokenRepository.save(verificationToken); // Đã dịch

        String emailBody = "Hi " + user.getFullName() + ",\n\n" // Đã dịch
                + "Your OTP code to verify your account is: <h3>" + otp + "</h3>" // Đã dịch
                + "This code will expire in 10 minutes.\n\n" // Đã dịch
                + "Thank you."; // Đã dịch
        
        
        emailService.sendEmail(user.getEmail(), "Verify Your Account", emailBody); // Đã dịch
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
        CompanyInvitation invitation = invitationService.validateInvitationToken(request.getInvitationToken()); // Đã dịch
        String invitedEmail = invitation.getEmail();

        // 2. Kiểm tra email (phòng trường hợp người dùng cũ cố tình gọi API này)
        if (userRepository.existsByEmail(invitedEmail)) { // Đã dịch
            throw new BadRequestException("This email already exists. Please log in to accept the invitation."); // Đã dịch
        }

        // 3. Tạo NguoiDung mới
        User newUser = User.builder() // Đã dịch
                .fullName(request.getFullName()) // Đã dịch
                .email(invitedEmail)
                .password(passwordEncoder.encode(request.getPassword())) // Đã dịch
                .status(UserStatus.ACTIVE) // Đã dịch
                .isEmailVerified(true) // Tự động xác thực // Đã dịch
                .build();
        
        User savedUser = userRepository.save(newUser); // Đã dịch

        // 4. Thêm người dùng vào công ty (SỬ DỤNG SERVICE CHUNG)
        invitationService.addMemberToCompany(savedUser, invitation.getCompany(), invitation.getRole()); // Đã dịch

        // 5. Cập nhật lời mời
        invitation.setStatus(InvitationStatus.ACCEPTED); // Đã dịch
        companyInvitationRepository.save(invitation); // Đã dịch

        // 6. Tự động đăng nhập và trả về token (Logic giữ nguyên)
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
}