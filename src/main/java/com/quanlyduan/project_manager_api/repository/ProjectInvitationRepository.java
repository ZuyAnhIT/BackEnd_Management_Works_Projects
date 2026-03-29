package com.quanlyduan.project_manager_api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quanlyduan.project_manager_api.model.ProjectInvitation;
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;

/**
 * Kho luu tru du lieu cho thuc the loi moi tham gia Du an (Project Invitation).
 * Quan ly trang thai gui, nhan va xac thuc loi moi thong qua ma Token duy nhat.
 */
@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Integer> {

    /**
     * Tim kiem loi moi dua tren ma Token xac thuc duy nhat.
     * @param token Ma dinh danh cua loi moi.
     * @return Ket qua tim kiem duoi dang Optional.
     */
    Optional<ProjectInvitation> findByToken(String token);

    /**
     * Kiem tra su ton tai cua loi moi o trang thai cho (PENDING) cho mot email trong du an.
     * Dung de ngan chan viec gui loi moi trung lap cho cung mot nguoi.
     * @param projectId ID cua du an.
     * @param email Dia chi email nhan loi moi.
     * @param status Trang thai loi moi (vi du: PENDING).
     * @return true neu da ton tai loi moi thoa man dieu kien.
     */
    boolean existsByProject_IdAndEmailAndStatus(Integer projectId, String email, InvitationStatus status);

    /**
     * Tim kiem loi moi theo du an, trang thai va loc theo tu khoa email.
     * Ket qua tra ve ho tro phan trang va sap xep.
     * @param projectId ID cua du an.
     * @param status Trang thai cua loi moi.
     * @param email Chuoi email can tim kiem (khong phan biet hoa thuong).
     * @param pageable Cau hinh phan trang.
     * @return Trang du lieu chua danh sach loi moi.
     */
    Page<ProjectInvitation> findByProject_IdAndStatusAndEmailContainingIgnoreCase(
            Integer projectId, 
            InvitationStatus status, 
            String email, 
            Pageable pageable
    );
}