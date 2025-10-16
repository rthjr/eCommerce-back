package com.app.ecom.service;

import java.util.List;
import java.util.Optional;

import com.app.ecom.dto.ProductRequest;
import com.app.ecom.dto.ProductResponse;
import com.app.ecom.entity.Product;

public interface ProductService {
	ProductResponse createProduct(ProductRequest productRequest);

	ProductResponse mapProdcutResponse(Product saveProduct);

	void updateProductFromRequest(Product product, ProductRequest productRequest);
	
	Optional<ProductResponse> updateProduct(Long id, ProductRequest productRequest);
	
	List<ProductResponse> getAllProducts();
	
	List<ProductResponse> searchProducts(String keyword);
	
	boolean deleteProduct(Long id);
}
