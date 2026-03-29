package com.quanlyduan.project_manager_api.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.model.CompanyMember;
import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

/**
 * Lop xay dung bo loc dong cho thuc the Thanh vien cong ty.
 * Ho tro tim kiem linh hoat tren nhieu truong du lieu va quan he lien ket.
 */
public class CompanyMemberSpecification {

    // Khai bao cac hang so ten truong de tranh hardcode
    private static final String FIELD_COMPANY = "company";
    private static final String FIELD_ID = "id";
    private static final String FIELD_USER = "user";
    private static final String FIELD_ROLE = "role";
    private static final String ATTR_FULL_NAME = "fullName";
    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_JOB_TITLE = "jobTitle";
    private static final String ATTR_ROLE_NAME = "roleName";
    private static final String ATTR_STATUS = "status";
    private static final String ATTR_PHONE = "phoneNumber";

    /**
     * Ham private de ngan viec khoi tao lop utility.
     */
    private CompanyMemberSpecification() {
    }

    /**
     * Tao bo loc dong dua tren cac tham so tim kiem cung cap tu Client.
     */
    public static Specification<CompanyMember> filterMembers(
            Integer companyId,
            String searchName,
            String searchEmail,
            String searchJobTitle,
            String searchRoleName,
            MemberStatus searchStatus,
            String searchPhone
    ) {
        // Khoi tao dieu kien bat buoc: Loc theo ID cong ty
        Specification<CompanyMember> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get(FIELD_COMPANY).get(FIELD_ID), companyId);

        // Ap dung cac dieu kien loc optional dua tren tham so dau vao
        spec = applyNameFilter(spec, searchName);
        spec = applyEmailFilter(spec, searchEmail);
        spec = applyJobTitleFilter(spec, searchJobTitle);
        spec = applyRoleFilter(spec, searchRoleName);
        spec = applyStatusFilter(spec, searchStatus);
        spec = applyPhoneFilter(spec, searchPhone);

        return spec;
    }

    // ======================================================
    // CAC HAM PRIVATE HO TRO (STEPDOWN RULE)
    // ======================================================

    private static Specification<CompanyMember> applyNameFilter(Specification<CompanyMember> spec, String name) {
        if (name != null && !name.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_USER, ATTR_FULL_NAME, name));
        }
        return spec;
    }

    private static Specification<CompanyMember> applyEmailFilter(Specification<CompanyMember> spec, String email) {
        if (email != null && !email.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_USER, ATTR_EMAIL, email));
        }
        return spec;
    }

    private static Specification<CompanyMember> applyJobTitleFilter(Specification<CompanyMember> spec, String jobTitle) {
        if (jobTitle != null && !jobTitle.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContains(ATTR_JOB_TITLE, jobTitle));
        }
        return spec;
    }

    private static Specification<CompanyMember> applyRoleFilter(Specification<CompanyMember> spec, String roleName) {
        if (roleName != null && !roleName.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_ROLE, ATTR_ROLE_NAME, roleName));
        }
        return spec;
    }

    private static Specification<CompanyMember> applyStatusFilter(Specification<CompanyMember> spec, MemberStatus status) {
        if (status != null) {
            return spec.and(JpaSpecificationUtil.attributeEquals(ATTR_STATUS, status));
        }
        return spec;
    }

    private static Specification<CompanyMember> applyPhoneFilter(Specification<CompanyMember> spec, String phone) {
        if (phone != null && !phone.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_USER, ATTR_PHONE, phone));
        }
        return spec;
    }
}