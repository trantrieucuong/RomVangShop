package vn.fs.controller.Sale;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.fs.entities.Role;
import vn.fs.entities.User;
import vn.fs.repository.RoleRepository;
import vn.fs.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
            Role defaultRole = roleRepository.findByName("ROLE_USER"); // cần khai báo RoleRepository
            user.setRoles(List.of(defaultRole));
        }

        // Lưu user
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser);
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
