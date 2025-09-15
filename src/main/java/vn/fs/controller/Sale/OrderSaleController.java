package vn.fs.controller.Sale;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.fs.dto.OrderDetailResponseDTO;
import vn.fs.dto.OrderResponseDTO;
import vn.fs.entities.*;
import vn.fs.repository.*;
import vn.fs.request.OrderPaymentBankRequest;
import vn.fs.request.OrderPaymentRequest;
import vn.fs.service.OrderPaymentService;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderSaleController {

    @Autowired
    private OrderRepository orderRepository;
//    @Autowired
//    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderDetailRepository orderDetailRepository;
    @Autowired
    OrderPaymentService orderPaymentService;
//    @Autowired
//    CustomerRepository customerRepository;
//    @Autowired
//    InvoiceItemRepository invoiceItemRepository;
//    @Autowired
//    InvoiceRepository invoiceRepository;
//    @Autowired
//    OrderPaymentService orderPaymentService;
    @Autowired
    CategoryRepository categoryRepository;

    private PayOS payOS;

    public OrderSaleController(PayOS payOS) {
        super();
        this.payOS = payOS;
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> createNewOrder() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long nextId = generateNextOrderId();
        String displayCode = String.format("DH%06d", nextId);

        Order order = new Order();
        order.setOrderId(nextId);
        order.setOrderDate(new Date());
        order.setAmount(0.0);
        order.setStatus(0);
        order.setNote("Đơn hàng mới tạo");
        order.setUser(user);

        orderRepository.save(order);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", nextId);          // ✅ Trả về orderId
        response.put("orderCode", displayCode);   // ✅ Trả về mã hiển thị

        return ResponseEntity.ok(response);
    }



    private Long generateNextOrderId() {
        Long maxId = orderRepository.findMaxOrderId();
        return (maxId != null ? maxId : 0L) + 1;
    }

    private String generateOrderDisplayCode(Long id) {
        return String.format("DH%06d", id); // 👉 đây là nơi sinh mã có "DH"
    }


//    private String generateNextOrderCode() {
//        String prefix = "DH";
//        String maxCode = orderRepository.findMaxOrderCodeStartingWith(prefix);
//
//        int nextNumber = 1;
//        if (maxCode != null && maxCode.length() == 8) {
//            try {
//                nextNumber = Integer.parseInt(maxCode.substring(2)) + 1;
//            } catch (NumberFormatException ignored) {
//            }
//        }
//
//        return String.format("%s%06d", prefix, nextNumber);
//    }

//    private String generateNextInvoiceCode() {
//        String prefix = "INV";
//        String maxCode = invoiceRepository.findMaxInvoiceCode();
//        int next = (maxCode != null && maxCode.startsWith(prefix))
//                ? Integer.parseInt(maxCode.substring(3)) + 1
//                : 1;
//        return String.format("%s%06d", prefix, next);
//    }
//
//    private String generateNextInvoiceItemCode() {
//        String prefix = "IVI";
//        String maxCode = invoiceItemRepository.findMaxInvoiceItemCode();
//        int next = (maxCode != null && maxCode.startsWith(prefix))
//                ? Integer.parseInt(maxCode.substring(3)) + 1
//                : 1;
//        return String.format("%s%06d", prefix, next);
//    }
//
@PostMapping("/thanh-toan")
public ResponseEntity<?> thanhToanDonHang(@RequestBody OrderPaymentRequest request) {
    try {
        System.out.println(">> Đã nhận request thanh toán: " + new ObjectMapper().writeValueAsString(request));
        OrderResponseDTO response = orderPaymentService.processCashPayment(request);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Lỗi thanh toán: " + e.getMessage());
    }
}



    @PostMapping("/thanh-toan-online")
    public ResponseEntity<?> thanhToanDonHangOnline(@RequestBody OrderPaymentBankRequest request) {
        try {
            System.out.println("🔰 Nhận request: " + request);

            if (request.getItems() == null || request.getItems().isEmpty()) {
                System.out.println("⚠️ Không có sản phẩm nào trong đơn hàng!");
            } else {
                for (OrderPaymentBankRequest.ItemDTO item : request.getItems()) {
                    System.out.println("✅ Sản phẩm: " + item.getProductId() + ", SL: " + item.getQuantity());
                }
            }

            OrderResponseDTO response = orderPaymentService.processPaymentOnline(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi xử lý thanh toán online: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("status", "INTERNAL_SERVER_ERROR", "message", e.getMessage())
            );
        }
    }






    @PostMapping("/createPaymentLinkSale")
    public void checkoutSale(HttpServletRequest request, HttpServletResponse httpServletResponse,
                             @RequestParam("orderId") Long orderId,
                             @RequestParam("amount") int amount,
                             @RequestParam(value = "userId", required = false) Long userId,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "address", required = false) String address,
                             @RequestParam(value = "description", required = false) String description) {
        try {
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);

            List<OrderPaymentBankRequest.ItemDTO> itemDTOList = orderDetails.stream().map(detail -> {
                OrderPaymentBankRequest.ItemDTO dto = new OrderPaymentBankRequest.ItemDTO();
                dto.setProductId(detail.getProduct().getProductId());
                dto.setName(detail.getProduct().getProductName());
                dto.setQuantity(detail.getQuantity());
                dto.setPrice(detail.getPrice());
                return dto;
            }).collect(Collectors.toList());

            OrderPaymentBankRequest paymentRequest = new OrderPaymentBankRequest();
            paymentRequest.setOrderId(orderId);
            paymentRequest.setAmount((double) amount);
            paymentRequest.setItems(itemDTOList);
            paymentRequest.setUserId(userId);
            paymentRequest.setPhone(phone);
            paymentRequest.setAddress(address);
            paymentRequest.setDescription(description);

            orderPaymentService.processPaymentOnline(paymentRequest);

            request.getSession().setAttribute("pendingOrderId", orderId);

            String baseUrl = getBaseUrl(request);
            String payDesc = "Thanh toán DH" + orderId;
            String returnUrl = baseUrl + "/sale/thanh-toan-online";
            String cancelUrl = baseUrl + "/sale/saleHome";

            long orderCode = System.currentTimeMillis() % 1_000_000;

            ItemData item = ItemData.builder()
                    .name("Rơm Vàng")
                    .quantity(1)
                    .price(amount)
                    .build();

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(payDesc)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .item(item)
                    .build();

            CheckoutResponseData data = payOS.createPaymentLink(paymentData);
            httpServletResponse.setHeader("Location", data.getCheckoutUrl());
            httpServletResponse.setStatus(302);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        String url = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            url += ":" + serverPort;
        }
        url += contextPath;
        return url;
    }



}
