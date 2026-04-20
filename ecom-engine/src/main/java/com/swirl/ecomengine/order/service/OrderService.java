package com.swirl.ecomengine.order.service;

import com.swirl.ecomengine.address.Address;
import com.swirl.ecomengine.cart.Cart;
import com.swirl.ecomengine.cart.service.CartService;
import com.swirl.ecomengine.common.exception.UnauthorizedException;
import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.order.exception.MissingOrderInformationException;
import com.swirl.ecomengine.order.exception.OrderAccessDeniedException;
import com.swirl.ecomengine.order.exception.OrderBadRequestException;
import com.swirl.ecomengine.order.exception.OrderNotFoundException;
import com.swirl.ecomengine.order.item.OrderItem;
import com.swirl.ecomengine.orderhistory.service.OrderHistoryService;
import com.swirl.ecomengine.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static com.swirl.ecomengine.common.StringUtils.hasText;
import static com.swirl.ecomengine.order.OrderStatus.PENDING;
import static com.swirl.ecomengine.order.OrderStatus.PROCESSING;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderHistoryService historyService;

    @PersistenceContext
    private EntityManager entityManager;

    // ---------------------------------------------------------
    // CREATE ORDER (Checkout)
    // ---------------------------------------------------------
    @Transactional
    public Order placeOrder(User user) {
        if (user == null) {
            throw new UnauthorizedException("User must be authenticated");
        }

        // ---------------------------------------------------------
        // USER PROFILE VALIDATION
        // ---------------------------------------------------------
        if (!hasText(user.getName())) {
            throw new MissingOrderInformationException("Name is required to place an order");
        }

        if (!hasText(user.getPhone())) {
            throw new MissingOrderInformationException("Phone number is required to place an order");
        }

        Address address = user.getAddress();
        if (address == null) {
            throw new MissingOrderInformationException("Address is required to place an order");
        }

        // ---------------------------------------------------------
        // CART VALIDATION
        // ---------------------------------------------------------
        Cart cart = cartService.getCart(user);

        if (cart == null) {
            throw new OrderBadRequestException("Cannot place order: cart is null");
        }

        if (cart.getItems().isEmpty()) {
            throw new OrderBadRequestException("Cannot place order with empty cart");
        }

        // ---------------------------------------------------------
        // CREATE ORDER
        // ---------------------------------------------------------
        Order order = Order.builder()
                .user(user)
                .status(PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        double total = 0;

        for (var cartItem : cart.getItems()) {
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productId(cartItem.getProduct().getId())
                    .productName(cartItem.getProduct().getName())
                    .price(cartItem.getProduct().getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();

            order.getItems().add(item);
            total += item.getPrice() * item.getQuantity();
        }

        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);

        cartService.clearCart(user);

        return saved;
    }

    // ---------------------------------------------------------
    // UPDATE ORDER STATUS (ADMIN ONLY)
    // ---------------------------------------------------------
    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus, User adminUser, String reason) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (!adminUser.isAdmin()) {
            throw new OrderAccessDeniedException();
        }

        OrderStatus oldStatus = order.getStatus();
        order.updateStatus(newStatus);
        Order saved = orderRepository.save(order);

        historyService.logStatusChange(order, oldStatus, newStatus, adminUser, reason);

        return saved;
    }

    // ---------------------------------------------------------
    // GET ORDER HISTORY (USER)
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<Order> getOrderHistory(User user, Pageable pageable) {
        return orderRepository.findByUser(user, pageable);
    }

    // ---------------------------------------------------------
    // GET ACTIVE ORDERS (PENDING + PROCESSING)
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public List<Order> getActiveOrders(User user) {
        return orderRepository.findByUserAndStatusIn(
                user,
                List.of(PENDING, PROCESSING)
        );
    }

    // ---------------------------------------------------------
    // GET ORDER BY ID (with ownership validation)
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public Order getOrderById(User user, Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new OrderAccessDeniedException();
        }

        return order;
    }

    // ---------------------------------------------------------
    // DELETE ORDER (Soft-delete)
    // ---------------------------------------------------------
    @Transactional
    public void delete(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        orderRepository.delete(order); // triggers @SQLDelete → sets deleted = true
    }

    // ---------------------------------------------------------
    // GET ALL DELETED ORDERS (Admin only)
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public List<Order> getDeletedOrders(User adminUser) {

        if (!adminUser.isAdmin()) {
            throw new OrderAccessDeniedException();
        }

        return orderRepository.findArchived();
    }

    // ---------------------------------------------------------
    // RESTORE ORDER (Admin only)
    // ---------------------------------------------------------
    @Transactional
    public void restore(Long id, User adminUser) {

        if (!adminUser.isAdmin()) {
            throw new OrderAccessDeniedException();
        }

        Order order = (Order) entityManager
                .createNativeQuery("SELECT * FROM orders WHERE id = :id", Order.class)
                .setParameter("id", id)
                .getSingleResult();

        if (order == null) {
            throw new OrderNotFoundException(id);
        }

        // Restore the order
        order.setDeleted(false);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    // ---------------------------------------------------------
    // UTILITY: RECALCULATE TOTAL PRICE
    // ---------------------------------------------------------
    public double calculateTotal(Order order) {
        return order.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
    }

    // ---------------------------------------------------------
    // UTILITY: SORT ITEMS (optional)
    // ---------------------------------------------------------
    public void sortItems(Order order) {
        order.getItems().sort(Comparator.comparing(OrderItem::getProductName));
    }
}
