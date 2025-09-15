package vn.fs.request;


import lombok.Getter;
import lombok.Setter;
import vn.fs.entities.User;

import java.util.List;

@Getter
@Setter
public class OrderPaymentRequest {
    private Long orderId;
    private String paymentMethod;
    private String address;
    private String phone;
    private Integer customerCode;
    private String description;
    private Long userId;
    private List<ItemDTO> items;
    @Getter
    @Setter
    public static class ItemDTO {
        private Long productId; // <-- sửa lại theo entity Product
        private String name;    // Tên sản phẩm (không bắt buộc nếu bạn đã có productId)
        private int quantity;
        private double price;   // kiểu double vì entity Product dùng double
    }
}
