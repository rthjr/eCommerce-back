package com.ecommerce.product;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.ecommerce.product.dtos.ProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
class ProductControllerTest extends BaseIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        // --- Positive Tests ---

        @Test
        void shouldCreateProduct() throws Exception {
                ProductRequest productRequest = createProductRequest();

                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Test Product"))
                                .andExpect(jsonPath("$.price").value(100.00));
        }

        @Test
        void shouldGetAllProducts() throws Exception {
                mockMvc.perform(get("/api/products"))
                                .andExpect(status().isOk());
        }

        @Test
        void shouldGetProductById() throws Exception {
                // 1. Create a product first
                ProductRequest productRequest = createProductRequest();
                MvcResult result = mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String response = result.getResponse().getContentAsString();
                Integer id = JsonPath.parse(response).read("$.id");

                // 2. Get by ID
                mockMvc.perform(get("/api/products/" + id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Test Product"));
        }

        @Test
        void shouldUpdateProduct() throws Exception {
                // 1. Create
                ProductRequest productRequest = createProductRequest();
                MvcResult result = mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productRequest)))
                                .andReturn();

                String response = result.getResponse().getContentAsString();
                Integer id = JsonPath.parse(response).read("$.id");

                // 2. Update
                ProductRequest updateRequest = new ProductRequest();
                updateRequest.setName("Updated Product");
                updateRequest.setPrice(new BigDecimal("150.00"));
                updateRequest.setDescription("New Desc");
                updateRequest.setStockQuantity(5);
                updateRequest.setImageUrl("http://newimg.com");
                updateRequest.setCategory("New Cat");

                mockMvc.perform(put("/api/products/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Product"))
                                .andExpect(jsonPath("$.price").value(150.00));
        }

        // --- Filter & Search Tests ---

        @Test
        void shouldSearchProducts() throws Exception {
                // 1. Create a product named "UniqueSearchTerm"
                ProductRequest productRequest = createProductRequest();
                productRequest.setName("UniqueSearchTerm");
                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productRequest)))
                                .andExpect(status().isCreated());

                // 2. Search
                mockMvc.perform(get("/api/products/search")
                                .param("keyword", "UniqueSearchTerm"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                                .andExpect(jsonPath("$[0].name").value("UniqueSearchTerm"));
        }

        @Test
        void shouldFilterProducts() throws Exception {
                // 1. Create specific product
                ProductRequest productRequest = createProductRequest();
                productRequest.setCategory("Gaming");
                productRequest.setPrice(new BigDecimal("500.00"));

                mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productRequest)))
                                .andExpect(status().isCreated());

                // 2. Filter
                mockMvc.perform(get("/api/products/filter")
                                .param("category", "Gaming")
                                .param("minPrice", "400")
                                .param("maxPrice", "600"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                                .andExpect(jsonPath("$.content[0].category").value("Gaming"));
        }

        @Test
        void shouldGetTopProducts() throws Exception {
                // Just verify the endpoint works, as logic depends on actual "top" definition
                // which might need more data
                mockMvc.perform(get("/api/products/top")
                                .param("limit", "5"))
                                .andExpect(status().isOk());
        }

        @Test
        void shouldDeleteProduct() throws Exception {
                // 1. Create
                ProductRequest productRequest = createProductRequest();
                MvcResult result = mockMvc.perform(post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productRequest)))
                                .andReturn();

                String response = result.getResponse().getContentAsString();
                Integer id = JsonPath.parse(response).read("$.id");

                // 2. Delete
                mockMvc.perform(delete("/api/products/" + id))
                                .andExpect(status().isNoContent());

                // 3. Verify it's gone
                mockMvc.perform(get("/api/products/" + id))
                                .andExpect(status().isNotFound());
        }

        // --- Negative Tests ---

        @Test
        void shouldReturn404ForInvalidProductId() throws Exception {
                mockMvc.perform(get("/api/products/999999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404ForDeleteInvalidProductId() throws Exception {
                mockMvc.perform(delete("/api/products/999999"))
                                .andExpect(status().isNotFound());
        }

        // Helper
        private ProductRequest createProductRequest() {
                ProductRequest productRequest = new ProductRequest();
                productRequest.setName("Test Product");
                productRequest.setDescription("Description");
                productRequest.setPrice(new BigDecimal("100.00"));
                productRequest.setImageUrl("http://image.com");
                productRequest.setCategory("Category");
                productRequest.setStockQuantity(10);
                return productRequest;
        }
}
