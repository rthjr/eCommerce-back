package com.ecommerce.order.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.order.models.Order;
import com.ecommerce.order.models.OrderItem;
import com.ecommerce.order.models.OrderStatus;
import com.ecommerce.order.models.PaymentResult;
import com.ecommerce.order.models.ShippingAddress;
import com.ecommerce.order.repositories.OrderRepository;

@Configuration
public class OrderDataInitializer {

    @Bean
    CommandLineRunner initOrderData(OrderRepository orderRepository) {
        return args -> {
            if (orderRepository.count() < 5) {

                List<Order> orders = Arrays.asList(

                        // Order 1: Completed (Paid + Delivered)
                        createCompletedOrder("user-001", "Jane", "Doe", "jane.doe@example.com",
                                "456 Customer Ave", "Los Angeles", "CA", "90001", "USA", "555-0101",
                                new BigDecimal("149.97"), new BigDecimal("10.00"), BigDecimal.ZERO,
                                "pay_abc123", 5),

                        // Order 2: Paid but not delivered
                        createPaidOrder("user-002", "Bob", "Smith", "bob.smith@example.com",
                                "789 Buyer Blvd", "Chicago", "IL", "60601", "USA", "555-0102",
                                new BigDecimal("89.99"), new BigDecimal("6.30"), BigDecimal.ZERO,
                                "pay_def456", 3),

                        // Order 3: Unpaid order
                        createUnpaidOrder("user-003", "Alice", "Johnson", "alice.johnson@example.com",
                                "321 Shopper Lane", "Houston", "TX", "77001", "USA", "555-0103",
                                new BigDecimal("59.99"), new BigDecimal("4.20"), new BigDecimal("5.00"), 2),

                        // Order 4: Another completed order for user-001
                        createCompletedOrder("user-001", "Jane", "Doe", "jane.doe@example.com",
                                "456 Customer Ave", "Los Angeles", "CA", "90001", "USA", "555-0101",
                                new BigDecimal("79.99"), new BigDecimal("5.60"), BigDecimal.ZERO,
                                "pay_ghi789", 10),

                        // Order 5: Paid order for user-002
                        createPaidOrder("user-002", "Bob", "Smith", "bob.smith@example.com",
                                "789 Buyer Blvd", "Chicago", "IL", "60601", "USA", "555-0102",
                                new BigDecimal("199.98"), new BigDecimal("14.00"), BigDecimal.ZERO,
                                "pay_jkl012", 7));

                orderRepository.saveAll(orders);
                System.out.println("✅ Order Service: 5 test orders loaded with various statuses.");
            }
        };
    }

    private Order createCompletedOrder(String userId, String firstName, String lastName, String email,
            String street, String city, String state, String zipCode, String country, String phone,
            BigDecimal itemsPrice, BigDecimal taxPrice, BigDecimal shippingPrice, String paymentId, int daysAgo) {

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.DELIVERED);
        order.setItemsPrice(itemsPrice);
        order.setTaxPrice(taxPrice);
        order.setShippingPrice(shippingPrice);
        order.setTotalAmount(itemsPrice.add(taxPrice).add(shippingPrice));
        order.setPaymentMethod("Stripe");

        // Shipping Address
        ShippingAddress shipping = new ShippingAddress();
        shipping.setFirstName(firstName);
        shipping.setLastName(lastName);
        shipping.setStreet(street);
        shipping.setCity(city);
        shipping.setState(state);
        shipping.setZipCode(zipCode);
        shipping.setCountry(country);
        shipping.setPhone(phone);
        order.setShippingAddress(shipping);

        // Payment Result
        PaymentResult payment = new PaymentResult();
        payment.setPaymentId(paymentId);
        payment.setStatus("completed");
        payment.setUpdateTime(LocalDateTime.now().minusDays(daysAgo).toString());
        payment.setEmailAddress(email);
        order.setPaymentResult(payment);

        // Payment and delivery status
        order.setIsPaid(true);
        order.setPaidAt(LocalDateTime.now().minusDays(daysAgo));
        order.setIsDelivered(true);
        order.setDeliveredAt(LocalDateTime.now().minusDays(daysAgo - 2));

        // Add sample order items
        OrderItem item1 = new OrderItem();
        item1.setProductId("1");
        item1.setQuantity(2);
        item1.setPrice(itemsPrice.divide(new BigDecimal("2")));
        item1.setOrder(order);

        order.setItems(Arrays.asList(item1));

        return order;
    }

    private Order createPaidOrder(String userId, String firstName, String lastName, String email,
            String street, String city, String state, String zipCode, String country, String phone,
            BigDecimal itemsPrice, BigDecimal taxPrice, BigDecimal shippingPrice, String paymentId, int daysAgo) {

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setItemsPrice(itemsPrice);
        order.setTaxPrice(taxPrice);
        order.setShippingPrice(shippingPrice);
        order.setTotalAmount(itemsPrice.add(taxPrice).add(shippingPrice));
        order.setPaymentMethod("PayPal");

        // Shipping Address
        ShippingAddress shipping = new ShippingAddress();
        shipping.setFirstName(firstName);
        shipping.setLastName(lastName);
        shipping.setStreet(street);
        shipping.setCity(city);
        shipping.setState(state);
        shipping.setZipCode(zipCode);
        shipping.setCountry(country);
        shipping.setPhone(phone);
        order.setShippingAddress(shipping);

        // Payment Result
        PaymentResult payment = new PaymentResult();
        payment.setPaymentId(paymentId);
        payment.setStatus("completed");
        payment.setUpdateTime(LocalDateTime.now().minusDays(daysAgo).toString());
        payment.setEmailAddress(email);
        order.setPaymentResult(payment);

        // Payment status only (not delivered yet)
        order.setIsPaid(true);
        order.setPaidAt(LocalDateTime.now().minusDays(daysAgo));
        order.setIsDelivered(false);

        // Add sample order items
        OrderItem item1 = new OrderItem();
        item1.setProductId("7");
        item1.setQuantity(1);
        item1.setPrice(itemsPrice);
        item1.setOrder(order);

        order.setItems(Arrays.asList(item1));

        return order;
    }

    private Order createUnpaidOrder(String userId, String firstName, String lastName, String email,
            String street, String city, String state, String zipCode, String country, String phone,
            BigDecimal itemsPrice, BigDecimal taxPrice, BigDecimal shippingPrice, int daysAgo) {

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setItemsPrice(itemsPrice);
        order.setTaxPrice(taxPrice);
        order.setShippingPrice(shippingPrice);
        order.setTotalAmount(itemsPrice.add(taxPrice).add(shippingPrice));
        order.setPaymentMethod("Stripe");

        // Shipping Address
        ShippingAddress shipping = new ShippingAddress();
        shipping.setFirstName(firstName);
        shipping.setLastName(lastName);
        shipping.setStreet(street);
        shipping.setCity(city);
        shipping.setState(state);
        shipping.setZipCode(zipCode);
        shipping.setCountry(country);
        shipping.setPhone(phone);
        order.setShippingAddress(shipping);

        // Not paid yet
        order.setIsPaid(false);
        order.setIsDelivered(false);
        order.setStripeClientSecret("seti_secret_" + System.currentTimeMillis());

        // Add sample order items
        OrderItem item1 = new OrderItem();
        item1.setProductId("8");
        item1.setQuantity(1);
        item1.setPrice(itemsPrice);
        item1.setOrder(order);

        order.setItems(Arrays.asList(item1));

        return order;
    }
}
