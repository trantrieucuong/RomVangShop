package vn.fs.controller.Sale;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.fs.entities.Role;
import vn.fs.entities.User;
import vn.fs.repository.RoleRepository;
import vn.fs.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

//    @GetMapping("/search")
//    public ResponseEntity<?> searchByPhone(@RequestParam String phone) {
//        Optional<User> user = userRepository.findByPhone(phone.trim());
//        if (user.isPresent()) {
//            return ResponseEntity.ok(user.get());
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
//        }
//    }

    @PostMapping("/addKhachHang")
    public ResponseEntity<?> createCustomer(@RequestBody User user) {
        // Chuẩn hóa email nếu có
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }

        // ✅ Kiểm tra trùng số điện thoại
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Số điện thoại đã tồn tại");
        }

        // ✅ Kiểm tra trùng email
        if (user.getEmail() != null && userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email đã tồn tại");
        }

        // Gán ngày đăng ký nếu chưa có
        if (user.getRegisterDate() == null) {
            user.setRegisterDate(new Date());
        }

        // Gán trạng thái mặc định là true nếu chưa set
        if (user.getStatus() == null) {
            user.setStatus(true);
        }

        // Nếu không gán role thì có thể set mặc định (ví dụ: ROLE_USER)
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName("ROLE_USER");
            user.setRoles(List.of(defaultRole));
        }
// 🔐 Tạo mật khẩu ngẫu nhiên và mã hóa bằng BCrypt
        String rawPassword = generateRandomPassword(8);
        user.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
        // Lưu user
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser);
    }
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
//    private String generateNextCustomerCode() {
//        String prefix = "KH";
//        String maxCode = customerRepository.findMaxCustomerCode(); // Ví dụ: KH000023
//        int nextNumber = 1;
//
//        if (maxCode != null && maxCode.startsWith(prefix)) {
//            try {
//                nextNumber = Integer.parseInt(maxCode.substring(2)) + 1;
//            } catch (NumberFormatException ignored) {
//            }
//        }
//
//        return String.format("%s%06d", prefix, nextNumber);
//    }


//    @GetMapping("/checkPhone")
//    public ResponseEntity<Boolean> checkPhone(@RequestParam String phone) {
//        boolean exists = customerRepository.existsByPhone(phone);
//        return ResponseEntity.ok(exists);
//    }
//    @GetMapping("/checkEmail")
//    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
//        if (email == null || email.trim().isEmpty()) {
//            return ResponseEntity.ok(false);
//        }
//        boolean exists = customerRepository.existsByEmailIgnoreCase(email.trim());
//
//        return ResponseEntity.ok(exists);
//    }
}
