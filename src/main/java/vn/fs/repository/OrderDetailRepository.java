package vn.fs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Order;
import vn.fs.entities.OrderDetail;
import vn.fs.entities.Product;


@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

	@Query(value = "select * from order_details where order_id = ?;", nativeQuery = true)
	List<OrderDetail> findByOrderId(Long id);
	
	// Statistics by product sold
    @Query(value = "SELECT p.product_name , \r\n"
    		+ "SUM(o.quantity) as quantity ,\r\n"
    		+ "SUM(o.quantity * o.price) as sum,\r\n"
    		+ "AVG(o.price) as avg,\r\n"
    		+ "Min(o.price) as min, \r\n"
    		+ "max(o.price) as max\r\n"
    		+ "FROM order_details o\r\n"
    		+ "INNER JOIN products p ON o.product_id = p.product_id\r\n"
    		+ "GROUP BY p.product_name;", nativeQuery = true)
    public List<Object[]> repo();
    
    // Statistics by category sold
    @Query(value = "SELECT c.category_name , \r\n"
    		+ "SUM(o.quantity) as quantity ,\r\n"
    		+ "SUM(o.quantity * o.price) as sum,\r\n"
    		+ "AVG(o.price) as avg,\r\n"
    		+ "Min(o.price) as min,\r\n"
    		+ "max(o.price) as max \r\n"
    		+ "FROM order_details o\r\n"
    		+ "INNER JOIN products p ON o.product_id = p.product_id\r\n"
    		+ "INNER JOIN categories c ON p.category_id = c.category_id\r\n"
    		+ "GROUP BY c.category_name;", nativeQuery = true)
    public List<Object[]> repoWhereCategory();
    
    // Statistics of products sold by year
    @Query(value = "Select YEAR(od.order_date) ,\r\n"
    		+ "SUM(o.quantity) as quantity ,\r\n"
    		+ "SUM(o.quantity * o.price) as sum,\r\n"
    		+ "AVG(o.price) as avg,\r\n"
    		+ "Min(o.price) as min,\r\n"
    		+ "max(o.price) as max \r\n"
    		+ "FROM order_details o\r\n"
    		+ "INNER JOIN orders od ON o.order_id = od.order_id\r\n"
    		+ "GROUP BY YEAR(od.order_date);", nativeQuery = true)
    public List<Object[]> repoWhereYear();
    
    // Statistics of products sold by month
    @Query(value = "Select month(od.order_date) ,\r\n"
    		+ "SUM(o.quantity) as quantity ,\r\n"
    		+ "SUM(o.quantity * o.price) as sum,\r\n"
    		+ "AVG(o.price) as avg,\r\n"
    		+ "Min(o.price) as min,\r\n"
    		+ "max(o.price) as max\r\n"
    		+ "FROM order_details o\r\n"
    		+ "INNER JOIN orders od ON o.order_id = od.order_id\r\n"
    		+"WHERE od.status = '2' "  // Thêm điều kiện trạng thái hoàn thành
    		+ "GROUP BY month(od.order_date);", nativeQuery = true)
    public List<Object[]> repoWhereMonth();
    
    // Statistics of products sold by quarter
    @Query(value = "Select QUARTER(od.order_date),\r\n"
    		+ "SUM(o.quantity) as quantity ,\r\n"
    		+ "SUM(o.quantity * o.price) as sum,\r\n"
    		+ "AVG(o.price) as avg,\r\n"
    		+ "Min(o.price) as min,\r\n"
    		+ "max(o.price) as max\r\n"
    		+ "FROM order_details o\r\n"
    		+ "INNER JOIN orders od ON o.order_id = od.order_id\r\n"
    		+ "GROUP By QUARTER(od.order_date);", nativeQuery = true)
    public List<Object[]> repoWhereQUARTER();
    
    // Statistics by user
    @Query(value = "SELECT c.name,\r\n"
    		+ "SUM(o.quantity) as quantity,\r\n"
    		+ "SUM(o.quantity * o.price) as sum,\r\n"
    		+ "AVG(o.price) as avg,\r\n"
    		+ "Min(o.price) as min,\r\n"
    		+ "max(o.price) as max\r\n"
    		+ "FROM order_details o\r\n"
    		+ "INNER JOIN orders p ON o.order_id = p.order_id\r\n"
    		+ "INNER JOIN user c ON p.user_id = c.user_id\r\n"
    		+ "GROUP BY c.user_id;", nativeQuery = true)
    public List<Object[]> reportCustommer();
	// tuyên
	Optional<OrderDetail> findByOrderAndProduct(Order order, Product product);
	List<OrderDetail> findByOrder(Order order);

}
