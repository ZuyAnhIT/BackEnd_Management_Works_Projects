package com.quanlyduan.project_manager_api.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.model.ProjectMember;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

/**
 * Lop xay dung bo loc dong cho thuc the Thanh vien du an.
 * Ho tro tim kiem linh hoat tren nhieu truong du lieu thong qua quan he JOIN.
 */
public class ProjectMemberSpecification {

    // Khai bao cac hang so ten truong trong Entity de tranh hardcode
    private static final String FIELD_PROJECT = "project";
    private static final String FIELD_ID = "id";
    private static final String FIELD_USER = "user";
    private static final String FIELD_ROLE = "role";
    private static final String ATTR_FULL_NAME = "fullName";
    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_ROLE_NAME = "roleName";
    private static final String ATTR_PHONE = "phoneNumber";

    /**
     * Constructor rieng tu de ngan viec khoi tao lop utility.
     */
    private ProjectMemberSpecification() {
    }

    /**
     * Tao bo loc dong dua tren cac tham so tim kiem cung cap tu Client.
     * @param projectId ID du an hien tai (Dieu kien bat buoc)
     * @param searchName Tim theo ten nguoi dung
     * @param searchEmail Tim theo dia chi email
     * @param searchRoleName Tim theo ten vai tro trong du an
     * @param searchPhone Tim theo so dien thoai
     * @return Specification da ghep noi cac dieu kien loc
     */
    public static Specification<ProjectMember> filterMembers(
            Integer projectId,
            String searchName,
            String searchEmail,
            String searchRoleName,
            String searchPhone
    ) {
        // Khoi tao dieu kien bat buoc: Loc theo ID du an
        Specification<ProjectMember> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get(FIELD_PROJECT).get(FIELD_ID), projectId);

        // Ap dung cac dieu kien loc optional (Stepdown Rule)
        spec = applyNameFilter(spec, searchName);
        spec = applyEmailFilter(spec, searchEmail);
        spec = applyRoleFilter(spec, searchRoleName);
        spec = applyPhoneFilter(spec, searchPhone);

        return spec;
    }

    // ======================================================
    // CAC HAM HO TRO (STEPDOWN RULE)
    // ======================================================

    /**
     * Loc theo ho ten nguoi dung (Yeu cau JOIN den bang User).
     */
    private static Specification<ProjectMember> applyNameFilter(Specification<ProjectMember> spec, String name) {
        if (name != null && !name.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_USER, ATTR_FULL_NAME, name));
        }
        return spec;
    }

    /**
     * Loc theo dia chi email (Yeu cau JOIN den bang User).
     */
    private static Specification<ProjectMember> applyEmailFilter(Specification<ProjectMember> spec, String email) {
        if (email != null && !email.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_USER, ATTR_EMAIL, email));
        }
        return spec;
    }

    /**
     * Loc theo ten vai tro (Yeu cau JOIN den bang Role).
     */
    private static Specification<ProjectMember> applyRoleFilter(Specification<ProjectMember> spec, String roleName) {
        if (roleName != null && !roleName.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_ROLE, ATTR_ROLE_NAME, roleName));
        }
        return spec;
    }

    /**
     * Loc theo so dien thoai (Yeu cau JOIN den bang User).
     */
    private static Specification<ProjectMember> applyPhoneFilter(Specification<ProjectMember> spec, String phone) {
        if (phone != null && !phone.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_USER, ATTR_PHONE, phone));
        }
        return spec;
    }
}