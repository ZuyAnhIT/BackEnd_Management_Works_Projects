package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Transaction;
import com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
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

}