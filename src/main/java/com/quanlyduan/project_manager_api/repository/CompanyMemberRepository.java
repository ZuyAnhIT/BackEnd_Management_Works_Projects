// File: src/main/java/com/quanlyduan/project_manager_api/repository/CompanyMemberRepository.java
package com.quanlyduan.project_manager_api.repository;

import com.quanlyduan.project_manager_api.model.CompanyMember; // Đã dịch
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Integer> { // Đã dịch
    
    boolean existsByCompany_IdAndUser_Email(Integer companyId, String email); // Đã dịch

    // Lấy tất cả thành viên của công ty
    List<CompanyMember> findByCompany_Id(Integer companyId); // Đã dịch

    // (Bảo mật) Kiểm tra xem user có phải là thành viên không
    boolean existsByCompany_IdAndUser_Id(Integer companyId, Integer userId); // Đã dịch


    Optional<CompanyMember> findByCompany_IdAndUser_Id(Integer companyId, Integer userId); // Đã dịch
    
    
    List<CompanyMember> findByUser_Id(Integer userId); // Đã dịch

    /**
     * Tìm thành viên đang HOẠT ĐỘNG theo Company ID và User ID.
     * Dùng cho kiểm tra bảo mật.
     */
    Optional<CompanyMember> findByCompany_IdAndUser_IdAndStatus(Integer companyId, Integer userId, MemberStatus status);

    
    /**
     * Kiểm tra thành viên HOẠT ĐỘNG có tồn tại không.
     */
    boolean existsByCompany_IdAndUser_IdAndStatus(Integer companyId, Integer userId, MemberStatus status);

}