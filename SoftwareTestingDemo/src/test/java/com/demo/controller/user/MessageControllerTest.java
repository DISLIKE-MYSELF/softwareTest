package com.demo.controller.user;

import com.demo.controller.user.MessageController;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.exception.LoginException;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageVoService messageVoService;

    // 模拟数据
    private List<Message> passMessageList;
    private Page<Message> passMessagePage;
    private List<MessageVo> messageVoList;
    private Message sampleMessage;
    private User sampleUser;

    @BeforeEach
    public void setup() {
        // 构造示例留言
        sampleMessage = new Message();
        sampleMessage.setMessageID(1);
        sampleMessage.setUserID("user001");
        sampleMessage.setContent("测试留言内容");
        sampleMessage.setState(1);
        sampleMessage.setTime(LocalDateTime.now());

        passMessageList = new ArrayList<>();
        passMessageList.add(sampleMessage);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        passMessagePage = new PageImpl<>(passMessageList, pageable, passMessageList.size());

        messageVoList = new ArrayList<>();
        MessageVo vo = new MessageVo();
        // 可根据实际 MessageVo 属性赋值
        messageVoList.add(vo);

        // 构造示例用户，用于session中模拟已登录用户
        sampleUser = new User();
        sampleUser.setUserID("user001");
        // 其他属性可根据需要赋值

        // 配置 MessageService 和 MessageVoService 的模拟行为
        given(messageService.findPassState(pageable)).willReturn(passMessagePage);
        given(messageVoService.returnVo(passMessageList)).willReturn(messageVoList);
        // 对于用户留言列表
        given(messageService.findByUser("user001", pageable)).willReturn(passMessagePage);
        // 修改、删除等测试用例直接不需要额外模拟返回值
    }

    // TC-01：测试 /message_list 页面，未登录时应抛出 LoginException
    @Test
    public void testMessageListWithoutLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/message_list"))
                .andExpect(result -> 
                    // 检查是否抛出 LoginException（异常信息中包含"请登录"关键字）
                    result.getResolvedException() instanceof LoginException &&
                    result.getResolvedException().getMessage().contains("请登录")
                );
    }

    // TC-02：测试 /message_list 页面，已登录时返回页面及设置模型属性
    @Test
    public void testMessageListWithLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", sampleUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/message_list").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("user_total"))
                .andExpect(view().name("message_list"));
    }

    // TC-03：测试 /message/getMessageList 接口，返回留言列表 JSON 数据
    @Test
    public void testGetMessageList() throws Exception {
        int page = 1;
        Pageable pageable = PageRequest.of(page - 1, 5, Sort.by("time").descending());
        given(messageService.findPassState(pageable)).willReturn(passMessagePage);

        mockMvc.perform(MockMvcRequestBuilders.get("/message/getMessageList")
                        .param("page", String.valueOf(page))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // 验证返回 JSON 数组的大小
                .andExpect(jsonPath("$", hasSize(messageVoList.size())));
    }

    // TC-04：测试 /message/findUserList 接口，未登录时抛出 LoginException
    @Test
    public void testFindUserListWithoutLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/message/findUserList")
                        .param("page", "1"))
                .andExpect(result ->
                        result.getResolvedException() instanceof LoginException &&
                        result.getResolvedException().getMessage().contains("请登录")
                );
    }

    // TC-05：测试 /message/findUserList 接口，已登录时返回该用户留言的 JSON 数据
    @Test
    public void testFindUserListWithLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", sampleUser);

        int page = 1;
        Pageable pageable = PageRequest.of(page - 1, 5, Sort.by("time").descending());
        given(messageService.findByUser("user001", pageable)).willReturn(passMessagePage);

        mockMvc.perform(MockMvcRequestBuilders.get("/message/findUserList")
                        .session(session)
                        .param("page", String.valueOf(page))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(messageVoList.size())));
    }

    // TC-06：测试 /sendMessage 接口，发送留言后重定向至 /message_list
    @Test
    public void testSendMessage() throws Exception {
        // 由于 sendMessage 接口内部调用了 messageService.create(message)
        // 此处只验证重定向效果即可
        mockMvc.perform(MockMvcRequestBuilders.post("/sendMessage")
                        .param("userID", "user001")
                        .param("content", "新留言内容"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));
    }

    // TC-07：测试 /modifyMessage.do 接口，修改留言返回 true
    @Test
    public void testModifyMessage() throws Exception {
        // 模拟根据 id 获取留言
        given(messageService.findById(1)).willReturn(sampleMessage);

        mockMvc.perform(MockMvcRequestBuilders.post("/modifyMessage.do")
                        .param("messageID", "1")
                        .param("content", "修改后的留言内容"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // TC-08：测试 /delMessage.do 接口，删除留言返回 true
    @Test
    public void testDelMessage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/delMessage.do")
                        .param("messageID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
