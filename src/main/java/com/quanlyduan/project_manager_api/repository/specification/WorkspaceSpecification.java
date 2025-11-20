// File: src/main/java/com/quanlyduan/project_manager_api/repository/specification/WorkspaceSpecification.java
package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.model.Workspace;
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import com.quanlyduan.project_manager_api.util.JpaSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

public class WorkspaceSpecification {

    public static Specification<Workspace> filterWorkspaces(
            Integer companyId,
            String searchName,         // Tìm theo Tên
            String searchCode,         // Tìm theo Mã
            String searchDescription,  // Tìm theo Mô tả
            WorkspaceStatus searchStatus // Tìm theo Trạng thái
    ) {
        Specification<Workspace> spec = (root, query, cb) -> 
                cb.equal(root.get("company").get("id"), companyId);

        // 2. Tìm theo Tên (name)
        if (searchName != null && !searchName.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("name", searchName));
        }

        // 3. Tìm theo Mã (workspaceCode)
        if (searchCode != null && !searchCode.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("workspaceCode", searchCode));
        }

        // 4. Tìm theo Mô tả (description)
        if (searchDescription != null && !searchDescription.isEmpty()) {
            spec = spec.and(JpaSpecificationUtil.attributeContains("description", searchDescription));
        }

        // 5. Tìm theo Trạng thái (status)
        if (searchStatus != null) {
            spec = spec.and(JpaSpecificationUtil.attributeEquals("status", searchStatus));
        }

        return spec;
    }
}