package vn.fs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.fs.entities.Product;
import vn.fs.entities.Comment;
import vn.fs.repository.ProductRepository;
import vn.fs.repository.CommentRepository;
import vn.fs.service.ProductService;

import java.util.List;

@Service // ✅ Đây mới là nơi cần @Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Product getProductByCode(String productCode) {
        return productRepository.getById(Long.valueOf(productCode));
    }

    @Override
    public List<Comment> getCommentsByProduct(String productCode) {
        return commentRepository.getCommentsByProduct(productCode);
    }
}
