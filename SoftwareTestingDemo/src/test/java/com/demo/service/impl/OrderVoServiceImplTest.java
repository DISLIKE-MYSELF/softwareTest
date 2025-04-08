package com.demo.service.impl;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderVoServiceImplTest {

    @InjectMocks
    private OrderVoServiceImpl orderVoService;

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 白盒测试：验证 returnOrderVoByOrderID 方法的内部逻辑是否正确。
     */
    @Test
    void testReturnOrderVoByOrderID_ValidOrder() {
        // 模拟数据
        Order order = new Order();
        order.setOrderID(1);
        order.setUserID("user1");
        order.setVenueID(2);
        order.setState(0);
        order.setOrderTime(LocalDateTime.now());
        order.setStartTime(LocalDateTime.now().plusHours(1));
        order.setHours(2);
        order.setTotal(200);

        Venue venue = new Venue();
        venue.setVenueID(2);
        venue.setVenueName("Venue1");

        // 模拟依赖方法返回值
        when(orderDao.findByOrderID(1)).thenReturn(order);
        when(venueDao.findByVenueID(2)).thenReturn(venue);

        // 调用被测方法
        OrderVo result = orderVoService.returnOrderVoByOrderID(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getOrderID());
        assertEquals("user1", result.getUserID());
        assertEquals(2, result.getVenueID());
        assertEquals("Venue1", result.getVenueName());
        assertEquals(200, result.getTotal());

        // 验证依赖方法是否被正确调用
        verify(orderDao, times(1)).findByOrderID(1);
        verify(venueDao, times(1)).findByVenueID(2);
    }

    /**
     * 黑盒测试：验证 returnOrderVoByOrderID 方法在订单不存在时的行为。
     */
    @Test
    void testReturnOrderVoByOrderID_OrderNotFound() {
        // 模拟订单不存在
        when(orderDao.findByOrderID(1)).thenReturn(null);

        // 调用被测方法并捕获异常
        Exception exception = assertThrows(NullPointerException.class, () -> orderVoService.returnOrderVoByOrderID(1));

        // 验证异常信息
        assertNotNull(exception);
        verify(orderDao, times(1)).findByOrderID(1);
        verify(venueDao, never()).findByVenueID(anyInt());
    }

    /**
     * 黑盒测试：验证 returnVo 方法在空订单列表时的行为。
     */
    @Test
    void testReturnVo_EmptyOrderList() {
        // 模拟空订单列表
        List<Order> orders = Collections.emptyList();

        // 调用被测方法
        List<OrderVo> result = orderVoService.returnVo(orders);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证依赖方法未被调用
        verify(orderDao, never()).findByOrderID(anyInt());
        verify(venueDao, never()).findByVenueID(anyInt());
    }

    /**
     * 黑盒测试：验证 returnVo 方法在订单列表为 null 时的行为。
     */
    @Test
    void testReturnVo_NullOrderList() {
        // 调用被测方法并捕获异常
        Exception exception = assertThrows(NullPointerException.class, () -> orderVoService.returnVo(null));

        // 验证异常信息
        assertNotNull(exception);

        // 验证依赖方法未被调用
        verify(orderDao, never()).findByOrderID(anyInt());
        verify(venueDao, never()).findByVenueID(anyInt());
    }
}