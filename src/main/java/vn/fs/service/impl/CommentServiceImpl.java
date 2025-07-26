package vn.fs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.fs.entities.Comment;
import vn.fs.repository.CommentRepository;
import vn.fs.service.CommentService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public List<Comment> getCommentsByProduct(String productCode) {
        return commentRepository.getCommentsByProduct(productCode);
    }

    @Override
    public void saveComment(Comment comment) {
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        comment.setRateDate(date);

        commentRepository.save(comment);
    }
}

