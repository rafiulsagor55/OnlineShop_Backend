package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Base64;

@Repository
public class ProductRepository {

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	// Method to retrieve a product by its ID
	public Product getProductById(String productId) {
		// Retrieve product details from the product table
		String productSql = "SELECT * FROM product WHERE id = :id";
		MapSqlParameterSource productParams = new MapSqlParameterSource();
		productParams.addValue("id", productId);

		Product product = jdbcTemplate.queryForObject(productSql, productParams, new ProductRowMapper());
		
		product.setRating(findProductRating(productId));

		// Retrieve sizes related to the product
		String sizesSql = "SELECT size FROM product_sizes WHERE product_id = :productId";
		MapSqlParameterSource sizesParams = new MapSqlParameterSource();
		sizesParams.addValue("productId", productId);

		List<String> sizes = jdbcTemplate.query(sizesSql, sizesParams, new SizeRowMapper());

		// Set the sizes in the product
		product.setSizes(sizes);

		// Retrieve colors and associated image data from the product_colors table
		String colorsSql = "SELECT color, image_data, content_type FROM product_colors WHERE product_id = :productId";
		MapSqlParameterSource colorsParams = new MapSqlParameterSource();
		colorsParams.addValue("productId", productId);

		List<ColorImagePair> colorImagePairs = jdbcTemplate.query(colorsSql, colorsParams, new ColorImageRowMapper());

		// Map colors to a list of reconstructed data URLs
		Map<String, List<String>> colors = new HashMap<>();
		for (ColorImagePair pair : colorImagePairs) {
			String reEncodedBase64 = Base64.getEncoder().encodeToString(pair.getImageData()); // Convert byte array to
																								// base64 string
			String reconstructedDataUrl = "data:" + pair.getContentType() + ";base64," + reEncodedBase64; // Create data
																											// URL
			colors.computeIfAbsent(pair.getColor(), k -> new ArrayList<>()).add(reconstructedDataUrl); // Store the data
																										// URL
		}

		// Set the colors in the product
		product.setColors(colors);

		return product;
	}

	// Method to save a product into the database
	public void saveProduct(Product product) {
		// Insert product details
		String insertProductSql = "INSERT INTO product (id, name, description, type, brand, material, category, availability, price, discount, rating, delivery_info, return_policy, trust_info, sizeDetails, gender) "
				+ "VALUES (:id, :name, :description, :type, :brand, :material, :category, :availability, :price, :discount, :rating, :deliveryInfo, :returnPolicy, :trustInfo, :sizeDetails, :gender)";

		MapSqlParameterSource productParams = new MapSqlParameterSource();
		productParams.addValue("id", product.getId());
		productParams.addValue("name", product.getName());
		productParams.addValue("description", product.getDescription());
		productParams.addValue("type", product.getType());
		productParams.addValue("brand", product.getBrand());
		productParams.addValue("material", product.getMaterial());
		productParams.addValue("category", product.getCategory());
		productParams.addValue("availability", product.getAvailability());
		productParams.addValue("price", product.getPrice());
		productParams.addValue("discount", product.getDiscount());
		productParams.addValue("rating", product.getRating());
		productParams.addValue("deliveryInfo", product.getDeliveryInfo());
		productParams.addValue("returnPolicy", product.getReturnPolicy());
		productParams.addValue("trustInfo", product.getTrustInfo());
		productParams.addValue("sizeDetails", product.getSizeDetails());
		productParams.addValue("gender", product.getGender());

		jdbcTemplate.update(insertProductSql, productParams);

		// Insert product sizes
		insertSizes(product.getId(), product.getSizes());

		// Insert product colors
		insertColors(product.getId(), product.getColors());
	}

	// Insert sizes for product
	private void insertSizes(String productId, List<String> sizes) {
		String insertSizeSql = "INSERT INTO product_sizes (product_id, size) VALUES (:productId, :size)";

		for (String size : sizes) {
			MapSqlParameterSource sizeParams = new MapSqlParameterSource();
			sizeParams.addValue("productId", productId);
			sizeParams.addValue("size", size);
			jdbcTemplate.update(insertSizeSql, sizeParams);
		}
	}

