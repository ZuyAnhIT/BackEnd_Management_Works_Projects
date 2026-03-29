package com.quanlyduan.project_manager_api.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.model.WorkspaceMember;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

/**
 * Lop cung cap cac bo loc dong cho thuc the WorkspaceMember.
 */
public class WorkspaceMemberSpecification {

    // Khai bao cac hang so ten truong trong Entity
    private static final String FIELD_WORKSPACE = "workspace";
    private static final String FIELD_ID = "id";
    private static final String FIELD_USER = "user";
    private static final String FIELD_FULL_NAME = "fullName";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_ROLE = "role";
    private static final String FIELD_ROLE_NAME = "roleName";
    private static final String FIELD_PHONE_NUMBER = "phoneNumber";

    // Constructor rieng tu de ngan viec khoi tao lop utility
    private WorkspaceMemberSpecification() {
    }

    /**
     * Tao bo loc dong dua tren nhieu tieu chi khac nhau.
     * @param workspaceId ID khong gian lam viec (Bat buoc)
     * @param searchName Ten thanh vien (Tim kiem gan dung)
     * @param searchEmail Email thanh vien (Tim kiem gan dung)
     * @param searchRoleName Ten vai tro (Tim kiem gan dung)
     * @param searchPhone So dien thoai (Tim kiem gan dung)
     */
    public static Specification<WorkspaceMember> filterMembers(
            Integer workspaceId,
            String searchName,      
            String searchEmail,     
            String searchRoleName,  
            String searchPhone      
    ) {
        // Khoi tao dieu kien bat buoc: Workspace ID
        Specification<WorkspaceMember> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get(FIELD_WORKSPACE).get(FIELD_ID), workspaceId);

        // Ap dung cac dieu kien loc tuy chon (Stepdown Rule)
        spec = addNameFilter(spec, searchName);
        spec = addEmailFilter(spec, searchEmail);
        spec = addRoleNameFilter(spec, searchRoleName);
        spec = addPhoneFilter(spec, searchPhone);

        return spec;
    }

    // ======================================================
    // CAC HAM PRIVATE HO TRO (STEPDOWN RULE)
    // ======================================================

    /**
     * Them bo loc theo ten nguoi dung (JOIN sang bang User).
     */
    private static Specification<WorkspaceMember> addNameFilter(Specification<WorkspaceMember> spec, String searchName) {
        if (searchName != null && !searchName.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_USER, FIELD_FULL_NAME, searchName));
        }
        return spec;
    }

    /**
     * Them bo loc theo email (JOIN sang bang User).
     */
    private static Specification<WorkspaceMember> addEmailFilter(Specification<WorkspaceMember> spec, String searchEmail) {
        if (searchEmail != null && !searchEmail.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_USER, FIELD_EMAIL, searchEmail));
        }
        return spec;
    }

    /**
     * Them bo loc theo ten vai tro (JOIN sang bang Role).
     */
    private static Specification<WorkspaceMember> addRoleNameFilter(Specification<WorkspaceMember> spec, String searchRoleName) {
        if (searchRoleName != null && !searchRoleName.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_ROLE, FIELD_ROLE_NAME, searchRoleName));
        }
        return spec;
    }

    /**
     * Them bo loc theo so dien thoai (JOIN sang bang User).
     */
    private static Specification<WorkspaceMember> addPhoneFilter(Specification<WorkspaceMember> spec, String searchPhone) {
        if (searchPhone != null && !searchPhone.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_USER, FIELD_PHONE_NUMBER, searchPhone));
        }
        return spec;
    }
}