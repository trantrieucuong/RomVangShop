package vn.fs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Product;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query(value = "SELECT * FROM products WHERE category_id = ?", nativeQuery = true)
	public List<Product> listProductByCategory(Long categoryId);

	@Query(value = "SELECT * FROM products AS b WHERE b.category_id = ?;", nativeQuery = true)
	List<Product> listProductByCategory10(Long categoryId);

	@Query(value = "SELECT p FROM Product p ORDER BY p.id DESC")
	public List<Product> listProductNew10();

	@Query(value = "SELECT * FROM products WHERE product_name LIKE %?1%" , nativeQuery = true)
	public List<Product> searchProduct(String productName);

	@Query(value = "SELECT c.category_id, c.category_name, COUNT(*) AS SoLuong " +
			"FROM products p " +
			"JOIN categories c ON p.category_id = c.category_id " +
			"GROUP BY c.category_id, c.category_name", nativeQuery = true)
	List<Object[]> listCategoryByProductName();

	@Query(value = "SELECT p.product_id, COUNT(*) AS SoLuong " +
			"FROM order_details p " +
			"JOIN products c ON p.product_id = c.product_id " +
			"GROUP BY p.product_id " +
			"ORDER BY SoLuong DESC LIMIT 20;", nativeQuery = true)
	public List<Object[]> bestSaleProduct20();

	@Query(value = "select * from products o where product_id in :ids", nativeQuery = true)
	List<Product> findByInventoryIds(@Param("ids") List<Integer> listProductId);
	//tuyên
	List<Product> findByCategory_CategoryId(Long categoryId);
	List<Product> findByProductNameContainingIgnoreCase(String productName);

}
