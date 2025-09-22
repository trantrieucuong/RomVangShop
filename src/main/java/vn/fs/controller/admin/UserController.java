package vn.fs.controller.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.fs.entities.Role;
import vn.fs.entities.User;
import vn.fs.repository.RoleRepository;
import vn.fs.repository.UserRepository;


@Controller
public class UserController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Value("${upload.path}")
    private String pathUploadImage;

    @GetMapping(value = "/admin/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String customer(Model model, Principal principal) {

        User user = userRepository.findByEmail(principal.getName());
        model.addAttribute("user", user);

        List<User> users = userRepository.findCustomerWithRoleUser();
        model.addAttribute("users", users);

        return "/admin/users";
    }

    @GetMapping(value = "/admin/nhanvien")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String nhanVien(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        model.addAttribute("user", user);

        // Chỉ lấy user có role là ROLE_USER
        List<User> users = userRepository.findSaleWithRoleUser();
        model.addAttribute("users", users);

        return "/admin/nhanvien";
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/admin/users/{userId}/lock")
    public String lockUser(@PathVariable("userId") Long userId,
                           RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(false); // Khóa tài khoản
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Đã khóa tài khoản: " + user.getName());
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + userId);
        }

        return "redirect:/admin/users";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/admin/users/{userId}/unlock")
    public String unlockUser(@PathVariable("userId") Long userId,
                             RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(true); // Mở khóa tài khoản
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Đã mở khóa tài khoản: " + user.getName());
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + userId);
        }

        return "redirect:/admin/users";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/admin/nhanvien/{userId}/lock")
    public String lockNhanVien(@PathVariable("userId") Long userId,
                               RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(false); // Khóa tài khoản
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Đã khóa tài khoản: " + user.getName());
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + userId);
        }

        return "redirect:/admin/nhanvien";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/admin/nhanvien/{userId}/unlock")
    public String unlockNhanVien(@PathVariable("userId") Long userId,
                                 RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(true); // Mở khóa tài khoản
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Đã mở khóa tài khoản: " + user.getName());
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với ID: " + userId);
        }

        return "redirect:/admin/nhanvien";
    }

    @PostMapping("/users/add")
    public String addUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password, // thêm dòng này
            @RequestParam(value = "status", required = false, defaultValue = "true") Boolean status,
            @RequestParam(value = "registerDate", required = false) Date registerDate,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            RedirectAttributes redirectAttributes) {

        // kiểm tra email đã tồn tại chưa
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            redirectAttributes.addFlashAttribute("error", "Email này đã tồn tại!");
            return "redirect:/admin/nhanvien";
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);

        // mã hóa mật khẩu người nhập
        user.setPassword(new BCryptPasswordEncoder().encode(password));

        user.setPhone(phone);
        user.setStatus(status);
        user.setRegisterDate(registerDate != null ? registerDate : new Date());

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName("ROLE_SALE");
            user.setRoles(List.of(defaultRole));
        }

        // xử lý upload ảnh
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                File dir = new File(pathUploadImage);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String fileName = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                File convFile = new File(dir, fileName);

                try (FileOutputStream fos = new FileOutputStream(convFile)) {
                    fos.write(avatarFile.getBytes());
                }
                user.setAvatar(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu ảnh đại diện!");
                user.setAvatar("default-avatar.png");
            }
        } else {
            user.setAvatar("default-avatar.png");
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Thêm nhân viên thành công!");
        return "redirect:/admin/nhanvien";
    }


    @PostMapping("/users/update")
    public String updateUser(
            @RequestParam("userId") Long userId,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
//            @RequestParam("status") Boolean status,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên!");
            return "redirect:/admin/nhanvien";
        }

        User user = optionalUser.get();

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Tên không được để trống!");
            return "redirect:/admin/nhanvien";
        }
        user.setName(name.trim());

        // Validate email
        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email không được để trống!");
            return "redirect:/admin/nhanvien";
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            redirectAttributes.addFlashAttribute("error", "Email không hợp lệ!");
            return "redirect:/admin/nhanvien";
        }

        // Nếu đổi email thì check trùng
        if (!user.getEmail().equals(email)) {
            User existingUser = userRepository.findByEmail(email);
            if (existingUser != null) {
                redirectAttributes.addFlashAttribute("error", "Email này đã tồn tại!");
                return "redirect:/admin/nhanvien";
            }
            user.setEmail(email.trim());
        }

        // Validate phone (nếu có)
        if (phone != null && !phone.trim().isEmpty()) {
            if (!phone.matches("\\d{9,11}")) { // chỉ cho phép 9–11 chữ số
                redirectAttributes.addFlashAttribute("error", "Số điện thoại không hợp lệ!");
                return "redirect:/admin/nhanvien";
            }
            user.setPhone(phone.trim());
        } else {
            user.setPhone(null);
        }

//        user.setStatus(status);

        // Upload avatar mới
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                File dir = new File(pathUploadImage);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                File convFile = new File(dir, fileName);

                try (FileOutputStream fos = new FileOutputStream(convFile)) {
                    fos.write(avatarFile.getBytes());
                }

                user.setAvatar(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu ảnh đại diện!");
            }
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Cập nhật nhân viên thành công!");
        return "redirect:/admin/nhanvien";
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
}
