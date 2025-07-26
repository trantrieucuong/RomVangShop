package vn.fs.service;

import org.springframework.stereotype.Service;
import vn.fs.entities.Product;
import vn.fs.entities.Comment;

import java.util.List;

public interface ProductService {
    Product getProductByCode(String productCode);
    List<Comment> getCommentsByProduct(String productCode);
}
