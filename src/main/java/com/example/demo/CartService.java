package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {
	@Autowired
    private CartRepository cartRepository;
	@Autowired
    private ProductRepository productRepository;
	
	public boolean IsProductExistForSpecificId(String productId) {
		return cartRepository.isProductExistForSpecificId(productId);
	}

    public void addToCart(Cart cart) {
        if (cartRepository.isProductInCart(cart)) {
            throw new IllegalArgumentException("Product already exists in cart!");
        }
        int result = cartRepository.addToCart(cart);
        if (result <= 0) {
        	throw new IllegalArgumentException("Error adding product to cart.");
        }
    }	
    
    public List<CartDTO> GetCartItemsByEmail(String email) {
        List<CartDTO> cartDTOs = new ArrayList<>();
        
        // Ensure cartRepository.getCartItemsByEmail(email) returns a list, not null.
        List<Cart> cartItemsList = cartRepository.getCartItemsByEmail(email);
        
        if (cartItemsList != null) {
            for (Cart cartItems : cartItemsList) {
                CartDTO cartDTO = new CartDTO();
                
                // Retrieve product details for each cart item
                ObjectForCart objectForCart = productRepository.getProductDetailsForCartById(cartItems.getProductId());
        
                cartDTO.setId(cartItems.getId());
                cartDTO.setProductId(cartItems.getProductId());
                cartDTO.setName(objectForCart.getName() + " - " + cartItems.getColor());
                cartDTO.setPrice(objectForCart.getPrice());
                cartDTO.setDiscount(objectForCart.getDiscount());
                cartDTO.setQuantity(1); // Set the quantity to 1 (or retrieve it if it's variable)
                cartDTO.setImage(productRepository.getImageUrlByProductIdAndColor(cartItems.getProductId(), cartItems.getColor()));
                cartDTO.setSelected(false); 
                cartDTO.setSize(cartItems.getSize());
                cartDTO.setColor(cartItems.getColor());
                cartDTOs.add(cartDTO);
            }
        }
        return cartDTOs;
    }

}
