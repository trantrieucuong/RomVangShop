package vn.fs.entities;

import java.util.Date;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_cancellations")
public class OrderCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reason;

    @Temporal(TemporalType.TIMESTAMP)
    private Date cancelledAt;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    @JsonBackReference
    private Order order;



}
