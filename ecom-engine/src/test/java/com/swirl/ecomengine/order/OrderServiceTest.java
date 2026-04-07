package com.swirl.ecomengine.order;

import com.swirl.ecomengine.cart.Cart;
import com.swirl.ecomengine.cart.service.CartService;
import com.swirl.ecomengine.order.exception.OrderAccessDeniedException;
import com.swirl.ecomengine.order.exception.OrderBadRequestException;
import com.swirl.ecomengine.order.exception.OrderNotFoundException;
import com.swirl.ecomengine.order.service.OrderService;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import testsupport.TestDataFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private CartService cartService;
    private OrderRepository orderRepository;
    private OrderService orderService;

    private User user;
    private Cart cart;

    @BeforeEach
    void setup() {
        cartService = mock(CartService.class);
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderService(cartService, orderRepository);

        user = new User();
        user.setId(1L);

        cart = TestDataFactory.cart(user);
    }

    // ---------------------------------------------------------
    // CREATE ORDER (Checkout)
    // ---------------------------------------------------------

    @Test
    void placeOrder_createsOrderFromCart() {
        var category = TestDataFactory.defaultCategory();
        var laptop = TestDataFactory.defaultProduct(category);

        cart.getItems().add(TestDataFactory.cartItem(laptop, 2));
        when(cartService.getCart(user)).thenReturn(cart);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(user);

        assertThat(captor.getValue().getItems()).hasSize(1);
    }

    @Test
    void placeOrder_clearsCartAfterSaving() {
        var category = TestDataFactory.defaultCategory();
        var laptop = TestDataFactory.defaultProduct(category);

        cart.getItems().add(TestDataFactory.cartItem(laptop, 1));
        when(cartService.getCart(user)).thenReturn(cart);

        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(user);

        verify(cartService).clearCart(user);
    }

    @Test
    void placeOrder_throwsWhenCartIsEmpty() {
        when(cartService.getCart(user)).thenReturn(cart);

        assertThatThrownBy(() -> orderService.placeOrder(user))
                .isInstanceOf(OrderBadRequestException.class)
                .hasMessageContaining("empty cart");
    }

    @Test
    void placeOrder_mapsOrderItemsCorrectly() {
        var category = TestDataFactory.defaultCategory();
        var laptop = TestDataFactory.defaultProduct(category);

        cart.getItems().add(TestDataFactory.cartItem(laptop, 2));
        when(cartService.getCart(user)).thenReturn(cart);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(user);

        var item = captor.getValue().getItems().get(0);
        assertThat(item.getProductName()).isEqualTo(laptop.getName());
        assertThat(item.getQuantity()).isEqualTo(2);
    }

    @Test
    void placeOrder_calculatesTotalPriceCorrectly() {
        var category = TestDataFactory.defaultCategory();
        var laptop = TestDataFactory.defaultProduct(category);

        cart.getItems().add(TestDataFactory.cartItem(laptop, 2));
        when(cartService.getCart(user)).thenReturn(cart);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(user);

        assertThat(captor.getValue().getTotalPrice())
                .isEqualTo(2 * laptop.getPrice());
    }

    // ---------------------------------------------------------
    // GET ORDER HISTORY
    // ---------------------------------------------------------

    @Test
    void getOrderHistory_returnsOrders() {
        Order o1 = Order.builder().id(1L).user(user).createdAt(LocalDateTime.now()).build();
        Order o2 = Order.builder().id(2L).user(user).createdAt(LocalDateTime.now()).build();

        when(orderRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(o1, o2));

        var result = orderService.getOrderHistory(user);

        assertThat(result).hasSize(2);
    }

    // ---------------------------------------------------------
    // GET ACTIVE ORDERS
    // ---------------------------------------------------------

    @Test
    void getActiveOrders_returnsPendingAndProcessing() {
        Order o1 = Order.builder().id(1L).status(OrderStatus.PENDING).user(user).build();
        Order o2 = Order.builder().id(2L).status(OrderStatus.PROCESSING).user(user).build();

        when(orderRepository.findByUserAndStatusIn(
                eq(user),
                eq(List.of(OrderStatus.PENDING, OrderStatus.PROCESSING))
        )).thenReturn(List.of(o1, o2));

        var result = orderService.getActiveOrders(user);

        assertThat(result).hasSize(2);
    }

    // ---------------------------------------------------------
    // GET ORDER BY ID
    // ---------------------------------------------------------

    @Test
    void getOrderById_returnsOrder_whenUserOwnsIt() {
        Order order = Order.builder().id(5L).user(user).build();

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        var result = orderService.getOrderById(user, 5L);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void getOrderById_throwsNotFound_whenMissing() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(user, 999L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void getOrderById_throwsAccessDenied_whenUserDoesNotOwnOrder() {
        User other = new User();
        other.setId(2L);

        Order order = Order.builder().id(5L).user(other).build();

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(user, 5L))
                .isInstanceOf(OrderAccessDeniedException.class);
    }
}