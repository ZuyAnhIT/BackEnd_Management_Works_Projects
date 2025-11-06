package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CompanyMember;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Integer> {
    
    boolean existsByCongTy_IdCongTyAndNguoiDung_Email(Integer congTyId, String email);

    // Lấy tất cả thành viên của công ty
    List<CompanyMember> findByCongTy_IdCongTy(Integer congTyId);

    // (Bảo mật) Kiểm tra xem user có phải là thành viên không
    boolean existsByCongTy_IdCongTyAndNguoiDung_IdNguoiDung(Integer congTyId, Integer nguoiDungId);


    Optional<CompanyMember> findByCongTy_IdCongTyAndNguoiDung_IdNguoiDung(Integer congTyId, Integer nguoiDungId);
    
    
    List<CompanyMember> findByNguoiDung_IdNguoiDung(Integer nguoiDungId);
}