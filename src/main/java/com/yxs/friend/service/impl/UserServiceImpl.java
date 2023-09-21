package com.yxs.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.yxs.friend.common.ErrorCode;
import com.yxs.friend.exception.BusinessException;
import com.yxs.friend.mapper.UserMapper;
import com.yxs.friend.model.domain.User;
import com.yxs.friend.service.UserService;
import com.yxs.friend.utiles.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yxs.friend.contant.UserContant.ADMIN_ROLE;
import static com.yxs.friend.contant.UserContant.USER_LOGIN_STATE;


/**
 * @author YIN
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2023-05-14 13:54:23
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    /**
     * 盐值，混淆密码来给密码加密
     */
    private static final String SALT = "yxs";


    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return 用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        //1.校验
        //1.1用户名密码不能位空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名密码不能为空");
        }

        //1.2账号长度大于4
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度大于4");
        }

        //1.3账号密码长度大于6
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度需要大于8");
        }

        //1.4验证码长度不能大于5
//        if (planetCode.length()>5) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码长度不能大于5");
//        }


        // 1.4账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户不能包含特殊字符");
        }

        //1.5两次密码要相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码要相同");
        }

        //1.6账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能重复");
        }

//        //1.6验证码不能重复
//        queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("planetCode", planetCode);
//        count = userMapper.selectCount(queryWrapper);
//        if (count > 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码不能重复");
//        }

        //2.加密
        final String SALT = "yxs";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //3.上传
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
//        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();

    }

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        //1.1用户名密码不能位空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        //1.2账号长度大于4
        if (userAccount.length() < 4) {
            return null;
        }
        //1.3账号密码长度大于6
        if (userPassword.length() < 8) {
            return null;
        }
        // 1.4账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户不存在");
        }
        //3.脱敏
        User safetyUser = getsafatyUser(user);
        //4.记录登录日志
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 脱敏方法
     *
     * @param originUser
     * @return
     */
    @Override
    public User getsafatyUser(User originUser) {
        if (originUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUpdateTime(originUser.getCreateTime());
        safetyUser.setProfile(originUser.getProfile());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    public List<User> searchUserByTags(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //方法1：用SQL实现
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList){
            userQueryWrapper = userQueryWrapper.like("tags",tagName);
        }
        List<User> users = userMapper.selectList(userQueryWrapper);
        return users.stream().map(this::getsafatyUser).collect(Collectors.toList());
        //方法2：用内存实现
        //1.先取出所有用户
//        QueryWrapper<User>  userQueryWrapper = new QueryWrapper<>();
//        List<User> users = userMapper.selectList(userQueryWrapper);
//        Gson gson = new Gson();
        //2.遍历用户
  //      for (User user : users){
//            //3.取出所有用户标签
//            String userTags = user.getTags();
//            //4.序列化
//            Set<User> userSet = gson.fromJson(userTags, new TypeToken<Set<String>>(){}.getType());
//            //5.对比
//            for (String tags:tagNameList){
//                if (!userSet.contains(tags)){
//                    return false;
//                }
//            }
//            return true;
 //       }
        //2.遍历用户
//        return users.stream().filter(user -> {
//            //3.取出所有用户标签
//            String userTags = user.getTags();
//            //4.序列化
//            Set<User> userSet = gson.fromJson(userTags, new TypeToken<Set<String>>(){}.getType());
//            //5.对比
//            for (String tags:tagNameList){
//                if (!userSet.contains(tags)){
//                    return false;
//                }
//            }
//            return true;
//        }).map(this::getsafatyUser).collect(Collectors.toList());

    }

    /**
     * 修改用户信息
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 补充校验，如果用户没有传任何要更新的值，就直接报错，不用执行 update 语句
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int result = userMapper.updateById(user);
        return result;
    }

    /**
     * 获取登录用户信息
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        if (userObj == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN);
//        }
//        return (User) userObj;
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObj;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
            // 仅管理员可查询
            Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
            User user = (User) userObj;
            return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User user) {
        // 仅管理员可查询
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 匹配推荐用户 （根据标签）
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        //queryWrapper.ne("id",loginUser.getId());
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags)) {
                continue;
            }
            if (user.getId() == loginUser.getId() || user.getId().equals(loginUser.getId())){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getsafatyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

//    @Override
//    public IPage<User> getMatchedUsersPage(long pageNum, long pageSize, User loginUser) {
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>()
//                .select("id", "tags")
//                .ne("tags", "[]")
//                .isNotNull("tags");
//
//        Gson gson = new Gson();
//        // 计算所有用户的相似度，并将用户及其相似度保存在一个 Map 中
//        String loginUserTags = loginUser.getTags();
//        List<String> tagList = gson.fromJson(loginUserTags, new TypeToken<List<String>>() {
//        }.getType());
//
//        HashMap<User, Integer> userScoreMap = new HashMap<>();
//        this.list(userQueryWrapper).stream()
//                .filter(user -> !user.getId().equals(loginUser.getId())) // 排除当前登录用户
//                .forEach(user -> userScoreMap.put(user,
//                        AlgorithmUtils.minDistance(gson.fromJson(user.getTags(), new TypeToken<List<String>>() {}.getType())
//                                , tagList)));
//
//        // 对 Map 中的用户进行排序
//        List<User> sortedUserList = sortMap(userScoreMap);
//        List<Long> validUserIdData = sortedUserList.stream().map(User::getId).collect(Collectors.toList());
//
//        // 构造分页参数，从数据库中查询指定范围的数据，并保持顺序
//        Page<User> page = new Page<>(pageNum, pageSize);
//        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
//                .in("id", validUserIdData)
//                .orderByAsc("FIELD(id, " + StringUtils.join(validUserIdData, ",") + ")");
//        IPage<User> userPage = this.page(page, queryWrapper);
//
//        // 将查询出来的数据进行安全处理，并返回分页结果
//        return userPage.convert(this::getsafatyUser);
//    }

    /**
     * map 排序
     *
     * @param map 需要排序的map 集合
     * @return 排好序的list
     */
    public static List<User> sortMap(Map<User, Integer> map) {
        //利用Map的entrySet方法，转化为list进行排序
        List<Map.Entry<User, Integer>> entryList = new ArrayList<>(map.entrySet());
        //利用Collections的sort方法对list排序
        entryList.sort(Comparator.comparingInt(Map.Entry::getValue));

        List<User> userList = new ArrayList<>();
        for (Map.Entry<User, Integer> e : entryList) {
            userList.add(e.getKey());
        }
        return userList;
    }


    @Override
    public Page<User> getcommend(long pageSize, long pageNum, User loginUser) {
        //先查缓存
        String redisKey = String.format("friend:user:recommend:%s", loginUser.getId());
        ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
        Page<User> userPage =(Page<User>) valueOperations.get(redisKey);
        if (userPage != null){
            return userPage;
        }
        //不存在查询数据库
        QueryWrapper<User> userQueryWrapper =new QueryWrapper<>();
        userQueryWrapper.ne("Id",loginUser.getId());
        userPage = this.page(new Page<>(pageNum, pageSize), userQueryWrapper);
        //写入缓存
        try {
            valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return userPage;
    }


}
