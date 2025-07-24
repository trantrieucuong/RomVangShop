package vn.fs.repository;


import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import vn.fs.entities.Blog;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, String> { // Đổi Integer thành String
    List<Blog> findByUser_UserId(Long userId, Sort sort);

    @Query(value = "SELECT b FROM Blog b where b.status=true ORDER BY b.createdAt DESC")
    List<Blog> findTop8ByOrderByCreatedAtDesc(Pageable pageable);

    Optional<Blog> findByBlogCode(String blogCode);
}
