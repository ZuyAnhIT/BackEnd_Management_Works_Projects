package com.quanlyduan.project_manager_api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.CompanyInvitation;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;

/**
 * Kho lưu trữ dữ liệu cho thực thể lời mời tham gia công ty (Company Invitation).
 * Quản lý các trạng thái gửi, nhận và xác thực lời mời thông qua mã Token duy nhất.
 */
@Repository
public interface CompanyInvitationRepository extends JpaRepository<CompanyInvitation, Integer> {

    /**
     * Truy vấn thông tin lời mời dựa trên mã Token xác thực.
     * @param token Mã định danh duy nhất của lời mời.
     * @return Kết quả tìm kiếm dưới dạng Optional.
     */
    Optional<CompanyInvitation> findByToken(String token);

    /**
     * Kiểm tra xem một email đã có lời mời ở trạng thái chờ (PENDING) trong công ty hay chưa.
     * @param companyId ID của công ty.
     * @param email Địa chỉ email nhận lời mời.
     * @param status Trạng thái của lời mời (ví dụ: PENDING).
     * @return true nếu đã tồn tại lời mời thỏa mãn điều kiện.
     */
    boolean existsByCompany_IdAndEmailAndStatus(Integer companyId, String email, InvitationStatus status);

    /**
     * Đếm số lượng lời mời theo công ty và trạng thái cụ thể.
     * Thường dùng để tính toán lại phân trang hoặc phục vụ mục đích thống kê.
     * @param companyId ID của công ty.
     * @param status Trạng thái cần đếm.
     * @return Tổng số lượng lời mời tìm thấy.
     */
    long countByCompany_IdAndStatus(Integer companyId, InvitationStatus status);

    /**
     * Tìm kiếm lời mời theo công ty, trạng thái và lọc theo email (không phân biệt hoa thường).
     * Kết quả được trả về dưới dạng phân trang.
     * @param companyId ID của công ty.
     * @param status Trạng thái lời mời.
     * @param email Chuỗi email cần tìm kiếm (hỗ trợ tìm kiếm theo từ khóa).
     * @param pageable Cấu hình phân trang và sắp xếp.
     * @return Trang dữ liệu chứa danh sách lời mời.
     */
    Page<CompanyInvitation> findByCompany_IdAndStatusAndEmailContainingIgnoreCase(
            Integer companyId, 
            InvitationStatus status, 
            String email, 
            Pageable pageable
    );
}