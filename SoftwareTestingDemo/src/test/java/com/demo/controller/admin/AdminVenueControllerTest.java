package com.demo.controller.admin;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.BDDMockito.given;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminVenueController.class)
public class AdminVenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    private Venue sampleVenue;

    @BeforeEach
    public void setup() {
        sampleVenue = new Venue();
        sampleVenue.setVenueID(1);
        sampleVenue.setVenueName("测试场馆");
        sampleVenue.setAddress("测试地址");
        sampleVenue.setDescription("测试描述");
        sampleVenue.setPrice(100);
        sampleVenue.setOpen_time("08:00");
        sampleVenue.setClose_time("18:00");
        sampleVenue.setPicture("test.jpg");

        Page<Venue> venuePage = new PageImpl<>(Collections.singletonList(sampleVenue));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());

        given(venueService.findAll(pageable)).willReturn(venuePage);
        given(venueService.findByVenueID(1)).willReturn(sampleVenue);
        given(venueService.countVenueName("测试场馆")).willReturn(1);
        given(venueService.countVenueName("新场馆")).willReturn(0);
    }

    @Test
    public void testVenueManage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("admin/venue_manage"));
    }

    @Test
    public void testVenueAdd() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/venue_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_add"));
    }

    @Test
    public void testEditVenue() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/venue_edit")
                        .param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("venue"))
                .andExpect(view().name("/admin/venue_edit"));
    }

    @Test
    public void testVenueList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/venueList.do")
                        .param("page", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].venueName").value("测试场馆"));
    }

    @Test
    public void testCheckVenueName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/checkVenueName.do")
                        .param("venueName", "测试场馆"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(MockMvcRequestBuilders.post("/checkVenueName.do")
                        .param("venueName", "新场馆"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testDelVenue() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/delVenue.do")
                        .param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // 以下两个接口涉及文件上传与 response 重定向，不便断言结果内容，仅验证 status
    @Test
    public void testAddVenue() throws Exception {
        MockMultipartFile file = new MockMultipartFile("picture", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        Mockito.when(venueService.create(Mockito.any())).thenReturn(1);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/addVenue.do")
                        .file(file)
                        .param("venueName", "新场馆")
                        .param("address", "地址")
                        .param("description", "描述")
                        .param("price", "200")
                        .param("open_time", "09:00")
                        .param("close_time", "17:00"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testModifyVenue() throws Exception {
        MockMultipartFile file = new MockMultipartFile("picture", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/modifyVenue.do")
                        .file(file)
                        .param("venueID", "1")
                        .param("venueName", "更新场馆")
                        .param("address", "更新地址")
                        .param("description", "更新描述")
                        .param("price", "150")
                        .param("open_time", "10:00")
                        .param("close_time", "16:00"))
                .andExpect(status().is3xxRedirection());
    }
}
