package com.quanlyduan.project_manager_api.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

/**
 * Lop xay dung bo loc dong cho thuc the Workspace.
 * Ho tro tim kiem theo cong ty, ten, ma va trang thai cua khong gian lam viec.
 */
public class WorkspaceSpecification {

    // Khai bao cac hang so ten truong trong Entity de tranh hardcode
    private static final String FIELD_COMPANY = "company";
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_WORKSPACE_CODE = "workspaceCode";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_STATUS = "status";

    // Constructor rieng tu de ngan viec khoi tao lop utility
    private WorkspaceSpecification() {
    }

    /**
     * Tao bo loc dong dua tren cac tham so tim kiem cung cap tu Client.
     * @param companyId ID cong ty hien tai (Dieu kien bat buoc)
     * @param searchName Tim theo ten khong gian lam viec
     * @param searchCode Tim theo ma khong gian lam viec
     * @param searchDescription Tim theo mo ta
     * @param searchStatus Tim theo trang thai (Enum)
     */
    public static Specification<Workspace> filterWorkspaces(
            Integer companyId,
            String searchName,      
            String searchCode,      
            String searchDescription,
            WorkspaceStatus searchStatus 
    ) {
        // 1. Dieu kien bat buoc: Workspace phai thuoc ve Company
        Specification<Workspace> spec = (root, query, cb) -> 
                cb.equal(root.get(FIELD_COMPANY).get(FIELD_ID), companyId);

        // 2. Ap dung cac dieu kien loc optional (Stepdown Rule)
        spec = applyNameFilter(spec, searchName);
        spec = applyCodeFilter(spec, searchCode);
        spec = applyDescriptionFilter(spec, searchDescription);
        spec = applyStatusFilter(spec, searchStatus);

        return spec;
    }

    // ======================================================
    // CAC HAM PRIVATE HO TRO (STEPDOWN RULE)
    // ======================================================

    /**
     * Loc theo ten Workspace su dung dieu kien LIKE.
     */
    private static Specification<Workspace> applyNameFilter(Specification<Workspace> spec, String searchName) {
        if (searchName != null && !searchName.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContains(FIELD_NAME, searchName));
        }
        return spec;
    }

    /**
     * Loc theo ma Workspace su dung dieu kien LIKE.
     */
    private static Specification<Workspace> applyCodeFilter(Specification<Workspace> spec, String searchCode) {
        if (searchCode != null && !searchCode.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContains(FIELD_WORKSPACE_CODE, searchCode));
        }
        return spec;
    }

    /**
     * Loc theo mo ta Workspace su dung dieu kien LIKE.
     */
    private static Specification<Workspace> applyDescriptionFilter(Specification<Workspace> spec, String searchDescription) {
        if (searchDescription != null && !searchDescription.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContains(FIELD_DESCRIPTION, searchDescription));
        }
        return spec;
    }

    /**
     * Loc theo trang thai (Enum) su dung dieu kien EQUAL.
     */
    private static Specification<Workspace> applyStatusFilter(Specification<Workspace> spec, WorkspaceStatus searchStatus) {
        if (searchStatus != null) {
            return spec.and(JpaSpecificationUtil.attributeEquals(FIELD_STATUS, searchStatus));
        }
        return spec;
    }
}