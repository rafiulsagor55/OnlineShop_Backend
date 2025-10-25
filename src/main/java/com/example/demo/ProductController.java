package com.example.demo;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
    	System.out.println("Called");
        return productService.getProductById(id);
    }

    @PostMapping("/add-product")
    public  ResponseEntity<?> saveProduct(@RequestBody Product product,@CookieValue(name = "admin_token", required = false) String jwt, HttpServletRequest request) {
        try {
			String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			System.out.println(ip);
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidityAdmin(jwt, ip, userAgent)) {
				productService.saveProduct(product);
				notificationService.createActivityLog(ActivityLog.builder()
				        .id(UUID.randomUUID().toString())
				        .title(String.format("Add new product"))
				        .message(String.format("The product \"%s\" has been successfully added to the catalog with ID: %s.", product.getName(), product.getId()))
				        .timestamp(new Timestamp(System.currentTimeMillis()))
				        .type("product-add")
				        .build());
				return ResponseEntity.ok("Product added successfully!");
			}
			else {
				throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
			}	
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}		
    }
    @GetMapping("/get-all-products")
    public ResponseEntity<?> GetAllProducts() {
    	System.out.println("Called");
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @GetMapping("/get-all-related-products")
    public ResponseEntity<?> GetRelatedProducts(@RequestParam String type,@RequestParam String category,@RequestParam String id) {
        return ResponseEntity.ok(productService.getRelatedProducts(type,category,id));
    }
    
    @GetMapping("/get-all-mens-products")
    public ResponseEntity<?> GetAllMensProducts() {
        return ResponseEntity.ok(productService.getAllTypeBasedProducts("Male","Unisex"));
    }
    
    @GetMapping("/get-all-womens-products")
    public ResponseEntity<?> GetAllWomensProducts() {
        return ResponseEntity.ok(productService.getAllTypeBasedProducts("Female","Unisex"));
    }
    
    @GetMapping("/get-all-kids-products")
    public ResponseEntity<?> GetAllKidsProducts() {
        return ResponseEntity.ok(productService.getAllTypeBasedProducts("Kids",null));
    }
    
    @GetMapping("/get-all-unisex-products")
    public ResponseEntity<?> GetAllUnisexProducts() {
        return ResponseEntity.ok(productService.getAllTypeBasedProducts("Unisex",null));
    }
    
    @GetMapping("/get-all-mens-products-admin")
    public ResponseEntity<?> GetAllMensProductsAdmin() {
        return ResponseEntity.ok(productService.getAllTypeBasedProductsadmin("Male","Unisex"));
    }
    
    @GetMapping("/get-all-womens-products-admin")
    public ResponseEntity<?> GetAllWomensProductsAdmin() {
        return ResponseEntity.ok(productService.getAllTypeBasedProductsadmin("Female","Unisex"));
    }
    
    @GetMapping("/get-all-kids-products-admin")
    public ResponseEntity<?> GetAllKidsProductsAdmin() {
        return ResponseEntity.ok(productService.getAllTypeBasedProductsadmin("Kids",null));
    }
    
    @GetMapping("/get-all-unisex-products-admin")
    public ResponseEntity<?> GetAllUnisexProductsAdmin() {
        return ResponseEntity.ok(productService.getAllTypeBasedProductsadmin("Unisex",null));
    }
    
    @GetMapping("/get-all-mens-products-limit")
    public ResponseEntity<?> GetAllMensProductsLimit() {
        return ResponseEntity.ok(productService.getAllTypeBasedProductsLimit("Male","Unisex"));
    }
    
    @GetMapping("/get-all-womens-products-limit")
    public ResponseEntity<?> GetAllWomensProductsLimit() {
        return ResponseEntity.ok(productService.getAllTypeBasedProductsLimit("Female","Unisex"));
    }
    
    @GetMapping("/get-all-kids-products-limit")
    public ResponseEntity<?> GetAllKidsProductsLimit() {
        return ResponseEntity.ok(productService.getAllTypeBasedProductsLimit("Kids",null));
    }
    
    @GetMapping("/get-all-unisex-products-limit")
    public ResponseEntity<?> GetAllUnisexProductsLimit() {
        return ResponseEntity.ok(productService.getAllTypeBasedProductsLimit("Unisex",null));
    }
    
    @PostMapping("/save-edited-product")
    public ResponseEntity<?> saveEditedProduct(@RequestBody Product product,@CookieValue(name = "admin_token", required = false) String jwt, HttpServletRequest request) {
    	try {
            String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			System.out.println(ip);
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidityAdmin(jwt, ip, userAgent)) {
	            productService.updateProduct(product);
	            notificationService.createActivityLog(ActivityLog.builder()
				        .id(UUID.randomUUID().toString())
				        .title(String.format("Product Edited"))
				        .message(String.format("The product \"%s\" has been successfully edited to the catalog with ID: %s.", product.getName(), product.getId()))
				        .timestamp(new Timestamp(System.currentTimeMillis()))
				        .type("product-edit")
				        .build());
				return ResponseEntity.ok("Edited product saved successfully!");
			}
			else {
				throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
			}	
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage()); 
		}
    }
    
    @DeleteMapping("/delete/{selectedProductId}")
	public ResponseEntity<?>DeleteProduct(@PathVariable String selectedProductId,@CookieValue(name = "admin_token", required = false) String jwt, HttpServletRequest request){
		try {
			String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			System.out.println(ip);
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidityAdmin(jwt, ip, userAgent)) {
				productService.DeleteProductById(selectedProductId);
				notificationService.createActivityLog(ActivityLog.builder()
				        .id(UUID.randomUUID().toString())
				        .title(String.format("Product Deleted"))
				        .message(String.format("The product \"%s\" has been successfully deleted.", selectedProductId))
				        .timestamp(new Timestamp(System.currentTimeMillis()))
				        .type("product-delete")
				        .build());
				return ResponseEntity.ok("Product "+selectedProductId+" deleted successfully.");
			}
			else {
				throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
			}	
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}			
	}
    
}