	private void insertColors(String productId, Map<String, List<String>> colors) {
		String insertColorSql = "INSERT INTO product_colors (product_id, color, image_data, content_type) VALUES (:productId, :color, :imageData, :contentType)";

		for (Map.Entry<String, List<String>> entry : colors.entrySet()) {
			String color = entry.getKey();
			for (String base64Image : entry.getValue()) {
				byte[] decodedBytes = null;
				String contentType = null;

				if (base64Image != null && base64Image.startsWith("data:")) {
					int commaIndex = base64Image.indexOf(",");
					if (commaIndex != -1) {
						// Extract MIME type (content type) from the base64 string
						contentType = base64Image.substring(5, commaIndex); // Extract MIME type like "image/png"
						base64Image = base64Image.substring(commaIndex + 1); // Remove the base64 prefix
						decodedBytes = Base64.getDecoder().decode(base64Image); // Decode base64 string to byte array
					}
				}
				
				System.out.println("Content type: "+contentType);

				// If valid image data was decoded, proceed to store it in the database
				if (decodedBytes != null) {
					MapSqlParameterSource colorParams = new MapSqlParameterSource();
					colorParams.addValue("productId", productId);
					colorParams.addValue("color", color);
					colorParams.addValue("imageData", decodedBytes); // Store the decoded image data
					colorParams.addValue("contentType", contentType); // Store the extracted MIME type

					jdbcTemplate.update(insertColorSql, colorParams);
				}
			}
		}
	}
	
	
	// Fetch all products and map them to ProductDTO
    public List<ProductDTO> getAllProducts() {
        // SQL query to fetch all products from the database
        String productSql = "SELECT * FROM product";
        List<ProductDTO> products = jdbcTemplate.query(productSql, new ProductDTORowMapper());

        // Iterate through each product and fetch sizes, unique colors, and the first image
        for (ProductDTO product : products) {
            // Fetch sizes for each product
            String sizesSql = "SELECT size FROM product_sizes WHERE product_id = :productId";
            MapSqlParameterSource sizesParams = new MapSqlParameterSource();
            sizesParams.addValue("productId", product.getId());
            List<String> sizes = jdbcTemplate.query(sizesSql, sizesParams, new SizeRowMapper());
            product.setSize(sizes);

            // Step 1: Fetch unique colors for the product
            String colorsSql = "SELECT DISTINCT color FROM product_colors WHERE product_id = :productId";
            MapSqlParameterSource colorsParams = new MapSqlParameterSource();
            colorsParams.addValue("productId", product.getId());

            List<String> colors = jdbcTemplate.query(colorsSql, colorsParams, (rs, rowNum) -> rs.getString("color"));
            product.setColor(colors);

            // Step 2: Retrieve only the first image for the product
            String firstImageSql = "SELECT image_data, content_type FROM product_colors WHERE product_id = :productId LIMIT 1";
            MapSqlParameterSource imageParams = new MapSqlParameterSource();
            imageParams.addValue("productId", product.getId());

            List<ColorImagePair> colorImagePairs = jdbcTemplate.query(firstImageSql, imageParams, new ImageRowMapper());

            // If there's a first image, store it in the DTO
            if (!colorImagePairs.isEmpty()) {
                ColorImagePair firstImage = colorImagePairs.get(0); // Get the first image
                String base64Image = Base64.getEncoder().encodeToString(firstImage.getImageData());
                String firstImageUrl = "data:" + firstImage.getContentType() + ";base64," + base64Image;
                product.setImageData(firstImageUrl);  // Set the first image URL
            }
        }

        return products;  // Return the list of ProductDTOs
    }
    
    
 // Fetch all products and map them to ProductDTO
    public List<ProductDTO> getAllTypeBasedProducts(String gender1,String gender2){
        // SQL query to fetch all products from the database
    	 String productSql=null;
    	 MapSqlParameterSource genderParams = new MapSqlParameterSource();
    	if (gender2==null) {
    		productSql = "SELECT * FROM product WHERE gender = :gender1";
    		 genderParams.addValue("gender1", gender1);
		}
    	else {
    		productSql = "SELECT * FROM product WHERE gender = :gender1 OR gender = :gender2";
   		    genderParams.addValue("gender1", gender1);
   		    genderParams.addValue("gender2", gender2);
   		  
    	}
           
        List<ProductDTO> products = jdbcTemplate.query(productSql,genderParams, new ProductDTORowMapper());

        // Iterate through each product and fetch sizes, unique colors, and the first image
        for (ProductDTO product : products) {
        	// Fetch rating for each product
        	System.out.println("product id: "+product.getId());
        	double rating=findProductRating(product.getId());
        	System.out.println("Rating :"+ rating);
        	product.setRating(rating);
            // Fetch sizes for each product
            String sizesSql = "SELECT size FROM product_sizes WHERE product_id = :productId";
            MapSqlParameterSource sizesParams = new MapSqlParameterSource();
            sizesParams.addValue("productId", product.getId());
            List<String> sizes = jdbcTemplate.query(sizesSql, sizesParams, new SizeRowMapper());
            product.setSize(sizes);

            // Step 1: Fetch unique colors for the product
            String colorsSql = "SELECT DISTINCT color FROM product_colors WHERE product_id = :productId";
            MapSqlParameterSource colorsParams = new MapSqlParameterSource();
            colorsParams.addValue("productId", product.getId());

            List<String> colors = jdbcTemplate.query(colorsSql, colorsParams, (rs, rowNum) -> rs.getString("color"));
            product.setColor(colors);

            // Step 2: Retrieve only the first image for the product
            String firstImageSql = "SELECT image_data, content_type FROM product_colors WHERE product_id = :productId LIMIT 1";
            MapSqlParameterSource imageParams = new MapSqlParameterSource();
            imageParams.addValue("productId", product.getId());

            List<ColorImagePair> colorImagePairs = jdbcTemplate.query(firstImageSql, imageParams, new ImageRowMapper());

            // If there's a first image, store it in the DTO
            if (!colorImagePairs.isEmpty()) {
                ColorImagePair firstImage = colorImagePairs.get(0); // Get the first image
                String base64Image = Base64.getEncoder().encodeToString(firstImage.getImageData());
                String firstImageUrl = "data:" + firstImage.getContentType() + ";base64," + base64Image;
                product.setImageData(firstImageUrl);  // Set the first image URL
            }
        }

        return products;  // Return the list of ProductDTOs
    }
    
    
    public String getImageUrlByProductIdAndColor(String productId, String color) {
        String sql = "SELECT image_data, content_type FROM product_colors WHERE product_id = :productId AND color = :color LIMIT 1";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("productId", productId);
        params.addValue("color", color);

        ColorImagePair colorImagePair = jdbcTemplate.queryForObject(sql, params, new ImageRowMapper());
        if (colorImagePair != null) {
            String base64Image = Base64.getEncoder().encodeToString(colorImagePair.getImageData());

            String imageUrl = "data:" + colorImagePair.getContentType() + ";base64," + base64Image;
            return imageUrl;
        }
        return null;
    }

	
	

