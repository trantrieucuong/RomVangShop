package vn.fs.service;

import vn.fs.entities.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> getCommentsByProduct(String productCode);
    void saveComment(Comment comment);
}
