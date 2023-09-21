package com.yxs.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yxs.friend.model.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author YIN
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2023-08-03 13:23:32
* @Entity generator.domain.User
*/
public interface UserMapper extends BaseMapper<User> {

    List<User> getUserInfoByTeamId(@Param("teamId") long teamId);

}




