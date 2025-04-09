package com.demo.controller.user;

import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import com.demo.entity.vo.VenueOrder;
import com.demo.exception.LoginException;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.data.domain.*;

import javax.servlet.http.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderVoService orderVoService;

    @Mock
    private VenueService venueService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOrderManageWithLogin() {
        User user = new User();
        user.setUserID("u1");

        when(request.getSession()).thenReturn(mock(HttpSession.class));
        when(request.getSession().getAttribute("user")).thenReturn(user);
        when(orderService.findUserOrder(eq("u1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        String result = orderController.order_manage(model, request);
        assertEquals("order_manage", result);
    }

    @Test
    void testOrderManageWithoutLoginShouldThrowException() {
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        when(request.getSession().getAttribute("user")).thenReturn(null);

        assertThrows(LoginException.class, () -> orderController.order_manage(model, request));
    }

    @Test
    void testOrderPlaceWithVenueID() {
        Venue venue = new Venue();
        venue.setVenueID(1);

        when(venueService.findByVenueID(1)).thenReturn(venue);

        String result = orderController.order_place(model, 1);
        verify(model).addAttribute("venue", venue);
        assertEquals("order_place", result);
    }

    @Test
    void testGetOrderListWithLogin() {
        User user = new User();
        user.setUserID("u2");

        List<Order> orders = List.of(new Order());
        Page<Order> page = new PageImpl<>(orders);
        List<OrderVo> vos = List.of(new OrderVo());

        when(request.getSession()).thenReturn(mock(HttpSession.class));
        when(request.getSession().getAttribute("user")).thenReturn(user);
        when(orderService.findUserOrder(eq("u2"), any(Pageable.class))).thenReturn(page);
        when(orderVoService.returnVo(orders)).thenReturn(vos);

        List<OrderVo> result = orderController.order_list(1, request);
        assertEquals(1, result.size());
    }
}