	// RowMapper for Product
	private static class ProductRowMapper implements RowMapper<Product> {
		@Override
		public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
			Product product = new Product();
			product.setId(rs.getString("id"));
			product.setName(rs.getString("name"));
			product.setDescription(rs.getString("description"));
			product.setSizeDetails(rs.getString("sizeDetails"));
			product.setType(rs.getString("type"));
			product.setBrand(rs.getString("brand"));
			product.setMaterial(rs.getString("material"));
			product.setCategory(rs.getString("category"));
			product.setAvailability(rs.getString("availability"));
			product.setPrice(rs.getDouble("price"));
			product.setDiscount(rs.getDouble("discount"));
			product.setRating(rs.getDouble("rating"));
			product.setDeliveryInfo(rs.getString("delivery_info"));
			product.setReturnPolicy(rs.getString("return_policy"));
			product.setTrustInfo(rs.getString("trust_info"));
			product.setGender(rs.getString("gender"));
			
			return product;
		}
	}

	// RowMapper for Size (Product Sizes)
	private static class SizeRowMapper implements RowMapper<String> {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getString("size");
		}
	}

	// RowMapper for ColorImagePair (Color and Image Data)
	private static class ColorImageRowMapper implements RowMapper<ColorImagePair> {
		@Override
		public ColorImagePair mapRow(ResultSet rs, int rowNum) throws SQLException {
			String color = rs.getString("color");
			byte[] imageData = rs.getBytes("image_data");
			String contentType = rs.getString("content_type");
			return ColorImagePair.builder().color(color).imageData(imageData).contentType(contentType).build();
		}
	}
	
	public boolean doesProductExist(String id) {
		String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM product WHERE id = :id";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("id", id);

		int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);

		return count > 0;
	}
	
