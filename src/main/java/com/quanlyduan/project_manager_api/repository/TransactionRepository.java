package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Transaction;
import com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    // Tìm giao dịch dựa vào mã đơn hàng
    Optional<Transaction> findByTransactionCode(String transactionCode);

    List<Transaction> findByCompanyIdAndStatusAndIdNot(Integer companyId, TransactionStatus status, Integer id);

    // Tìm các giao dịch theo Trạng thái (PENDING) và được tạo TRƯỚC một mốc thời gian (15 phút trước)
    List<Transaction> findByStatusAndCreatedAtBefore(TransactionStatus status, LocalDateTime cutoffTime);

    @Query("SELECT t FROM Transaction t WHERE t.company.id = :companyId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR t.createdAt <= :endDate)")
    Page<Transaction> filterTransactions(
            @Param("companyId") Integer companyId,
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
            
    // Hàm tìm chi tiết bằng mã giao dịch
    Optional<Transaction> findByTransactionCodeAndCompanyId(String transactionCode, Integer companyId);
}