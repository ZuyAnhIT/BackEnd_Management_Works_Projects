package com.quanlyduan.project_manager_api.repository.specification;

import com.quanlyduan.project_manager_api.dto.request.TagFilterRequest;
import com.quanlyduan.project_manager_api.model.Tag;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class TagSpecification {

    public static Specification<Tag> getFilterSpec(Integer projectId, TagFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. BẮT BUỘC: Lọc theo Project ID
            predicates.add(cb.equal(root.get("project").get("id"), projectId));

            if (filter != null) {
                // 2. Lọc theo Keyword (Tìm kiếm gần đúng trong Name hoặc Description)
                if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                    String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                    Predicate hasName = cb.like(cb.lower(root.get("name")), pattern);
                    Predicate hasDesc = cb.like(cb.lower(root.get("description")), pattern);
                    predicates.add(cb.or(hasName, hasDesc));
                }

                // 3. Lọc theo danh sách tên (Multi terms - VD: user check chọn "Bug" và "Urgent")
                if (filter.getNames() != null && !filter.getNames().isEmpty()) {
                    predicates.add(root.get("name").in(filter.getNames()));
                }

                // 4. Lọc theo người tạo (Created By)
                if (filter.getCreatedById() != null) {
                    predicates.add(cb.equal(root.get("createdBy").get("id"), filter.getCreatedById()));
                }

                // 5. Lọc theo ngày tạo (From - To)
                if (filter.getCreatedFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
                }
                if (filter.getCreatedTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
                }
                
                // (Chờ DB update): Nếu có cột status/updatedAt thì thêm logic vào đây
            }

            // Kết hợp tất cả điều kiện bằng AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}