package com.quanlyduan.project_manager_api.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;

/**
 * Lop xay dung bo loc dong cho thuc the Du an (Project).
 * Ho tro tim kiem linh hoat trong pham vi mot Khong gian lam viec (Workspace).
 */
public class ProjectSpecification {

    // Khai bao cac hang so ten truong de tranh hardcode
    private static final String FIELD_WORKSPACE = "workspace";
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PROJECT_CODE = "projectCode";
    private static final String FIELD_MANAGER = "manager";
    private static final String ATTR_FULL_NAME = "fullName";
    private static final String FIELD_STATUS = "status";

    /**
     * Constructor rieng tu de ngan viec khoi tao lop utility.
     */
    private ProjectSpecification() {
    }

    /**
     * Tao bo loc dong dua tren cac tham so tim kiem cung cap tu Client.
     * @param workspaceId ID khong gian lam viec (Dieu kien bat buoc)
     * @param searchName Tim kiem theo ten du an
     * @param searchCode Tim kiem theo ma du an
     * @param searchManager Tim kiem theo ten nguoi quan ly
     * @param searchStatus Tim kiem theo trang thai du an
     * @return Specification da ghep noi cac dieu kien loc
     */
    public static Specification<Project> filterProjects(
            Integer workspaceId,
            String searchName,
            String searchCode,
            String searchManager,
            ProjectStatus searchStatus
    ) {
        // Khoi tao dieu kien bat buoc: Loc theo ID Workspace
        Specification<Project> spec = (root, query, cb) -> 
                cb.equal(root.get(FIELD_WORKSPACE).get(FIELD_ID), workspaceId);

        // Ap dung cac dieu kien loc optional (Stepdown Rule)
        spec = applyNameFilter(spec, searchName);
        spec = applyCodeFilter(spec, searchCode);
        spec = applyManagerFilter(spec, searchManager);
        spec = applyStatusFilter(spec, searchStatus);

        return spec;
    }

    // ======================================================
    // CAC HAM PRIVATE HO TRO (STEPDOWN RULE)
    // ======================================================

    /**
     * Loc theo ten du an su dung dieu kien LIKE.
     */
    private static Specification<Project> applyNameFilter(Specification<Project> spec, String name) {
        if (name != null && !name.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContains(FIELD_NAME, name));
        }
        return spec;
    }

    /**
     * Loc theo ma du an su dung dieu kien LIKE.
     */
    private static Specification<Project> applyCodeFilter(Specification<Project> spec, String code) {
        if (code != null && !code.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContains(FIELD_PROJECT_CODE, code));
        }
        return spec;
    }

    /**
     * Loc theo ten nguoi quan ly (Yeu cau JOIN den bang User).
     */
    private static Specification<Project> applyManagerFilter(Specification<Project> spec, String managerName) {
        if (managerName != null && !managerName.isEmpty()) {
            return spec.and(JpaSpecificationUtil.attributeContainsJoin(FIELD_MANAGER, ATTR_FULL_NAME, managerName));
        }
        return spec;
    }

    /**
     * Loc theo trang thai du an su dung dieu kien EQUAL cho Enum.
     */
    private static Specification<Project> applyStatusFilter(Specification<Project> spec, ProjectStatus status) {
        if (status != null) {
            return spec.and(JpaSpecificationUtil.attributeEquals(FIELD_STATUS, status));
        }
        return spec;
    }
}