package vn.fs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.fs.entities.Role;
import vn.fs.repository.RoleRepository;

@SpringBootTest
class GreenyShopApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void createRoles() {
        roleRepository.save(new Role("ROLE_USER"));
        roleRepository.save(new Role("ROLE_ADMIN"));
        roleRepository.save(new Role("ROLE_SALE"));

        System.out.println("✅ Đã tạo 3 role mặc định!");
    }

}
