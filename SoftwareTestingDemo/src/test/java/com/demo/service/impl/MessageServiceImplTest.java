package com.demo.service.impl;

import com.demo.dao.MessageDao;
import com.demo.entity.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageServiceImplTest {

    @InjectMocks
    private MessageServiceImpl messageService;

    @Mock
    private MessageDao messageDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() {
        Message message = new Message();
        message.setMessageID(1);
        when(messageDao.getOne(1)).thenReturn(message);

        Message result = messageService.findById(1);
        assertNotNull(result);
        assertEquals(1, result.getMessageID());
    }

    @Test
    void testFindByUser() {
        Page<Message> page = new PageImpl<>(Collections.singletonList(new Message()));
        when(messageDao.findAllByUserID(eq("user1"), any(PageRequest.class))).thenReturn(page);

        Page<Message> result = messageService.findByUser("user1", PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testCreate() {
        Message message = new Message();
        message.setMessageID(100);
        when(messageDao.save(any(Message.class))).thenReturn(message);

        int id = messageService.create(message);
        assertEquals(100, id);
    }

    @Test
    void testDelById() {
        messageService.delById(10);
        verify(messageDao, times(1)).deleteById(10);
    }

    @Test
    void testUpdate() {
        Message message = new Message();
        messageService.update(message);
        verify(messageDao).save(message);
    }

    @Test
    void testConfirmMessage() {
        Message message = new Message();
        message.setMessageID(1);
        when(messageDao.findByMessageID(1)).thenReturn(message);

        messageService.confirmMessage(1);
        verify(messageDao).updateState(eq(MessageServiceImpl.STATE_PASS), eq(1));
    }

    @Test
    void testConfirmMessageNotFound() {
        when(messageDao.findByMessageID(1)).thenReturn(null);
        Exception exception = assertThrows(RuntimeException.class, () -> messageService.confirmMessage(1));
        assertEquals("留言不存在", exception.getMessage());
    }

    @Test
    void testRejectMessage() {
        Message message = new Message();
        message.setMessageID(2);
        when(messageDao.findByMessageID(2)).thenReturn(message);

        messageService.rejectMessage(2);
        verify(messageDao).updateState(eq(MessageServiceImpl.STATE_REJECT), eq(2));
    }

    @Test
    void testRejectMessageNotFound() {
        when(messageDao.findByMessageID(2)).thenReturn(null);
        Exception exception = assertThrows(RuntimeException.class, () -> messageService.rejectMessage(2));
        assertEquals("留言不存在", exception.getMessage());
    }

    @Test
    void testFindWaitState() {
        Page<Message> page = new PageImpl<>(Collections.singletonList(new Message()));
        when(messageDao.findAllByState(eq(MessageServiceImpl.STATE_NO_AUDIT), any(PageRequest.class))).thenReturn(page);

        Page<Message> result = messageService.findWaitState(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindPassState() {
        Page<Message> page = new PageImpl<>(Collections.singletonList(new Message()));
        when(messageDao.findAllByState(eq(MessageServiceImpl.STATE_PASS), any(PageRequest.class))).thenReturn(page);

        Page<Message> result = messageService.findPassState(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }
}
