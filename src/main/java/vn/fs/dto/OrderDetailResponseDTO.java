package vn.fs.dto;

import lombok.Data;

@Data
public class OrderDetailResponseDTO {
    private String productName;
    private int quantity;
    private double price;
}


