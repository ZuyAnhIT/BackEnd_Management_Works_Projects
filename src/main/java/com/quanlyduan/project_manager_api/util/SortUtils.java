package com.quanlyduan.project_manager_api.util;

import java.util.Map;
import org.springframework.data.domain.Sort;

/**
 * Lop tien ich ho tro tao doi tuong Sort an toan bang cach anh xa ten truong tu Client sang Entity.
 * Giup ngan chan loi khi Frontend truyen sai ten truong va dam bao tinh bao mat cho he thong.
 */
public final class SortUtils {

    private static final String ASCENDING_DIRECTION = "asc";
    private static final String ERR_INSTANTIATION = "This is a utility class and cannot be instantiated";

    // Ngan chan viec khoi tao doi tuong
    private SortUtils() {
        throw new UnsupportedOperationException(ERR_INSTANTIATION);
    }

    /**
     * Tao doi tuong Sort dua tren tham so dau vao va ban do anh xa truong (Field Mapping).
     * * @param sortByParam Ten truong sap xep tu Client (vd: "name").
     * @param sortDirParam Huong sap xep ("asc" hoac "desc").
     * @param defaultField Truong sap xep mac dinh cua Entity (vd: "createdAt").
     * @param fieldMapping Map anh xa tu ten Client sang ten thuc te trong Entity.
     * @return Doi tuong Sort da duoc cau hinh.
     */
    public static Sort createSort(String sortByParam,
                                  String sortDirParam,
                                  String defaultField,
                                  Map<String, String> fieldMapping) {

        // 1. Xac dinh huong sap xep (Mac dinh la DESC)
        Sort.Direction direction = ASCENDING_DIRECTION.equalsIgnoreCase(sortDirParam) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;

        // 2. Xac dinh truong sap xep thuc te
        String actualField = defaultField;
        
        if (sortByParam != null && !sortByParam.isBlank() && fieldMapping != null) {
            actualField = fieldMapping.getOrDefault(sortByParam, defaultField);
        }

        return Sort.by(direction, actualField);
    }
}