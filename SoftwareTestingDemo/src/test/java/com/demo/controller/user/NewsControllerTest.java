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
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
public class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    private News sampleNews;

    @BeforeEach
    public void setup() {
        sampleNews = new News();
        sampleNews.setNewsID(1);
        sampleNews.setTitle("Test News");
        sampleNews.setTime(LocalDateTime.now());
    }

    @Test
    public void testGetNewsDetail() throws Exception {
        when(newsService.findById(1)).thenReturn(sampleNews);

        mockMvc.perform(get("/news").param("newsID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news"))
                .andExpect(view().name("news"));
    }

    @Test
    public void testGetNewsListJson() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        List<News> newsList = Arrays.asList(sampleNews);
        Page<News> page = new PageImpl<>(newsList, pageable, 1);
        when(newsService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/news/getNewsList").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    public void testGetNewsListPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        List<News> newsList = Arrays.asList(sampleNews);
        Page<News> page = new PageImpl<>(newsList, pageable, 1);
        when(newsService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("news_list"));
    }
}
