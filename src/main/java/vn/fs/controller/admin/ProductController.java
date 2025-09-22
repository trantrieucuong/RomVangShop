package vn.fs.controller.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.fs.entities.Category;
import vn.fs.entities.Product;
import vn.fs.entities.User;
import vn.fs.excel.ExcelHelper;
import vn.fs.repository.CategoryRepository;
import vn.fs.repository.ProductRepository;
import vn.fs.repository.UserRepository;


@Controller
@RequestMapping("/admin")
public class ProductController{
	
	@Value("${upload.path}")
	private String pathUploadImage;

	@Autowired
	ProductRepository productRepository;

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

	public ProductController(CategoryRepository categoryRepository,
			ProductRepository productRepository) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
	}

	// show list product - table list
	@ModelAttribute("products")
	public List<Product> showProduct(Model model) {
		List<Product> products = productRepository.findAll();
		model.addAttribute("products", products);

		return products;
	}

	@GetMapping(value = "/products")
	public String products(Model model, Principal principal) {
		Product product = new Product();
		model.addAttribute("product", product);

		return "admin/products";
	}

	// add product
	@PostMapping(value = "/addProduct")
	public String addProduct(@ModelAttribute("product") Product product,
							 @RequestParam("file") MultipartFile file,
							 ModelMap model) {

		// Lưu ảnh
		if (!file.isEmpty()) {
			try {
				File convFile = new File(pathUploadImage + "/" + file.getOriginalFilename());
				FileOutputStream fos = new FileOutputStream(convFile);
				fos.write(file.getBytes());
				fos.close();
				product.setProductImage(file.getOriginalFilename());
			} catch (IOException e) {
				e.printStackTrace(); // log lỗi
			}
		}
product.setStatus(true);
		product.setEnteredDate(new Date());

		// Check sản phẩm trùng tên
		Optional<Product> optionalProduct = productRepository.findByProductNameIgnoreCase(product.getProductName());

		if (optionalProduct.isPresent()) {
			// Sản phẩm đã tồn tại -> cộng dồn số lượng
			Product existingProduct = optionalProduct.get();
			existingProduct.setQuantity(existingProduct.getQuantity() + product.getQuantity());
			existingProduct.setEnteredDate(new Date()); // cập nhật ngày nhập
            existingProduct.setStatus(true);
			productRepository.save(existingProduct);
			model.addAttribute("message", "Cập nhật số lượng sản phẩm thành công");
		} else {
			// Sản phẩm mới -> thêm vào
			productRepository.save(product);
			model.addAttribute("message", "Thêm sản phẩm mới thành công");
		}

		return "redirect:/admin/products";
	}

	// show select option ở add product
	@ModelAttribute("categoryList")
	public List<Category> showCategory(Model model) {
		List<Category> categoryList = categoryRepository.findAll();
		model.addAttribute("categoryList", categoryList);

		return categoryList;
	}
	
	// get Edit brand
	@GetMapping(value = "/editProduct/{id}")
	public String editCategory(@PathVariable("id") Long id, ModelMap model) {
		Product product = productRepository.findById(id).orElse(null);

		model.addAttribute("product", product);

		return "admin/editProduct";
	}





	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute("product") Product product,
								@RequestParam("file") MultipartFile file,
								@RequestParam("oldImageName") String oldImageName,
								RedirectAttributes redirectAttributes) {

		try {
			// Lấy product cũ từ DB
			Product existingProduct = productRepository.findById(product.getProductId())
					.orElse(null);

			if (existingProduct == null) {
				redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm cần cập nhật!");
				return "redirect:/admin/products";
			}

			// Nếu có file mới thì lưu file mới
			if (!file.isEmpty()) {
				String fileName = file.getOriginalFilename();
				File uploadFile = new File(pathUploadImage + "/" + fileName);
				try (FileOutputStream fos = new FileOutputStream(uploadFile)) {
					fos.write(file.getBytes());
				}
				product.setProductImage(fileName);
			} else {
				// Nếu không có ảnh mới thì giữ ảnh cũ
				product.setProductImage(oldImageName);
			}

			// Giữ nguyên ngày thêm cũ
			product.setEnteredDate(existingProduct.getEnteredDate());

			// Lưu cập nhật
			productRepository.save(product);

			redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
		} catch (IOException e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật sản phẩm!");
		}

		return "redirect:/admin/products";
	}


	@GetMapping("/deleteProduct/{id}")
	public String toggleProductStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		try {
			Optional<Product> productOpt = productRepository.findById(id);

			if (productOpt.isPresent()) {
				Product product = productOpt.get();
				// 🔄 Đảo trạng thái
				product.setStatus(!Boolean.TRUE.equals(product.getStatus()));
				productRepository.save(product);

				String msg = product.getStatus()
						? "Sản phẩm đã được kích hoạt trở lại."
						: "Sản phẩm đã được ẩn thành công.";
				redirectAttributes.addFlashAttribute("message", msg);
			} else {
				redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật trạng thái sản phẩm!");
		}
		return "redirect:/admin/products";
	}



	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(true);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
	}

	//tuyen
	@PostMapping("/products/import")
	public String importProducts(@RequestParam("file") MultipartFile file,
								 RedirectAttributes redirectAttributes) {
		try (InputStream is = file.getInputStream()) {
			List<Product> products = ExcelHelper.parseExcelToProductsWithAction(is, categoryRepository, productRepository);
			productRepository.saveAll(products);
			redirectAttributes.addFlashAttribute("success", "Import thành công " + products.size() + " sản phẩm");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi import: " + e.getMessage());
			e.printStackTrace();
		}
		return "redirect:/admin/products";
	}
