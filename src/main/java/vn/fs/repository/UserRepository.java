package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.fs.entities.User;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByEmail(String email);
	Optional<User> findByPhone(String phone);


}
