package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.fs.entities.OrderCancellation;

public interface OrderCancellationRepository extends JpaRepository<OrderCancellation, Long> {
}
