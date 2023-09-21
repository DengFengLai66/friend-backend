package com.yxs.friend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxs.friend.model.domain.Team;
import com.yxs.friend.model.domain.User;
import com.yxs.friend.model.dto.TeamQuery;
import com.yxs.friend.model.request.TeamJoinRequest;
import com.yxs.friend.model.request.TeamQuitRequest;
import com.yxs.friend.model.request.TeamUpdateRequest;
import com.yxs.friend.model.vo.TeamUserVO;

import java.util.List;

/**
* @author YIN
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-08-06 18:22:35
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team , User loginUser);

    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest team, User loginUser);

    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(long id, User loginUser);

    List<User> listJoinUsers(Long id);
}
