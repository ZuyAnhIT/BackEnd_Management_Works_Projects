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

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Bắt lỗi validation (@Valid) - Dùng ApiResponse.error(message, data)
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
        
      
        ApiResponse<Map<String, String>> errorResponse = ApiResponse.error(
            "Dữ liệu đầu vào không hợp lệ", // Đã dịch
            errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Bắt lỗi 400 - Dùng ApiResponse.error(message)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(BadRequestException ex) {
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Bắt lỗi 404 - Dùng ApiResponse.error(message)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Bắt lỗi chung cho Authentication (ví dụ: email chưa xác thực, user bị khóa)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        // Phân biệt lỗi sai mật khẩu
        if (ex instanceof BadCredentialsException) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // 401
                    .body(ApiResponse.error("Email hoặc mật khẩu không hợp lệ")); // Đã dịch
        }
        
        // Bắt các lỗi khác (vd: user bị khóa, user chưa xác thực email)
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401
                .body(ApiResponse.error("Xác thực thất bại:" + ex.getMessage())); // Đã dịch
    }
    
    // Bắt lỗi 403 (Forbidden) từ @PreAuthorize
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        // ex.getMessage() thường là "Access is denied"
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403 Forbidden
                .body(ApiResponse.error("Bạn không có quyền thực hiện hành động này.")); // Đã dịch
    }
    
    // Bắt tất cả các lỗi 500 khác - Dùng ApiResponse.error(message)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        ex.printStackTrace();
        
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Đã xảy ra lỗi trên máy chủ: " + ex.getMessage())); // Đã dịch
    }
}