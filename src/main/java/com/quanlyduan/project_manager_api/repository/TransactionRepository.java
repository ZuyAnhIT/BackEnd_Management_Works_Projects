package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.Transaction;
import com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    // Tìm giao dịch dựa vào mã đơn hàng
    Optional<Transaction> findByTransactionCode(String transactionCode);

    List<Transaction> findByCompanyIdAndStatusAndIdNot(Integer companyId, TransactionStatus status, Integer id);
}