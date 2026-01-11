package com.ecommerce.product.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.product.models.Product;
import com.ecommerce.product.repositories.ProductRepository;

import com.ecommerce.product.dtos.ProductResponse;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldReturnProductById() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<ProductResponse> found = productService.getProductById(1L);

        assertEquals("Test Product", found.get().getName());
        verify(productRepository).findById(1L);
    }
}
