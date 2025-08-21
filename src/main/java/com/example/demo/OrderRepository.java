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
public class OrderRepository {
 
	@Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    // Method to save the order
    public void createOrder(Order order) {
        String orderInsertSql = "INSERT INTO orders (UUID, status, customer, email, contact, address, deliveryMethod, paymentMethod, payment, processedDate, shippedDate, deliveredDate, readyDate, pickedDate) " +
                "VALUES (:UUID, :status, :customer, :email, :contact, :address, :deliveryMethod, :paymentMethod, :payment, :processedDate, :shippedDate, :deliveredDate, :readyDate, :pickedDate)";
        
        MapSqlParameterSource orderParams = new MapSqlParameterSource()
                .addValue("UUID", order.getUUID())
                .addValue("status", order.getStatus())
                .addValue("customer", order.getCustomer())
                .addValue("email", order.getEmail())
                .addValue("contact", order.getContact())
                .addValue("address", order.getAddress())
                .addValue("deliveryMethod", order.getDeliveryMethod())
                .addValue("paymentMethod", order.getPaymentMethod())
                .addValue("payment", order.getPayment())
                .addValue("processedDate", order.getProcessedDate())
                .addValue("shippedDate", order.getShippedDate())
                .addValue("deliveredDate", order.getDeliveredDate())
                .addValue("readyDate", order.getReadyDate())
                .addValue("pickedDate", order.getPickedDate());

        jdbcTemplate.update(orderInsertSql, orderParams);
        saveItems(order.getItems(), order.getUUID());
    }

    // Method to save items
    private void saveItems(List<Item> items, String orderUUID) {
        String itemInsertSql = "INSERT INTO order_items (order_UUID, productId, color, size, quantity, price, name) " +
                "VALUES (:order_UUID, :productId, :color, :size, :quantity, :price, :name)";
        
        for (Item item : items) {
        	Product product=getProductInfo(item.getProductId());
            MapSqlParameterSource itemParams = new MapSqlParameterSource();
            Map<String, Object> params = Map.of(
                "order_UUID", orderUUID,  // Linking to the order via UUID
                "productId", item.getProductId(),
                "color", item.getColor(),
                "size", item.getSize(),
                "quantity", item.getQuantity(),
                "price",(product.getPrice()-(product.getPrice()*(product.getDiscount()/100))),
                "name",product.getName()
            );

            itemParams.addValues(params);
            jdbcTemplate.update(itemInsertSql, itemParams);
        }
    }
    
    public boolean isProductExist(String id, String size, String color) {
        String sql = "SELECT 1 " +
                     "FROM product p " +
                     "JOIN product_sizes ps ON p.id = ps.product_id " +
                     "JOIN product_colors pc ON p.id = pc.product_id " +
                     "WHERE p.id = :id " +
                     "AND ps.size = :size " +
                     "AND pc.color = :color " +
                     "LIMIT 1";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("size", size)
                .addValue("color", color);

        // Log the query and parameters for debugging
        System.out.println("SQL: " + sql);
        System.out.println("Parameters: id=" + id + ", size=" + size + ", color=" + color);

        List<Integer> result = jdbcTemplate.queryForList(sql, params, Integer.class);

        return !result.isEmpty();
    }
    
    public Boolean productAvailability(String id) {
        String sql = "SELECT COUNT(*) FROM product WHERE id = :id AND availability = 'In Stock'";

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
    
    public List<Order> getOrdersByEmail(String email) {
        String sql = "SELECT UUID,serialId,date, status, customer, email, contact, address, " +
                     "deliveryMethod, paymentMethod, payment, processedDate, " +
                     "shippedDate, deliveredDate, readyDate, pickedDate " +
                     "FROM orders WHERE email = :email";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Order.class));
    }

    public List<Item> getOrderItemsByEmail(String email) {
        String sql = "SELECT item_id,productId, color, size, quantity, order_UUID, price, name " +
                     "FROM order_items WHERE order_UUID IN (SELECT UUID FROM orders WHERE email = :email)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Item.class));
    }
    
    public Product getProductInfo(String productId) {
        String sql = "SELECT name, price, discount FROM product WHERE id = :productId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId);

        return jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(Product.class));
    }



}
