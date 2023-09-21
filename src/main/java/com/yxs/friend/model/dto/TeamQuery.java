package com.yxs.friend.model.dto;

import com.yxs.friend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @author: yxs
 * @create: 2023-08-07 20:57
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamQuery extends PageRequest {
    /**
     * id
     */
    private Long id ;

    /**
     * id集合
     */
    private List<Long> idList;


    /**
     * 队伍名称
     */
    private String name;

    /**
     * 搜索条件
     */
    private String searchText;

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

}
