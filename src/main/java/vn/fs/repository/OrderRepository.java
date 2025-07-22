package vn.fs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Order;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query(value = "select * from orders where user_id = ?1", nativeQuery = true)
	List<Order> findOrderByUserId(Long id);

	List<Order> findAllByUser_UserIdAndStatus(Long userId, int status);

	@Query("SELECT o FROM Order o WHERE FUNCTION('MONTH', o.orderDate) = :month")
	List<Order> findByMonth(@Param("month") int month);
	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.cancellation")
	List<Order> findAllWithCancellation();



	@Query("SELECT SUM(o.amount) FROM Order o WHERE o.status = 2")
	    Double findTotalRevenue();
	    
	 @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 2")
	 Long countSuccessfulOrders();
	    
	 @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 3")
	 Long countCancelledOrders();
	 
	 @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 0")
	 Long countNewOrders();
//tuyên
	@Query("SELECT MAX(o.orderId) FROM Order o")
	Long findMaxOrderId();
}
