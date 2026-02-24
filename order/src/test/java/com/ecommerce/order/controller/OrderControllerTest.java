package com.ecommerce.order.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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
import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.dtos.PaymentResultDTO;
import com.ecommerce.order.dtos.ProductResponse;
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

        @MockBean
        private ProductServiceClient productServiceClient;

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

        private void stubProductServiceForOrders() {
                ProductResponse product = new ProductResponse();
                product.setStockQuantity(999);
                product.setName("Mock Product");
                given(productServiceClient.getProductDetails(anyString())).willReturn(product);
                doNothing().when(productServiceClient).reduceStock(anyString(), anyInt());
        }

        // --- Positive Tests ---

        @Test
        void shouldCreateOrder() throws Exception {
                stubProductServiceForOrders();
                // Mock Cart Service to return items
                given(cartService.getCart("user-123")).willReturn(List.of(
                                createCartItem("p1", "Product 1", new BigDecimal("50.00"), 1, "img", "M", "Red")));

                mockMvc.perform(post("/api/orders")
                                .header("X-User-ID", "user-123")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                                .andExpect(jsonPath("$.itemsPrice").value(50.00))
                                .andExpect(jsonPath("$.shippingPrice").value(0.00))
                                .andExpect(jsonPath("$.taxPrice").value(0.00))
                                .andExpect(jsonPath("$.totalAmount").value(50.00));

                // Verify cart cleared
                verify(cartService).clearCart("user-123");
        }

        @Test
        void shouldGetOrderById() throws Exception {
                stubProductServiceForOrders();
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
                stubProductServiceForOrders();
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
                stubProductServiceForOrders();
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
                stubProductServiceForOrders();
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

        @Test
        void shouldReturnShippingQuoteForCambodiaProvinceRule() throws Exception {
                String configPayload = """
                                {
                                  "defaultShippingPrice": 2.50,
                                  "cambodiaProvinceRates": [
                                    { "province": "Phnom Penh", "price": 4.25, "active": true }
                                  ]
                                }
                                """;

                mockMvc.perform(put("/api/admin/shipping-config")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(configPayload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.defaultShippingPrice").value(2.50))
                                .andExpect(jsonPath("$.cambodiaProvinceRates", hasSize(1)));

                String quotePayload = """
                                {
                                  "shippingAddress": {
                                    "firstName": "A",
                                    "lastName": "B",
                                    "street": "Street",
                                    "city": "Phnom Penh",
                                    "state": "Phnom Penh",
                                    "country": "Cambodia",
                                    "phone": "0123456789"
                                  }
                                }
                                """;

                mockMvc.perform(post("/api/shipping/quote")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(quotePayload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.shippingPrice").value(4.25))
                                .andExpect(jsonPath("$.matchedRuleType").value("CAMBODIA_PROVINCE"))
                                .andExpect(jsonPath("$.matchedProvince").value("Phnom Penh"));
        }

        @Test
        void shouldGetAndUpdateAdminShippingConfig() throws Exception {
                mockMvc.perform(get("/api/admin/shipping-config"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.defaultShippingPrice").exists())
                                .andExpect(jsonPath("$.cambodiaProvinceRates").isArray());

                String payload = """
                                {
                                  "defaultShippingPrice": 3.00,
                                  "cambodiaProvinceRates": [
                                    { "province": "Siem Reap", "price": 5.75, "active": true },
                                    { "province": "Kandal", "price": 4.50, "active": false }
                                  ]
                                }
                                """;

                mockMvc.perform(put("/api/admin/shipping-config")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.defaultShippingPrice").value(3.00))
                                .andExpect(jsonPath("$.cambodiaProvinceRates", hasSize(2)));
        }

        @Test
        void shouldRejectInvalidAdminShippingConfig() throws Exception {
                String duplicateProvincePayload = """
                                {
                                  "defaultShippingPrice": 1.00,
                                  "cambodiaProvinceRates": [
                                    { "province": "Phnom Penh", "price": 2.00, "active": true },
                                    { "province": "  phnom   penh  ", "price": 3.00, "active": true }
                                  ]
                                }
                                """;

                mockMvc.perform(put("/api/admin/shipping-config")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(duplicateProvincePayload))
                                .andExpect(status().isBadRequest());

                String negativePricePayload = """
                                {
                                  "defaultShippingPrice": -1.00,
                                  "cambodiaProvinceRates": []
                                }
                                """;

                mockMvc.perform(put("/api/admin/shipping-config")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(negativePricePayload))
                                .andExpect(status().isBadRequest());
        }
}
