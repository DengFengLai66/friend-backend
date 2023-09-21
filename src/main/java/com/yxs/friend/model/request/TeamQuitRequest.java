package com.yxs.friend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍请求体
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -2789045321525769689L;

    /**
     * id
     */
    private Long teamId;


}
