package com.ecommerce.order.services;

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

import com.ecommerce.order.dtos.OrderResponse;
import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderStatus;
import com.ecommerce.order.repositories.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

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
}
