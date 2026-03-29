package com.quanlyduan.project_manager_api.util;

import java.util.Collection;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;

import org.springframework.data.jpa.domain.Specification;

/**
 * Lop tien ich ho tro xay dung cac truy van dong (Dynamic Query) su dung JPA Specification.
 */
public final class JpaSpecificationUtil {

    private static final String LIKE_PATTERN = "%%%s%%";

    // Ngan chan viec khoi tao doi tuong tu lop tien ich
    private JpaSpecificationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Tao dieu kien so sanh bang (EQUAL).
     * Thuong dung cho cac truong ID, Boolean, Enum hoac cac truong can khop chinh xac.
     * * @param field Ten truong trong Entity.
     * @param value Gia tri can so sanh.
     * @return Specification ket qua (Conjunction neu gia tri null).
     */
    public static <T> Specification<T> attributeEquals(String field, Object value) {
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
    }

    /**
     * Tao dieu kien tim kiem theo chuoi (LIKE) - Khong phan biet chu hoa/thuong.
     * * @param field Ten truong trong Entity.
     * @param value Gia tri chuoi can tim kiem.
     * @return Specification ket qua (Conjunction neu gia tri null/rong).
     */
    public static <T> Specification<T> attributeContains(String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        String pattern = String.format(LIKE_PATTERN, value.toLowerCase());
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), pattern);
    }

    /**
     * Tao dieu kien tim kiem trong khoang (BETWEEN).
     * Ho tro tim kiem chi theo gia tri toi thieu (>= min) hoac chi theo gia tri toi da (<= max).
     * * @param field Ten truong trong Entity.
     * @param min Gia tri toi thieu (co bao gom).
     * @param max Gia tri toi đa (co bao gom).
     * @return Specification ket qua.
     */
    public static <T, Y extends Comparable<? super Y>> Specification<T> attributeBetween(String field, Y min, Y max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return criteriaBuilder.conjunction();
            }

            Path<Y> path = root.get(field);
            if (min != null && max != null) {
                return criteriaBuilder.between(path, min, max);
            } else if (min != null) {
                return criteriaBuilder.greaterThanOrEqualTo(path, min);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(path, max);
            }
        };
    }

    /**
     * Tao dieu kien tim kiem trong mot danh sach cac gia tri (IN).
     * * @param field Ten truong trong Entity.
     * @param values Tap hop cac gia tri hop le.
     * @return Specification ket qua.
     */
    public static <T> Specification<T> attributeIn(String field, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        return (root, query, criteriaBuilder) -> root.get(field).in(values);
    }

    /**
     * Tao dieu kien tim kiem chuoi tren bang lien ket (LEFT JOIN).
     * Su dung LEFT JOIN de dam bao khong mat du lieu neu truong lien ket bi null.
     * * @param joinAttribute Ten thuoc tinh doi tuong can lien ket (JOIN).
     * @param field Ten truong trong doi tuong duoc lien ket.
     * @param value Gia tri tim kiem.
     * @return Specification ket qua.
     */
    public static <T, R> Specification<T> attributeContainsJoin(String joinAttribute, String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        String pattern = String.format(LIKE_PATTERN, value.toLowerCase());

        return (root, query, criteriaBuilder) -> {
            Join<T, R> join = root.join(joinAttribute, JoinType.LEFT);
            return criteriaBuilder.like(criteriaBuilder.lower(join.get(field)), pattern);
        };
    }
}