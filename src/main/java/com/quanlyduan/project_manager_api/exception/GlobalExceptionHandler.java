// File: src/main/java/com/quanlyduan/project_manager_api/exception/GlobalExceptionHandler.java
package com.quanlyduan.project_manager_api.exception;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Lớp xử lý ngoại lệ toàn cục (Global Exception Handler).
 * Bắt và xử lý các lỗi HTTP chung (4xx, 5xx) và lỗi nghiệp vụ (Custom Exceptions)
 * để trả về phản hồi API nhất quán (ApiResponse).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ========================================================================
    // 1. XỬ LÝ LỖI VALIDATION (HTTP 400 - BAD REQUEST)
    // ========================================================================

    /**
     * Bắt lỗi validation (@Valid, @NotNull, @NotBlank...) cho các đối tượng @RequestBody.
     * Trả về HTTP 400 kèm chi tiết lỗi từng trường.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        // Trả về ApiResponse.error với map lỗi chi tiết
        ApiResponse<Map<String, String>> errorResponse = ApiResponse.error(
            "Invalid input data", 
            errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Bắt lỗi nghiệp vụ (BadRequestException) do Service layer ném ra.
     * Trả về HTTP 400.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(BadRequestException ex) {
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ========================================================================
    // 2. XỬ LÝ LỖI TÌM KIẾM TÀI NGUYÊN (HTTP 404 - NOT FOUND)
    // ========================================================================

    /**
     * Bắt lỗi không tìm thấy tài nguyên (ResourceNotFoundException) do Service layer ném ra.
     * Trả về HTTP 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ========================================================================
    // 3. XỬ LÝ LỖI XÁC THỰC & PHÂN QUYỀN (HTTP 401, 403)
    // ========================================================================

    /**
     * Bắt lỗi chung cho Authentication (Xác thực - HTTP 401).
     * Bao gồm lỗi sai mật khẩu, user bị khóa, user chưa xác thực email.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        
        // Trường hợp 1: Sai email/mật khẩu
        if (ex instanceof BadCredentialsException) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // 401
                    .body(ApiResponse.error("Invalid email or password")); 
        }
        
        // Trường hợp 2: Lỗi khác (vd: user bị khóa, user chưa xác thực email - thường được xử lý trong UserDetails)
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401
                .body(ApiResponse.error("Authentication failed: " + ex.getMessage()));
    }
    
    /**
     * Bắt lỗi 403 (Forbidden) từ @PreAuthorize.
     * Khi người dùng có token hợp lệ nhưng thiếu quyền để thực hiện hành động.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403
                .body(ApiResponse.error("You do not have permission to perform this action."));
    }
    
    // ========================================================================
    // 4. XỬ LÝ LỖI CHUNG (HTTP 500)
    // ========================================================================

    /**
     * Bắt tất cả các lỗi không được xử lý ở trên (Fallback Global Handler).
     * Trả về HTTP 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        // In Stack Trace ra console server để debug
        ex.printStackTrace();
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                .body(ApiResponse.error("An internal server error occurred: " + ex.getMessage())); 
    }
}