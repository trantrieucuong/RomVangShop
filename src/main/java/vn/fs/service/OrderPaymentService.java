package vn.fs.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.fs.dto.OrderDetailResponseDTO;
import vn.fs.dto.OrderResponseDTO;
import vn.fs.entities.Order;
import vn.fs.entities.OrderDetail;
import vn.fs.entities.Product;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.OrderRepository;
import vn.fs.repository.ProductRepository;
import vn.fs.request.OrderPaymentBankRequest;
import vn.fs.request.OrderPaymentRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {
    private final OrderRepository orderRepository;
//    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
//    private final InvoiceRepository invoiceRepository;

    public OrderResponseDTO processCashPayment(OrderPaymentRequest request) {
        System.out.println("==> Đã nhận request thanh toán: " + request);

        Optional<Order> orderOpt = orderRepository.findById(request.getOrderId());
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy đơn hàng");
        }

        Order order = orderOpt.get();
        order.setStatus(1); // Đã thanh toán
        order.setAddress(request.getAddress());
        order.setPhone(request.getPhone());
        order.setNote(request.getDescription());

        // ✅ Lấy danh sách OrderDetail thực tế từ DB
        List<OrderDetail> savedDetails = orderDetailRepository.findByOrder(order);

        // ✅ Tính lại tổng tiền từ OrderDetail
        double total = 0;
        for (OrderDetail detail : savedDetails) {
            total += detail.getPrice() * detail.getQuantity();
        }
        order.setAmount(total);
        order = orderRepository.save(order);

        // ✅ Tạo phản hồi
        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setOrderId(order.getOrderId());
        responseDTO.setNote(order.getNote());
        responseDTO.setOrderDate(order.getOrderDate());
        responseDTO.setAmount(order.getAmount());
        responseDTO.setCustomerName(
                request.getCustomerCode() != null ? request.getCustomerCode() : "Khách lẻ"
        );

        // ✅ Danh sách sản phẩm chi tiết
        List<OrderDetailResponseDTO> itemDTOs = new ArrayList<>();
        for (OrderDetail detail : savedDetails) {
            Product product = detail.getProduct();
            OrderDetailResponseDTO itemDTO = new OrderDetailResponseDTO();
            itemDTO.setProductName(product != null ? product.getProductName() : "[Không rõ sản phẩm]");
            itemDTO.setQuantity(detail.getQuantity());
            itemDTO.setPrice(detail.getPrice());
            itemDTOs.add(itemDTO);
        }
        responseDTO.setItems(itemDTOs);

        System.out.println("✅ Thanh toán thành công cho đơn: " + order.getOrderId());
        return responseDTO;
    }



    @Transactional
    public OrderResponseDTO processPaymentOnline(OrderPaymentBankRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() == 1) {
            throw new RuntimeException("Đơn hàng đã được thanh toán");
        }

        order.setStatus(1);
//        order.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        order.setAmount(request.getAmount());
        orderRepository.save(order);

        // Trừ tồn kho
        for (OrderPaymentBankRequest.ItemDTO dto : request.getItems()) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + dto.getProductId()));
            if (product.getQuantity() < dto.getQuantity()) {
                throw new RuntimeException("Không đủ tồn kho cho sản phẩm: " + product.getProductName());
            }
            product.setQuantity(product.getQuantity() - dto.getQuantity());
            productRepository.save(product);
        }

        // Lấy danh sách OrderDetail đã lưu
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        List<OrderDetailResponseDTO> itemDTOs = new ArrayList<>();

        for (OrderDetail detail : orderDetails) {
            Product product = detail.getProduct();
            OrderDetailResponseDTO dto = new OrderDetailResponseDTO();
            dto.setProductName(product != null ? product.getProductName() : "[Không rõ sản phẩm]");
            dto.setQuantity(detail.getQuantity());
            dto.setPrice(detail.getPrice());
            itemDTOs.add(dto);
        }

        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(order.getOrderId());
        response.setNote(order.getNote());
        response.setOrderDate(order.getOrderDate());
        response.setAmount(order.getAmount());
//        response.setCustomerName(order.getCustomer() != null ? order.getCustomer().getName() : "Khách lẻ");
        response.setCustomerName("Tuyên");
        response.setItems(itemDTOs);


        return response;
    }

}
