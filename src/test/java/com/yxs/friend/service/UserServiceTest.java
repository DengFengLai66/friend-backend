//package com.yxs.friend.service;
//
//import com.yxs.friend.model.domain.User;
//import org.junit.Assert;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import javax.annotation.Resource;
//import java.util.Arrays;
//import java.util.List;
//
//@SpringBootTest
//public class UserServiceTest {
//
//    @Resource
//    UserService userService;
//
//    @Test
//    public void testAddUser(){
//        User user = new User();
//        user.setUsername("yxs");
//        user.setUserAccount("123");
//        user.setUserPassword("123");
//        user.setAvatarUrl("http://gitlab.code-nav.cn/uploads/-/system/user/avatar/1/avatar.png?width=40");
//        user.setGender(0);
//        user.setPhone("123");
//        user.setEmail("123");
//
//        boolean result = userService.save(user);
//        System.out.println(user.getId());
//        Assertions.assertTrue(result);
//
//    }
//
//    @Test
//    void userRegister() {
//        String userAccount = "yxs6";
//        String userPassword = "";
//        String checkPassword = "123456";
//        String planetCode = "1";
//        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//
//        userAccount = "yxs";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//
//        userAccount = "yxs6";
//        userPassword = "123456";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//
//        userAccount = "yxs 6666";
//        userPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//
//        checkPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//
//        userAccount = "yxs668";
//        checkPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//
//        userAccount = "yxs6";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertTrue(result > 0);
//    }
//
//    @Test
//    public void searchUserByTags() {
//        List<String> tagNameList = Arrays.asList("Java","python");
//        List<User> users = userService.searchUserByTags(tagNameList);
//        Assert.assertNotNull(users);
//
//    }
//}