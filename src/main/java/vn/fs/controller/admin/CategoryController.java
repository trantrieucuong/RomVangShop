package vn.fs.controller.admin;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    @PostMapping("/addCategory")
    public String addCategory(@Validated @ModelAttribute("category") Category category,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập đầy đủ thông tin!");
            return "redirect:/admin/categories";
        }

        Optional<Category> existingCategory = Optional.ofNullable(
                categoryRepository.findByCategoryNameIgnoreCase(category.getCategoryName())
        );

        if (existingCategory.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Tên danh mục đã tồn tại!");
            return "redirect:/admin/categories";
        }

        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("message", "Thêm danh mục thành công!");
        return "redirect:/admin/categories";
    }



    // get Edit category
	@GetMapping(value = "/editCategory/{id}")
	public String editCategory(@PathVariable("id") Long id, ModelMap model) {
		Category category = categoryRepository.findById(id).orElse(null);

		model.addAttribute("category", category);

		return "admin/editCategory";
	}

	// delete category
	@GetMapping("/delete/{id}")
	public String delCategory(@PathVariable("id") Long id, Model model) {
		categoryRepository.deleteById(id);

		model.addAttribute("message", "Delete successful!");

		return "redirect:/admin/categories";
	}
}
