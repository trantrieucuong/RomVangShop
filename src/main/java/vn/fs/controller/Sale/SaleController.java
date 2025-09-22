package vn.fs.controller.Sale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.fs.entities.*;
import vn.fs.repository.*;
import vn.fs.request.OrderItemRequest;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.*;

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
    @Autowired
    RoleRepository roleRepository;

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
        List<Product> products = productRepository.findByQuantityGreaterThanAndStatus(0,true); // Chỉ lấy sản phẩm có số lượng > 0
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);

        return "sale/banhangoff/index";
    }

//        // ✅ Phục hồi đơn hàng nếu có từ session
//        Long pendingOrderId = (Long) session.getAttribute("pendingOrderId");
//        if (pendingOrderId != null) {
//            Order order = orderRepository.findById(pendingOrderId).orElse(null);
//            if (order != null) {
//                model.addAttribute("restoredOrder", order);
//            }
//            session.removeAttribute("pendingOrderId"); // ✅ Xóa sau khi sử dụng
//        }

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
        if (categoryId == null) {
            // ✅ Chỉ lấy sản phẩm status = true và còn hàng
            return productRepository.findByStatusTrueAndQuantityGreaterThan(0);
        } else {
            // ✅ Theo category + status = true + còn hàng
            return productRepository.findByCategory_CategoryIdAndStatusTrueAndQuantityGreaterThan(categoryId, 0);
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


    @PostMapping("/create-or-update-customer")
    @ResponseBody
    public ResponseEntity<?> createOrUpdateCustomer(@RequestBody Map<String, String> formData) {
        String name = formData.get("name");
        String email = formData.get("email");
        String phone = formData.get("phone");

        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null) {
            // ✅ Nếu user đã tồn tại -> chỉ cập nhật số điện thoại
            existingUser.setPhone(phone);
            userRepository.save(existingUser);
            return ResponseEntity.ok("✅ Đã cập nhật số điện thoại cho khách hàng.");
        } else {
            // ✅ Nếu chưa tồn tại -> tạo mới user
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPhone(phone);

            // 🔐 Tạo mật khẩu ngẫu nhiên và mã hóa bằng BCrypt
            String rawPassword = generateRandomPassword(8);
            newUser.setPassword(new BCryptPasswordEncoder().encode(rawPassword));

            newUser.setStatus(true); // kích hoạt tài khoản
            newUser.setRegisterDate(new Date());

            // Gán role mặc định là ROLE_USER (hoặc ROLE_SALE tùy bạn)
            Role defaultRole = roleRepository.findByName("ROLE_USER");
            newUser.setRoles(List.of(defaultRole));

            userRepository.save(newUser);

            // ✅ Có thể log mật khẩu để admin biết nếu cần
            System.out.println("🔐 Mật khẩu ngẫu nhiên của user " + email + ": " + rawPassword);

            return ResponseEntity.ok("✅ Đã thêm khách hàng mới thành công.");
        }
    }


    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    @GetMapping("/phone/search")
    public ResponseEntity<?> searchByPhone(@RequestParam String phone) {
        Optional<User> customer = userRepository.findByPhone(phone.trim());
        if (customer.isPresent()) {
            return ResponseEntity.ok(customer.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }
    @GetMapping("/checkPhone")
    public ResponseEntity<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = userRepository.existsByPhone(phone);
        return ResponseEntity.ok(exists);
    }
    @GetMapping("/checkEmail")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.ok(false);
        }
        boolean exists = userRepository.existsByEmailIgnoreCase(email.trim());

        return ResponseEntity.ok(exists);
    }
    @GetMapping("/thanh-toan-online")
    public String showSuccessInvoice(HttpSession session, Model model) {
        Long orderId = (Long) session.getAttribute("pendingOrderId");
        if (orderId == null) {
            return "redirect:/sale/saleHome"; // fallback nếu không có đơn
        }
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return "redirect:/sale/saleHome";
        }
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", order.getOrderDetails());

        // Nếu vẫn cần hiển thị sản phẩm & category cho navbar/menu
        List<Product> products = productRepository.findByQuantityGreaterThanAndStatus(0,true); // Chỉ lấy sản phẩm có số lượng > 0
        List<Category> categories = categoryRepository.findAll();

        // Dọn session
        session.removeAttribute("pendingOrderId");

        return "admin/hoaDonInRa";
    }

    @GetMapping("/saleHome1")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SALE')")
    public String check(Model model, HttpSession session) {


        return "admin/hoaDonInRa";
    }
}
