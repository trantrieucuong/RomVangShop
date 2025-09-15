package vn.fs.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.fs.entities.Category;
import vn.fs.entities.Product;
import vn.fs.repository.CategoryRepository;
import vn.fs.repository.ProductRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelHelper {
    public static List<Product> parseExcelToProductsWithAction(InputStream is,
                                                               CategoryRepository categoryRepo,
                                                               ProductRepository productRepo) throws IOException {
        List<Product> products = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // bỏ header
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String action = row.getCell(0).getStringCellValue().trim().toLowerCase();

            // Đọc dữ liệu sản phẩm
            String name = row.getCell(1).getStringCellValue();
            int quantity = (int) row.getCell(2).getNumericCellValue();
            double price = row.getCell(3).getNumericCellValue();
            int discount = (int) row.getCell(4).getNumericCellValue();
            String image = row.getCell(5).getStringCellValue();
            String description = row.getCell(6).getStringCellValue();

            LocalDate enteredDate = row.getCell(7).getLocalDateTimeCellValue().toLocalDate();
            boolean status = row.getCell(8).getBooleanCellValue();
            Long categoryId = (long) row.getCell(9).getNumericCellValue();

            Category category = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy category: " + categoryId));

            if ("add".equals(action)) {
                Product product = new Product();
                product.setProductName(name);
                product.setQuantity(quantity);
                product.setPrice(price);
                product.setDiscount(discount);
                product.setProductImage(image);
                product.setDescription(description);
                product.setEnteredDate(Date.from(enteredDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                product.setStatus(status);
                product.setCategory(category);
                products.add(product);
            } else if ("update".equals(action)) {
                // update theo productName (hoặc ID nếu bạn có cột ID)
                productRepo.findByProductNameIgnoreCase(name).ifPresent(existing -> {
                    existing.setQuantity(quantity);
                    existing.setPrice(price);
                    existing.setDiscount(discount);
                    existing.setProductImage(image);
                    existing.setDescription(description);
                    existing.setEnteredDate(Date.from(enteredDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    existing.setStatus(status);
                    existing.setCategory(category);
                    products.add(existing);
                });
            }
        }
        workbook.close();
        return products;
    }
}

