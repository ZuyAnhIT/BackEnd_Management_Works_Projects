package com.quanlyduan.project_manager_api.model.common.enums;

/**
 * Chu kỳ thanh toán của gói cước SaaS.
 */
public enum BillingCycle {
    MONTHLY,  // Trả phí hàng tháng (Cộng thêm 30 ngày)
    YEARLY    // Trả phí hàng năm (Cộng thêm 365 ngày - thường sẽ có giá ưu đãi hơn)
}