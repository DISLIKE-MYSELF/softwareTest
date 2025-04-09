package com.demo.controller.user;

import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private MultipartFile multipartFile;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        mockUser = new User();
        mockUser.setUserID("testUser");
        mockUser.setPassword("123456");
        mockUser.setIsadmin(0);
    }

    @Test
    void testLogin_User() throws IOException {
        when(userService.checkLogin("testUser", "123456")).thenReturn(mockUser);

        String result = userController.login("testUser", "123456", request);
        assertEquals("/index", result);
        assertNotNull(request.getSession().getAttribute("user"));
    }

    @Test
    void testLogin_Admin() throws IOException {
        mockUser.setIsadmin(1);
        when(userService.checkLogin("admin", "admin123")).thenReturn(mockUser);

        String result = userController.login("admin", "admin123", request);
        assertEquals("/admin_index", result);
        assertNotNull(request.getSession().getAttribute("admin"));
    }

    @Test
    void testLogin_Fail() throws IOException {
        when(userService.checkLogin("fail", "fail")).thenReturn(null);

        String result = userController.login("fail", "fail", request);
        assertEquals("false", result);
    }

    @Test
    void testRegister() throws IOException {
        doNothing().when(userService).create(any(User.class));
        userController.register("u1", "name", "pass", "mail", "123", response);
        assertEquals("login", response.getRedirectedUrl());
    }

    @Test
    void testLogout() throws IOException {
        request.getSession().setAttribute("user", mockUser);
        userController.logout(request, response);
        assertNull(request.getSession().getAttribute("user"));
        assertEquals("/index", response.getRedirectedUrl());
    }

    @Test
    void testCheckPassword() {
        when(userService.findByUserID("testUser")).thenReturn(mockUser);
        assertTrue(userController.checkPassword("testUser", "123456"));
        assertFalse(userController.checkPassword("testUser", "wrong"));
    }

    @Test
    void testUpdateUser() throws Exception {
        when(userService.findByUserID("testUser")).thenReturn(mockUser);
        when(multipartFile.getOriginalFilename()).thenReturn("");
        doNothing().when(userService).updateUser(any(User.class));

        userController.updateUser("newName", "testUser", "newPass", "email", "123", multipartFile, request, response);
        assertEquals("user_info", response.getRedirectedUrl());
    }
}
