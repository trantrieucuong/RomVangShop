package vn.fs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDTO {
    private int status;
    private String message;
}
