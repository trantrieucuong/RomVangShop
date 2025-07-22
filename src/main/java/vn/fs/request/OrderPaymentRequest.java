package vn.fs.request;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderPaymentRequest {
    private Long orderId; // <-- Dùng Long vì orderId là khóa chính trong entity Order
    private String paymentMethod;
    private String address;
    private String phone;
    private String customerCode; // Có thể null nếu là khách lẻ
    private String description;  // Ví dụ: "Khách lẻ"
    private List<ItemDTO> items; // Danh sách sản phẩm trong đơn

    @Getter
    @Setter
    public static class ItemDTO {
        private Long productId; // <-- sửa lại theo entity Product
        private String name;    // Tên sản phẩm (không bắt buộc nếu bạn đã có productId)
        private int quantity;
        private double price;   // kiểu double vì entity Product dùng double
    }
}
