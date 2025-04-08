package com.demo.service.impl;

import com.demo.dao.NewsDao;
import com.demo.entity.News;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NewsServiceImplTest {

    @InjectMocks
    private NewsServiceImpl newsService;

    @Mock
    private NewsDao newsDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        News news1 = new News();
        news1.setNewsID(1);
        news1.setTitle("新闻一");

        News news2 = new News();
        news2.setNewsID(2);
        news2.setTitle("新闻二");

        List<News> newsList = Arrays.asList(news1, news2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<News> mockPage = new PageImpl<>(newsList, pageable, newsList.size());

        when(newsDao.findAll(pageable)).thenReturn(mockPage);

        Page<News> result = newsService.findAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("新闻一", result.getContent().get(0).getTitle());
    }

    @Test
    void testFindById() {
        News news = new News();
        news.setNewsID(1);
        news.setTitle("标题");

        when(newsDao.getOne(1)).thenReturn(news);

        News result = newsService.findById(1);
        assertNotNull(result);
        assertEquals("标题", result.getTitle());
    }

    @Test
    void testCreate() {
        News news = new News();
        news.setNewsID(1);
        news.setTitle("新增新闻");

        when(newsDao.save(news)).thenReturn(news);

        int result = newsService.create(news);
        assertEquals(1, result);
    }

    @Test
    void testDelById() {
        doNothing().when(newsDao).deleteById(1);
        newsService.delById(1);
        verify(newsDao, times(1)).deleteById(1);
    }

    @Test
    void testUpdate() {
        News news = new News();
        news.setNewsID(1);
        news.setTitle("更新标题");

        when(newsDao.save(news)).thenReturn(news);
        newsService.update(news);
        verify(newsDao, times(1)).save(news);
    }
}
