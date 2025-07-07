package vn.fs.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import vn.fs.commom.CommomDataService;
import vn.fs.entities.Order;
import vn.fs.entities.OrderCancellation;
import vn.fs.entities.OrderDetail;
import vn.fs.entities.User;
import vn.fs.repository.OrderCancellationRepository;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.OrderRepository;
import vn.fs.repository.UserRepository;

@Controller
public class ProfileController extends CommomController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderDetailRepository orderDetailRepository;

	@Autowired
	private CommomDataService commomDataService;

	@Autowired
	private OrderCancellationRepository orderCancellationRepository;

	@GetMapping("/profile")
	public String profile(Model model,
						  Principal principal,
						  @RequestParam("page") Optional<Integer> page,
						  @RequestParam("size") Optional<Integer> size,
						  @RequestParam(name = "orderStatus", required = false) Optional<Short> orderStatus) {

		if (principal == null) {
			return "redirect:/dang-nhap";
		}

		User user = userRepository.findByEmail(principal.getName());
		model.addAttribute("user", user);

		int currentPage = page.orElse(1);
		int pageSize = size.orElse(6);
		Short selectedStatus = orderStatus.orElse(null);

		Page<Order> orderPage = findPaginated(PageRequest.of(currentPage - 1, pageSize), user, selectedStatus);

		int totalPages = orderPage.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		model.addAttribute("selectedStatus", selectedStatus);
		commomDataService.commonData(model, user);
		model.addAttribute("orderByUser", orderPage);

		return "web/profile";
	}

	@GetMapping("/profile/orders")
	public String orderFragment(Model model,
								Principal principal,
								@RequestParam("page") Optional<Integer> page,
								@RequestParam("size") Optional<Integer> size,
								@RequestParam(name = "orderStatus", required = false) Optional<Short> orderStatus) {

		if (principal == null) {
			return "redirect:/dang-nhap";
		}

		User user = userRepository.findByEmail(principal.getName());
		model.addAttribute("user", user);

		int currentPage = page.orElse(1);
		int pageSize = size.orElse(6);
		Short selectedStatus = orderStatus.orElse(null);

		Page<Order> orderPage = findPaginated(PageRequest.of(currentPage - 1, pageSize), user, selectedStatus);

		int totalPages = orderPage.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		model.addAttribute("selectedStatus", selectedStatus);
		model.addAttribute("orderByUser", orderPage);

		return "web/fragments/orderTable :: orderTable";
	}




	public Page<Order> findPaginated(Pageable pageable, User user, Short status) {
		List<Order> orders;

		if (status == null) {
			orders = orderRepository.findOrderByUserId(user.getUserId());
		} else {
			orders = orderRepository.findAllByUser_UserIdAndStatus(user.getUserId(), status);
		}

		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;

		List<Order> pagedList;
		if (orders.size() < startItem) {
			pagedList = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, orders.size());
			pagedList = orders.subList(startItem, toIndex);
		}

		return new PageImpl<>(pagedList, PageRequest.of(currentPage, pageSize), orders.size());
	}

	@GetMapping("/order/detail/{order_id}")
	public ModelAndView detail(Model model, Principal principal,
							   @PathVariable("order_id") Long id) {

		if (principal == null) {
			return new ModelAndView("redirect:/dang-nhap");
		}

		User user = userRepository.findByEmail(principal.getName());
		model.addAttribute("user", user);

		List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(id);
		model.addAttribute("orderDetail", orderDetails);

		commomDataService.commonData(model, user);
		return new ModelAndView("web/historyOrderDetail");
	}

//	@RequestMapping("/order/cancel/{order_id}")
//	public ModelAndView cancel(ModelMap model, @PathVariable("order_id") Long id) {
//		Optional<Order> orderOpt = orderRepository.findById(id);
//		if (orderOpt.isEmpty()) {
//			return new ModelAndView("redirect:/profile", model);
//		}
//
//		Order order = orderOpt.get();
//		order.setStatus((short) 3); // Trạng thái hủy
//		orderRepository.save(order);
//
//		return new ModelAndView("redirect:/profile", model);
//	}
	@PostMapping("/order/cancel")
	@ResponseBody
	public ResponseEntity<?> cancelOrder(@RequestParam("orderId") Long orderId,
										 @RequestParam("reason") String reason,
										 Principal principal) {
		if (principal == null) {
			return ResponseEntity.status(401).body("Bạn cần đăng nhập để thực hiện thao tác này.");
		}

		Optional<Order> orderOpt = orderRepository.findById(orderId);
		if (orderOpt.isEmpty()) {
			return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng.");
		}

		Order orderInDb = orderOpt.get();

		// ✅ Chỉ cập nhật trạng thái nếu đúng trạng thái ban đầu
		if (orderInDb.getStatus() != 0) {
			return ResponseEntity.badRequest().body("Chỉ có thể yêu cầu hủy đơn hàng đang chờ xác nhận.");
		}

		// ✅ Cập nhật trạng thái (giữ nguyên các thông tin khác)
		orderInDb.setStatus((short) 4); // Chờ xác thực huỷ
		orderRepository.save(orderInDb);

		// ✅ Lưu lý do huỷ
		OrderCancellation cancellation = new OrderCancellation();
		cancellation.setOrder(orderInDb);
		cancellation.setReason(reason);
		cancellation.setCancelledAt(new Date());
		orderCancellationRepository.save(cancellation);

		return ResponseEntity.ok("Yêu cầu huỷ đơn hàng đã được gửi. Vui lòng chờ xác nhận từ quản trị viên.");
	}



}
