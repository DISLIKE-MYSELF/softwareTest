package com.demo.controller.user;

import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.exception.LoginException;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageVoService messageVoService;

    @Mock
    private HttpSession session;

    private List<Message> mockMessages;
    private List<MessageVo> mockMessageVos;

    @BeforeEach
    void setUp() {
        Message message1 = new Message();
        message1.setMessageID(1);
        message1.setUserID("user1");
        message1.setContent("Message 1");
        message1.setTime(LocalDateTime.now());

        Message message2 = new Message();
        message2.setMessageID(2);
        message2.setUserID("user2");
        message2.setContent("Message 2");
        message2.setTime(LocalDateTime.now());

        mockMessages = Arrays.asList(message1, message2);
        mockMessageVos = Arrays.asList(new MessageVo(), new MessageVo());
    }

    @Test
    void testMessageList() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<Message> page = new PageImpl<>(mockMessages, pageable, mockMessages.size());
        when(messageService.findPassState(pageable)).thenReturn(page);
        when(messageVoService.returnVo(mockMessages)).thenReturn(mockMessageVos);

        mockMvc.perform(get("/message/getMessageList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(messageService, times(1)).findPassState(pageable);
        verify(messageVoService, times(1)).returnVo(mockMessages);
    }

    @Test
    void testUserMessageListWithLogin() throws Exception {
        User user = new User();
        user.setUserID("user1");

        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        when(session.getAttribute("user")).thenReturn(user);
        when(messageService.findByUser("user1", pageable)).thenReturn(new PageImpl<>(mockMessages));
        when(messageVoService.returnVo(mockMessages)).thenReturn(mockMessageVos);

        mockMvc.perform(get("/message/findUserList").param("page", "1").sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(messageService, times(1)).findByUser("user1", pageable);
        verify(messageVoService, times(1)).returnVo(mockMessages);
    }

    @Test
    void testUserMessageListWithoutLogin() throws Exception {
        mockMvc.perform(get("/message/findUserList").param("page", "1"))
                .andExpect(status().is4xxClientError());

        verify(messageService, never()).findByUser(anyString(), any(Pageable.class));
        verify(messageVoService, never()).returnVo(anyList());
    }

    @Test
    void testSendMessage() throws Exception {
        mockMvc.perform(post("/sendMessage")
                        .param("userID", "user1")
                        .param("content", "Test Message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/message_list"));

        verify(messageService, times(1)).create(any(Message.class));
    }

    @Test
    void testModifyMessage() throws Exception {
        Message message = new Message();
        message.setMessageID(1);
        when(messageService.findById(1)).thenReturn(message);

        mockMvc.perform(post("/modifyMessage.do")
                        .param("messageID", "1")
                        .param("content", "Updated Content"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService, times(1)).update(any(Message.class));
    }

    @Test
    void testDeleteMessage() throws Exception {
        mockMvc.perform(post("/delMessage.do").param("messageID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService, times(1)).delById(1);
    }
}