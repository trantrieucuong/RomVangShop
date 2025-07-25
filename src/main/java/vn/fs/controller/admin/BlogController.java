package vn.fs.controller.admin;

import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.fs.entities.Blog;
import vn.fs.service.BlogService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/blogs")
public class BlogController {
    private final BlogService blogService;

    // Danh sách tất cả bài viết
    @GetMapping
    public String getHomePage(Model model) {
        // Lấy tất cả bài viết sắp xếp theo createdAt giảm dần
        List<Blog> blogList = blogService.getAllBlogs();
        model.addAttribute("blogList", blogList);
        return "admin/indexBlog";
    }

    // Danh sách bài viết của tôi
    @GetMapping("/own-blogs")
    public String getOwnPage(Model model) {
        // Lấy tất cả của user đang đăng nhập, sắp xếp theo createdAt giảm dần
        // Lấy user đang đăng nhập lấy trong session với key là "currentUser"
        // Lấy bài viết theo userId
        List<Blog> blogList = blogService.getAllBlogOfCurrentUser();
        model.addAttribute("blogList", blogList);
        return "admin/own-blog";
    }

    // Tạo bài viết
    @GetMapping("/create")
    public String getCreatePage(Model model) {
        return "admin/create";
    }
    //
    // Chi tiết bài viết
//    @GetMapping("/{id}/detail")
//    public String getDetailPage(@PathVariable String blogCode, Model model) {
//        // Lấy bài viết theo id
//        Blog blog = blogService.getBlogById(blogCode);
//        model.addAttribute("blog", blog);
//        return "admin/blog/detail";
//    }
    @GetMapping("/{blogCode}/detail")
    public String getDetailPage(@PathVariable String blogCode, Model model) {
        // Lấy bài viết theo blogCode
        Blog blog = blogService.getBlogById(blogCode);


        // Kiểm tra xem blog có tồn tại hay không
        if (blog != null) {
            model.addAttribute("blog", blog);
            model.addAttribute("blogCode", blogCode); // Thêm blogCode vào mô hình
            return "admin/detailBlog"; // Trả về tên template Thymeleaf
        } else {
            // Nếu không tìm thấy blog, có thể chuyển hướng hoặc hiển thị thông báo lỗi
            return "admin/error"; // Hoặc chuyển hướng về trang lỗi
        }
    }

}
