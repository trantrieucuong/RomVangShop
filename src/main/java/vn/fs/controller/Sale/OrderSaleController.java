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




    @GetMapping("/thanh-toan-online")
    public String showSuccessInvoice(@RequestParam("invCode") String maHoaDon, Model model) {

        List<Product> products = productRepository.findAll(); // lấy toàn bộ sản phẩm
        List<Category> categories = categoryRepository.findAll(); // lấy tất cả danh mục

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        return "sale/banhangoff/index";


    }
//
//
//
//
//    // Tạo mã mới kiểu OI000001
//    private String generateNextOrderItemCode() {
//        String prefix = "OI";
//        String maxCode = orderItemRepository.findMaxCode(); // ví dụ: OI000123
//        int nextNumber = 1;
//
//        if (maxCode != null && maxCode.startsWith(prefix)) {
//            try {
//                nextNumber = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
//            } catch (NumberFormatException e) {
//                System.err.println("Lỗi phân tích mã OrderItem: " + maxCode);
//            }
//        }
//
//        String result = String.format("%s%06d", prefix, nextNumber);
//        System.out.println("🔢 Mã OrderItem mới: " + result);
//        return result;
//    }
//
//        @PostMapping("/createPaymentLink")
//        public void checkout(HttpServletRequest request, HttpServletResponse httpServletResponse,
//                             @RequestParam("invCode") String maDonHang, @RequestParam("amount") int amount) {
//            try {
//                List<OrderItemDTO> orderItemDTOList = orderItemRepository.findByOrderCode(maDonHang);
//
//// Convert từ OrderItemDTO sang ItemDTO
//                List<OrderPaymentBankRequest.ItemDTO> itemDTOList = orderItemDTOList.stream().map(orderItem -> {
//                    OrderPaymentBankRequest.ItemDTO itemDTO = new OrderPaymentBankRequest.ItemDTO();
//                    itemDTO.setCode(orderItem.getCode());
//                    itemDTO.setName(orderItem.getName());
//                    itemDTO.setQty(orderItem.getQty());
//                    itemDTO.setPrice(orderItem.getPrice());
//                    return itemDTO;
//                }).collect(Collectors.toList());
//
//// Gán vào request
//                OrderPaymentBankRequest paymentRequest = new OrderPaymentBankRequest();
//                paymentRequest.setInvCode(maDonHang);
//                paymentRequest.setAmount(amount);
//                paymentRequest.setItems(itemDTOList);
//
//                // ✅ Gọi xử lý lưu hóa đơn
//                orderPaymentService.processPaymentOnline(paymentRequest);
//                // ✅ Bước 2: Tạo URL PayOS như cũ
//                final String baseUrl = getBaseUrl(request);
//                final String productName = "Rơm Vàng";
//                final String description = "Thanh toan " + maDonHang;
//                final String returnUrl = baseUrl + "/admin/banhangoff";
//                final String cancelUrl = baseUrl + "/admin/banhangoff";
//                final int price = amount;
//
//                // Gen order code
//                String currentTimeString = String.valueOf(new Date().getTime());
//                long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));
//                ItemData item = ItemData.builder().name(productName).quantity(1).price(price).build();
//                PaymentData paymentData = PaymentData.builder()
//                        .orderCode(orderCode)
//                        .amount(price)
//                        .description(description)
//                        .returnUrl(returnUrl)
//                        .cancelUrl(cancelUrl)
//                        .item(item)
//                        .build();
//
//                CheckoutResponseData data = payOS.createPaymentLink(paymentData);
//
//                // ✅ Bước 3: Redirect sang PayOS
//                String checkoutUrl = data.getCheckoutUrl();
//                httpServletResponse.setHeader("Location", checkoutUrl);
//                httpServletResponse.setStatus(302);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

    @PostMapping("/createPaymentLinkSale")
    public void checkoutSale(HttpServletRequest request, HttpServletResponse httpServletResponse,
                             @RequestParam("orderId") Long orderId,
                             @RequestParam("amount") int amount) {
        try {
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);

            // Chuyển sang DTO
            List<OrderPaymentBankRequest.ItemDTO> itemDTOList = orderDetails.stream().map(detail -> {
                OrderPaymentBankRequest.ItemDTO dto = new OrderPaymentBankRequest.ItemDTO();
                dto.setProductId(detail.getProduct().getProductId());
                dto.setName(detail.getProduct().getProductName());
                dto.setQuantity(detail.getQuantity());
                dto.setPrice(detail.getPrice());
                return dto;
            }).collect(Collectors.toList());

            // Gán vào request
            OrderPaymentBankRequest paymentRequest = new OrderPaymentBankRequest();
            paymentRequest.setOrderId(orderId);
            paymentRequest.setAmount(amount);
            paymentRequest.setItems(itemDTOList);

            // ✅ Gọi xử lý thanh toán
            orderPaymentService.processPaymentOnline(paymentRequest);

            // ✅ Lưu lại orderId vào Session để phục hồi khi quay lại
            request.getSession().setAttribute("pendingOrderId", orderId);

            // Tạo URL PayOS
            final String baseUrl = getBaseUrl(request);
            final String description = "Thanh toán DH" + orderId;
            final String returnUrl = baseUrl + "/sale/saleHome"; // Quay lại
            final String cancelUrl = baseUrl + "/sale/saleHome"; // Bấm hủy cũng quay lại

            String currentTimeString = String.valueOf(System.currentTimeMillis());
            long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));

            ItemData item = ItemData.builder()
                    .name("Rơm Vàng")
                    .quantity(1)
                    .price(amount)
                    .build();

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .item(item)
                    .build();

            CheckoutResponseData data = payOS.createPaymentLink(paymentData);

            // ✅ Redirect đến PayOS
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
