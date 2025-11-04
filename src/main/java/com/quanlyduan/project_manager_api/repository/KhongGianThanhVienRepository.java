package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.KhongGianThanhVien;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhongGianThanhVienRepository extends JpaRepository<KhongGianThanhVien, Integer> {

    // *** THÊM PHƯƠNG THỨC NÀY ***
    /**
     * Tìm kiếm tư cách thành viên dựa trên ID không gian và ID người dùng.
     * @param khongGianId ID của không gian
     * @param nguoiDungId ID của người dùng
     * @return Optional<KhongGianThanhVien>
     */
    Optional<KhongGianThanhVien> findByKhongGian_IdKhongGianAndNguoiDung_IdNguoiDung(
        Integer khongGianId, Integer nguoiDungId
    );
    
}