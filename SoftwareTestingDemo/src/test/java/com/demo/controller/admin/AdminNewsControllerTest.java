package com.demo.controller.admin;

import com.demo.controller.admin.AdminNewsController;
import com.demo.entity.News;
import com.demo.service.NewsService;
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

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminNewsController.class)
public class AdminNewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    // 模拟数据
    private Page<News> newsPage;
    private News sampleNews;

    @BeforeEach
    public void setup() {
        // 构造一个示例新闻对象
        sampleNews = new News();
        sampleNews.setNewsID(1);
        sampleNews.setTitle("测试新闻");
        sampleNews.setContent("测试内容");
        sampleNews.setTime(LocalDateTime.now());

        List<News> newsList = new ArrayList<>();
        newsList.add(sampleNews);

        Pageable pageableAsc = PageRequest.of(0, 10, Sort.by("time").ascending());
        newsPage = new PageImpl<>(newsList, pageableAsc, newsList.size());

        // 配置 newsService.findAll(...) 方法在调用时返回模拟数据
        given(newsService.findAll(pageableAsc)).willReturn(newsPage);
    }

    // 测试 /news_manage 页面，验证返回视图名称及模型中 total 属性设置
    @Test
    public void testNewsManage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/news_manage"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("admin/news_manage"));
    }

    // 测试 /news_add 页面跳转
    @Test
    public void testNewsAdd() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/news_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_add"));
    }

    // 测试 /news_edit 页面，通过newsID获取具体新闻并设置到model中
    @Test
    public void testNewsEdit() throws Exception {
        int newsID = 1;
        // 模拟 newsService.findById() 方法返回 sampleNews
        given(newsService.findById(newsID)).willReturn(sampleNews);

        mockMvc.perform(MockMvcRequestBuilders.get("/news_edit")
                        .param("newsID", String.valueOf(newsID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news"))
                .andExpect(view().name("/admin/news_edit"));
    }

    // 测试 /newsList.do 接口，返回新闻列表JSON数据
    @Test
    public void testNewsList() throws Exception {
        int page = 1;
        Pageable pageableDesc = PageRequest.of(page - 1, 10, Sort.by("time").descending());
        // 为分页接口专门准备模拟数据
        given(newsService.findAll(pageableDesc)).willReturn(newsPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/newsList.do")
                        .param("page", String.valueOf(page))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // 判断返回的 JSON 数组中是否存在对象
                .andExpect(jsonPath("$[0]").exists());
    }

    // 测试 /delNews.do 接口，删除指定新闻
    @Test
    public void testDelNews() throws Exception {
        int newsID = 1;
        // 此处可以额外添加对 newsService.delById(newsID) 的模拟（若需要验证方法是否调用）
        mockMvc.perform(MockMvcRequestBuilders.post("/delNews.do")
                        .param("newsID", String.valueOf(newsID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // 测试 /modifyNews.do 接口，修改新闻后重定向到 news_manage
    @Test
    public void testModifyNews() throws Exception {
        int newsID = 1;
        String newTitle = "修改后的标题";
        String newContent = "修改后的内容";

        // 模拟查询新闻对象
        given(newsService.findById(newsID)).willReturn(sampleNews);
        // 模拟 update 方法调用（无需返回值）

        mockMvc.perform(MockMvcRequestBuilders.post("/modifyNews.do")
                        .param("newsID", String.valueOf(newsID))
                        .param("title", newTitle)
                        .param("content", newContent))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));
    }

    // 测试 /addNews.do 接口，添加新闻后重定向到 news_manage
    @Test
    public void testAddNews() throws Exception {
        String title = "新增新闻标题";
        String content = "新增新闻内容";

        // 模拟 create 方法调用（无需返回值）
        mockMvc.perform(MockMvcRequestBuilders.post("/addNews.do")
                        .param("title", title)
                        .param("content", content))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));
    }
}
