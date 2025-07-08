package vn.fs.controller;

import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import vn.fs.commom.CommomDataService;
import vn.fs.config.Config;
import vn.fs.config.PaypalPaymentIntent;
import vn.fs.config.PaypalPaymentMethod;
import vn.fs.entities.*;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.OrderRepository;
import vn.fs.repository.UserPointRepository;
import vn.fs.service.PaypalService;
import vn.fs.service.ShoppingCartService;
import vn.fs.service.VnpayService;
import vn.fs.util.Utils;


@Controller
public class CartController extends CommomController {

	@Autowired
	HttpSession session;

	@Autowired
	CommomDataService commomDataService;

	@Autowired
	ShoppingCartService shoppingCartService;

	@Autowired
	private PaypalService paypalService;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	OrderDetailRepository orderDetailRepository;

	@Autowired
	VnpayService vnpayService;

	@Autowired
	private UserPointRepository userPointRepository;


	public Order orderFinal = new Order();

	public static final String URL_PAYPAL_SUCCESS = "pay/success";
	public static final String URL_PAYPAL_CANCEL = "pay/cancel";
	private Logger log = LoggerFactory.getLogger(getClass());


	// add cartItem
	@GetMapping(value = "/addToCart")
	public String add(@RequestParam("productId") Long productId, HttpServletRequest request, Model model) {

		Product product = productRepository.findById(productId).orElse(null);

		session = request.getSession();
		Collection<CartItem> cartItems = shoppingCartService.getCartItems();
		if (product != null) {
			CartItem item = new CartItem();
			BeanUtils.copyProperties(product, item);
			item.setQuantity(1);
			item.setProduct(product);
			item.setId(productId);
			shoppingCartService.add(item);
		}
		session.setAttribute("cartItems", cartItems);
		model.addAttribute("totalCartItems", shoppingCartService.getCount());

		return "redirect:/products";
	}

	
	@GetMapping(value = "/remove/{id}")
	public String remove(@PathVariable("id") Long id, HttpServletRequest request, Model model) {
	    Collection<CartItem> cartItems = shoppingCartService.getCartItems();
	    session = request.getSession();
	    
	    // Tìm và xóa CartItem từ id
	    Optional<CartItem> optionalCartItem = cartItems.stream()
	                                                   .filter(item -> item.getId().equals(id))
	                                                   .findFirst();
	    if (optionalCartItem.isPresent()) {
	        CartItem itemToRemove = optionalCartItem.get();
	        
	        // Xóa CartItem khỏi giỏ hàng
	        shoppingCartService.remove(itemToRemove);
	        
	        // Cập nhật danh sách cartItems trong session
	        cartItems.remove(itemToRemove);
	    }
	    
	    model.addAttribute("totalCartItems", shoppingCartService.getCount());
	    return "redirect:/checkout";
	}

	private void updateUserPointAfterPayment(User user, double totalAmount) {
		int earnedPoints = (int) (totalAmount / 1000); // 1000 VND = 1 điểm
		Optional<UserPoint> userPointOpt = userPointRepository.findByUser(user);

		if (userPointOpt.isPresent()) {
			UserPoint userPoint = userPointOpt.get();
			userPoint.setPoint(userPoint.getPoint() + earnedPoints);
			userPointRepository.save(userPoint);
		} else {
			UserPoint newUserPoint = new UserPoint(user, 3 + earnedPoints); // 3 điểm thưởng khách mới
			userPointRepository.save(newUserPoint);
		}
	}


	// show check out
	@GetMapping(value = "/checkout")
	public String checkOut(Model model, User user) {

		Order order = new Order();
		model.addAttribute("order", order);

		Collection<CartItem> cartItems = shoppingCartService.getCartItems();
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("total", shoppingCartService.getAmount());
		model.addAttribute("NoOfItems", shoppingCartService.getCount());
		double totalPrice = 0;
		for (CartItem cartItem : cartItems) {
			double price = cartItem.getQuantity() * cartItem.getProduct().getPrice();
			totalPrice += price - (price * cartItem.getProduct().getDiscount() / 100);
		}

		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("totalCartItems", shoppingCartService.getCount());
		commomDataService.commonData(model, user);

		return "web/shoppingCart_checkout";
	}

