package com.yxs.friend.model.vo;

import com.yxs.friend.model.domain.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author: yxs
 * @create: 2023-08-09 20:53
 **/
@Data
public class TeamUserVO implements Serializable {


    private static final long serialVersionUID = -4669112122636841757L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 跟新时间
     */
    private Date updateTime;

    /**
     * 队长
     */
    UserVO CreateUser;

    /**
     * 加入队伍得用户
     */
    List<User> userList;

    /**
     * 加入队伍得数量
     */
    private Integer hasJoinNum;

    /**
     * 是否已加入
     */
    private boolean hasJoin=false;
}
