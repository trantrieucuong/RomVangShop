package vn.fs.controller.admin;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.fs.entities.Category;
import vn.fs.entities.User;
import vn.fs.repository.CategoryRepository;
import vn.fs.repository.UserRepository;



@Controller
@RequestMapping("/admin")
public class CategoryController {

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	UserRepository userRepository;

	@ModelAttribute(value = "user")
	public User user(Model model, Principal principal, User user) {

		if (principal != null) {
			model.addAttribute("user", new User());
			user = userRepository.findByEmail(principal.getName());
			model.addAttribute("user", user);
		}

		return user;
	}

	// show list category - table list
	@ModelAttribute("categories")
	public List<Category> showCategory(Model model) {
		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("categories", categories);

		return categories;
	}

	@GetMapping(value = "/categories")
	public String categories(Model model, Principal principal) {
		Category category = new Category();
		model.addAttribute("category", category);

		return "admin/categories";
	}

	// add category

//	@PostMapping(value = "/addCategory")
//	public String addCategory(@Validated @ModelAttribute("category") Category category,
//							  BindingResult bindingResult,
//							  RedirectAttributes redirectAttributes) {
//
//		if (bindingResult.hasErrors()) {
//			redirectAttributes.addFlashAttribute("error", "failure");
//			return "redirect:/admin/categories";
//		}
//
//		Optional<Category> existingCategory = Optional.ofNullable(
//				categoryRepository.findByCategoryNameIgnoreCase(category.getCategoryName())
//		);
//
//		if (existingCategory.isPresent()) {
//			redirectAttributes.addFlashAttribute("error", "Tên danh mục đã tồn tại!");
//			return "redirect:/admin/categories";
//		}
//
//		categoryRepository.save(category);
//		redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công!");
//
//		return "redirect:/admin/categories";
//	}

	@PostMapping(value = "/addCategory")
	public String addCategory(@ModelAttribute("category") Category category,
							  RedirectAttributes redirectAttributes) {

		String categoryName = category.getCategoryName();

		//  Check rỗng
		if (categoryName == null || categoryName.trim().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Tên thể loại không được để trống!");
			return "redirect:/admin/categories";
		}

		//  Check độ dài
		if (categoryName.length() > 50) {
			redirectAttributes.addFlashAttribute("error", "Tên thể loại không được quá 50 ký tự!");
			return "redirect:/admin/categories";
		}

		//  Check ký tự đặc biệt
		if (!categoryName.matches("^[\\p{L}0-9 ]+$")) {
			redirectAttributes.addFlashAttribute("error", "Tên thể loại chỉ được chứa chữ cái, số và khoảng trắng!");
			return "redirect:/admin/categories";
		}

		//  Check trùng tên trong DB
		Optional<Category> existingCategory =
				Optional.ofNullable(categoryRepository.findByCategoryNameIgnoreCase(categoryName));

		if (existingCategory.isPresent()) {
			redirectAttributes.addFlashAttribute("error", "Tên danh mục đã tồn tại!");
			return "redirect:/admin/categories";
		}

		categoryRepository.save(category);
		redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công!");

        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("message", "Thêm danh mục thành công!");
        return "redirect:/admin/categories";
    }



	// get Edit category
	@GetMapping(value = "/editCategory/{id}")
	public String editCategory(@PathVariable("id") Long id,
							   ModelMap model,
							   RedirectAttributes redirectAttributes) {

		// ✅ Validate id
		if (id == null || id <= 0) {
			redirectAttributes.addFlashAttribute("error", "ID danh mục không hợp lệ!");
			return "redirect:/admin/categories";
		}

		// ✅ Tìm category
		Optional<Category> categoryOpt = categoryRepository.findById(id);

		if (categoryOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục với ID: " + id);
			return "redirect:/admin/categories";
		}

		Category category = categoryOpt.get();
		String categoryName = category.getCategoryName();

		// ✅ Validate categoryName
		if (categoryName == null || categoryName.trim().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Tên thể loại không được để trống!");
			return "redirect:/admin/categories";
		}

		if (categoryName.length() > 50) {
			redirectAttributes.addFlashAttribute("error", "Tên thể loại không được quá 50 ký tự!");
			return "redirect:/admin/categories";
		}

		if (!categoryName.matches("^[\\p{L}0-9 ]+$")) {
			redirectAttributes.addFlashAttribute("error", "Tên thể loại chỉ được chứa chữ cái, số và khoảng trắng!");
			return "redirect:/admin/categories";
		}

		// ✅ Nếu hợp lệ thì cho edit
		model.addAttribute("category", category);
		return "admin/editCategory";
	}


	@GetMapping("/delete/{id}")
	public String delCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		try {
			categoryRepository.deleteById(id);
			redirectAttributes.addFlashAttribute("message", "Xóa thành công!");
		} catch (Exception e) {
			// Bắt lỗi khi đang có sản phẩm liên kết
			redirectAttributes.addFlashAttribute("error", "Không thể xóa: Thể loại đang có sản phẩm đi kèm!");
		}
		return "redirect:/admin/categories";
	}

}
