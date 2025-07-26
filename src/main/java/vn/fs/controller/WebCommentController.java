package vn.fs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.fs.entities.Comment;
import vn.fs.entities.OrderDetail;
import vn.fs.entities.Product;
import vn.fs.entities.User;
import vn.fs.repository.CommentRepository;
import vn.fs.repository.OrderDetailRepository;
import vn.fs.repository.UserRepository;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping("/comments")
public class WebCommentController {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public String addComment(@RequestParam("orderDetailId") Long orderDetailId,
                             @RequestParam("productId") Long productId,
                             @RequestParam("rating") Double rating,
                             @RequestParam("content") String content,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {

        // Kiểm tra đăng nhập
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để đánh giá.");
            return "redirect:/dang-nhap";
        }

        // Lấy thông tin người dùng
        User user = userRepository.findByEmail(principal.getName());

        // Kiểm tra đơn hàng tồn tại
        Optional<OrderDetail> optionalDetail = orderDetailRepository.findById(orderDetailId);
        if (optionalDetail.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin đơn hàng.");
            return "redirect:/profile";
        }

        OrderDetail orderDetail = optionalDetail.get();
        Product product = orderDetail.getProduct();

        // Kiểm tra đã đánh giá chưa
        boolean exists = commentRepository.existsByOrderDetail_OrderDetailId(orderDetailId);
        if (exists) {
            redirectAttributes.addFlashAttribute("error", "Bạn đã đánh giá sản phẩm này.");
            return "redirect:/order/detail/" + orderDetail.getOrder().getOrderId();
        }

        // Tạo bình luận
        Comment comment = new Comment();
        comment.setOrderDetail(orderDetail);
        comment.setUser(user);
        comment.setProduct(product);
        comment.setRating(rating);
        comment.setContent(content);
        comment.setRateDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        commentRepository.save(comment);
        redirectAttributes.addFlashAttribute("success", "Đánh giá đã được gửi!");

        return "redirect:/order/detail/" + orderDetail.getOrder().getOrderId();
    }

    @PostMapping("/comments/add")
    public String addCommentt(@RequestParam("orderDetailId") Long orderDetailId,
                             @RequestParam("productId") Long productId,
                             @RequestParam("rating") Double rating,
                             @RequestParam("content") String content,
                             Principal principal,
                             RedirectAttributes redirectAttrs) {

        if (principal == null) {
            redirectAttrs.addFlashAttribute("error", "Bạn cần đăng nhập để đánh giá.");
            return "redirect:/dang-nhap";
        }

        Optional<OrderDetail> opt = orderDetailRepository.findById(orderDetailId);
        if (opt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/profile";
        }
        OrderDetail od = opt.get();

        if (commentRepository.existsByOrderDetail_OrderDetailId(orderDetailId)) {
            redirectAttrs.addFlashAttribute("error", "Bạn đã đánh giá sản phẩm này rồi.");
            return "redirect:/order/detail/" + od.getOrder().getOrderId();
        }

        User user = userRepository.findByEmail(principal.getName());

        Comment comment = new Comment();
        comment.setOrderDetail(od);
        comment.setProduct(od.getProduct());
        comment.setUser(user);
        comment.setRating(rating);
        comment.setContent(content);
        comment.setRateDate(new Date());

        commentRepository.save(comment);
        redirectAttrs.addFlashAttribute("success", "Đánh giá đã gửi!");

        return "redirect:/order/detail/" + od.getOrder().getOrderId();
    }
}
