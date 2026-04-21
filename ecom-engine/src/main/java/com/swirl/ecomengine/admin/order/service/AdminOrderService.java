package com.swirl.ecomengine.admin.order.service;

import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.order.exception.OrderAccessDeniedException;
import com.swirl.ecomengine.order.exception.OrderNotFoundException;
import com.swirl.ecomengine.orderhistory.service.OrderHistoryService;
import com.swirl.ecomengine.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Tag(name = "Admin Orders", description = "Admin-only order management service")
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderHistoryService historyService;

    @PersistenceContext
    private EntityManager entityManager;

    // ---------------------------------------------------------
    // GET
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public List<Order> getAllOrders(User adminUser) {
        if (!adminUser.isAdmin()) {
            throw new OrderAccessDeniedException();
        }

        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status, User adminUser) {

        if (!adminUser.isAdmin()) {
            throw new OrderAccessDeniedException();
        }

        return orderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Order> getDeletedOrders(User adminUser) {

        if (!adminUser.isAdmin()) {
            throw new OrderAccessDeniedException();
        }

        return orderRepository.findArchived();
    }

    // ---------------------------------------------------------
    // RESTORE ORDER
    // ---------------------------------------------------------
    @Transactional
    public void restore(Long id, User adminUser) {

        if (!adminUser.isAdmin()) {
            throw new OrderAccessDeniedException();
        }

        Order order = entityManager.find(Order.class, id);

        if (order == null) {
            throw new OrderNotFoundException(id);
        }

        order.setDeleted(false);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    // ---------------------------------------------------------
    // UPDATE ORDER STATUS
    // ---------------------------------------------------------
    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus, User adminUser, String reason) {

        if (!adminUser.isAdmin()) {
            throw new OrderAccessDeniedException();
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderStatus oldStatus = order.getStatus();
        order.updateStatus(newStatus);

        Order saved = orderRepository.save(order);

        historyService.logStatusChange(order, oldStatus, newStatus, adminUser, reason);

        return saved;
    }
}