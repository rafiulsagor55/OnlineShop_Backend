package com.example.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CartRepository {
	@Autowired
	private NamedParameterJdbcTemplate JdbcTemplate;

	public int addToCart(Cart cart) {
		String sql = "INSERT INTO cart (email, product_id, color, size) "
				+ "VALUES (:email, :productId, :color, :size)";
		Map<String, Object> params = new HashMap<>();
		params.put("email", cart.getEmail());
		params.put("productId", cart.getProductId());
		params.put("color", cart.getColor());
		params.put("size", cart.getSize());
		return JdbcTemplate.update(sql, params);
	}
	
	public boolean isProductInCart(Cart cart) {
	    String checkSql = "SELECT COUNT(*) FROM cart " +
	                      "WHERE email = :email AND product_id = :productId AND color = :color AND size = :size";
	    Map<String, Object> params = new HashMap<>();
	    params.put("email", cart.getEmail());
	    params.put("productId", cart.getProductId());
	    params.put("color", cart.getColor());
	    params.put("size", cart.getSize());

	    int count = JdbcTemplate.queryForObject(checkSql, params, Integer.class);

	    return count > 0;
	}
	
	public boolean isProductExistForSpecificId(String productId) {
		String checkSql = "SELECT COUNT(*) FROM product  WHERE id = :productId";
		Map<String, Object> params = new HashMap<>();
		params.put("productId", productId);
		int count = JdbcTemplate.queryForObject(checkSql, params, Integer.class);
	    return count > 0;
	}

	// Method to retrieve all cart items for a specific email
    public List<Cart> getCartItemsByEmail(String email) {
        String sql = "SELECT * FROM cart WHERE email = :email";
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        return JdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Cart.class));
    }
    
    public void deleteCartItem(String id) {
		String sql="DELETE FROM cart WHERE id = :id";
		Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        JdbcTemplate.update(sql, params);
		
	}
    
    public void updateCartItemCount(String email, int count) {
        String sql = "INSERT INTO newItemsCounter (email, notifications, cartItems) " +
                     "VALUES (:email, 0, :cartItems) " +
                     "ON DUPLICATE KEY UPDATE cartItems = :cartItems";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("cartItems", count);

        JdbcTemplate.update(sql, params);
    }

    public int getCartItemCount(String email) {
        String sql = "SELECT cartItems FROM newItemsCounter WHERE email = :email";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);

        try {
            Integer count = JdbcTemplate.queryForObject(sql, params, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isEmailValid(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = :email";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);
        Integer count = JdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

}
