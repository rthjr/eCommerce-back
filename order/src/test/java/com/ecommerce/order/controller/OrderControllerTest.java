package com.ecommerce.order.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.BaseIntegrationTest;
import com.ecommerce.order.dtos.PaymentResultDTO;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.services.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@AutoConfigureMockMvc
@Transactional
class OrderControllerTest extends BaseIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CartService cartService;

        // Helper
        private CartItem createCartItem(String productId, String name, BigDecimal price, int quantity, String img,
                        String size, String color) {
                CartItem item = new CartItem();
                item.setProductId(productId);
                item.setProductName(name);
                item.setPrice(price);
                item.setQuantity(quantity);
                item.setProductImage(img);
                item.setSelectedSize(size);
                item.setSelectedColor(color);
                return item;
        }

        // --- Positive Tests ---

        @Test
        void shouldCreateOrder() throws Exception {
                // Mock Cart Service to return items
                given(cartService.getCart("user-123")).willReturn(List.of(
                                createCartItem("p1", "Product 1", new BigDecimal("50.00"), 1, "img", "M", "Red")));

                mockMvc.perform(post("/api/orders")
                                .header("X-User-ID", "user-123")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                                .andExpect(jsonPath("$.totalAmount").value(50.00));

                // Verify cart cleared
                verify(cartService).clearCart("user-123");
        }

        @Test
        void shouldGetOrderById() throws Exception {
                // 1. Create Order
                given(cartService.getCart("user-123")).willReturn(List.of(
                                createCartItem("p1", "Product 1", new BigDecimal("50.00"), 1, "img", "M", "Red")));

                MvcResult result = mockMvc.perform(post("/api/orders")
                                .header("X-User-ID", "user-123")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andReturn();

                String response = result.getResponse().getContentAsString();
                Integer id = JsonPath.parse(response).read("$.id");

                // 2. Get By ID
                mockMvc.perform(get("/api/orders/" + id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(id));
        }

        @Test
        void shouldGetUserOrders() throws Exception {
                // 1. Create Order
                given(cartService.getCart("user-123")).willReturn(List.of(
                                createCartItem("p1", "Product 1", new BigDecimal("50.00"), 1, "img", "M", "Red")));

                mockMvc.perform(post("/api/orders")
                                .header("X-User-ID", "user-123")
                                .contentType(MediaType.APPLICATION_JSON));

                // 2. Get User Orders
                mockMvc.perform(get("/api/orders/myorders")
                                .header("X-User-ID", "user-123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        void shouldMarkOrderAsPaid() throws Exception {
                // 1. Create Order
                given(cartService.getCart("user-123")).willReturn(List.of(
                                createCartItem("p1", "Product 1", new BigDecimal("50.00"), 1, "img", "M", "Red")));

                MvcResult result = mockMvc.perform(post("/api/orders")
                                .header("X-User-ID", "user-123")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated())
                                .andReturn();

                String response = result.getResponse().getContentAsString();
                Integer id = JsonPath.parse(response).read("$.id");

                // 2. Mark as Paid
                PaymentResultDTO paymentResult = new PaymentResultDTO("pay_123", "COMPLETED", "2023-12-01",
                                "test@test.com");

                mockMvc.perform(put("/api/orders/" + id + "/pay")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paymentResult)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isPaid").value(true));
        }

        @Test
        void shouldMarkOrderAsDelivered() throws Exception {
                // 1. Create Order
                given(cartService.getCart("user-123")).willReturn(List.of(
                                createCartItem("p1", "Product 1", new BigDecimal("50.00"), 1, "img", "M", "Red")));

                MvcResult result = mockMvc.perform(post("/api/orders")
                                .header("X-User-ID", "user-123")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated())
                                .andReturn();

                String response = result.getResponse().getContentAsString();
                Integer id = JsonPath.parse(response).read("$.id");

                // 2. Mark as Delivered
                mockMvc.perform(put("/api/orders/" + id + "/deliver"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isDelivered").value(true));
        }

        // --- Negative Tests ---

        @Test
        void shouldReturn404ForInvalidOrderId() throws Exception {
                mockMvc.perform(get("/api/orders/999999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn400ForEmptyCart() throws Exception {
                // Mock empty cart
                given(cartService.getCart("user-empty")).willReturn(List.of());

                mockMvc.perform(post("/api/orders")
                                .header("X-User-ID", "user-empty")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest());
        }
}
