package com.demo.controller.admin;

import com.demo.controller.admin.AdminVenueController;
import com.demo.entity.Venue;
import com.demo.service.VenueService;
import com.demo.utils.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminVenueController.class)
public class AdminVenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    // 模拟数据
    private List<Venue> venueList;
    private Page<Venue> venuePage;
    private Venue sampleVenue;

    @BeforeEach
    public void setup() {
        // 构造一个示例场馆对象
        sampleVenue = new Venue();
        sampleVenue.setVenueID(1);
        sampleVenue.setVenueName("测试场馆");
        sampleVenue.setAddress("测试地址");
        sampleVenue.setDescription("测试描述");
        sampleVenue.setPrice(100);
        sampleVenue.setOpen_time("08:00");
        sampleVenue.setClose_time("22:00");
        sampleVenue.setPicture("default.jpg");

        venueList = new ArrayList<>();
        venueList.add(sampleVenue);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());
        venuePage = new PageImpl<>(venueList, pageable, venueList.size());

        // 模拟 venueService.findAll 返回分页数据
        given(venueService.findAll(any(Pageable.class))).willReturn(venuePage);
        // 模拟 venueService.findByVenueID 返回示例场馆
        given(venueService.findByVenueID(anyInt())).willReturn(sampleVenue);
    }

    // 测试 /venue_manage 页面，验证视图名称和模型中 total 属性
    @Test
    public void testVenueManage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("admin/venue_manage"));
    }

    // 测试 /venue_edit 页面，根据 venueID 获取场馆信息
    @Test
    public void testVenueEdit() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/venue_edit")
                        .param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("venue"))
                .andExpect(view().name("/admin/venue_edit"));
    }

    // 测试 /venue_add 页面跳转
    @Test
    public void testVenueAdd() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/venue_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_add"));
    }

    // 测试 /venueList.do 接口，返回场馆列表 JSON 数据
    @Test
    public void testGetVenueList() throws Exception {
        int page = 1;
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("venueID").ascending());
        given(venueService.findAll(pageable)).willReturn(venuePage);

        mockMvc.perform(MockMvcRequestBuilders.get("/venueList.do")
                        .param("page", String.valueOf(page))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].venueName").value("测试场馆"));
    }

    // 测试 /addVenue.do 接口：添加场馆后根据返回重定向判断
    @Test
    public void testAddVenue() throws Exception {
        // 模拟场馆创建返回正数ID表示成功
        given(venueService.create(any(Venue.class))).willReturn(1);

        // 模拟文件上传：测试场景1-文件名非空
        MockMultipartFile file = new MockMultipartFile("picture", "venue.jpg",
                MediaType.IMAGE_JPEG_VALUE, "fakeImageContent".getBytes());

        // 利用 Mockito.mockStatic 模拟 FileUtil.saveVenueFile 静态方法返回值
        try (MockedStatic<FileUtil> fileUtilMock = Mockito.mockStatic(FileUtil.class)) {
            fileUtilMock.when(() -> FileUtil.saveVenueFile(any())).thenReturn("savedVenue.jpg");

            mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                            .file(file)
                            .param("venueName", "新场馆")
                            .param("address", "新地址")
                            .param("description", "新描述")
                            .param("price", "200")
                            .param("open_time", "09:00")
                            .param("close_time", "21:00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("venue_manage"));
        }

        // 模拟文件上传：测试场景2-文件为空时
        MockMultipartFile emptyFile = new MockMultipartFile("picture", "",
                MediaType.IMAGE_JPEG_VALUE, new byte[0]);
        try (MockedStatic<FileUtil> fileUtilMock = Mockito.mockStatic(FileUtil.class)) {
            // 此处不期望调用 FileUtil.saveVenueFile，因为文件名为空
            fileUtilMock.when(() -> FileUtil.saveVenueFile(any())).thenReturn("");

            mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                            .file(emptyFile)
                            .param("venueName", "新场馆2")
                            .param("address", "新地址2")
                            .param("description", "新描述2")
                            .param("price", "300")
                            .param("open_time", "10:00")
                            .param("close_time", "20:00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("venue_manage"));
        }
    }

    // 测试 /modifyVenue.do 接口：修改场馆信息后重定向
    @Test
    public void testModifyVenue() throws Exception {
        // 模拟修改时采用非空文件更新图片
        MockMultipartFile file = new MockMultipartFile("picture", "update.jpg",
                MediaType.IMAGE_JPEG_VALUE, "updateImageContent".getBytes());

        try (MockedStatic<FileUtil> fileUtilMock = Mockito.mockStatic(FileUtil.class)) {
            fileUtilMock.when(() -> FileUtil.saveVenueFile(any())).thenReturn("updatedVenue.jpg");

            mockMvc.perform(MockMvcRequestBuilders.multipart("/modifyVenue.do")
                            .file(file)
                            .param("venueID", "1")
                            .param("venueName", "修改场馆")
                            .param("address", "修改地址")
                            .param("description", "修改描述")
                            .param("price", "150")
                            .param("open_time", "08:30")
                            .param("close_time", "22:30"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("venue_manage"));
        }
    }

    // 测试 /delVenue.do 接口：删除场馆操作
    @Test
    public void testDelVenue() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/delVenue.do")
                        .param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // 测试 /checkVenueName.do 接口：检查场馆名称是否唯一
    @Test
    public void testCheckVenueName() throws Exception {
        // 模拟名称已存在（返回数量大于或等于1）
        given(venueService.countVenueName("测试场馆")).willReturn(1);
        // 模拟名称不存在（返回0）
        given(venueService.countVenueName("新场馆")).willReturn(0);

        mockMvc.perform(MockMvcRequestBuilders.post("/checkVenueName.do")
                        .param("venueName", "测试场馆"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(MockMvcRequestBuilders.post("/checkVenueName.do")
                        .param("venueName", "新场馆"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
