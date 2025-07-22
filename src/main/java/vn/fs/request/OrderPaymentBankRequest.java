package vn.fs.request;


import lombok.Data;

import java.util.List;

@Data
public class OrderPaymentBankRequest {

    private Long orderId; // ✅ Dùng orderId thay vì invCode
    private double amount;
    private String customerCode;
    private String address;
    private String phone;
    private String description;
    private List<ItemDTO> items;

    @Data
    public static class ItemDTO {
        private Long productId;  // ✅ Dùng productId thay vì productCode
        private String name;     // Tên sản phẩm
        private int quantity;    // Số lượng
        private double price;      // Đơn giá
    }
}