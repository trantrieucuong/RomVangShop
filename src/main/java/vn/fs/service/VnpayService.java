package vn.fs.service;


import org.springframework.stereotype.Service;
import vn.fs.config.Config;
import vn.fs.entities.User;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VnpayService {

    public String createPaymentUrl(int amount, User customer, String cartCode, String orderCode) {
        try {
            if (orderCode == null || orderCode.isEmpty()) {
                orderCode = "hd" + UUID.randomUUID().toString().substring(0, 6);
            }

            String vnp_TxnRef = orderCode;
            String vnp_IpAddr = "127.0.0.1";
            String vnp_TmnCode = Config.vnp_TmnCode;
            String vnp_HashSecret = Config.secretKey;
            String orderInfo = "Thanh toan don hang " + orderCode;
            String vnp_ReturnUrl = Config.vnp_ReturnUrl;

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", Config.vnp_Version);
            vnp_Params.put("vnp_Command", Config.vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNĐ nhân 100
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", cartCode); // ✅ Truyền cartCode
            vnp_Params.put("vnp_OrderType", Config.vnp_OrderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);



            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String vnp_CreateDate = LocalDateTime.now().format(formatter);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            // Bước 1: sắp xếp key theo thứ tự alpha
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String name : fieldNames) {
                String value = vnp_Params.get(name);
                if (value != null && !value.isEmpty()) {
                    hashData.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8)).append('&');
                    query.append(URLEncoder.encode(name, StandardCharsets.UTF_8))
                            .append('=')
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                            .append('&');
                }
            }

            // Xóa dấu & cuối
            if (hashData.length() > 0) {
                hashData.setLength(hashData.length() - 1);
                query.setLength(query.length() - 1);
            }

            String secureHash = Config.hmacSHA512(vnp_HashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);

            return Config.vnp_PayUrl + "?" + query.toString();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo URL VNPay", e);
        }
    }
}
