package com.quanlyduan.project_manager_api.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.PageResponseDTO;
import com.quanlyduan.project_manager_api.dto.response.company.AdminCompanyResponse;
import com.quanlyduan.project_manager_api.dto.response.company.Tenant360Response;
import com.quanlyduan.project_manager_api.service.CompanyAdminService;

/**
 * Controller quản lý thông tin công ty dành cho quyền Quản trị viên hệ thống.
 */
@RestController
@RequestMapping("/api/admin/companies")
public class AdminCompanyController {

    // Khai báo các hằng số mặc định cho phân trang, tìm kiếm và trạng thái
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE = "10";
    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final String DEFAULT_SORT_DIR = "desc";
    
    private static final String STATUS_SUSPENDED = "SUSPENDED";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final CompanyAdminService companyAdminService;

    // Khởi tạo thủ công để tiêm phụ thuộc thay vì dùng RequiredArgsConstructor
    public AdminCompanyController(CompanyAdminService companyAdminService) {
        this.companyAdminService = companyAdminService;
    }

    /**
     * Lấy danh sách tất cả các công ty có hỗ trợ phân trang và sắp xếp.
     */
    @GetMapping
    @PreAuthorize("@securityService.hasSystemPermission('tenant:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<AdminCompanyResponse>>> getCompanies(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIR) String sortDir) {

        PageResponseDTO<AdminCompanyResponse> response = companyAdminService.getCompanies(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Fetched companies successfully.", response));
    }

    /**
     * Tìm kiếm công ty theo nhiều tiêu chí kết hợp.
     */
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasSystemPermission('tenant:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<AdminCompanyResponse>>> searchCompanies(
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) String searchCode,
            @RequestParam(required = false) String searchEmail,
            @RequestParam(required = false) String searchStatus,
            @RequestParam(required = false) String searchPlanCode,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIR) String sortDir) {

        PageResponseDTO<AdminCompanyResponse> response = companyAdminService.searchCompanies(
                searchName, searchCode, searchEmail, searchStatus, searchPlanCode, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success("Searched companies successfully.", response));
    }

    /**
     * Lấy thông tin chi tiết toàn diện của một công ty.
     */
    @GetMapping("/{companyId}/detail")
    @PreAuthorize("@securityService.hasSystemPermission('tenant:view')")
    public ResponseEntity<ApiResponse<Tenant360Response>> getCompany360ViewForAdmin(
            @PathVariable Integer companyId) {
            
        Tenant360Response response = companyAdminService.getTenant360View(companyId);
        return ResponseEntity.ok(ApiResponse.success("Fetched tenant 360 view successfully.", response));
    }
    
    /**
     * Đình chỉ hoạt động của một công ty.
     */
    @PutMapping("/{companyId}/suspend")
    @PreAuthorize("@securityService.hasSystemPermission('tenant:suspend')")
    public ResponseEntity<ApiResponse<Void>> suspendCompany(@PathVariable Integer companyId) {
        companyAdminService.changeCompanyStatus(companyId, STATUS_SUSPENDED);
        return ResponseEntity.ok(ApiResponse.success("Company has been suspended successfully.", null));
    }

    /**
     * Kích hoạt lại hoạt động của một công ty đã bị đình chỉ.
     */
    @PutMapping("/{companyId}/activate")
    @PreAuthorize("@securityService.hasSystemPermission('tenant:suspend')")
    public ResponseEntity<ApiResponse<Void>> activateCompany(@PathVariable Integer companyId) {
        companyAdminService.changeCompanyStatus(companyId, STATUS_ACTIVE);
        return ResponseEntity.ok(ApiResponse.success("Company has been activated successfully.", null));
    }

    /**
     * Xem lich su giao dich (hoa don) cua mot cong ty cu the.
     */
    @GetMapping("/{companyId}/transactions")
    @PreAuthorize("@securityService.hasSystemPermission('tenant:view')")
    public ResponseEntity<ApiResponse<PageResponseDTO<com.quanlyduan.project_manager_api.dto.response.TransactionHistoryResponse>>> getCompanyTransactions(
            @PathVariable Integer companyId,
            @RequestParam(required = false) com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIR) String sortDir) {

        PageResponseDTO<com.quanlyduan.project_manager_api.dto.response.TransactionHistoryResponse> response = 
                companyAdminService.getCompanyTransactionHistory(companyId, status, startDate, endDate, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success("Retrieved company transaction history successfully.", response));
    }
}