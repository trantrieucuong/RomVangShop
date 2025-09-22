package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.fs.entities.Category;
import vn.fs.entities.Product;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // tuyên
    Category findByCategoryNameIgnoreCase(String categoryName);


}
