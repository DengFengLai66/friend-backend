package com.yxs.friend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxs.friend.mapper.UserTeamMapper;
import com.yxs.friend.model.domain.UserTeam;
import com.yxs.friend.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author YIN
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-08-06 18:24:00
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




