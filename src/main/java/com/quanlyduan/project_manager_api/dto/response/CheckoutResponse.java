package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
    
    // Mã đơn hàng của hệ thống chúng ta (để hiển thị cho khách hàng lưu vết)
    private String transactionCode; 
    
    // Đường link cực kỳ quan trọng do PayOS trả về (Frontend mở link này lên để hiển thị mã QR)
    private String checkoutUrl;     
}