	// submit checkout
	@PostMapping(value = "/checkout")
	@Transactional
	public String checkedOut(Model model, Order order, HttpServletRequest request, User user)
			throws MessagingException {

		String checkOut = request.getParameter("checkOut");

		// Lấy giỏ hàng
		Collection<CartItem> cartItems = shoppingCartService.getCartItems();

		// Tính tổng tiền
		double totalPrice = 0;
		for (CartItem cartItem : cartItems) {
			double price = cartItem.getQuantity() * cartItem.getProduct().getPrice();
			totalPrice += price - (price * cartItem.getProduct().getDiscount() / 100);
		}

		// Lưu địa chỉ & số điện thoại vào session (dùng lại cho VNPay)
		String address = order.getAddress();
		String phone = order.getPhone();
		session.setAttribute("checkout_address", address);
		session.setAttribute("checkout_phone", phone);

		// Gán các giá trị cho orderFinal để tái sử dụng cho PayPal
		BeanUtils.copyProperties(order, orderFinal);

		// -------- PAYPAL --------
		if (StringUtils.equals(checkOut, "paypal")) {
			String cancelUrl = Utils.getBaseURL(request) + "/" + URL_PAYPAL_CANCEL;
			String successUrl = Utils.getBaseURL(request) + "/" + URL_PAYPAL_SUCCESS;
			try {
				double totalUsd = totalPrice / 25456; // Tỷ giá tạm thời
				Payment payment = paypalService.createPayment(totalUsd, "USD", PaypalPaymentMethod.paypal,
						PaypalPaymentIntent.sale, "Thanh toán qua PayPal", cancelUrl, successUrl);
				for (Links links : payment.getLinks()) {
					if (links.getRel().equals("approval_url")) {
						return "redirect:" + links.getHref();
					}
				}
			} catch (PayPalRESTException e) {
				log.error(e.getMessage());
			}
		}

		// -------- VNPay --------
		else if (StringUtils.equals(checkOut, "vnpay")) {
			// Lấy hoặc tạo mã giỏ hàng
			String cartCode = (String) session.getAttribute("cartCode");
			if (cartCode == null) {
				cartCode = "GH" + Config.getRandomNumber(4);
				session.setAttribute("cartCode", cartCode);
			}

			// Gộp orderInfo: cartCode|address|phone
			String orderInfo = cartCode + "|" + address + "|" + phone;

			// Mã đơn hàng
			String orderCode = "hd" + System.currentTimeMillis();

			// Tạo URL thanh toán
			String paymentUrl = vnpayService.createPaymentUrl((int) totalPrice, user, orderInfo, orderCode);
			return "redirect:" + paymentUrl;
		}

		// -------- Thanh toán COD (trả tiền mặt) --------
		Date date = new Date();
		order.setOrderDate(date);
		order.setStatus(0); // Chưa xử lý
		order.setAmount(totalPrice);
		order.setUser(user);
		order.setNote("Thanh toán khi nhận hàng");

		orderRepository.save(order);
		updateUserPointAfterPayment(user, totalPrice);


		// Lưu chi tiết đơn hàng
		for (CartItem cartItem : cartItems) {
			OrderDetail orderDetail = new OrderDetail();
			orderDetail.setOrder(order);
			orderDetail.setProduct(cartItem.getProduct());
			orderDetail.setQuantity(cartItem.getQuantity());
			orderDetail.setPrice(cartItem.getProduct().getPrice());
			orderDetailRepository.save(orderDetail);
		}

		// Gửi email xác nhận
		commomDataService.sendSimpleEmail(user.getEmail(), "RomVang-Shop Xác Nhận Đơn hàng",
				"Cảm ơn bạn đã đặt hàng!", cartItems, totalPrice, order);

		// Dọn dẹp
		shoppingCartService.clear();
		session.removeAttribute("cartItems");
		session.removeAttribute("checkout_address");
		session.removeAttribute("checkout_phone");

		model.addAttribute("orderId", order.getOrderId());

		return "redirect:/checkout_success";
	}


	// paypal
	@GetMapping(URL_PAYPAL_SUCCESS)
	public String successPay(@RequestParam("" + "" + "") String paymentId, @RequestParam("PayerID") String payerId,
			HttpServletRequest request, User user, Model model) throws MessagingException {
		Collection<CartItem> cartItems = shoppingCartService.getCartItems();
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("total", shoppingCartService.getAmount());

		double totalPrice = 0;
		for (CartItem cartItem : cartItems) {
			double price = cartItem.getQuantity() * cartItem.getProduct().getPrice();
			totalPrice += price - (price * cartItem.getProduct().getDiscount() / 100);
		}
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("totalCartItems", shoppingCartService.getCount());

		try {
			Payment payment = paypalService.executePayment(paymentId, payerId);
			if (payment.getState().equals("approved")) {

				session = request.getSession();
				Date date = new Date();
				orderFinal.setOrderDate(date);
				orderFinal.setStatus(0);
				orderFinal.getOrderId();
				orderFinal.setUser(user);
				orderFinal.setAmount(totalPrice);
				orderRepository.save(orderFinal);
				updateUserPointAfterPayment(user, totalPrice);


				for (CartItem cartItem : cartItems) {
					OrderDetail orderDetail = new OrderDetail();
					orderDetail.setQuantity(cartItem.getQuantity());
					orderDetail.setOrder(orderFinal);
					orderDetail.setProduct(cartItem.getProduct());
					double unitPrice = cartItem.getProduct().getPrice();
					orderDetail.setPrice(unitPrice);
					orderDetailRepository.save(orderDetail);
				}

				// sendMail
				commomDataService.sendSimpleEmail(user.getEmail(), "RomVang-Shop Xác Nhận Đơn hàng", "aaaa", cartItems,
						totalPrice, orderFinal);

				shoppingCartService.clear();
				session.removeAttribute("cartItems");
				model.addAttribute("orderId", orderFinal.getOrderId());
				orderFinal = new Order();
				return "redirect:/checkout_paypal_success";
			}
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}
		return "redirect:/";
	}

