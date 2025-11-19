// File: src/main/java/com/quanlyduan/project_manager_api/util/SortUtils.java
package com.quanlyduan.project_manager_api.util;

import org.springframework.data.domain.Sort;

import java.util.Map;

public class SortUtils {

    /**
     * Tạo đối tượng Sort an toàn với khả năng ánh xạ tên trường.
     * * @param sortByParam       Tên trường client gửi lên (vd: "name", "role")
     * @param sortDirectionParam Hướng sắp xếp ("asc", "desc")
     * @param defaultSortField  Trường mặc định trong Entity (vd: "joinedAt")
     * @param fieldMapping      Map ánh xạ từ tên client -> tên Entity (vd: "name" -> "user.fullName")
     * @return Sort object
     */
    public static Sort createSort(String sortByParam, 
                                  String sortDirectionParam, 
                                  String defaultSortField, 
                                  Map<String, String> fieldMapping) {

        // 1. Xác định hướng (Mặc định là DESC nếu không nói gì, vì ta thường muốn xem cái mới nhất)
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirectionParam != null && sortDirectionParam.equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }

        // 2. Xác định trường thực tế trong JPA
        String actualFieldName = defaultSortField;
        
        if (sortByParam != null && !sortByParam.trim().isEmpty()) {
            // Nếu có trong map ánh xạ thì lấy giá trị ánh xạ, nếu không thì bỏ qua về default
            if (fieldMapping != null && fieldMapping.containsKey(sortByParam)) {
                actualFieldName = fieldMapping.get(sortByParam);
            } else if (fieldMapping == null || fieldMapping.isEmpty()) {
                 // Trường hợp không dùng map (ít khi xảy ra nếu muốn an toàn), ta dùng chính value đó
                 // Nhưng tốt nhất nên dùng map để whitelist.
                 // Ở đây nếu không map được, ta quay về default để tránh lỗi 500
                 actualFieldName = defaultSortField; 
            }
        }

        return Sort.by(direction, actualFieldName);
    }
}