//	public double findProductRating(String productId) {
//		String sql="SELECT AVG(rating) FROM ratings WHERE product_id = :product_id";
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("product_id", productId);
//		return jdbcTemplate.queryForObject(sql, params, Double.class);
//	}

	public double findProductRating(String productId) {
	    String sql = "SELECT AVG(rating) FROM ratings WHERE product_id = :product_id";
	    MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue("product_id", productId);
	    try {
	        Double result = jdbcTemplate.queryForObject(sql, params, Double.class);
	        return result != null ? result : 0.0;
	    } catch (Exception e) {
	        return 0.0;
	    }
	}
	private static class ProductDTORowMapper implements RowMapper<ProductDTO> {
	    @Override
	    public ProductDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
	        ProductDTO productDTO = new ProductDTO();
	        productDTO.setId(rs.getString("id"));
	        productDTO.setName(rs.getString("name"));
	        productDTO.setDescription(rs.getString("description"));
	        productDTO.setType(rs.getString("type"));
	        productDTO.setBrand(rs.getString("brand"));
	        productDTO.setMaterial(rs.getString("material"));
	        productDTO.setCategory(rs.getString("category"));
	        productDTO.setAvailability(rs.getString("availability"));
	        productDTO.setPrice(rs.getDouble("price"));
	        productDTO.setDiscount(rs.getDouble("discount"));
	        productDTO.setRating(rs.getDouble("rating"));
	        productDTO.setDeliveryInfo(rs.getString("delivery_info"));
	        productDTO.setReturnPolicy(rs.getString("return_policy"));
	        productDTO.setTrustInfo(rs.getString("trust_info"));
	        productDTO.setGender(rs.getString("gender"));
	        return productDTO;
	    }
	}
	
	private static class ImageRowMapper implements RowMapper<ColorImagePair> {
	    @Override
	    public ColorImagePair mapRow(ResultSet rs, int rowNum) throws SQLException {
	        byte[] imageData = rs.getBytes("image_data");
	        String contentType = rs.getString("content_type");
	        return ColorImagePair.builder().imageData(imageData).contentType(contentType).build();
	    }
	}
	
	public void deleteProductById(String id) {
		String DELETE_PRODUCT_BY_ID_SQL = "DELETE FROM product WHERE id = :id";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("id", id);
		jdbcTemplate.update(DELETE_PRODUCT_BY_ID_SQL, params);
	}
	
	public ObjectForCart getProductDetailsForCartById(String productId) {
        String sql = "SELECT name, price, discount FROM product WHERE id = :productId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("productId", productId);

        // Query the database and map the result to ProductDTO
        return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
        	ObjectForCart objectForCart = new ObjectForCart();
        	objectForCart.setName(rs.getString("name"));
        	objectForCart.setPrice(rs.getDouble("price"));
        	objectForCart.setDiscount(rs.getDouble("discount"));
            return objectForCart;
        });
    }



}
