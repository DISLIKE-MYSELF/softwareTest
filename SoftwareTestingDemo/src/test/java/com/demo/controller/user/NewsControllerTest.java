package com.demo.controller.user;

import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    private List<News> mockNewsList;

    @BeforeEach
    void setUp() {
        News news1 = new News();
        news1.setNewsID(1);
        news1.setTitle("News 1");
        news1.setTime(LocalDateTime.parse("2025-04-13T00:00:00"));

        News news2 = new News();
        news2.setNewsID(2);
        news2.setTitle("News 2");
        news2.setTime(LocalDateTime.parse("2025-04-12T00:00:00"));

        mockNewsList = Arrays.asList(news1, news2);
    }

    @Test
    void testNews() throws Exception {
        News mockNews = mockNewsList.get(0);
        when(newsService.findById(1)).thenReturn(mockNews);

        mockMvc.perform(get("/news").param("newsID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news"))
                .andExpect(model().attribute("news", mockNews))
                .andExpect(view().name("news"));

        verify(newsService, times(1)).findById(1);
    }

    @Test
    void testNewsListJson() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<News> mockPage = new PageImpl<>(mockNewsList, pageable, mockNewsList.size());
        when(newsService.findAll(pageable)).thenReturn(mockPage);

        mockMvc.perform(get("/news/getNewsList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("News 1"))
                .andExpect(jsonPath("$.content[1].title").value("News 2"));

        verify(newsService, times(1)).findAll(pageable);
    }

    @Test
    void testNewsListHtml() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<News> mockPage = new PageImpl<>(mockNewsList, pageable, mockNewsList.size());
        when(newsService.findAll(pageable)).thenReturn(mockPage);

        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("news_list", mockNewsList))
                .andExpect(view().name("news_list"));

        verify(newsService, times(2)).findAll(pageable); // Called twice in the controller
    }
}