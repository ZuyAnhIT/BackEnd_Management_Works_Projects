package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CongTyThanhVien;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CongTyThanhVienRepository extends JpaRepository<CongTyThanhVien, Integer> {
    
    boolean existsByCongTy_IdCongTyAndNguoiDung_Email(Integer congTyId, String email);

    // Lấy tất cả thành viên của công ty
    List<CongTyThanhVien> findByCongTy_IdCongTy(Integer congTyId);

    // (Bảo mật) Kiểm tra xem user có phải là thành viên không
    boolean existsByCongTy_IdCongTyAndNguoiDung_IdNguoiDung(Integer congTyId, Integer nguoiDungId);


    Optional<CongTyThanhVien> findByCongTy_IdCongTyAndNguoiDung_IdNguoiDung(Integer congTyId, Integer nguoiDungId);
    
    
    List<CongTyThanhVien> findByNguoiDung_IdNguoiDung(Integer nguoiDungId);
}