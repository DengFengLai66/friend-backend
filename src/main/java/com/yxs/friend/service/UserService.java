package com.yxs.friend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yxs.friend.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author YIN
* @description 针对表【user】的数据库操作Service
* @createDate 2023-05-14 13:54:23
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getsafatyUser(User originUser);

    /**
     * 根据用户标签搜索用户
     * @param tagNameList
     * @return
     */
    List<User> searchUserByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 判断是不是管理员
     * @param request
     * @return
     */
     boolean isAdmin(HttpServletRequest request);

    /**
     * 判断是不是管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    List<User> matchUsers(long num, User user);

    Page<User> getcommend(long pageSize, long pageNum, User loginUser);

    //IPage<User> getMatchedUsersPage(long pageNum, long pageSize, User user);
}
