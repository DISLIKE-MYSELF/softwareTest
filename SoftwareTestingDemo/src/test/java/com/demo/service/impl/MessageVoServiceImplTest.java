package com.demo.service.impl;

import com.demo.dao.MessageDao;
import com.demo.dao.UserDao;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageVoServiceImplTest {

    @InjectMocks
    private MessageVoServiceImpl messageVoService;

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReturnMessageVoByMessageID() {
        // 构造测试数据
        Message message = new Message();
        message.setMessageID(1);
        message.setUserID("u123");
        message.setContent("你好");
        message.setTime(LocalDateTime.parse("2024-01-01T12:00:00"));
        message.setState(1);

        User user = new User();
        user.setUserID("u123");
        user.setUserName("张三");
        user.setPicture("z3.jpg");

        // 模拟 Dao 返回
        when(messageDao.findByMessageID(1)).thenReturn(message);
        when(userDao.findByUserID("u123")).thenReturn(user);

        // 调用 service 方法
        MessageVo result = messageVoService.returnMessageVoByMessageID(1);

        // 断言结果
        assertNotNull(result);
        assertEquals(1, result.getMessageID());
        assertEquals("u123", result.getUserID());
        assertEquals("你好", result.getContent());
        assertEquals("张三", result.getUserName());
        assertEquals("z3.jpg", result.getPicture());
        assertEquals(1, result.getState());
    }

    @Test
    void testReturnVo() {
        // 创建一条 Message 数据
        Message message1 = new Message();
        message1.setMessageID(1);
        message1.setUserID("u123");

        Message message2 = new Message();
        message2.setMessageID(2);
        message2.setUserID("u456");

        // 准备对应的 user 和 message
        Message fullMessage1 = new Message();
        fullMessage1.setMessageID(1);
        fullMessage1.setUserID("u123");
        fullMessage1.setContent("留言1");
        fullMessage1.setTime(LocalDateTime.parse("2024-01-01T12:00:00"));
        fullMessage1.setState(1);

        Message fullMessage2 = new Message();
        fullMessage2.setMessageID(2);
        fullMessage2.setUserID("u456");
        fullMessage2.setContent("留言2");
        fullMessage2.setTime(LocalDateTime.parse("2024-01-01T12:00:00"));
        fullMessage2.setState(0);

        User user1 = new User();
        user1.setUserID("u123");
        user1.setUserName("张三");
        user1.setPicture("pic1.jpg");

        User user2 = new User();
        user2.setUserID("u456");
        user2.setUserName("李四");
        user2.setPicture("pic2.jpg");

        // 模拟 Dao 调用
        when(messageDao.findByMessageID(1)).thenReturn(fullMessage1);
        when(userDao.findByUserID("u123")).thenReturn(user1);
        when(messageDao.findByMessageID(2)).thenReturn(fullMessage2);
        when(userDao.findByUserID("u456")).thenReturn(user2);

        // 调用
        List<MessageVo> result = messageVoService.returnVo(Arrays.asList(message1, message2));

        // 验证结果
        assertEquals(2, result.size());

        assertEquals("留言1", result.get(0).getContent());
        assertEquals("张三", result.get(0).getUserName());

        assertEquals("留言2", result.get(1).getContent());
        assertEquals("李四", result.get(1).getUserName());
    }
}
