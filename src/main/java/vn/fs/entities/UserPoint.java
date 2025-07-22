package vn.fs.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_points")
public class UserPoint implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // liên kết tới User
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    private Integer point;

    public UserPoint() {}

    public UserPoint(User user, Integer point) {
        this.user = user;
        this.point = point;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public void addPoint(int amount) {
        this.point += amount;
    }
}
