package com.yxs.friend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yxs.friend.common.BaseResponse;
import com.yxs.friend.common.DeleteRequest;
import com.yxs.friend.common.ErrorCode;
import com.yxs.friend.common.ResultUtil;
import com.yxs.friend.exception.BusinessException;
import com.yxs.friend.mapper.UserTeamMapper;
import com.yxs.friend.model.domain.Team;
import com.yxs.friend.model.domain.User;
import com.yxs.friend.model.domain.UserTeam;
import com.yxs.friend.model.dto.TeamQuery;
import com.yxs.friend.model.request.TeamAddRequest;
import com.yxs.friend.model.request.TeamJoinRequest;
import com.yxs.friend.model.request.TeamQuitRequest;
import com.yxs.friend.model.request.TeamUpdateRequest;
import com.yxs.friend.model.vo.TeamUserVO;
import com.yxs.friend.service.TeamService;
import com.yxs.friend.service.UserService;
import com.yxs.friend.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: yxs
 * @create: 2023-08-06 18:35
 **/
@RestController
@RequestMapping("/team")
//@CrossOrigin(originPatterns = {"http://localhost:5173"},allowCredentials = "true")
@Slf4j
public class TeamController {
    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    UserTeamMapper userTeamMapper;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long teamId = teamService.addTeam(team,loginUser);
        return ResultUtil.success(teamId);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if (teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");
        }
        return ResultUtil.success(result);
    }

    @GetMapping("/get")
    public BaseResponse<TeamUserVO> getTeamById(@RequestParam long id){
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TeamQuery teamQuery = new TeamQuery();
        teamQuery.setId(id);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        if (CollectionUtils.isEmpty(teamList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        TeamUserVO team = teamList.get(0);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        List<User> joinUser = teamService.listJoinUsers(team.getId());
        team.setUserList(joinUser);
        team.setHasJoin(true);
        //判断队伍加入的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.eq("teamId",id);
        Long hasJoinNum = userTeamMapper.selectCount(userTeamJoinQueryWrapper);
        team.setHasJoinNum(Math.toIntExact(hasJoinNum));
        return ResultUtil.success(team);
    }


    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        //查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,isAdmin);
        //取出队伍id集合，在后面用于判断用户是否已加入
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            //已加入队伍的id集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team ->{
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e){}
        //判断队伍加入的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId",teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team ->{
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size());
        });
        return ResultUtil.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtil.success(teamPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"加入失败");
        }
        return ResultUtil.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"退出失败");
        }
        return ResultUtil.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if (deleteRequest == null || deleteRequest.getId() <=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtil.success(true);
    }


    /**
     * 获取自己创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyTeams(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());

        //查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);
        //取出队伍id集合，在后面用于判断用户是否已加入
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            //已加入队伍的id集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team ->{
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e){}
        //判断队伍加入的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId",teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team ->{
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size());
        });
        return ResultUtil.success(teamList);
    }

    /**
     * 获取自己加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listJoinTeams(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        //取出登录用户id
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //根据用户id取出不重复的工作室id
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        //List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);


        //查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);
        //取出队伍id集合，在后面用于判断用户是否已加入
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            //User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            userTeamList = userTeamService.list(userTeamQueryWrapper);
            //已加入队伍的id集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team ->{
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e){}
        //判断队伍加入的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId",teamIdList);
        userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team ->{
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size());
        });
        return ResultUtil.success(teamList);
    }
}
