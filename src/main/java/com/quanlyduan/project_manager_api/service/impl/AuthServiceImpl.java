package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.dto.request.RegisterRequest;
import com.quanlyduan.project_manager_api.dto.request.VerifyEmailRequest;
import com.quanlyduan.project_manager_api.exception.BadRequestException;
import com.quanlyduan.project_manager_api.exception.ResourceNotFoundException;
import com.quanlyduan.project_manager_api.model.NguoiDung;
import com.quanlyduan.project_manager_api.model.Token;
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;
import com.quanlyduan.project_manager_api.model.common.enums.UserStatus;
import com.quanlyduan.project_manager_api.repository.NguoiDungRepository;
import com.quanlyduan.project_manager_api.repository.TokenRepository;
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
import com.quanlyduan.project_manager_api.dto.response.LoginResponse;
import com.quanlyduan.project_manager_api.model.common.enums.TokenStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;

import java.util.Random;

@Service
@RequiredArgsConstructor // Tự động @Autowired các trường final
public class AuthServiceImpl implements AuthService {

    private final NguoiDungRepository nguoiDungRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    
    // === THÊM DÒNG NÀY ===
    @Value("${jwt.refresh-token-expiration-min}")
    private long refreshTokenExpirationMin;
    
    private static final long OTP_EXPIRATION_MINUTES = 10;
    

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Kiểm tra email tồn tại
        if (nguoiDungRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        // 2. Hash mật khẩu
        String hashedPassword = passwordEncoder.encode(request.getMatKhau());

        // 3. Tạo NguoiDung mới
        NguoiDung newUser = NguoiDung.builder()
                .hoTen(request.getHoTen())
                .email(request.getEmail())
                .matKhau(hashedPassword)
                .trangThai(UserStatus.HOAT_DONG)
                .xacThucEmail(false)
                .build();

        // 4. Lưu người dùng
        NguoiDung savedUser = nguoiDungRepository.save(newUser);

        // 5. Tạo và gửi token xác thực
        sendVerificationEmail(savedUser);
    }
    
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. Xác thực người dùng (username/password)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMatKhau()
                )
        );
        
        // 2. Nếu xác thực thành công, set vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Lấy thông tin NguoiDung (chúng ta cần Id để lưu RefreshToken)
        NguoiDung user = nguoiDungRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi lạ: Không tìm thấy user sau khi đăng nhập"));
                
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

    private void saveRefreshTokenToDB(NguoiDung user, String refreshToken) {
        
        Token token = Token.builder()
                .nguoiDung(user)
                .token(refreshToken)
                .loaiToken(TokenType.REFRESH)
                .trangThai(TokenStatus.HOAT_DONG)
                
                // Đổi logic ở dòng này từ .plusMillis() sang .plusMinutes()
                .ngayHetHan(LocalDateTime.now().plusMinutes(refreshTokenExpirationMin))
                
                .build();
        tokenRepository.save(token);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        // 1. Tìm Refresh Token trong CSDL
        Token storedToken = tokenRepository
                .findByTokenAndLoaiToken(request.getRefreshToken(), TokenType.REFRESH)
                .orElse(null); // Không ném lỗi, chỉ đơn giản là không tìm thấy

        if (storedToken == null) {
            // Nếu không tìm thấy token, có thể nó đã bị đăng xuất ở thiết bị khác
            // Hoặc client gửi rác. Cứ trả về thành công.
            return; 
        }

        // 2. Xóa token khỏi CSDL
        tokenRepository.delete(storedToken);
        
        // (Cách 2: Nếu bạn muốn giữ lại lịch sử)
        // storedToken.setTrangThai(TokenStatus.DA_THU_HOI);
        // tokenRepository.save(storedToken);
    }
     

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        // 1. Tìm người dùng
        NguoiDung user = nguoiDungRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + request.getEmail()));

        // 2. Kiểm tra nếu đã xác thực
        if (user.getXacThucEmail()) {
            throw new BadRequestException("Email này đã được xác thực");
        }
        
        // 3. Tìm token (OTP)
        Token token = tokenRepository.findByTokenAndLoaiToken(request.getOtp(), TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException("OTP không hợp lệ"));

        // 4. Kiểm tra token có đúng của người dùng này không
        if (!token.getNguoiDung().getIdNguoiDung().equals(user.getIdNguoiDung())) {
             throw new BadRequestException("OTP không hợp lệ");
        }

        // 5. Kiểm tra token hết hạn
        if (token.getNgayHetHan().isBefore(LocalDateTime.now())) {
            // (Nên có logic gửi lại OTP ở đây)
            throw new BadRequestException("OTP đã hết hạn");
        }

        // 6. Xác thực thành công
        user.setXacThucEmail(true);
        nguoiDungRepository.save(user);

        // 7. Xóa token đã sử dụng
        tokenRepository.delete(token);
    }

    // --- Private Helper Methods ---

    private void sendVerificationEmail(NguoiDung user) {
        // 1. Tạo OTP
        String otp = generateOtp();

        // 2. Tạo đối tượng Token
        Token verificationToken = Token.builder()
                .nguoiDung(user)
                .token(otp) // Lưu OTP vào trường token
                .loaiToken(TokenType.EMAIL_VERIFICATION)
                .ngayHetHan(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                .build();

        // 3. Lưu Token
        tokenRepository.save(verificationToken);

        String emailBody = "Chào " + user.getHoTen() + ",\n\n"
                + "Mã OTP để xác thực tài khoản của bạn là: <h3>" + otp + "</h3>" // Thêm chút HTML
                + "Mã này sẽ hết hạn sau 10 phút.\n\n"
                + "Cảm ơn bạn.";
        
        
        emailService.sendEmail(user.getEmail(), "Xác thực tài khoản", emailBody);
    }

    private String generateOtp() {
        // Tạo OTP 6 chữ số
        Random random = new Random();
        int otpNumber = 100000 + random.nextInt(900000);
        return String.valueOf(otpNumber);
    }
}