package com.swirl.ecomengine.order;

import com.swirl.ecomengine.cart.Cart;
import com.swirl.ecomengine.cart.service.CartService;
import com.swirl.ecomengine.order.exception.MissingOrderInformationException;
import com.swirl.ecomengine.order.exception.OrderAccessDeniedException;
import com.swirl.ecomengine.order.exception.OrderBadRequestException;
import com.swirl.ecomengine.order.exception.OrderNotFoundException;
import com.swirl.ecomengine.order.service.OrderService;
import com.swirl.ecomengine.orderhistory.service.OrderHistoryService;
import com.swirl.ecomengine.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import testsupport.EntityManagerTestConfig;
import testsupport.TestDataFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(EntityManagerTestConfig.class)
class OrderServiceTest {

    @Autowired
    private EntityManager entityManager;

    private CartService cartService;
    private OrderRepository orderRepository;
    private OrderService orderService;

    private User user;
    private Cart cart;

    @BeforeEach
    void setup() {
        cartService = mock(CartService.class);
        orderRepository = mock(OrderRepository.class);
        OrderHistoryService historyService = mock(OrderHistoryService.class);

        orderService = new OrderService(cartService, orderRepository, historyService);

        user = TestDataFactory.user(pwd -> "encoded");
        user.setId(1L);
        user.setAddress(TestDataFactory.address(user));

        cart = TestDataFactory.cart(user);

        ReflectionTestUtils.setField(orderService, "entityManager", entityManager);
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
    void placeOrder_throwsWhenNameMissing() {
        user.setName(null);
        when(cartService.getCart(user)).thenReturn(cart);

        assertThatThrownBy(() -> orderService.placeOrder(user))
                .isInstanceOf(MissingOrderInformationException.class)
                .hasMessageContaining("Name");
    }

    @Test
    void placeOrder_throwsWhenPhoneMissing() {
        user.setPhone(null);
        when(cartService.getCart(user)).thenReturn(cart);

        assertThatThrownBy(() -> orderService.placeOrder(user))
                .isInstanceOf(MissingOrderInformationException.class)
                .hasMessageContaining("Phone");
    }

    @Test
    void placeOrder_throwsWhenAddressMissing() {
        user.setAddress(null);
        when(cartService.getCart(user)).thenReturn(cart);

        assertThatThrownBy(() -> orderService.placeOrder(user))
                .isInstanceOf(MissingOrderInformationException.class)
                .hasMessageContaining("Address");
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
    // CREATE ORDER (cart missing)
    // ---------------------------------------------------------
    @Test
    void placeOrder_throwsBadRequest_whenCartIsNull() {
        when(cartService.getCart(user)).thenReturn(null);

        assertThatThrownBy(() -> orderService.placeOrder(user))
                .isInstanceOf(OrderBadRequestException.class)
                .hasMessageContaining("cart");
    }

    // ---------------------------------------------------------
    // GET ORDER HISTORY
    // ---------------------------------------------------------

    @Test
    void getOrderHistory_returnsOrders() {
        Order o1 = TestDataFactory.order(user);
        Order o2 = TestDataFactory.order(user);


        Pageable pageable = PageRequest.of(0, 20);
        Page<Order> page = new PageImpl<>(List.of(o1, o2), pageable, 2);

        when(orderRepository.findByUser(user, pageable))
                .thenReturn(page);

        Page<Order> result = orderService.getOrderHistory(user, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // ---------------------------------------------------------
    // GET ORDER HISTORY (empty)
    // ---------------------------------------------------------
    @Test
    void getOrderHistory_returnsEmptyPage_whenNoOrdersExist() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Order> emptyPage = Page.empty(pageable);

        when(orderRepository.findByUser(user, pageable))
                .thenReturn(emptyPage);

        Page<Order> result = orderService.getOrderHistory(user, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ---------------------------------------------------------
    // GET ORDER HISTORY (verifies pageable is forwarded correctly)
    // ---------------------------------------------------------
    @Test
    void getOrderHistory_passesCorrectPageableToRepository() {
        Pageable pageable = PageRequest.of(2, 10); // page=2, size=10

        Page<Order> emptyPage = Page.empty(pageable);

        when(orderRepository.findByUser(user, pageable))
                .thenReturn(emptyPage);

        Page<Order> result = orderService.getOrderHistory(user, pageable);

        // Verify repository was called with EXACT pageable
        verify(orderRepository).findByUser(user, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
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
    // GET ACTIVE ORDERS (empty)
    // ---------------------------------------------------------
    @Test
    void getActiveOrders_returnsEmptyList_whenNoActiveOrdersExist() {
        when(orderRepository.findByUserAndStatusIn(
                eq(user),
                eq(List.of(OrderStatus.PENDING, OrderStatus.PROCESSING))
        )).thenReturn(List.of());

        var result = orderService.getActiveOrders(user);

        assertThat(result).isEmpty();
    }

    // ---------------------------------------------------------
    // GET ORDER BY ID
    // ---------------------------------------------------------

    @Test
    void getOrderById_returnsOrder_whenUserOwnsIt() {
        Order order = TestDataFactory.order(user);
        order.setId(5L);

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
        User otherUser = TestDataFactory.user(pwd -> "encoded");
        otherUser.setId(2L);

        Order order = Order.builder().id(5L).user(otherUser).build();

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(user, 5L))
                .isInstanceOf(OrderAccessDeniedException.class);
    }

    // ---------------------------------------------------------
    // DELETE ORDER (soft‑delete)
    // ---------------------------------------------------------
    @Test
    void deleteOrder_softDeletesOrder() {
        Order order = TestDataFactory.order(user);
        order.setId(10L);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        orderService.delete(10L);

        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_throwsNotFound_whenOrderMissing() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.delete(999L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ---------------------------------------------------------
    // RESTORE ORDER (ADMIN ONLY)
    // ---------------------------------------------------------
    @Test
    void restoreOrder_restoresSoftDeletedOrder() {
        // Arrange
        User admin = TestDataFactory.admin(pwd -> "encoded");

        Order deletedOrder = TestDataFactory.order(user);
        deletedOrder.setId(20L);
        deletedOrder.setDeleted(true);

        Query query = mock(Query.class);

        when(entityManager.createNativeQuery(anyString(), eq(Order.class)))
                .thenReturn(query);

        when(query.setParameter("id", 20L))
                .thenReturn(query);

        when(query.getSingleResult())
                .thenReturn(deletedOrder);

        when(orderRepository.save(deletedOrder))
                .thenReturn(deletedOrder);

        // Act
        orderService.restore(20L, admin);

        // Assert
        assertThat(deletedOrder.isDeleted()).isFalse();
        verify(orderRepository).save(deletedOrder);
    }

    @Test
    void restoreOrder_throwsNotFound_whenOrderMissing() {
        User admin = TestDataFactory.admin(pwd -> "encoded");

        // Mock native query
        var query = mock(Query.class);

        when(entityManager.createNativeQuery(anyString(), eq(Order.class)))
                .thenReturn(query);

        when(query.setParameter(eq("id"), eq(123L)))
                .thenReturn(query);

        when(query.getSingleResult())
                .thenReturn(null); // simulate missing order

        assertThatThrownBy(() -> orderService.restore(123L, admin))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void restoreOrder_throwsAccessDenied_whenUserIsNotAdmin() {
        assertThatThrownBy(() -> orderService.restore(5L, user))
                .isInstanceOf(OrderAccessDeniedException.class);
    }
}