// Xuất file eexecl nhé ...__)__

@PostMapping("/export-products")
public void exportProducts(
		@RequestParam(value = "selectedIds", required = false) List<Long> selectedIds,
		HttpServletResponse response) throws IOException {

	response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	String fileName = "products_" + System.currentTimeMillis() + ".xlsx";
	response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

	List<Product> products;
	if (selectedIds != null && !selectedIds.isEmpty()) {
		products = productRepository.findAllById(selectedIds);
	} else {
		products = productRepository.findAll();
	}

	Workbook workbook = new XSSFWorkbook();
	Sheet sheet = workbook.createSheet("Products");
	String[] columns = {"Product Name", "Quantity", "Price", "Discount", "Image",
			"Description", "Entered Date", "Status", "Category"};

	Row headerRow = sheet.createRow(0);
	for (int i = 0; i < columns.length; i++) {
		headerRow.createCell(i).setCellValue(columns[i]);
	}

	int rowNum = 1;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	for (Product p : products) {
		Row row = sheet.createRow(rowNum++);
		row.createCell(0).setCellValue(p.getProductName());
		row.createCell(1).setCellValue(p.getQuantity());
		row.createCell(2).setCellValue(p.getPrice());
		row.createCell(3).setCellValue(p.getDiscount());
		row.createCell(4).setCellValue(p.getProductImage() != null ? p.getProductImage() : "");
		row.createCell(5).setCellValue(p.getDescription() != null ? p.getDescription() : "");

		if (p.getEnteredDate() != null) {
			LocalDate localDate = ((java.sql.Date) p.getEnteredDate()).toLocalDate();
			row.createCell(6).setCellValue(localDate.format(formatter));
		} else {
			row.createCell(6).setCellValue("");
		}
		row.createCell(7).setCellValue(
				p.getStatus() == null ? "" : p.getStatus().toString()
		);
		row.createCell(8).setCellValue(
				p.getCategory() != null ? p.getCategory().getCategoryName() : ""
		);
	}
	for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

	workbook.write(response.getOutputStream());
	workbook.close();
}


}
