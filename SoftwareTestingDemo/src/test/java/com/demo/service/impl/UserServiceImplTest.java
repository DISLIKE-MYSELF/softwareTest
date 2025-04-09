package com.demo.service.impl;

import com.demo.dao.UserDao;
import com.demo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByUserID() {
        User user = new User();
        user.setUserID("user1");
        when(userDao.findByUserID("user1")).thenReturn(user);

        User result = userService.findByUserID("user1");
        assertNotNull(result);
        assertEquals("user1", result.getUserID());
    }

    @Test
    void testFindById() {
        User user = new User();
        user.setId(1);
        when(userDao.findById(1)).thenReturn(user);

        User result = userService.findById(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testFindByUserIDWithPagination() {
        Page<User> page = new PageImpl<>(Collections.singletonList(new User()));
        when(userDao.findAllByIsadmin(0, PageRequest.of(0, 10))).thenReturn(page);

        Page<User> result = userService.findByUserID(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testCheckLogin() {
        User user = new User();
        user.setUserID("user1");
        user.setPassword("password");
        when(userDao.findByUserIDAndPassword("user1", "password")).thenReturn(user);

        User result = userService.checkLogin("user1", "password");
        assertNotNull(result);
        assertEquals("user1", result.getUserID());
    }

    @Test
    void test_CheckLogin() {
        int id = 1;
        String userID = "test";
        String password = "123";
        String email = "222@qq.com";
        String phone = "12345678901";
        int isadmin = 0;
        String user_name = "nickname";
        String picture = "picture";
        User user = new User(id, userID, user_name, password, email, phone, isadmin, picture);

        when(userDao.findByUserIDAndPassword(userID, password)).thenReturn(user);
        User res = userService.checkLogin(userID, password + "1");
        assertNotEquals(null, res);
        assertAll("test find by userID",
                () -> assertEquals(userID, res.getUserID()),
                () -> assertEquals(password, res.getPassword()));
        verify(userDao).findByUserIDAndPassword(userID, password);
    }

    @Test
    void testCreate() {
        User user = new User();
        when(userDao.findAll()).thenReturn(Collections.singletonList(user));

        int result = userService.create(user);
        verify(userDao).save(user);
        assertEquals(1, result);
    }

    @Test
    void testDelByID() {
        userService.delByID(1);
        verify(userDao, times(1)).deleteById(1);
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        userService.updateUser(user);
        verify(userDao).save(user);
    }

    @Test
    void testCountUserID() {
        when(userDao.countByUserID("user1")).thenReturn(1);

        int result = userService.countUserID("user1");
        assertEquals(1, result);
    }
}