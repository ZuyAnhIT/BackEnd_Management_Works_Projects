package com.quanlyduan.project_manager_api.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;

/**
 * Lop tap trung xu ly tat ca cac ngoai le cua he thong.
 * Chuyen doi cac loi thanh dinh dang phan hoi chuan ApiResponse.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Khai bao cac hang so thong bao loi
    private static final String MSG_VALIDATION_FAILED = "Input validation failed";
    private static final String MSG_AUTH_FAILED = "Authentication failed";
    private static final String MSG_BAD_CREDENTIALS = "Invalid email or password";
    private static final String MSG_ACCESS_DENIED = "Access denied: You do not have permission for this action";
    private static final String MSG_INTERNAL_ERROR = "An unexpected error occurred: ";

    // Constructor viet thu cong
    public GlobalExceptionHandler() {
        super();
    }

    // ======================================================
    // 1. XU LY LOI DU LIEU VA NGHERP VU (HTTP 400, 404, 402)
    // ======================================================

    /**
     * Xu ly loi khi du lieu dau vao khong hop le theo cau hinh Validation.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpHeaders headers,
            HttpStatusCode status, 
            WebRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> errorResponse = ApiResponse.error(MSG_VALIDATION_FAILED, validationErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Bat cac loi nghiep vu sai lech yeu cau tu phia Client.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Xu ly loi khi khong tim thay tai nguyen yeu cau trong he thong.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Xu ly loi khi cong ty vuot qua han muc tai nguyen cua goi cuoc.
     */
    @ExceptionHandler(OverageException.class)
    public ResponseEntity<ApiResponse<Object>> handleOverageException(OverageException ex) {
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ======================================================
    // 2. XU LY BAO MAT VA PHAN QUYEN (HTTP 401, 403)
    // ======================================================

    /**
     * Xu ly loi xac thuc nguoi dung nhu sai mat khau hoac token khong hop le.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        String message = MSG_AUTH_FAILED;
        
        if (ex instanceof BadCredentialsException) {
            message = MSG_BAD_CREDENTIALS;
        } else if (ex.getMessage() != null) {
            message = ex.getMessage();
        }
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message));
    }
    
    /**
     * Xu ly loi khi nguoi dung da dang nhap nhung khong co quyen truy cap tai nguyen.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(MSG_ACCESS_DENIED));
    }
    
    // ======================================================
    // 3. XU LY LOI HE THONG (HTTP 500)
    // ======================================================

    /**
     * Bat tat ca cac loi chua duoc phan loai cu the de dam bao he thong khong bi ngat quang.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        // Ghi log loi he thong de phuc vu tra cuu sau nay
        logger.error("Internal System Error: ", ex);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(MSG_INTERNAL_ERROR + ex.getMessage())); 
    }
}