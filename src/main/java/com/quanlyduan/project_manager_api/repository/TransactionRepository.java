package com.quanlyduan.project_manager_api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.Transaction;
import com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus;

/**
 * Kho lưu trữ dữ liệu quản lý các giao dịch thanh toán (Transactions).
 * Hỗ trợ tra cứu hóa đơn, lọc lịch sử giao dịch và xử lý các giao dịch quá hạn.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    // Khai báo hằng số cho câu truy vấn lọc giao dịch nâng cao
    String FILTER_TRANSACTIONS_QUERY = "SELECT t FROM Transaction t WHERE t.company.id = :companyId " +
                                       "AND (:status IS NULL OR t.status = :status) " +
                                       "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
                                       "AND (:endDate IS NULL OR t.createdAt <= :endDate)";

    // ======================================================
    // 1. TRUY VẤN CHI TIẾT (RETRIEVAL)
    // ======================================================

    /**
     * Tìm kiếm giao dịch dựa trên mã đơn hàng duy nhất.
     * @param transactionCode Mã giao dịch từ cổng thanh toán hoặc hệ thống sinh ra.
     */
    Optional<Transaction> findByTransactionCode(String transactionCode);

    /**
     * Tìm kiếm chi tiết giao dịch theo mã và ID công ty để đảm bảo tính bảo mật.
     * @param transactionCode Mã giao dịch.
     * @param companyId ID của công ty sở hữu giao dịch.
     */
    Optional<Transaction> findByTransactionCodeAndCompanyId(String transactionCode, Integer companyId);

    // ======================================================
    // 2. TÌM KIẾM VÀ LỌC (FILTERING)
    // ======================================================

    /**
     * Lọc danh sách giao dịch của công ty theo trạng thái và khoảng thời gian.
     * Kết quả trả về hỗ trợ phân trang và sắp xếp.
     */
    @Query(FILTER_TRANSACTIONS_QUERY)
    Page<Transaction> filterTransactions(
            @Param("companyId") Integer companyId,
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ======================================================
    // 3. TIỆN ÍCH VÀ LẬP LỊCH (UTILITIES & SCHEDULER)
    // ======================================================

    /**
     * Tìm danh sách các giao dịch khác của cùng một công ty có cùng trạng thái.
     * Thường dùng để kiểm tra các giao dịch trùng lặp hoặc xử lý xung đột.
     */
    List<Transaction> findByCompanyIdAndStatusAndIdNot(Integer companyId, TransactionStatus status, Integer id);

    /**
     * Tìm các giao dịch có trạng thái cụ thể được tạo trước một mốc thời gian.
     * Phục vụ cho Scheduler để quét và hủy các giao dịch chờ thanh toán đã quá hạn (ví dụ: sau 15 phút).
     * @param status Trạng thái cần quét (thường là PENDING).
     * @param cutoffTime Mốc thời gian giới hạn để so sánh.
     */
    List<Transaction> findByStatusAndCreatedAtBefore(TransactionStatus status, LocalDateTime cutoffTime);

    // ======================================================
    // 4. BÁO CÁO THỐNG KÊ (FINANCIAL ANALYTICS)
    // ======================================================

    String SUM_REVENUE_BY_DATE_RANGE = 
        "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
        "WHERE t.status = :status AND t.paidAt >= :startDate AND t.paidAt <= :endDate";

    String GROUP_REVENUE_BY_PLAN = 
        "SELECT t.plan.name, SUM(t.amount) FROM Transaction t " +
        "WHERE t.status = :status AND t.paidAt >= :startDate AND t.paidAt <= :endDate " +
        "GROUP BY t.plan.name ORDER BY SUM(t.amount) DESC";

    String FIND_RECENT_TRANSACTIONS = 
        "SELECT t FROM Transaction t JOIN FETCH t.company c JOIN FETCH t.plan p " +
        "ORDER BY t.createdAt DESC";

    /**
     * Tinh tong doanh thu trong mot khoang thoi gian nhat dinh voi trang thai giao dich cu the.
     * Su dung truong paidAt (ngay thuc te tien ve) de tinh MRR chinh xac nhat.
     */
    @Query(SUM_REVENUE_BY_DATE_RANGE)
    BigDecimal sumRevenueByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate, 
                                              @Param("status") TransactionStatus status);

    /**
     * Gom nhom tong doanh thu theo ten goi cuoc (Plan Name).
     * Phuc vu viec ve bieu do tron (Pie Chart) the hien ty trong doanh thu.
     */
    @Query(GROUP_REVENUE_BY_PLAN)
    List<Object[]> groupRevenueByPlanAndDateRange(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate, 
                                                  @Param("status") TransactionStatus status);

    /**
     * Lay danh sach cac giao dich gan nhat tren he thong de hien thi tren Dashboard.
     */
    @Query(FIND_RECENT_TRANSACTIONS)
    List<Transaction> findRecentTransactions(Pageable pageable);
}