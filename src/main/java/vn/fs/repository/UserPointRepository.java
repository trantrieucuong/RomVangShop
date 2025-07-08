package vn.fs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.fs.entities.User;
import vn.fs.entities.UserPoint;

import java.util.Optional;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {
    Optional<UserPoint> findByUser(User user);
}
