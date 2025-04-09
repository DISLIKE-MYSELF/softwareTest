package com.demo.controller.admin;

import com.demo.controller.admin.AdminOrderController;
import com.demo.entity.Order;
import com.demo.entity.vo.OrderVo;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.BDDMockito.given;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminOrderController.class)
public class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderVoService orderVoService;

    // 构造模拟数据
    private List<Order> auditOrderList;
    private List<Order> noAuditOrderList;
    private Page<Order> noAuditOrderPage;
    private List<OrderVo> orderVoList;
    private Order sampleOrder;

    @BeforeEach
    public void setup() {
        // 构造一个示例订单对象
        sampleOrder = new Order();
        sampleOrder.setOrderID(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        sampleOrder.setOrderTime(LocalDateTime.parse("2025-03-14 10:00:00", formatter)); // 假定订单时间为字符串类型（根据实际类型修改）

        // 构造审核通过订单列表（已审核订单）
        auditOrderList = new ArrayList<>();
        auditOrderList.add(sampleOrder);

        // 构造未审核订单列表
        noAuditOrderList = new ArrayList<>();
        noAuditOrderList.add(sampleOrder);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("orderTime").descending());
        noAuditOrderPage = new PageImpl<>(noAuditOrderList, pageable, noAuditOrderList.size());

        // 构造 OrderVo 列表
        orderVoList = new ArrayList<>();
        OrderVo vo = new OrderVo();
        // 根据实际 OrderVo 属性进行设置
        orderVoList.add(vo);

        // 模拟 orderService 与 orderVoService 的调用行为
        // reservation_manage 页面：审核订单列表
        given(orderService.findAuditOrder()).willReturn(auditOrderList);
        // reservation_manage 页面：未审核订单分页查询
        Pageable pageableAsc = PageRequest.of(0, 10, Sort.by("orderTime").descending());
        given(orderService.findNoAuditOrder(pageableAsc)).willReturn(noAuditOrderPage);
        // 将订单列表转换为订单 VO 列表
        given(orderVoService.returnVo(auditOrderList)).willReturn(orderVoList);
        // 接口：获取未审核订单（按页）时的 OrderVo 转换
        given(orderVoService.returnVo(noAuditOrderList)).willReturn(orderVoList);
    }

    // 测试 /reservation_manage 页面，验证返回视图名称、模型中 order_list 及 total 属性设置
    @Test
    public void testReservationManage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/reservation_manage"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("order_list"))
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("admin/reservation_manage"));
    }

    // 测试 /admin/getOrderList.do 接口，获取未审核订单的 JSON 数据
    @Test
    public void testGetNoAuditOrder() throws Exception {
        int page = 1;
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("orderTime").descending());
        given(orderService.findNoAuditOrder(pageable)).willReturn(noAuditOrderPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/getOrderList.do")
                        .param("page", String.valueOf(page))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // 检查返回的 JSON 数组是否存在数据
                .andExpect(jsonPath("$[0]").exists());
    }

    // 测试 /passOrder.do 接口，确认订单（审核通过）
    @Test
    public void testConfirmOrder() throws Exception {
        int orderID = 1;
        mockMvc.perform(MockMvcRequestBuilders.post("/passOrder.do")
                        .param("orderID", String.valueOf(orderID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // 测试 /rejectOrder.do 接口，拒绝订单审核
    @Test
    public void testRejectOrder() throws Exception {
        int orderID = 1;
        mockMvc.perform(MockMvcRequestBuilders.post("/rejectOrder.do")
                        .param("orderID", String.valueOf(orderID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
