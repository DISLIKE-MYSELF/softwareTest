package com.demo.controller;

import com.demo.entity.Message;
import com.demo.entity.News;
import com.demo.entity.Venue;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import com.demo.service.NewsService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class IndexControllerTest {

    @InjectMocks
    private IndexController indexController;

    @Mock
    private NewsService newsService;

    @Mock
    private VenueService venueService;

    @Mock
    private MessageVoService messageVoService;

    @Mock
    private MessageService messageService;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIndexPage() {
        // 模拟数据
        List<Venue> venueList = Arrays.asList(new Venue(), new Venue());
        List<News> newsList = Arrays.asList(new News(), new News());
        List<Message> messageRawList = Arrays.asList(new Message(), new Message());
        List<MessageVo> messageVoList = Arrays.asList(new MessageVo(), new MessageVo());

        Page<Venue> venuePage = new PageImpl<>(venueList);
        Page<News> newsPage = new PageImpl<>(newsList);
        Page<Message> messagePage = new PageImpl<>(messageRawList);

        // 设置mock返回
        when(venueService.findAll(any(Pageable.class))).thenReturn(venuePage);
        when(newsService.findAll(any(Pageable.class))).thenReturn(newsPage);
        when(messageService.findPassState(any(Pageable.class))).thenReturn(messagePage);
        when(messageVoService.returnVo(messageRawList)).thenReturn(messageVoList);

        // 执行测试
        String result = indexController.index(model);

        // 验证模型添加
        verify(model).addAttribute("venue_list", venueList);
        verify(model).addAttribute("news_list", newsList);
        verify(model).addAttribute("message_list", messageVoList);
        verify(model).addAttribute("user", null);
        assertEquals("index", result);
    }

    @Test
    void testAdminIndexPage() {
        String result = indexController.admin_index(model);
        assertEquals("admin/admin_index", result);
    }
}
