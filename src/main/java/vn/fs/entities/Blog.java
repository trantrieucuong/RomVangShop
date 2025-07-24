package vn.fs.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;


import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "blogs")
public class Blog {
    @Id
    @Column(name = "blog_code", length = 10, nullable = false)
    String blogCode;

    String title;
    String slug;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "TEXT")
    String content;

    String thumbnail;
    Boolean status;

    Date createdAt;
    Date updatedAt;
    Date publishedAt;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User user;

    @PrePersist
    public void prePersist() {
        createdAt = new Date();
        updatedAt = new Date();
        if(status) {
            publishedAt = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = new Date();
        if(status) {
            publishedAt = new Date();
        } else {
            publishedAt = null;
        }

    }

    @Override
    public String toString() {
        return "Blog{" +
                "blogCode='" + blogCode + '\'' +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", description='" + description + '\'' +
                ", content='" + content + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", publishedAt=" + publishedAt +
                ", user=" + user +
                '}';
    }
}
