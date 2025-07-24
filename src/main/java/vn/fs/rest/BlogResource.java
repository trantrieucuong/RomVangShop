package vn.fs.rest;


import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.fs.dto.UpsertBlogRequest;
import vn.fs.entities.Blog;
import vn.fs.service.BlogService;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogResource {
    private final BlogService blogService;

    // Lấy tất cả bài viết
    @GetMapping
    public ResponseEntity<List<Blog>> getAllBlogs() {
        List<Blog> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs); // status code 200
    }

    // Lấy tất cả bài viết của user đang đăng nhập
    @GetMapping("/current-user")
    public ResponseEntity<List<Blog>> getAllBlogOfCurrentUser() {
        List<Blog> blogs = blogService.getAllBlogOfCurrentUser();
        return ResponseEntity.ok(blogs); // status code 200
    }

    // Lấy bài viết theo mã
    @GetMapping("/{blogCode}")
    public ResponseEntity<Blog> getBlogById(@PathVariable String blogCode) { // Chuyển từ Integer sang String
        Blog blog = blogService.getBlogById(blogCode);
        return ResponseEntity.ok(blog); // status code 200
    }

    // Tạo bài viết
    @PostMapping
    public ResponseEntity<Blog> createBlog(@RequestBody UpsertBlogRequest request) {
        Blog blog = blogService.createBlog(request);
        return ResponseEntity.ok(blog); // status code 200
    }

    // Cập nhật bài viết
    @PutMapping("/{blogCode}")
    public ResponseEntity<Blog> updateBlog(@RequestBody UpsertBlogRequest request, @PathVariable String blogCode) { // Chuyển từ Integer sang String
        Blog blog = blogService.updateBlog(blogCode, request);
        return ResponseEntity.ok(blog); // status code 200
    }

    // Xóa bài viết
    @DeleteMapping("/{blogCode}")
    public ResponseEntity<Void> deleteBlog(@PathVariable String blogCode) { // Chuyển từ Integer sang String
        blogService.deleteBlog(blogCode);
        return ResponseEntity.noContent().build(); // status code 204
    }

    // Upload thumbnail
    @PostMapping("/{blogCode}/upload-thumbnail")
    public ResponseEntity<String> uploadThumbnail(@RequestParam("file") MultipartFile file, @PathVariable String blogCode) { // Chuyển từ Integer sang String
        String filePath = blogService.uploadThumbnail(blogCode, file);
        return ResponseEntity.ok(filePath); // status code 200
    }
}
