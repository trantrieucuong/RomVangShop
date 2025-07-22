package vn.fs.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderResponseDTO {
        private Long orderId;
        private String note;
        private Date orderDate;
        private double amount;
        private String customerName;
        private List<OrderDetailResponseDTO> items;
}


