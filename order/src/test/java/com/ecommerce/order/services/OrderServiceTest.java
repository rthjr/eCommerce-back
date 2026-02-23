package com.ecommerce.order.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.dtos.CreateOrderRequest;
import com.ecommerce.order.dtos.OrderResponse;
import com.ecommerce.order.dtos.ProductResponse;
import com.ecommerce.order.dtos.ShippingAddressDTO;
import com.ecommerce.order.dtos.ShippingQuoteResponse;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderStatus;
import com.ecommerce.order.repositories.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private ShippingPricingService shippingPricingService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldReturnOrderById() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setItems(java.util.Collections.emptyList()); // Initialize items to avoid NPE

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<OrderResponse> found = orderService.getOrderById(1L);

        assertEquals(new BigDecimal("99.99"), found.get().getTotalAmount());
        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldCreateOrderWithQuantityAwareTotalsAndZeroTax() {
        CartItem item = new CartItem();
        item.setProductId("p1");
        item.setProductName("Product 1");
        item.setPrice(new BigDecimal("12.50"));
        item.setQuantity(3);

        ProductResponse product = new ProductResponse();
        product.setStockQuantity(10);

        when(cartService.getCart("user-1")).thenReturn(List.of(item));
        when(productServiceClient.getProductDetails("p1")).thenReturn(product);
        when(shippingPricingService.quoteShipping(any(ShippingAddressDTO.class)))
                .thenReturn(new ShippingQuoteResponse(new BigDecimal("4.25"), "CAMBODIA_PROVINCE", "Phnom Penh"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0, Order.class);
            saved.setId(1L);
            return saved;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                new ShippingAddressDTO("A", "B", "Street", "Phnom Penh", "Phnom Penh", "12000", "Cambodia", "012"),
                "Cash");

        Optional<OrderResponse> created = orderService.createOrder("user-1", request);

        assertTrue(created.isPresent());
        assertEquals(new BigDecimal("37.50"), created.get().getItemsPrice());
        assertEquals(new BigDecimal("4.25"), created.get().getShippingPrice());
        assertEquals(new BigDecimal("0.00"), created.get().getTaxPrice());
        assertEquals(new BigDecimal("41.75"), created.get().getTotalAmount());
        verify(cartService).clearCart("user-1");
    }
}