	// done checkout ship cod
	@GetMapping(value = "/checkout_success")
	public String checkoutSuccess(Model model, User user) {
		commomDataService.commonData(model, user);

		return "web/checkout_success";

	}

	// done checkout paypal
	@GetMapping(value = "/checkout_paypal_success")
	public String paypalSuccess(Model model, User user) {
		commomDataService.commonData(model, user);

		return "web/checkout_paypal_success";

	}


	@PutMapping(value = "/updateQuantity", params = { "productId", "quantity" })
	@ResponseBody
	public ResponseEntity<String> updateQ(@RequestParam("productId") Long id,
										  @RequestParam("quantity") int qty) {
		shoppingCartService.updateQuantity(id, qty);
		return ResponseEntity.ok("updated");
	}


	@GetMapping("/api/payment/vnpay_return")
	public String handleVnpayReturn(@RequestParam Map<String, String> params,
									HttpServletRequest request,
									Model model,
									HttpSession session,
									Principal principal) throws MessagingException {

		String responseCode = params.get("vnp_ResponseCode");
		String orderCode = params.get("vnp_TxnRef");    // ví dụ: hd123456
		String orderInfo = params.get("vnp_OrderInfo"); // ví dụ: GH1234|Số 1 HN|0912345678

		// ❌ Nếu thất bại thì quay lại trang thanh toán
		if (responseCode == null || !"00".equals(responseCode)) {
			return "redirect:/checkout";
		}

		// ✅ Tách thông tin từ orderInfo
		String cartCode = "";
		String address = "";
		String phone = "";

		if (orderInfo != null) {
			String[] parts = orderInfo.split("\\|");
			cartCode = parts.length > 0 ? parts[0] : "";
			address = parts.length > 1 ? parts[1] : "";
			phone = parts.length > 2 ? parts[2] : "";
		}

		// ✅ Lấy user từ Principal
		User user = userRepository.findByEmail(principal.getName());

		// ✅ Lấy giỏ hàng
		Collection<CartItem> cartItems = shoppingCartService.getCartItems();
		double totalPrice = shoppingCartService.getAmount();

		// ✅ Tạo đơn hàng
		Order order = new Order();
		order.setOrderDate(new Date());
		order.setStatus(0); // chưa xử lý
		order.setAmount(totalPrice);
		order.setUser(user);
		order.setNote("Thanh toán qua VNPay");
		order.setAddress(address);
		order.setPhone(phone);

		// ✅ Lưu đơn hàng để lấy orderId
		order = orderRepository.save(order);
		updateUserPointAfterPayment(user, totalPrice);


		// ✅ Lưu từng sản phẩm trong đơn hàng
		for (CartItem cartItem : cartItems) {
			OrderDetail detail = new OrderDetail();
			detail.setOrder(order);
			detail.setProduct(cartItem.getProduct());
			detail.setQuantity(cartItem.getQuantity());
			detail.setPrice(cartItem.getProduct().getPrice());
			orderDetailRepository.save(detail);
		}

		// ✅ Gửi mail xác nhận
		commomDataService.sendSimpleEmail(
				user.getEmail(),
				"Xác nhận đơn hàng qua VNPay",
				"Cảm ơn bạn đã đặt hàng!",
				cartItems,
				totalPrice,
				order
		);

		// ✅ Dọn dẹp giỏ hàng
		shoppingCartService.clear();
		session.removeAttribute("cartItems");
		session.removeAttribute("checkout_address");
		session.removeAttribute("checkout_phone");
		session.removeAttribute("cartCode");

		// ✅ Chuyển hướng về trang thành công
		return "redirect:/checkout_success";
	}

}