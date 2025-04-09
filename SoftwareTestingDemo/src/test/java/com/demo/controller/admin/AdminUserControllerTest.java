package com.demo.controller.admin;

import com.demo.controller.admin.AdminUserController;
import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // 模拟数据
    private Page<User> userPage;
    private List<User> userList;
    private User sampleUser;

    @BeforeEach
    public void setup() {
        // 构造一个示例用户对象
        sampleUser = new User();
        sampleUser.setId(1);
        sampleUser.setUserID("u001");
        sampleUser.setUserName("测试用户");
        sampleUser.setPassword("123456");
        sampleUser.setEmail("test@example.com");
        sampleUser.setPhone("13800138000");
        sampleUser.setPicture("default.jpg");

        userList = new ArrayList<>();
        userList.add(sampleUser);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        userPage = new PageImpl<>(userList, pageable, userList.size());

        // 配置 userService.findByUserID(Pageable) 方法返回模拟数据
        given(userService.findByUserID(pageable)).willReturn(userPage);
        // 配置 userService.findById(id) 方法返回示例用户
        given(userService.findById(1)).willReturn(sampleUser);
        // 配置 userService.findByUserID(oldUserID) 方法（用于修改时查找原用户）
        given(userService.findByUserID("u001")).willReturn(sampleUser);
    }

    // 测试 /user_manage 页面，验证返回视图名称和模型中 total 属性
    @Test
    public void testUserManage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("admin/user_manage"));
    }

    // 测试 /user_add 页面跳转
    @Test
    public void testUserAdd() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_add"));
    }

    // 测试 /userList.do 接口，返回用户列表 JSON 数据
    @Test
    public void testUserList() throws Exception {
        int page = 1;
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("id").ascending());
        given(userService.findByUserID(pageable)).willReturn(userPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/userList.do")
                        .param("page", String.valueOf(page))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userID").value("u001"));
    }

    // 测试 /user_edit 页面，通过 id 获取用户并设置到 model 中
    @Test
    public void testUserEdit() throws Exception {
        int id = 1;
        mockMvc.perform(MockMvcRequestBuilders.get("/user_edit")
                        .param("id", String.valueOf(id)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("admin/user_edit"));
    }

    // 测试 /modifyUser.do 接口，修改用户后重定向到 user_manage 页面
    @Test
    public void testModifyUser() throws Exception {
        // 此处仅验证重定向，因为用户更新逻辑在 service 层进行
        mockMvc.perform(MockMvcRequestBuilders.post("/modifyUser.do")
                        .param("userID", "u002")
                        .param("oldUserID", "u001")
                        .param("userName", "修改后的用户")
                        .param("password", "654321")
                        .param("email", "mod@example.com")
                        .param("phone", "13900139000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));
    }

    // 测试 /addUser.do 接口，添加用户后重定向到 user_manage 页面
    @Test
    public void testAddUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/addUser.do")
                        .param("userID", "u003")
                        .param("userName", "新增用户")
                        .param("password", "111111")
                        .param("email", "add@example.com")
                        .param("phone", "13700137000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));
    }

    // 测试 /checkUserID.do 接口，检查用户ID是否存在
    @Test
    public void testCheckUserID() throws Exception {
        // 模拟 userService.countUserID 返回 1 表示该用户ID已存在
        given(userService.countUserID("u001")).willReturn(1);
        // 对于新用户ID返回 0 的情况
        given(userService.countUserID("u004")).willReturn(0);

        mockMvc.perform(MockMvcRequestBuilders.post("/checkUserID.do")
                        .param("userID", "u001"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(MockMvcRequestBuilders.post("/checkUserID.do")
                        .param("userID", "u004"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // 测试 /delUser.do 接口，删除指定用户
    @Test
    public void testDelUser() throws Exception {
        int id = 1;
        mockMvc.perform(MockMvcRequestBuilders.post("/delUser.do")
                        .param("id", String.valueOf(id)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
