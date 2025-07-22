package vn.fs.controller.Sale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.fs.entities.*;
import vn.fs.repository.*;
import vn.fs.request.OrderItemRequest;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/sale")
public class SaleController {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

//    @ModelAttribute(value = "user")
//    public User user(Model model, Principal principal, User user) {
//
//        if (principal != null) {
//            model.addAttribute("user", new User());
//            user = userRepository.findByEmail(principal.getName());
//            model.addAttribute("user", user);
//        }
//
//        return user;
//    }
@ModelAttribute("user")
public User user(Principal principal) {
    if (principal != null) {
        return userRepository.findByEmail(principal.getName());
    }
    return new User(); // tránh null pointer
}


    @GetMapping("/saleHome")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SALE')")
    public String saleHome(Model model, HttpSession session) {
        List<Product> products = productRepository.findAll();
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);

//        // ✅ Phục hồi đơn hàng nếu có từ session
//        Long pendingOrderId = (Long) session.getAttribute("pendingOrderId");
//        if (pendingOrderId != null) {
//            Order order = orderRepository.findById(pendingOrderId).orElse(null);
//            if (order != null) {
//                model.addAttribute("restoredOrder", order);
//            }
//            session.removeAttribute("pendingOrderId"); // ✅ Xóa sau khi sử dụng
//        }

        return "sale/banhangoff/index";
    }

//    @GetMapping("/qlKhachHang")
//    public String qlKhachHang(Model model) {
//
//        List<Customer> customers = customerRepositor  y.findAll();
//        model.addAttribute("customers", customers);
//        return "sale/customer/index"; // tên file HTML
//    }

//    @GetMapping("/banhangoff/products")
//    @ResponseBody
//    public List<Product> getProductsByCategory(@RequestParam(value = "category", required = false) String categoryCode) {
//        if (categoryCode == null || categoryCode.isEmpty()) {
//            return productRepository.findAll();
//        } else {
//            return productRepository.findByCategory_CategoryCode(categoryCode);
//        }
//    }
//    @GetMapping("/banhangoff/search")
//    @ResponseBody
//    public List<Product> searchProductsByName(@RequestParam String name) {
//        if (name == null || name.trim().isEmpty()) {
//            return productRepository.findAll();
//        }
//        return productRepository.findByNameContainingIgnoreCase(name.trim());
//    }
@PostMapping("/banhangoff/update-stock")
@ResponseBody
public ResponseEntity<?> updateStock(@RequestBody Map<String, Object> data) {
    try {
        // ✅ Đọc và ép kiểu productId từ JSON request
        Long productId = Long.valueOf(data.get("productId").toString());
        int quantityChange = (int) data.get("quantity"); // Âm là trừ, dương là cộng

        System.out.println("👉 Nhận yêu cầu cập nhật tồn kho:");
        System.out.println("- Mã sản phẩm: " + productId);
        System.out.println("- Thay đổi số lượng: " + quantityChange);

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            System.out.println("❌ Không tìm thấy sản phẩm có ID: " + productId);
            return ResponseEntity.status(404).body("Không tìm thấy sản phẩm");
        }

        Product product = productOpt.get();
        int currentQty = product.getQuantity();
        int newQty = currentQty + quantityChange;

        System.out.println("- Số lượng hiện tại: " + currentQty);
        System.out.println("- Số lượng mới sau cập nhật: " + newQty);

        if (newQty < 0) {
            System.out.println("❌ Cập nhật thất bại: tồn kho không đủ");
            return ResponseEntity.status(400).body("Tồn kho không đủ");
        }

        product.setQuantity(newQty);
        productRepository.save(product);

        System.out.println("✅ Cập nhật tồn kho thành công cho sản phẩm: " + productId);
        return ResponseEntity.ok("Cập nhật tồn kho thành công");

    } catch (Exception e) {
        System.out.println("🔥 Lỗi hệ thống khi cập nhật tồn kho: " + e.getMessage());
        return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
    }
}

    @PostMapping("/add-item")
    public ResponseEntity<?> addItemToOrder(@RequestBody OrderItemRequest dto) {
        Optional<Order> orderOpt = orderRepository.findById(dto.getOrderId());
        Optional<Product> productOpt = productRepository.findById(dto.getProductId());

        if (orderOpt.isEmpty() || productOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng hoặc sản phẩm");
        }

        Order order = orderOpt.get();
        Product product = productOpt.get();

        int requestQty = dto.getQuantity();
        if (requestQty <= 0) {
            return ResponseEntity.badRequest().body("Số lượng phải lớn hơn 0");
        }

        // Kiểm tra tồn kho
        if (product.getQuantity() < requestQty) {
            return ResponseEntity.badRequest().body("Không đủ tồn kho cho sản phẩm " + product.getProductName());
        }

        // ✅ Nếu sản phẩm đã tồn tại trong đơn -> cập nhật số lượng
        Optional<OrderDetail> existingDetail = orderDetailRepository.findByOrderAndProduct(order, product);
        if (existingDetail.isPresent()) {
            OrderDetail detail = existingDetail.get();
            detail.setQuantity(detail.getQuantity() + requestQty);
            detail.setPrice(dto.getPrice()); // cập nhật giá nếu cần
            orderDetailRepository.save(detail);
        } else {
            // ✅ Nếu chưa có thì tạo mới
            OrderDetail newDetail = new OrderDetail();
            newDetail.setOrder(order);
            newDetail.setProduct(product);
            newDetail.setQuantity(requestQty);
            newDetail.setPrice(dto.getPrice());
            orderDetailRepository.save(newDetail);
        }

        // ✅ Cập nhật tồn kho sau khi thêm
        product.setQuantity(product.getQuantity() - requestQty);
        productRepository.save(product);

        return ResponseEntity.ok("Đã thêm sản phẩm vào đơn hàng");
    }

    @GetMapping("/banhangoff/products")
    @ResponseBody
    public List<Product> getProductsByCategory(@RequestParam(value = "category", required = false) Long categoryId) {
        if (categoryId == null ) {
            return productRepository.findAll();
        } else {
            return productRepository.findByCategory_CategoryId(categoryId);
        }
    }
    @GetMapping("/banhangoff/search")
    @ResponseBody
    public List<Product> searchProductsByName(@RequestParam String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.findByProductNameContainingIgnoreCase(productName.trim());
    }

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

}
