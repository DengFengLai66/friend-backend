package com.yxs.friend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -2789045321525769689L;

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}
