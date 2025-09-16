package vn.fs.controller.admin;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import vn.fs.dto.OrderExcelExporter;
import vn.fs.entities.*;
import vn.fs.repository.*;
import vn.fs.service.OrderDetailService;
import vn.fs.service.SendMailService;
import java.time.LocalDate;
import java.time.ZoneId;


@Controller
@RequestMapping("/admin")
public class OrderController {

	@Autowired
	OrderDetailService orderDetailService;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	OrderDetailRepository orderDetailRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	SendMailService sendMailService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserPointRepository userPointRepository;

	private void updateUserPoint(User user, double totalAmount, boolean isAdd) {
		int pointChange = (int) (totalAmount / 1000); // 1000 VND = 1 điểm
		Optional<UserPoint> optional = userPointRepository.findByUser(user);

		if (optional.isPresent()) {
			UserPoint userPoint = optional.get();
			if (isAdd) {
				userPoint.setPoint(userPoint.getPoint() + pointChange);
			} else {
				userPoint.setPoint(Math.max(0, userPoint.getPoint() - pointChange));
			}
			userPointRepository.save(userPoint);
		} else if (isAdd) {
			UserPoint newPoint = new UserPoint(user, 3 + pointChange);
			userPointRepository.save(newPoint);
		}
	}


	@ModelAttribute(value = "user")
	public User user(Model model, Principal principal, User user) {

		if (principal != null) {
			model.addAttribute("user", new User());
			user = userRepository.findByEmail(principal.getName());
			model.addAttribute("user", user);
		}

		return user;
	}

