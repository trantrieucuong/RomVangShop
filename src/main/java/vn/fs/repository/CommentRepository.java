package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Comment;

import java.util.List;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{
    List<Comment> getCommentsByProduct(String productCode);


    @Query("SELECT c FROM Comment c WHERE c.product.productId = :productId")
    List<Comment> findByProductId(@Param("productId") Long productId);

    boolean existsByOrderDetail_OrderDetailId(Long orderDetailId);



}
