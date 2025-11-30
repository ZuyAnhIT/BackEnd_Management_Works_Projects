// File: src/main/java/com/quanlyduan/project_manager_api/util/SortUtils.java
package com.quanlyduan.project_manager_api.util;

import org.springframework.data.domain.Sort;

import java.util.Map;

public class SortUtils {

    /**
     * Tạo đối tượng Sort an toàn với khả năng ánh xạ tên trường.
     * Phương thức này giúp chuyển đổi tham số sắp xếp từ client (ví dụ: "name", "asc")
     * thành tên trường thực tế trong JPA Entity (ví dụ: "user.fullName") một cách an toàn,
     * đồng thời áp dụng trường mặc định nếu tham số không hợp lệ.
     * * @param sortByParam       Tên trường client gửi lên (vd: "name", "role")
     * @param sortDirectionParam Hướng sắp xếp ("asc", "desc")
     * @param defaultSortField  Trường mặc định trong Entity (vd: "createdAt")
     * @param fieldMapping      Map ánh xạ từ tên client -> tên Entity (vd: "name" -> "user.fullName")
     * @return Sort object
     */
    public static Sort createSort(String sortByParam,
                                  String sortDirectionParam,
                                  String defaultSortField,
                                  Map<String, String> fieldMapping) {

        // 1. Xác định hướng sắp xếp (Mặc định là DESC)
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirectionParam != null && sortDirectionParam.equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }

        // 2. Xác định trường sắp xếp thực tế trong Entity
        String actualFieldName = defaultSortField; // Mặc định là trường default

        if (sortByParam != null && !sortByParam.trim().isEmpty()) {
            // Kiểm tra trong map ánh xạ:
            if (fieldMapping != null && fieldMapping.containsKey(sortByParam)) {
                // Nếu tìm thấy, sử dụng tên trường thực tế
                actualFieldName = fieldMapping.get(sortByParam);
            } else if (fieldMapping == null || fieldMapping.isEmpty()) {
                // Trường hợp không có map ánh xạ (không an toàn): Giữ nguyên logic quay về default
                actualFieldName = defaultSortField;
            }
            // Nếu không có trong map, `actualFieldName` vẫn giữ giá trị `defaultSortField` (an toàn)
        }

        // 3. Tạo và trả về đối tượng Sort
        return Sort.by(direction, actualFieldName);
    }
}