package vn.fs.controller.admin;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
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
			@RequestParam(value = "orderStatus", required = false) Integer status
	) {
		if (month == null) {
			month = LocalDate.now().getMonthValue(); // mặc định là tháng hiện tại
		}

		List<Integer> months = IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList());
		model.addAttribute("months", months);

		// Lấy toàn bộ đơn hàng của tháng
		List<Order> allOrdersByMonth = orderRepository.findByMonth(month);
		List<Order> orderDetailst = orderRepository.findAllWithCancellation();

		// Đếm theo trạng thái
		model.addAttribute("countNew", allOrdersByMonth.stream().filter(o -> o.getStatus() == 0).count());
		model.addAttribute("countConfirmed", allOrdersByMonth.stream().filter(o -> o.getStatus() == 1).count());
		model.addAttribute("countDelivered", allOrdersByMonth.stream().filter(o -> o.getStatus() == 2).count());
		model.addAttribute("countCanceled", allOrdersByMonth.stream().filter(o -> o.getStatus() == 3).count());
		model.addAttribute("countCanceleds", allOrdersByMonth.stream().filter(o -> o.getStatus() == 4).count());

		// Lọc theo trạng thái nếu có
		List<Order> orderDetails = allOrdersByMonth;
		if (status != null) {
			orderDetails = allOrdersByMonth.stream()
					.filter(o -> o.getStatus() == status)
					.collect(Collectors.toList());
		}

		model.addAttribute("orderDetails", orderDetails);
		model.addAttribute("orderDetailst", orderDetailst);
		model.addAttribute("selectedMonth", month);
		model.addAttribute("selectedStatus", status);

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
	public void exportToExcel(HttpServletResponse response) throws IOException {

		response.setContentType("application/octet-stream");
		String headerKey = "Content-Disposition";
		String headerValue = "attachement; filename=donHang.xlsx";

		response.setHeader(headerKey, headerValue);

		List<Order> lisOrders = orderDetailService.listAll();

		OrderExcelExporter excelExporter = new OrderExcelExporter(lisOrders);
		excelExporter.export(response);

	}

}
