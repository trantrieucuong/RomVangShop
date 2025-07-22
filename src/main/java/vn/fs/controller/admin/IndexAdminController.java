package vn.fs.controller.admin;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.fs.entities.User;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.OrderRepository;
import vn.fs.repository.UserRepository;


@Controller
@RequestMapping("/admin")
public class IndexAdminController{

	@Autowired
	UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	OrderDetailRepository orderDetailRepository;

	@ModelAttribute(value = "user")
	public User user(Model model, Principal principal, User user) {

		if (principal != null) {
			model.addAttribute("user", new User());
			user = userRepository.findByEmail(principal.getName());
			model.addAttribute("user", user);
		}

		return user;
	}

	@GetMapping(value = "/home")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
	public String showDashboard(Model model) {
//		 List<Object[]> monthlyStats = orderDetailRepository.repoWhereMonth();
//		model.addAttribute("monthlyStats", monthlyStats);

		List<Object[]> monthlyStats = orderDetailRepository.repoWhereMonth();
		// Chuyển đổi danh sách monthlyStats thành mảng JSON
		ObjectMapper objectMapper = new ObjectMapper();
		String monthlyStatsJson;
		try {
			monthlyStatsJson = objectMapper.writeValueAsString(monthlyStats);
		} catch (JsonProcessingException e) {
			// Xử lý ngoại lệ nếu có
			monthlyStatsJson = "[]"; // Trả về một mảng JSON rỗng nếu có lỗi
		}
		// Truyền mảng JSON vào model
		model.addAttribute("monthlyStats", monthlyStatsJson);


		model.addAttribute("totalRevenue", orderRepository.findTotalRevenue());
		model.addAttribute("successfulOrders", orderRepository.countSuccessfulOrders());
		model.addAttribute("cancelledOrders", orderRepository.countCancelledOrders());
		model.addAttribute("totalUsers", userRepository.count());
		model.addAttribute("newOrders", orderRepository.countNewOrders());

		return "admin/index"; // Tên của file template trong thư mục templates/admin
	}

}
