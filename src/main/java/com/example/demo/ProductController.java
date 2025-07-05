package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
    	System.out.println("Called");
        return productService.getProductById(id);
    }

    @PostMapping("/add-product")
    public String saveProduct(@RequestBody Product product) {
        productService.saveProduct(product);
        return "Product saved successfully!";
    }
    @GetMapping("/get-all-products")
    public ResponseEntity<?> GetAllProducts() {
    	System.out.println("Called");
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @PostMapping("/save-edited-product")
    public String saveEditedProduct(@RequestBody Product product) {
    	try {
            productService.DeleteProductById(product.getId());
            productService.saveProduct(product);
            return "Edited product saved successfully!";
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage()); 
		}
    }
    
}

