package com.quanlyduan.project_manager_api.exception;

// DTOs
import com.quanlyduan.project_manager_api.dto.response.ApiResponse;

// Spring Framework - Web & Http
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Spring Security
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;

// Validation & Utils
import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp xử lý ngoại lệ toàn cục (Global Exception Handler).
 * Tập trung tất cả các lỗi xảy ra trong quá trình thực thi và chuyển đổi chúng
 * thành định dạng ApiResponse chuẩn để trả về cho Client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ==========================================
    // 1. XỬ LÝ LỖI VALIDATION (HTTP 400)
    // ==========================================

    /**
     * Xử lý lỗi khi dữ liệu đầu vào không vượt qua được tầng Validation (@Valid).
     * Trả về danh sách chi tiết các trường bị lỗi.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpHeaders headers,
            HttpStatusCode status, 
            WebRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> errorResponse = ApiResponse.error(
            "Input validation failed", 
            validationErrors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Bắt lỗi nghiệp vụ chung (BadRequestException).
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ==========================================
    // 2. XỬ LÝ LỖI TÀI NGUYÊN (HTTP 404)
    // ==========================================

    /**
     * Bắt lỗi khi không tìm thấy thực thể trong Database.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ==========================================
    // 3. XỬ LÝ BẢO MẬT & PHÂN QUYỀN (HTTP 401, 403)
    // ==========================================

    /**
     * Xử lý các lỗi liên quan đến xác thực (Authentication).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        String message = "Authentication failed";
        
        if (ex instanceof BadCredentialsException) {
            message = "Invalid email or password";
        } else if (ex.getMessage() != null) {
            message = ex.getMessage();
        }
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message));
    }
    
    /**
     * Xử lý lỗi khi người dùng không có quyền thực hiện hành động (Authorization).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied: You do not have permission for this action."));
    }
    
    // ==========================================
    // 4. XỬ LÝ LỖI HỆ THỐNG (HTTP 500)
    // ==========================================

    /**
     * Chốt chặn cuối cùng cho tất cả các ngoại lệ chưa được khai báo xử lý cụ thể.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        // Log stack trace để hỗ trợ quá trình debugging tại server
        logger.error("Internal Server Error: ", ex);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage())); 
    }

    /**
     * Xử lý lỗi Vượt quá giới hạn gói cước (Quota Exceeded).
     * Trả về mã HTTP 402 (PAYMENT_REQUIRED) để Frontend biết đường hiển thị Popup nâng cấp gói.
     */
    @ExceptionHandler(OverageException.class)
    public ResponseEntity<ApiResponse<Object>> handleOverageException(OverageException ex) {
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiResponse.error(ex.getMessage()));
    }
}