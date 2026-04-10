package com.swirl.ecomengine.order.service;

import com.swirl.ecomengine.cart.Cart;
import com.swirl.ecomengine.cart.service.CartService;
import com.swirl.ecomengine.common.exception.UnauthorizedException;
import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.order.exception.OrderAccessDeniedException;
import com.swirl.ecomengine.order.exception.OrderBadRequestException;
import com.swirl.ecomengine.order.exception.OrderNotFoundException;
import com.swirl.ecomengine.order.item.OrderItem;
import com.swirl.ecomengine.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;

    public OrderService(CartService cartService, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
    }

    // ---------------------------------------------------------
    // CREATE ORDER (Checkout)
    // ---------------------------------------------------------
    @Transactional
    public Order placeOrder(User user) {
        if (user == null) {
            throw new UnauthorizedException("User must be authenticated");
        }

        Cart cart = cartService.getCart(user);

        if (cart == null) {
            throw new OrderBadRequestException("Cannot place order: cart is null");
        }

        if (cart.getItems().isEmpty()) {
            throw new OrderBadRequestException("Cannot place order with empty cart");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
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
    public Order updateStatus(Long id, OrderStatus newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (!isValidTransition(order.getStatus(), newStatus)) {
            throw new OrderBadRequestException(
                    "Invalid status transition: " + order.getStatus() + " → " + newStatus
            );
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    /**
     * Validates allowed order status transitions.
     * <pre>
     * PENDING     → PROCESSING, CANCELLED
     * PROCESSING  → COMPLETED, CANCELLED
     * COMPLETED   → (no transitions)
     * CANCELLED   → (no transitions)
     */
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case PENDING -> (to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED);
            case PROCESSING -> (to == OrderStatus.COMPLETED || to == OrderStatus.CANCELLED);
            default -> false;
        };
    }

    // ---------------------------------------------------------
    // UPDATE ORDER STATUS
    // ---------------------------------------------------------
    @Transactional
    public Order updateStatus(User user, Long id, OrderStatus newStatus) {
        Order order = getOrderById(user, id);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    // ---------------------------------------------------------
    // GET ORDER HISTORY
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // ---------------------------------------------------------
    // GET ACTIVE ORDERS (PENDING + PROCESSING)
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public List<Order> getActiveOrders(User user) {
        return orderRepository.findByUserAndStatusIn(
                user,
                List.of(OrderStatus.PENDING, OrderStatus.PROCESSING)
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
