package iuh.fit.order_service.model;

public enum OrderStatus {
    CREATED,      // Khách hàng vừa tạo đơn
    CONFIRMED,    // Admin xác nhận đơn hàng
    PREPARING,    // Nhà hàng đang chuẩn bị món
    DELIVERING,   // Đang giao hàng
    DELIVERED,    // Giao thành công
    CANCELED,     // Khách huỷ
    FAILED        // Admin từ chối / hết hàng
}


