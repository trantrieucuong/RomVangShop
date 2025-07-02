package vn.fs.service;

import java.util.Collection;

import org.springframework.stereotype.Service;

import vn.fs.entities.CartItem;
import vn.fs.entities.Product;


@Service
public interface ShoppingCartService {

	int getCount();

	double getAmount();

	void clear();

	Collection<CartItem> getCartItems();

	void remove(CartItem item);

	void add(CartItem item);
	
	public void updateQuantity(Long productId, int newQuantity);

	void remove(Product product);

}
