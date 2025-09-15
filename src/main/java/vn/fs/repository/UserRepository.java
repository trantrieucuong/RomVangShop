package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.fs.entities.User;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByEmail(String email);
	Optional<User> findByPhone(String phone);

// tuyên
@Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = 3 OR r.name = 'ROLE_SALE'")
List<User> findSaleWithRoleUser();
	@Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.id = 1 OR r.name = 'ROLE_USER'")
	List<User> findCustomerWithRoleUser();

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByPhone(String phone);

}
