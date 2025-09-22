package vn.fs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Category;
import vn.fs.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Lấy sản phẩm theo categoryId (native SQL)
    @Query(value = "SELECT * FROM products WHERE category_id = ?1", nativeQuery = true)
    List<Product> listProductByCategory(Long categoryId);

    // Lấy sản phẩm theo categoryId (JPQL)
    List<Product> findByCategory_CategoryId(Long categoryId);

    // Lấy 10 sản phẩm mới nhất (JPQL)
    @Query(value = "SELECT p FROM Product p ORDER BY p.productId DESC")
    List<Product> listProductNew10();

    // Lấy 10 sản phẩm mới nhất (native SQL)
    @Query(value = "SELECT * FROM products ORDER BY product_id DESC LIMIT 10", nativeQuery = true)
    List<Product> listProductNew101();

    // Tìm kiếm sản phẩm theo tên (native SQL)
    @Query(value = "SELECT * FROM products WHERE product_name LIKE %?1%", nativeQuery = true)
    List<Product> searchProduct(String productName);

    // Đếm số lượng sản phẩm theo từng category
    @Query(value = "SELECT c.category_id, c.category_name, COUNT(*) AS SoLuong " +
            "FROM products p " +
            "JOIN categories c ON p.category_id = c.category_id " +
            "GROUP BY c.category_id, c.category_name", nativeQuery = true)
    List<Object[]> listCategoryByProductName();

    // Top 20 sản phẩm bán chạy nhất
    @Query(value = "SELECT p.product_id, COUNT(*) AS SoLuong " +
            "FROM order_details p " +
            "JOIN products c ON p.product_id = c.product_id " +
            "GROUP BY p.product_id " +
            "ORDER BY SoLuong DESC LIMIT 20", nativeQuery = true)
    List<Object[]> bestSaleProduct20();

    // Lấy danh sách sản phẩm theo danh sách id
    @Query(value = "SELECT * FROM products WHERE product_id IN :ids", nativeQuery = true)
    List<Product> findByInventoryIds(@Param("ids") List<Integer> listProductId);

    // ================== Derived Query Methods ==================

    // Tìm sản phẩm theo tên chứa từ khóa (không phân biệt hoa thường)
    List<Product> findByProductNameContainingIgnoreCase(String productName);

    // Tìm sản phẩm có số lượng > quantity và status = true/false
    List<Product> findByQuantityGreaterThanAndStatus(int quantity, boolean status);

    // Tìm sản phẩm theo tên (ignore case) + category
    Optional<Product> findByProductNameIgnoreCaseAndCategory(String name, Category category);

    // Tìm sản phẩm theo tên (ignore case)
    Optional<Product> findByProductNameIgnoreCase(String productName);

    // Tất cả sản phẩm còn hoạt động (status = true) và còn hàng
    List<Product> findByStatusTrueAndQuantityGreaterThan(int quantity);

    // Sản phẩm theo category, còn hoạt động và còn hàng
    List<Product> findByCategory_CategoryIdAndStatusTrueAndQuantityGreaterThan(Long categoryId, int quantity);

    // ⭐ Lấy tất cả sản phẩm còn hoạt động (status = true)
    List<Product> findByStatusTrue();
}
