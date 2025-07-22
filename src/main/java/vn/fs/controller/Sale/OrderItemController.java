package vn.fs.controller.Sale;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.fs.repository.OrderRepository;
import vn.fs.repository.ProductRepository;

@RestController
@RequestMapping("/order-items")
public class OrderItemController {
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ProductRepository productRepository;
//    @Autowired
//    OrderItemRepository orderItemRepository;
//    @PostMapping("/add-item")
//    public ResponseEntity<?> addItemToOrder(@RequestBody OrderItemRequest dto) {
//        Order order = orderRepository.findByOrderCode(dto.getOrderCode());
//        Optional<Product> productOpt = productRepository.findByProductCode(dto.getProductCode());
//
//        if (order == null || productOpt.isEmpty()) {
//            return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng hoặc sản phẩm");
//        }
//
//        Product product = productOpt.get();
//
//        // 🔍 Kiểm tra xem đã có OrderItem này trong đơn hàng chưa
//        Optional<OrderItem> existingItemOpt = orderItemRepository.findByOrderAndProduct(order, product);
//        if (existingItemOpt.isPresent()) {
//            // Nếu có thì cộng dồn số lượng
//            OrderItem existing = existingItemOpt.get();
//            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
//            orderItemRepository.save(existing);
//            return ResponseEntity.ok("Đã cập nhật số lượng sản phẩm trong đơn hàng");
//        }
//
//        // ❌ Nếu chưa có thì tạo mới (INSERT)
//        OrderItem item = new OrderItem();
//        item.setOrderItemCode(generateNextOrderItemCode());
//        item.setOrder(order);
//        item.setProduct(product);
//        item.setName(dto.getName());
//        item.setPrice(dto.getPrice());
//        item.setQuantity(dto.getQuantity());
//
//        orderItemRepository.save(item);
//        return ResponseEntity.ok("Thêm sản phẩm vào đơn hàng thành công");
//    }
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



}
