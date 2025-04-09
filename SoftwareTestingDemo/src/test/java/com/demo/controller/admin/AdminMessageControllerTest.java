package com.demo.controller.admin;

import com.demo.controller.admin.AdminMessageController;
import com.demo.entity.Message;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.mockito.BDDMockito.given;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.ArrayList;
import java.util.List;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminMessageController.class)
public class AdminMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageVoService messageVoService;

    // 构造模拟数据
    private Page<Message> messagePage;
    private List<MessageVo> messageVoList;

    @BeforeEach
    public void setup() {
        List<Message> messages = new ArrayList<>();
        // 添加一个模拟的消息对象（可根据需要设置属性）
        Message msg = new Message();
        messages.add(msg);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("time").descending());
        messagePage = new PageImpl<>(messages, pageable, messages.size());

        messageVoList = new ArrayList<>();
        MessageVo messageVo = new MessageVo();
        // 可设置 messageVo 对象的属性
        messageVoList.add(messageVo);

        // 配置 findWaitState 方法模拟返回数据
        given(messageService.findWaitState(pageable)).willReturn(messagePage);
    }

    // 测试 /message_manage 页面跳转及模型数据设置
    @Test
    public void testMessageManage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/message_manage"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("admin/message_manage"));
    }

    // 测试 /messageList.do 接口返回消息列表
    @Test
    public void testMessageList() throws Exception {
        int page = 1;
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("time").descending());
        // 针对指定分页构造模拟数据
        given(messageService.findWaitState(pageable)).willReturn(messagePage);
        given(messageVoService.returnVo(messagePage.getContent())).willReturn(messageVoList);

        mockMvc.perform(MockMvcRequestBuilders.get("/messageList.do")
                        .param("page", String.valueOf(page))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // 检查返回的 JSON 数组中存在第一个对象
                .andExpect(jsonPath("$[0]").exists());
    }

    // 测试 /passMessage.do 接口审核通过消息
    @Test
    public void testPassMessage() throws Exception {
        int messageID = 1;
        mockMvc.perform(MockMvcRequestBuilders.post("/passMessage.do")
                        .param("messageID", String.valueOf(messageID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // 测试 /rejectMessage.do 接口审核拒绝消息
    @Test
    public void testRejectMessage() throws Exception {
        int messageID = 1;
        mockMvc.perform(MockMvcRequestBuilders.post("/rejectMessage.do")
                        .param("messageID", String.valueOf(messageID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // 测试 /delMessage.do 接口删除消息
    @Test
    public void testDelMessage() throws Exception {
        int messageID = 1;
        mockMvc.perform(MockMvcRequestBuilders.get("/delMessage.do")
                        .param("messageID", String.valueOf(messageID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
