package com.yxs.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxs.friend.common.ErrorCode;
import com.yxs.friend.exception.BusinessException;
import com.yxs.friend.mapper.TeamMapper;
import com.yxs.friend.mapper.UserMapper;
import com.yxs.friend.model.domain.Team;
import com.yxs.friend.model.domain.User;
import com.yxs.friend.model.domain.UserTeam;
import com.yxs.friend.model.dto.TeamQuery;
import com.yxs.friend.model.enums.TeamStatusEnum;
import com.yxs.friend.model.request.TeamJoinRequest;
import com.yxs.friend.model.request.TeamQuitRequest;
import com.yxs.friend.model.request.TeamUpdateRequest;
import com.yxs.friend.model.vo.TeamUserVO;
import com.yxs.friend.model.vo.UserVO;
import com.yxs.friend.service.TeamService;
import com.yxs.friend.service.UserService;
import com.yxs.friend.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author YIN
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-08-06 18:22:35
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (team.getExpireTime() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(team.getExpireTime());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            team.setExpireTime(calendar.getTime());
        } else {
            team.setExpireTime(null);
        }
        final long userId = loginUser.getId();
        // 3. 校验信息
        // 7. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        //   1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //   2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //   3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //   4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        // 6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        // 8. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        // 9. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;

    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null){
            //工作室id
            Long id = teamQuery.getId();
            if (id != null && id >= 0){
                queryWrapper.eq("id",id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)){
                queryWrapper.like("description",description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum >= 0){
                queryWrapper.eq("maxNum",maxNum);
            }
            //创始人userid
            Long userId = teamQuery.getUserId();
            if (userId != null && userId> 0){
                queryWrapper.eq("userId",userId);
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (status != null){
                if (statusEnum == null) {
                    Team teamById = this.getTeamById(id);
                    Integer status1 = teamById.getStatus();
                    if (status1==2){
                        statusEnum = TeamStatusEnum.SECRET;
                    }else {
                        statusEnum = TeamStatusEnum.PUBLIC;
                    }
                }
                if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                    throw new BusinessException(ErrorCode.NO_AUTH);
                }
                queryWrapper.eq("status", statusEnum.getValue());
            }
        }

        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

        //查询负责人信息
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdate, User loginUser) {
        if (teamUpdate == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long id = teamUpdate.getId();
        if (id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(teamUpdate.getId());
        if (oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        boolean isAdmin = userService.isAdmin(loginUser);
        if (oldTeam.getUserId() != loginUser.getId() && !isAdmin){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdate.getStatus());
        Team updateTeam = new Team();
        String password = teamUpdate.getPassword();
        BeanUtils.copyProperties(teamUpdate, updateTeam);
        if (statusEnum.equals(TeamStatusEnum.SECRET)){
            if (StringUtils.isBlank(password)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须有密码");
            }
            if (password.equals(oldTeam.getPassword())){
                updateTeam.setPassword(oldTeam.getPassword());
            } else {
                updateTeam.setPassword(password);
            }
        }
        return this.updateById(updateTeam);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)){
            if (StringUtils.isBlank(password) || !team.getPassword().equals(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }

        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        long userjoin = userTeamService.count(userTeamQueryWrapper);
        if (userjoin > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入五个队伍");
        }
        // 不能重复加入已加入的队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teamId", teamId);
        long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
        if (hasUserJoinTeam > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
        }
        // 已加入队伍的人数
        long jointeam = this.countTeamUserByTeamId(teamId);
        if (jointeam > team.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超过人数");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean result = userTeamService.save(userTeam);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        // 队伍只剩一人，解散
        if (teamHasJoinNum == 1) {
            // 删除队伍
            this.removeById(teamId);
        } else {
            // 队伍还剩至少两人
            // 是队长
            if (team.getUserId() == userId) {
                // 把队伍转移给最早加入的用户
                // 1. 查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        // 移除关系
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        Team team = getTeamById(id);
        long teamId = team.getId();
        Long userId = loginUser.getId();
        if (!team.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.NO_AUTH,"你不是该队伍得队长");
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除关联信息失败");
        }
        return this.removeById(teamId);
    }

    @Override
    public List<User> listJoinUsers(Long id) {
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userMapper.getUserInfoByTeamId(id);
        return userList.stream().map(user -> userService.getsafatyUser(user)).collect(Collectors.toList());
    }

    /**
     * 根据 id 获取队伍信息
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

}




