package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.NguoiDungRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NguoiDungRoleRepository extends JpaRepository<NguoiDungRole, Integer> {

    /**
     * Tìm tất cả các vai trò cấp hệ thống của một người dùng.
     * @param nguoiDungId ID của người dùng
     * @return Danh sách các NguoiDungRole
     */
    List<NguoiDungRole> findByNguoiDung_IdNguoiDung(Integer nguoiDungId);
}