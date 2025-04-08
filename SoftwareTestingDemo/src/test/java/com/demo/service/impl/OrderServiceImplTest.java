package com.demo.service.impl;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() {
        Order order = new Order();
        order.setOrderID(1);
        when(orderDao.getOne(1)).thenReturn(order);

        Order result = orderService.findById(1);
        assertNotNull(result);
        assertEquals(1, result.getOrderID());
    }

    @Test
    void testFindDateOrder() {
        List<Order> orders = Collections.singletonList(new Order());
        when(orderDao.findByVenueIDAndStartTimeIsBetween(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(orders);

        List<Order> result = orderService.findDateOrder(1, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        assertEquals(1, result.size());
    }

    @Test
    void testFindUserOrder() {
        Page<Order> page = new PageImpl<>(Collections.singletonList(new Order()));
        when(orderDao.findAllByUserID(eq("user1"), any(PageRequest.class))).thenReturn(page);

        Page<Order> result = orderService.findUserOrder("user1", PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testUpdateOrder() {
        Venue venue = new Venue();
        venue.setVenueID(1);
        venue.setPrice(100);
        Order order = new Order();
        when(venueDao.findByVenueName("Venue1")).thenReturn(venue);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.updateOrder(1, "Venue1", LocalDateTime.now(), 2, "user1");

        verify(orderDao).save(order);
        assertEquals(200, order.getTotal());
    }

    @Test
    void testSubmit() {
        Venue venue = new Venue();
        venue.setVenueID(1);
        venue.setPrice(100);
        when(venueDao.findByVenueName("Venue1")).thenReturn(venue);

        orderService.submit("Venue1", LocalDateTime.now(), 2, "user1");

        verify(orderDao).save(any(Order.class));
    }

    @Test
    void testDelOrder() {
        orderService.delOrder(1);
        verify(orderDao, times(1)).deleteById(1);
    }

    @Test
    void testConfirmOrder() {
        Order order = new Order();
        order.setOrderID(1);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.confirmOrder(1);

        verify(orderDao).updateState(OrderServiceImpl.STATE_WAIT, 1);
    }

    @Test
    void testConfirmOrderNotFound() {
        when(orderDao.findByOrderID(1)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> orderService.confirmOrder(1));
        assertEquals("订单不存在", exception.getMessage());
    }

    @Test
    void testFinishOrder() {
        Order order = new Order();
        order.setOrderID(1);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.finishOrder(1);

        verify(orderDao).updateState(OrderServiceImpl.STATE_FINISH, 1);
    }

    @Test
    void testRejectOrder() {
        Order order = new Order();
        order.setOrderID(1);
        when(orderDao.findByOrderID(1)).thenReturn(order);

        orderService.rejectOrder(1);

        verify(orderDao).updateState(OrderServiceImpl.STATE_REJECT, 1);
    }

    @Test
    void testFindNoAuditOrder() {
        Page<Order> page = new PageImpl<>(Collections.singletonList(new Order()));
        when(orderDao.findAllByState(eq(OrderServiceImpl.STATE_NO_AUDIT), any(PageRequest.class))).thenReturn(page);

        Page<Order> result = orderService.findNoAuditOrder(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindAuditOrder() {
        List<Order> orders = Collections.singletonList(new Order());
        when(orderDao.findAudit(OrderServiceImpl.STATE_WAIT, OrderServiceImpl.STATE_FINISH)).thenReturn(orders);

        List<Order> result = orderService.findAuditOrder();
        assertEquals(1, result.size());
    }
}