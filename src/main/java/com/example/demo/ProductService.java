package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Method to retrieve product by ID
    public Product getProductById(String productId) {
        return productRepository.getProductById(productId);
    }

    // Method to save a product
    public void saveProduct(Product product) {
        try {
        	if(!productRepository.doesProductExist(product.getId())) {
        		productRepository.saveProduct(product);
        	}
        	else {
        		throw new IllegalArgumentException("Product already exist for "+product.getId()+"! Please use new product ID.");
        	}
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage()); 
		}
    }
    
    public List<ProductDTO> getAllProducts() {
    	try {
			return productRepository.getAllProducts();
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage()); 
		}
    	
    }
    
    public List<ProductDTO> getRelatedProducts(String type,String category,String id) {
    	try {
			return productRepository.getRelatedProducts(type,category,id);
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage()); 
		}
    	
    }
    
    public List<ProductDTO> getAllTypeBasedProducts(String gender1,String gender2) {
    	try {
			return productRepository.getAllTypeBasedProducts(gender1,gender2);
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage()); 
		}
    	
    }
    
    public List<ProductDTO> getAllTypeBasedProductsadmin(String gender1,String gender2) {
    	try {
			return productRepository.getAllTypeBasedProductsAdmin(gender1,gender2);
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage()); 
		}
    	
    }
    
    public List<ProductDTO> getAllTypeBasedProductsLimit(String gender1,String gender2) {
    	try {
			return productRepository.getAllTypeBasedProductsLimit(gender1,gender2);
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage()); 
		}
    	
    }
    
    public void DeleteProductById(String id) {
    	productRepository.deleteProductById(id);
    }
    
    public String GetImageUrlByProductIdAndColor(String productId, String color) {
    	return productRepository.getImageUrlByProductIdAndColor(productId, color);
    }
    
    public ObjectForCart GetProductDetailsForCartById(String productId) {
    	return productRepository.getProductDetailsForCartById(productId);
    }
}

