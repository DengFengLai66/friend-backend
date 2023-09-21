package com.yxs.friend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yxs.friend.common.BaseResponse;
import com.yxs.friend.common.ErrorCode;
import com.yxs.friend.common.ResultUtil;
import com.yxs.friend.exception.BusinessException;
import com.yxs.friend.model.domain.User;
import com.yxs.friend.model.request.UserLoginRequest;
import com.yxs.friend.model.request.UserRegisterRequest;
import com.yxs.friend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.yxs.friend.contant.UserContant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
//@CrossOrigin(originPatterns = {"http://localhost:5173"},allowCredentials = "true")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            //return ResultUtil.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
//        String planetCode = userRegisterRequest.getPlanetCode();
//        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
        long userRegister = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtil.success(userRegister);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
         if (userLoginRequest == null) {
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtil.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        int userLogout = userService.userLogout(request);
        return ResultUtil.success(userLogout);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        //TODO校验是否合法
        User user = userService.getById(userId);
        User getsafatyUser = userService.getsafatyUser(user);
        return ResultUtil.success(getsafatyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String userName, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userName)) {
            queryWrapper.like("userName", userName);
        }
        //return userService.list(queryWrapper);
        List<User> userList = userService.list(queryWrapper);
        List<User> userList1 = userList.stream().map(user -> userService.getsafatyUser(user)).collect(Collectors.toList());
        return ResultUtil.success(userList1);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> remmendUsers(long pageSize,long pageNum ,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<User> userPage = userService.getcommend(pageSize, pageNum, loginUser);
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        Page<User> userList = userService.page(new Page<>(pageNum, pageSize),queryWrapper);
        return ResultUtil.success(userPage);
    }

   @GetMapping("/search/tags")
   public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtil.success(userList);
    }

    @GetMapping("/updateTags")
    public BaseResponse<Integer> updateTags(@RequestParam Long id, @RequestParam(required = false) List<String> tagNameList, HttpServletRequest request) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择至少一个标签");
        }
        Gson gson = new Gson();
        String tags = gson.toJson(tagNameList);

        User loginUser = userService.getLoginUser(request);
        User user = new User();
        user.setTags(tags);
        user.setId(id);
        int i = userService.updateUser(user, loginUser);
        return ResultUtil.success(i);
    }


    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtil.success(result);
    }

    @PostMapping("/delete")
   public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean remove = userService.removeById(id);
        return ResultUtil.success(remove);
    }

    /**
     * 获取最匹配的用户
     *
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long pageNum, HttpServletRequest request) {
        if (pageNum <= 0 || pageNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        List<User> users = userService.matchUsers(pageNum, user);
        return ResultUtil.success(users);
    }

}
