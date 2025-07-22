package vn.fs.request;

import lombok.Data;


@Data
public class OrderItemRequest {

        private Long orderId;
        private Long productId;
        private String name;
        private Double price;
        private Integer quantity;




}