    @GetMapping("/orders")
    public String orders(
            Model model,
            Principal principal,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "orderStatus", required = false) Integer status,
            @RequestParam(value = "orderId", required = false) String orderId
    ) {
        LocalDate today = LocalDate.now(); // FAKE DATE để test

        int selectedMonth = (month != null) ? month : today.getMonthValue();
        model.addAttribute("selectedMonth", selectedMonth);

        List<Integer> months = IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList());
        model.addAttribute("months", months);

        // --- nếu có nhập orderId (tìm gần đúng) ---
        if (orderId != null && !orderId.trim().isEmpty()) {
            List<Order> searchResults = orderRepository.searchByOrderIdLike(orderId.trim());
            model.addAttribute("orderDetails", searchResults != null ? searchResults : new ArrayList<>());
            model.addAttribute("orderDetailst", new ArrayList<>());
            model.addAttribute("selectedStatus", status);
            model.addAttribute("searchOrderId", orderId);

            model.addAttribute("countNew", 0);
            model.addAttribute("countConfirmed", 0);
            model.addAttribute("countDelivered", 0);
            model.addAttribute("countCanceled", 0);
            model.addAttribute("countCanceleds", 0);

            return "admin/orders";
        }

        // --- lấy toàn bộ đơn của tháng hiện tại ---
        List<Order> allOrdersByMonth = orderRepository.findByMonth(selectedMonth);
        if (allOrdersByMonth == null) allOrdersByMonth = new ArrayList<>();

        if (today.getDayOfMonth() == 1) {
            LocalDate lastDayPrevMonth = today.minusMonths(1)
                    .withDayOfMonth(today.minusMonths(1).lengthOfMonth());

            List<Order> lastDayPrevMonthOrders = orderRepository.findAll().stream()
                    .filter(o -> o.getStatus() == 0)
                    .filter(o -> o.getOrderDate() != null)
                    .filter(o -> {
                        LocalDate orderDate = Instant.ofEpochMilli(o.getOrderDate().getTime())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        return orderDate.isEqual(lastDayPrevMonth);
                    })
                    .collect(Collectors.toList());

            allOrdersByMonth.addAll(lastDayPrevMonthOrders);
        }

        List<Order> orderDetailst = orderRepository.findAllWithCancellation();
        if (orderDetailst == null) orderDetailst = new ArrayList<>();

        List<Order> orderDetails;
        if (status != null) {
            orderDetails = allOrdersByMonth.stream()
                    .filter(o -> o.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            orderDetails = new ArrayList<>(allOrdersByMonth);
        }

        long countNew = allOrdersByMonth.stream().filter(o -> o.getStatus() == 0).count();
        long countConfirmed = allOrdersByMonth.stream().filter(o -> o.getStatus() == 1).count();
        long countDelivered = allOrdersByMonth.stream().filter(o -> o.getStatus() == 2).count();
        long countCanceled = allOrdersByMonth.stream().filter(o -> o.getStatus() == 3).count();
        long countCanceleds = allOrdersByMonth.stream().filter(o -> o.getStatus() == 4).count();

        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("orderDetailst", orderDetailst);
        model.addAttribute("selectedStatus", status);

        model.addAttribute("countNew", countNew);
        model.addAttribute("countConfirmed", countConfirmed);
        model.addAttribute("countDelivered", countDelivered);
        model.addAttribute("countCanceled", countCanceled);
        model.addAttribute("countCanceleds", countCanceleds);

        return "admin/orders";
    }





    @GetMapping("/order/detail/{order_id}")
	public ModelAndView detail(ModelMap model, @PathVariable("order_id") Long id) {

		List<OrderDetail> listO = orderDetailRepository.findByOrderId(id);

		model.addAttribute("amount", orderRepository.findById(id).get().getAmount());
		model.addAttribute("orderDetail", listO);
		model.addAttribute("orderId", id);
		// set active front-end
		model.addAttribute("menuO", "menu");
		return new ModelAndView("admin/editOrder", model);
	}

	@RequestMapping("/order/cancel/{order_id}")
	public ModelAndView cancel(ModelMap model, @PathVariable("order_id") Long id) {
		Optional<Order> o = orderRepository.findById(id);
		if (o.isEmpty()) {
			return new ModelAndView("forward:/admin/orders", model);
		}
		Order oReal = o.get();
		oReal.setStatus((short) 3);
		orderRepository.save(oReal);

		return new ModelAndView("forward:/admin/orders", model);
	}

	@GetMapping("/order/approveCancel/{order_id}")
	@Transactional
	public ModelAndView approveCancel(@PathVariable("order_id") Long id, ModelMap model) {
		Optional<Order> o = orderRepository.findById(id);
		if (o.isEmpty()) {
			model.addAttribute("message", "Không tìm thấy đơn hàng.");
			return new ModelAndView("forward:/admin/orders", model);
		}
		Order order = o.get();

		// Chỉ duyệt huỷ nếu đang là "chờ huỷ" (status = 4)
		if (order.getStatus() == 4) {
			// Nếu đơn đã được duyệt trước đó → trừ điểm
			if (order.getStatus() == 1) {
				updateUserPoint(order.getUser(), order.getAmount(), false);
			}

			order.setStatus(3); // Đã huỷ
			orderRepository.save(order);

			model.addAttribute("message", "Duyệt huỷ đơn hàng thành công.");
		} else {
			model.addAttribute("message", "Không thể duyệt huỷ đơn hàng này.");
		}

		return new ModelAndView("forward:/admin/orders", model);
	}



	@RequestMapping("/order/confirm/{order_id}")
	public ModelAndView confirm(ModelMap model, @PathVariable("order_id") Long id) {
		Optional<Order> o = orderRepository.findById(id);
		if (o.isEmpty()) {
			return new ModelAndView("forward:/admin/orders", model);
		}
		Order oReal = o.get();

		// Chỉ duyệt nếu đơn đang là "chờ xử lý"
		if (oReal.getStatus() == 0) {
			oReal.setStatus(1); // Đã duyệt
			orderRepository.save(oReal);

			// Cộng điểm
			updateUserPoint(oReal.getUser(), oReal.getAmount(), true);
		}

		return new ModelAndView("forward:/admin/orders", model);
	}


	@RequestMapping("/order/delivered/{order_id}")
	public ModelAndView delivered(ModelMap model, @PathVariable("order_id") Long id) {
		Optional<Order> o = orderRepository.findById(id);
		if (o.isEmpty()) {
			return new ModelAndView("forward:/admin/orders", model);
		}
		Order oReal = o.get();
		oReal.setStatus((short) 2);
		orderRepository.save(oReal);

		Product p = null;
		List<OrderDetail> listDe = orderDetailRepository.findByOrderId(id);
		for (OrderDetail od : listDe) {
			p = od.getProduct();
			p.setQuantity(p.getQuantity() - od.getQuantity());
			productRepository.save(p);
		}

		return new ModelAndView("forward:/admin/orders", model);
	}

	// to excel
    @GetMapping(value = "/export")
    public void exportToExcel(
            HttpServletResponse response,
            @RequestParam(value = "month", required = false) Integer month
    ) throws IOException {

        // Lấy tháng hiện tại nếu month null
        LocalDate today = LocalDate.now();
        int selectedMonth = (month != null) ? month : today.getMonthValue();

        // Tạo tên file theo tháng
        String filename = "donHang_thang" + selectedMonth + ".xlsx";

        // Set header để trình duyệt tải file đúng tên và encode UTF-8
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + java.net.URLEncoder.encode(filename, "UTF-8"));

        // Lấy danh sách đơn hàng theo tháng
        List<Order> orders = orderRepository.findByMonth(selectedMonth);
        if (orders == null) orders = new ArrayList<>();

        // Nếu hôm nay là ngày 1, thêm các đơn status=0 ngày cuối tháng trước
        if (today.getDayOfMonth() == 1) {
            LocalDate lastDayPrevMonth = today.minusMonths(1)
                    .withDayOfMonth(today.minusMonths(1).lengthOfMonth());

            List<Order> lastDayPrevMonthOrders = orderRepository.findAll().stream()
                    .filter(o -> o.getStatus() == 0)
                    .filter(o -> o.getOrderDate() != null)
                    .filter(o -> {
                        LocalDate orderDate = Instant.ofEpochMilli(o.getOrderDate().getTime())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        return orderDate.isEqual(lastDayPrevMonth);
                    })
                    .collect(Collectors.toList());

            orders.addAll(lastDayPrevMonthOrders);
        }

        // Xuất Excel với tháng đã chọn
        OrderExcelExporter excelExporter = new OrderExcelExporter(orders, selectedMonth);
        excelExporter.export(response);
    }




//	@GetMapping("/order/invoice/{orderId}")
//	public String getInvoiceHtml(@PathVariable String orderId, Model model) {
//		Order order = orderRepository.findOrderById(orderId); // Lấy đơn hàng
//		model.addAttribute("order", order);
//		return "admin/hoaDonInRa"; // File HTML chứa hóa đơn
//	}


}
