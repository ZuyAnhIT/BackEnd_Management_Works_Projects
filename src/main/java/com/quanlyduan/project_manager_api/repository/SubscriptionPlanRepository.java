package com.quanlyduan.project_manager_api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.SubscriptionPlan;

/**
 * Kho lưu trữ dữ liệu quản lý các Gói cước dịch vụ (Subscription Plans).
 * Hỗ trợ các thao tác cấu hình gói cước, kiểm tra mã gói và lọc danh sách gói đang kinh doanh.
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer>, JpaSpecificationExecutor<SubscriptionPlan> {

    // ======================================================
    // 1. TRUY VẤN THEO MÃ ĐỊNH DANH (PLAN CODE)
    // ======================================================

    /**
     * Tìm kiếm gói cước chính xác theo mã định danh (Ví dụ: "FREE", "PREMIUM").
     */
    Optional<SubscriptionPlan> findByPlanCode(String planCode);

    /**
     * Tìm kiếm gói cước theo mã định danh không phân biệt chữ hoa chữ thường.
     * Thường dùng cho các logic kiểm tra hoặc Scheduler.
     */
    Optional<SubscriptionPlan> findByPlanCodeIgnoreCase(String planCode);

    // ======================================================
    // 2. TRUY VẤN THEO TRẠNG THÁI (AVAILABILITY)
    // ======================================================

    /**
     * Lấy danh sách các gói cước đang ở trạng thái mở bán (Active).
     * Phục vụ cho giao diện hiển thị bảng giá cho khách hàng.
     */
    Page<SubscriptionPlan> findByIsActiveTrue(Pageable pageable);

    /**
     * Tìm kiếm chi tiết gói cước theo ID và đảm bảo gói đó vẫn đang hoạt động.
     * Dùng cho luồng đăng ký hoặc nâng cấp gói cước của khách hàng.
     */
    Optional<SubscriptionPlan> findByIdAndIsActiveTrue(Integer id);

    // ======================================================
    // 3. KIỂM TRA RÀNG BUỘC (VALIDATION)
    // ======================================================

    /**
     * Kiểm tra xem mã gói cước đã tồn tại trong hệ thống hay chưa.
     */
    boolean existsByPlanCode(String planCode);

    /**
     * Kiểm tra xem mã gói cước đã tồn tại ở một bản ghi khác (khác ID cung cấp) hay chưa.
     * Sử dụng để ràng buộc tính duy nhất của Plan Code trong luồng cập nhật.
     */
    boolean existsByPlanCodeAndIdNot(String planCode, Integer id);
}