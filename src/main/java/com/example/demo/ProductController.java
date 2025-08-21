package com.example.demo;

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

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
    	System.out.println("Called");
        return productService.getProductById(id);
    }

    @PostMapping("/add-product")
    public  ResponseEntity<?> saveProduct(@RequestBody Product product,@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request) {
        try {
			String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			System.out.println(ip);
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidity(jwt, ip, userAgent)) {
				productService.saveProduct(product);
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
    
    @PostMapping("/save-edited-product")
    public ResponseEntity<?> saveEditedProduct(@RequestBody Product product,@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request) {
    	try {
            String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			System.out.println(ip);
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidity(jwt, ip, userAgent)) {
				productService.DeleteProductById(product.getId());
	            productService.saveProduct(product);
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
	public ResponseEntity<?>DeleteProduct(@PathVariable String selectedProductId,@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request){
		try {
			String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			System.out.println(ip);
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidity(jwt, ip, userAgent)) {
				productService.DeleteProductById(selectedProductId);
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

