package vn.fs.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order implements Serializable {

	@Id
	@Column(name = "order_id")
	private Long orderId;
	@Temporal(TemporalType.DATE)
	private Date orderDate;
	private Double amount;
	private String address;
	private String phone;
	private int status;

	private String note;

	@OneToMany(mappedBy = "order")
	@JsonManagedReference
	private List<OrderDetail> orderDetails;

	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	private OrderCancellation cancellation;


	@ManyToOne
	@JoinColumn(name = "userId")
	private User user;




}
