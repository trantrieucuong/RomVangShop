package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Comment;

import java.util.List;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{
    List<Comment> getCommentsByProduct(String productCode);

    boolean existsByOrderDetail_OrderDetailId(Long orderDetailId);


}
