package com.swirl.ecomengine.wishlist;

import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.wishlist.controller.WishlistController;
import com.swirl.ecomengine.wishlist.dto.WishlistResponse;
import com.swirl.ecomengine.wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestConfigMinimal;
import testsupport.TestDataFactory;
import testsupport.WebMvcTestConfig;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
@Import({SecurityTestConfigMinimal.class, WebMvcTestConfig.class})
@ActiveProfiles("test-controller")
class WishlistControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;
    @MockBean private WishlistService wishlistService;
    @MockBean private WishlistMapper wishlistMapper;

    private User user;

    @BeforeEach
    void setup() throws Exception {
        user = TestDataFactory.user(pwd -> "encoded");
        user.setId(1L);

        when(authenticatedUserArgumentResolver.supportsParameter(
                argThat(param -> param.getParameterType().equals(User.class))
        )).thenReturn(true);

        when(authenticatedUserArgumentResolver.resolveArgument(
                any(), any(), any(), any()
        )).thenReturn(user);
    }

    // ---------------------------------------------------------
    // GET /wishlist
    // ---------------------------------------------------------
    @Test
    void getWishlist_Returns200() throws Exception {
        WishlistItem item = new WishlistItem();
        item.setId(100L);

        WishlistResponse response = new WishlistResponse(100L, 10L, "Test", 99.0, "desc", 1L);

        when(wishlistService.getWishlist(any())).thenReturn(List.of(item));
        when(wishlistMapper.toResponse(item)).thenReturn(response);

        mockMvc.perform(get("/wishlist"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // POST /wishlist/{productId}
    // ---------------------------------------------------------
    @Test
    void addWishlist_Returns204() throws Exception {
        mockMvc.perform(post("/wishlist/10"))
                .andExpect(status().isNoContent());

        verify(wishlistService).addToWishlist(10L, user);
    }

    // ---------------------------------------------------------
    // DELETE /wishlist/{productId}
    // ---------------------------------------------------------
    @Test
    void removeWishlist_Returns204() throws Exception {
        mockMvc.perform(delete("/wishlist/10"))
                .andExpect(status().isNoContent());

        verify(wishlistService).removeFromWishlist(10L, user);
    }

    // ---------------------------------------------------------
    // DELETE /wishlist
    // ---------------------------------------------------------
    @Test
    void clearWishlist_Returns204() throws Exception {
        mockMvc.perform(delete("/wishlist"))
                .andExpect(status().isNoContent());

        verify(wishlistService).clearWishlist(user);
    }
}
