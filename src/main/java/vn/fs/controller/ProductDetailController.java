package vn.fs.controller;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import vn.fs.commom.CommomDataService;
import vn.fs.entities.Comment;
import vn.fs.entities.Product;
import vn.fs.entities.User;
import vn.fs.repository.CommentRepository;
import vn.fs.repository.ProductRepository;


@Controller
public class ProductDetailController extends CommomController{
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	CommomDataService commomDataService;


	@Autowired
	CommentRepository commentRepository;

	@GetMapping(value = "productDetail")
	public String productDetail(@RequestParam("id") Long id, Model model, User user,Principal principal) {

		Product product = productRepository.findById(id).orElse(null);
		List<Comment> comments = commentRepository.findByProductId(product.getProductId());
		model.addAttribute("product", product);
		model.addAttribute("comments", comments);

		commomDataService.commonData(model, user);
		listProductByCategory10(model, product.getCategory().getCategoryId());

		if (principal != null) {
			User currentUser = userRepository.findByEmail(principal.getName());
			model.addAttribute("currentUser", currentUser);
		}
		return "web/productDetail";
	}
	
	// Gợi ý top 10 sản phẩm cùng loại
	public void listProductByCategory10(Model model, Long categoryId) {
		List<Product> products = productRepository.listProductByCategory10(categoryId);
		model.addAttribute("productByCategory", products);
	}


	@PreAuthorize("hasRole('ROLE_USER')")
	@PostMapping("/comments/edit/{id}")
	@ResponseBody
	public String editComment(@PathVariable Long id,
							  @RequestParam String content,
							  Principal principal) {   // 👈 lấy Principal thay vì User

		Comment comment = commentRepository.findById(id).orElse(null);

		if (comment == null) {
			return "Không tìm thấy comment";
		}

		// lấy user hiện tại từ username
		User currentUser = userRepository.findByEmail(principal.getName()); // hoặc findByUsername tuỳ bạn

		if (comment.getUser() == null || !comment.getUser().getUserId().equals(currentUser.getUserId())) {
			return "Bạn không có quyền sửa comment này";
		}

		long diffMillis = new Date().getTime() - comment.getRateDate().getTime();
		long hours = diffMillis / (1000 * 60 * 60);

		if (hours > 24) {
			return "Chỉ được sửa trong vòng 24h sau khi đánh giá";
		}

		comment.setContent(content);
		commentRepository.save(comment);

		return "Sửa thành công";
	}



	// ROLE_ADMIN: xóa comment
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@DeleteMapping("/comments/delete/{id}")
	@ResponseBody
	public ResponseEntity<?> deleteComment(@PathVariable Long id) {
		if (!commentRepository.existsById(id)) {
			return ResponseEntity.badRequest().body("Không tìm thấy comment");
		}
		commentRepository.deleteById(id);
		return ResponseEntity.ok("Đã xóa thành công");
	}
}
