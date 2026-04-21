package com.swirl.ecomengine.admin.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.admin.order.controller.AdminOrderController;
import com.swirl.ecomengine.admin.order.service.AdminOrderService;
import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderMapper;
import com.swirl.ecomengine.order.dto.OrderResponse;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.TestDataFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test-controller")
class AdminOrderControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AdminOrderService service;
    @MockBean private OrderMapper mapper;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    private User admin;

    @BeforeEach
    void setup() throws Exception {
        admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        when(authenticatedUserArgumentResolver.supportsParameter(any())).thenReturn(true);

        Mockito.when(authenticatedUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(admin);
    }

    // ---------------------------------------------------------
    // ADMIN: GET ALL ORDERS
    // ---------------------------------------------------------
    @Test
    void adminCanListAllOrders() throws Exception {
        Order order = TestDataFactory.order(admin);

        Mockito.when(service.getAllOrders(any(User.class)))
                .thenReturn(List.of(order));

        Mockito.when(mapper.toResponse(order))
                .thenReturn(new OrderResponse(
                        1L,
                        order.getTotalPrice(),
                        order.getStatus(),
                        order.getCreatedAt(),
                        List.of()
                ));

        mvc.perform(get("/admin/orders"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // ADMIN: GET ARCHIVED ORDERS
    // ---------------------------------------------------------
    @Test
    void adminCanSeeArchivedOrders() throws Exception {
        Order archived = TestDataFactory.order(admin);
        archived.setDeleted(true);

        Mockito.when(service.getDeletedOrders(any(User.class)))
                .thenReturn(List.of(archived));

        Mockito.when(mapper.toResponse(archived))
                .thenReturn(new OrderResponse(
                        2L,
                        archived.getTotalPrice(),
                        archived.getStatus(),
                        archived.getCreatedAt(),
                        List.of()
                ));

        mvc.perform(get("/admin/orders/archived"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // ADMIN: RESTORE ORDER
    // ---------------------------------------------------------
    @Test
    void adminCanRestoreOrder() throws Exception {
        mvc.perform(post("/admin/orders/5/restore"))
                .andExpect(status().isNoContent());
    }

    // ---------------------------------------------------------
    // FAIL TESTS
    // ---------------------------------------------------------

    @Test
    void getAllOrdersFailsWhenServiceThrows() throws Exception {
        Mockito.when(service.getAllOrders(any(User.class)))
                .thenThrow(new RuntimeException("DB error"));

        mvc.perform(get("/admin/orders"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllOrdersFailsWhenMapperThrows() throws Exception {
        Order order = TestDataFactory.order(admin);

        Mockito.when(service.getAllOrders(any(User.class)))
                .thenReturn(List.of(order));

        Mockito.when(mapper.toResponse(order))
                .thenThrow(new RuntimeException("Mapping failed"));

        mvc.perform(get("/admin/orders"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getArchivedOrdersFailsWhenServiceReturnsNull() throws Exception {
        Mockito.when(service.getDeletedOrders(any(User.class)))
                .thenReturn(null);

        mvc.perform(get("/admin/orders/archived"))
                .andExpect(status().isInternalServerError());
    }
}